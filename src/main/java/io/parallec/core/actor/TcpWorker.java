/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.actor;

import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.actor.message.type.RequestWorkerMsgType;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.exception.ActorMessageTypeInvalidException;
import io.parallec.core.exception.HttpRequestCreateException;
import io.parallec.core.exception.TcpUdpRequestCreateException;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcErrorMsgUtils;
import io.parallec.core.util.PcStringUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
//import akka.actor.PoisonPill;
import akka.actor.UntypedActor;

/**
 * A worker for single TCP request class 
 *
 * @author Yuanteng (Jeff) Pei
 */
public class TcpWorker extends UntypedActor {

    /** The actor max operation timeout sec. */
    private int actorMaxOperationTimeoutSec;

    /** The tcp meta. */
    private final TcpMeta tcpMeta;
    
    /** The target host. */
    private String targetHost;

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(TcpWorker.class);

    /** The sender. */
    private ActorRef sender = null;

    /** The cause. */
    // cause will be the exception to log as in PROCESS_ON_EXCEPTION
    private Throwable cause;

    /** The try count. */
    private int tryCount = 0;

    /** The timeout message cancellable. */
    private Cancellable timeoutMessageCancellable = null;

    /** The timeout duration. */
    private FiniteDuration timeoutDuration = null;

    /** The sent reply. */
    // private FiniteDuration retryDuration = null;
    private boolean sentReply = false;

    /** The channel. */
    private Channel channel = null;
    
    /** The response sb. */
    private StringBuilder responseSb = new StringBuilder();
    
    
    /**
     * Instantiates a new tcp worker.
     *
     * @param actorMaxOperationTimeoutSec the actor max operation timeout sec
     * @param tcpMeta the tcp meta
     * @param targetHost the target host
     */
    public TcpWorker(final int actorMaxOperationTimeoutSec,
            final TcpMeta tcpMeta, final String targetHost) {
        super();
        this.actorMaxOperationTimeoutSec = actorMaxOperationTimeoutSec;
        this.tcpMeta = tcpMeta;
        this.targetHost = targetHost;
    }

    /**
     * Creates the tcpClient with proper handler.
     *
     * @return the bound request builder
     * @throws HttpRequestCreateException
     *             the http request create exception
     */
    public ClientBootstrap bootStrapTcpClient()
            throws HttpRequestCreateException {

        ClientBootstrap tcpClient = null;
        try {

            // Configure the client.
            tcpClient = new ClientBootstrap(tcpMeta.getChannelFactory());

            // Configure the pipeline factory.
            tcpClient.setPipelineFactory(new MyPipelineFactory(TcpUdpSshPingResourceStore.getInstance().getTimer(),
                    this, tcpMeta.getTcpIdleTimeoutSec())
            );

            tcpClient.setOption("connectTimeoutMillis",
                    tcpMeta.getTcpConnectTimeoutMillis());
            tcpClient.setOption("tcpNoDelay", true);
          // tcpClient.setOption("keepAlive", true);

        } catch (Exception t) {
            throw new TcpUdpRequestCreateException(
                    "Error in creating request in Tcpworker. "
                            + " If tcpClient is null. Then fail to create.", t);
        }

        return tcpClient;

    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        try {
            if (message instanceof RequestWorkerMsgType) {
                switch ((RequestWorkerMsgType) message) {
                case PROCESS_REQUEST:
                    tryCount++;

                    if (tryCount == 1) {
                        sender = getSender();

                        ClientBootstrap tcpClient = bootStrapTcpClient();

                        // Start the connection attempt.
                        ChannelFuture future = tcpClient
                                .connect(new InetSocketAddress(targetHost,
                                        tcpMeta.getTcpPort()));


                        //first schedule timeout before the sync wait
                        timeoutDuration = Duration.create(
                                actorMaxOperationTimeoutSec, TimeUnit.SECONDS);

                        timeoutMessageCancellable = getContext()
                                .system()
                                .scheduler()
                                .scheduleOnce(
                                        timeoutDuration,
                                        getSelf(),
                                        RequestWorkerMsgType.PROCESS_ON_TIMEOUT,
                                        getContext().system().dispatcher(),
                                        getSelf());
                        
                        // Wait until the connection attempt succeeds or fails.
                        channel = future.awaitUninterruptibly()
                                .getChannel();
                        ChannelFuture requestFuture = null;

                        // Sends the line to server need. line ending
                        requestFuture = channel.write(tcpMeta.getCommand()
                                + "\r\n");

                        // Wait until all messages are flushed before closing
                        // the channel.
                        if (requestFuture != null) {
                            requestFuture.await();
                        }
                    } else {
                        getLogger().error(
                                "duplicated PROCESS_REQUEST msg. ignore...");
                    }
                    break;

                case CANCEL:
                    if (sender == null)
                        sender = getSender();
                    getLogger().info(
                            "TCP Request was CANCELLED.................{}",
                            targetHost);
                    cancelCancellable();
                    reply(null, true, PcConstants.REQUEST_CANCELED,
                            PcConstants.REQUEST_CANCELED, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                case PROCESS_ON_EXCEPTION:

                    String errorSummary = PcErrorMsgUtils.replaceErrorMsg(cause
                            .toString());
                    String stackTrace = PcStringUtils.printStackTrace(cause);
                    cancelCancellable();
                    reply(null, true, errorSummary, stackTrace, PcConstants.NA,
                            PcConstants.NA_INT);

                    break;

                case PROCESS_ON_TIMEOUT:

                    getLogger().error("PROCESS_ON_TIMEOUT.................{}",
                            targetHost);
                    cancelCancellable();

                    String errorMsg = String
                            .format("TcpWorker Timedout after %d SEC (no response but no exception catched). Check URL: may be very slow or stuck.",
                                    actorMaxOperationTimeoutSec);

                    reply(null, true, errorMsg, errorMsg, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                case CHECK_FUTURE_STATE:
                default:
                    sender = getSender();
                    this.cause = new ActorMessageTypeInvalidException(
                            "ActorMessageTypeInvalidException error for on "
                                    + this.targetHost);
                    getSelf().tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION,
                            getSelf());
                    break;
                }
            } else {
                unhandled(message);
                sender = getSender();
                this.cause = new ActorMessageTypeInvalidException(
                        "ActorMessageTypeInvalidException error for TCP on "
                                + this.targetHost);
                getSelf().tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION,
                        getSelf());
            }
        } catch (Exception e) {
            this.cause = e;
            getSelf()
                    .tell(RequestWorkerMsgType.PROCESS_ON_EXCEPTION, getSelf());
        }
    }

