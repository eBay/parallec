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
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.commander.workflow.ssh.SshProvider;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ActorMessageTypeInvalidException;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcErrorMsgUtils;
import io.parallec.core.util.PcStringUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
//import akka.actor.PoisonPill;
import akka.actor.UntypedActor;



/**
 *
 * Using callable to start a new thread to run the SSH execution task (which
 * make take minutes). Check if future is read every 0.5seconds Make sure the
 * message are handled async. Note that a blocking message will block all
 * subsequent messages.
 * 
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class SshWorker extends UntypedActor {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(SshWorker.class);
    
    /** The actor max operation timeout sec. */
    private int actorMaxOperationTimeoutSec;

    /** The sender. */
    private ActorRef sender = null;

    /** The cause. */
    private Throwable cause;

    /** The try count. */
    private int tryCount = 0;

    /** The timeout message cancellable. */
    private Cancellable timeoutMessageCancellable = null;

    /** The timeout duration. */
    private FiniteDuration timeoutDuration = null;

    /** The sent reply. */
    private boolean sentReply = false;

    /** The ssh meta. */
    private SshMeta sshMeta;

    /** The target host. */
    private String targetHost;

    /** The response future. */
    private Future<ResponseOnSingeRequest> responseFuture;

    /**
     * Instantiates a new ssh worker.
     *
     * @param actorMaxOperationTimeoutSec the actor max operation timeout sec
     * @param sshMeta the ssh meta
     * @param targetHost the target host
     */
    public SshWorker(int actorMaxOperationTimeoutSec, SshMeta sshMeta,
            String targetHost) {
        super();
        this.actorMaxOperationTimeoutSec = actorMaxOperationTimeoutSec;
        this.sshMeta = sshMeta;
        this.targetHost = targetHost;
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

                        SshTask sshTask = new SshTask(sshMeta, targetHost);
                        setResponseFuture(TcpUdpSshPingResourceStore.getInstance()
                                .getThreadPoolForSshPing().submit(sshTask));

                        getContext()
                                .system()
                                .scheduler()
                                .scheduleOnce(
                                        (FiniteDuration) Duration.create(0.5,
                                                TimeUnit.SECONDS),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                getSelf()
                                                        .tell(RequestWorkerMsgType.CHECK_FUTURE_STATE,
                                                                getSelf());
                                            }
                                        }, getContext().system().dispatcher());

                    } else {
                        getLogger().error(
                                "duplicated PROCESS_REQUEST msg. ignore...");
                    }

                    break;

                case CHECK_FUTURE_STATE:
                    getLogger().debug(
                            "checking if SSH callable future completed... for "
                                    + targetHost);
                    if (getResponseFuture().isDone()) {
                        getLogger().debug(
                                "SSH Provider callable returned. for "
                                        + targetHost);
                        ResponseOnSingeRequest sshResponse = getResponseFuture()
                                .get();
                        onComplete(sshResponse);
                        break;
                    } else {
                        // schedule again
                        getContext()
                                .system()
                                .scheduler()
                                .scheduleOnce(
                                        (FiniteDuration) Duration.create(
                                                ParallecGlobalConfig.sshFutureCheckIntervalSec,
                                                TimeUnit.SECONDS),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                getSelf()
                                                        .tell(RequestWorkerMsgType.CHECK_FUTURE_STATE,
                                                                getSelf());
                                            }
                                        }, getContext().system().dispatcher());

                    }
                    break;
                case CANCEL:
                    getLogger()
                            .info("Request was CANCELLED.................on SSH host {}",
                                    targetHost);
                    cancelCancellable();
                    if (sender == null)
                        sender = getSender();
                    reply(null, true, PcConstants.REQUEST_CANCELED,
                            PcConstants.REQUEST_CANCELED, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                case PROCESS_ON_EXCEPTION:

                    String displayError = PcErrorMsgUtils.replaceErrorMsg(cause
                            .toString());

                    String stackTrace = PcStringUtils.printStackTrace(cause);

                    cancelCancellable();
                    reply(null, true, displayError, stackTrace, PcConstants.NA,
                            PcConstants.NA_INT);

                    break;

                case PROCESS_ON_TIMEOUT:
                    getLogger().error(
                            "Inside PROCESS_ON_TIMEOUT.................target: "
                                    + this.targetHost + "......... at "
                                    + PcDateUtils.getNowDateTimeStrStandard());
                    cancelCancellable();

                    String errorMsg = String
                            .format("SshWorker Timedout after %d SEC (no response but no exception catched). Details more info",
                                    actorMaxOperationTimeoutSec);
                    reply(null, true, errorMsg, errorMsg, PcConstants.NA,
                            PcConstants.NA_INT);
                    break;

                default:
                    break;
                }
            } else {
                unhandled(message);
                this.cause = new ActorMessageTypeInvalidException(
                        "ActorMessageTypeInvalidException error for host "
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
     * On complete.
     *
     * @param sshResponse
     *            the ssh response
     */
    public void onComplete(ResponseOnSingeRequest sshResponse) {
        cancelCancellable();
        reply(sshResponse.getResponseBody(),
                sshResponse.isFailObtainResponse(),
                sshResponse.getErrorMessage(), null,
                sshResponse.getStatusCode(), sshResponse.getStatusCodeInt());
    }

    /**
     * Cancel cancellable.
     */
    public void cancelCancellable() {

        if (timeoutMessageCancellable != null) {
            timeoutMessageCancellable.cancel();
        }
        if (getResponseFuture() != null && !getResponseFuture().isDone()) {
            getResponseFuture().cancel(true);
        }
    }

    /**
     * Reply.
     *
     * @param response
     *            the response
     * @param failObtainResponse
     *            the fail obtain response
     * @param errorMessage
     *            the error message
     * @param stackTrace
     *            the stack trace
     * @param statusCode
     *            the status code
     * @param statusCodeInt
     *            the status code int
     */
    private void reply(final String response, final boolean failObtainResponse,
            final String errorMessage, final String stackTrace,
            final String statusCode, final int statusCodeInt) {

        /**
         * this is needed if NIO has not even send out! MEMROY LEAK if not.
         * double check
         */

        if (!isSentReply()) {
            //must update sentReply first to avoid duplicated msg.
            sentReply = true;
            final ResponseOnSingeRequest res = new ResponseOnSingeRequest(
                    response, failObtainResponse, errorMessage, stackTrace,
                    statusCode, statusCodeInt,
                    PcDateUtils.getNowDateTimeStrStandard(), null);

            if (!getContext().system().deadLetters().equals(sender)) {
                sender.tell(res, getSelf());
            }

            getLogger().debug(
                    "DEBUG: real response: " + response + " err: "
                            + errorMessage);
            if (getContext() != null) {
                getContext().stop(getSelf());
            }
        }
    }

    /**
     * Checks if is sent reply.
     *
     * @return true, if is sent reply
     */
    public boolean isSentReply() {
        return sentReply;
    }

    /**
     * Sets the sent reply.
     *
     * @param sentReply
     *            the new sent reply
     */
    public void setSentReply(boolean sentReply) {
        this.sentReply = sentReply;
    }

    /**
     * Gets the response future.
     *
     * @return the response future
     */
    public Future<ResponseOnSingeRequest> getResponseFuture() {
        return responseFuture;
    }

    /**
     * Sets the response future.
     *
     * @param responseFuture
     *            the new response future
     */
    public void setResponseFuture(Future<ResponseOnSingeRequest> responseFuture) {
        this.responseFuture = responseFuture;
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
        SshWorker.logger = logger;
    }

    /**
     * thread to return the future.
     *
     * @author Yuanteng (Jeff) Pei
     */
    private static class SshTask implements Callable<ResponseOnSingeRequest> {

        /** The ssh meta. */
        private SshMeta sshMeta;

        /** The target host. */
        private String targetHost;

        /**
         * Instantiates a new ssh task.
         *
         * @param sshMeta
         *            the ssh meta
         * @param targetHost
         *            the target host
         */
        public SshTask(SshMeta sshMeta, String targetHost) {
            this.sshMeta = sshMeta;
            this.targetHost = targetHost;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public ResponseOnSingeRequest call() throws Exception {
            SshProvider sshProvider = new SshProvider(sshMeta, targetHost);
            return sshProvider.executeSshCommand();
        }
    }// end class

}
