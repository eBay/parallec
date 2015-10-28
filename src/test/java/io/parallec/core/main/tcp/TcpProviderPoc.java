package io.parallec.core.main.tcp;

/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

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
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 */
public final class TcpProviderPoc {
    protected static final Logger logger = LoggerFactory
            .getLogger(TcpProviderPoc.class);

    private static final ChannelHandler stringDecoder = new StringDecoder();
    private static final ChannelHandler stringEncoder = new StringEncoder();
    /** The Constant instance. */
    private final static TcpProviderPoc instance = new TcpProviderPoc();

    
    public static TcpProviderPoc getInstance() {
        return instance;
    }
    
    private TcpProviderPoc(){
        
    }
    
    public void request() throws Exception {
        
        // Parse options.
        String host = "localhost";
        int port = 10081;
        int connectTimeoutMillis = 2000;
        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        final TelnetClientHandler handler = new TelnetClientHandler();

        // Configure the pipeline factory.
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024,
                        Delimiters.lineDelimiter()));
                pipeline.addLast("stringDecoder", stringDecoder);
                pipeline.addLast("stringEncoder", stringEncoder);
                pipeline.addLast("handler", handler);
                return pipeline;
            }
        });
        
        
        bootstrap.setOption("connectTimeoutMillis",connectTimeoutMillis);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,
                port));

        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.awaitUninterruptibly().getChannel();
        // Read commands from the stdin.
        ChannelFuture lastWriteFuture = null;
        String command = "hadoopMonitorFromClient";

        // Sends the line to server.
        lastWriteFuture = channel.write(command + "\r\n");

        // Wait until all messages are flushed before closing the channel.
        if (lastWriteFuture != null) {
            lastWriteFuture.await();
        }

        // note that we need to wait for the response before

        // wait time before close the channel too early that msg will not be
        // received.

        while (!handler.channelCompleted) {
            Thread.sleep(1l);
        }

        // Close the connection. Make sure the close operation ends because
        // all I/O operations are asynchronous in Netty.
        channel.close().awaitUninterruptibly();
        // Shut down all thread pools to exit.
        bootstrap.releaseExternalResources();
        
    }
    
    public static void main(String[] args) throws Exception {

        TcpProviderPoc.getInstance().request();

    }

    public static class TelnetClientHandler extends SimpleChannelHandler {

        public boolean channelCompleted = false;

        private int count = 0;

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            // Print out the line received from the server.
            logger.info("DONE." + ++count);

            // logger.info("MSG_RECEIVE_AT_CLIENT: {}",e.getMessage().toString());
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
                throws Exception {
            logger.info("channel is closed. ");
            channelCompleted = true;
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            logger.warn("Unexpected exception from 1111downstream.",
                    e.getCause());
            e.getChannel().close();
        }
    }//end handler class
}//end out class