    /**
     * Cancel cancellable.
     */
    public void cancelCancellable() {

        if (timeoutMessageCancellable != null) {
            timeoutMessageCancellable.cancel();
        }

    }

    /**
     * First close the connection. Then reply.
     *
     * @param response
     *            the response
     * @param error
     *            the error
     * @param errorMessage
     *            the error message
     * @param stackTrace
     *            the stack trace
     * @param statusCode
     *            the status code
     * @param statusCodeInt
     *            the status code int
     */
    private void reply(final String response, final boolean error,
            final String errorMessage, final String stackTrace,
            final String statusCode, final int statusCodeInt) {

        
        if (!sentReply) {
            
            //must update sentReply first to avoid duplicated msg.
            sentReply = true;
            // Close the connection. Make sure the close operation ends because
            // all I/O operations are asynchronous in Netty.
            if(channel!=null && channel.isOpen())
                channel.close().awaitUninterruptibly();
            final ResponseOnSingeRequest res = new ResponseOnSingeRequest(
                    response, error, errorMessage, stackTrace, statusCode,
                    statusCodeInt, PcDateUtils.getNowDateTimeStrStandard(), null);
            if (!getContext().system().deadLetters().equals(sender)) {
                sender.tell(res, getSelf());
            }
            if (getContext() != null) {
                getContext().stop(getSelf());
            }
        }

    }

    /**
     * On complete.
     *
     * @param response the response
     * @param error the error
     * @param errorMessage the error message
     * @param stackTrace the stack trace
     * @param statusCode the status code
     * @param statusCodeInt the status code int
     */
    public void onComplete(String response, boolean error,
            String errorMessage, final String stackTrace,
            String statusCode,  int statusCodeInt
            ) {
        cancelCancellable();
        reply(response, error, errorMessage, stackTrace, statusCode, statusCodeInt);
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Sets the logger.
     *
     * @param logger
     *            the new logger
     */
    public static void setLogger(Logger logger) {
        TcpWorker.logger = logger;
    }


    /**
     * define the list of handlers for this channel.
     *
     * @author Yuanteng (Jeff) Pei
     */
    public static class MyPipelineFactory implements ChannelPipelineFactory{
        
        /** The idle state handler. */
        private final ChannelHandler idleStateHandler;
        
        /** The tcp worker. */
        private final TcpWorker tcpWorker;
        
        /** The my idle handler. */
        private final MyIdleHandler myIdleHandler;
        
        /**
         * Instantiates a new my pipeline factory.
         *
         * @param timer the timer
         * @param tcpWorker the tcp worker
         * @param idleTimeoutSec the idle timeout sec
         */
        public MyPipelineFactory(Timer timer, TcpWorker tcpWorker, int idleTimeoutSec) {
            this.tcpWorker = tcpWorker;
            this.idleStateHandler = new IdleStateHandler(timer, 0, 0, idleTimeoutSec); 
            this.myIdleHandler = new MyIdleHandler(tcpWorker);
        }

        /* (non-Javadoc)
         * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
         */
        public ChannelPipeline getPipeline() {
            
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("idleTimer",idleStateHandler);
            pipeline.addLast("idleHandler",myIdleHandler);
            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
                    1024, Delimiters.lineDelimiter()));
            pipeline.addLast("stringDecoder", TcpMeta.stringDecoder);
            pipeline.addLast("stringEncoder", TcpMeta.stringEncoder);
            pipeline.addLast("handler",
                    new TcpChannelHandler(tcpWorker));

            return pipeline;
        }
        
    }
    
    /**
     * how to pass the idle event back to the worker.
     *
     * @author Yuanteng (Jeff) Pei
     */
    public static class MyIdleHandler extends IdleStateAwareChannelHandler {

        /** The tcp worker. */
        private final TcpWorker tcpWorker;
        
        /**
         * Instantiates a new my idle handler.
         *
         * @param tcpWorker the tcp worker
         */
        public MyIdleHandler(TcpWorker tcpWorker) {
            super();
            this.tcpWorker = tcpWorker;
            
        }
        
        /* (non-Javadoc)
         * @see org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler#channelIdle(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.handler.timeout.IdleStateEvent)
         */
        @Override
        public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
            logger.info("In IDLE event handler for TCP...");
            
            //there are 3 states. READER/WRITER/ALL
            if (e.getState() == IdleState.ALL_IDLE){
                int statusCodeInt = 0;
                String statusCode = statusCodeInt + " SUCCESSFUL";
                String errMsg="idleTimeout to finish";
                
                tcpWorker.onComplete(tcpWorker.responseSb.toString(), false, 
                        errMsg, errMsg, statusCode, statusCodeInt);
            }
        }
    }
    

    /**
     * The Netty response/channel handler.
     *
     * @author Yuanteng (Jeff) Pei
     */
    public static class TcpChannelHandler extends SimpleChannelHandler {

        /** The has caught exception. */
        public boolean hasCaughtException = false;
        
        /** The tcp worker. */
        private final TcpWorker tcpWorker;

        /**
         * Instantiates a new tcp channel handler.
         *
         * @param tcpWorker the tcp worker
         */
        public TcpChannelHandler(TcpWorker tcpWorker) {
            super();
            this.tcpWorker = tcpWorker;

        }

        /** The msg recv count. */
        private int msgRecvCount = 0;

        

        /* (non-Javadoc)
         * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
         */
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            //add \n to the end
            tcpWorker.responseSb.append(e.getMessage().toString()+"\n");
            logger.debug("DONE." + ++msgRecvCount);
            logger.debug("MSG_RECEIVED_AT_TCP_CLIENT: {}", e.getMessage().toString());
        }

        /**
         * Why not use channelClosed: if fail to establish a channel 
         * (e.g. connection refused). will also call channelClosed. 
         *
         * @param ctx the ctx
         * @param e the e
         * @throws Exception the exception
         */
        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
                throws Exception {
            logger.debug("channel is closed. ");
            
            int statusCodeInt = 0;
            String statusCode = statusCodeInt + " SUCCESSFUL";
            
            tcpWorker.onComplete(tcpWorker.responseSb.toString(), false, 
                    null, null, statusCode, statusCodeInt);

        }
        

        /* (non-Javadoc)
         * @see org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            
            if(!hasCaughtException){
                hasCaughtException=true;
                e.getChannel().close();
                String errMsg = e.getCause().getLocalizedMessage();
                logger.debug("TCP Handler exceptionCaught: {} . ", errMsg);
                
                int statusCodeInt = 1;
                String statusCode = statusCodeInt + " FAILURE";
                
                tcpWorker.onComplete(tcpWorker.responseSb.toString(), true, 
                        errMsg, errMsg, statusCode, statusCodeInt);
                
            }
        }
    }// end handler class

}
