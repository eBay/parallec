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
package io.parallec.core.resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * Provide external resources needed for netty based TCP worker:
 * ChannelFactory and HashedWheelTimer timer
 *
 * @author Yuanteng (Jeff) Pei
 */
public class TcpSshPingResourceStore {


    /** The singleton instance. */
    private final static TcpSshPingResourceStore instance = new TcpSshPingResourceStore();

    public static TcpSshPingResourceStore getInstance() {
        return instance;
    }
    private ChannelFactory channelFactory; 
    private HashedWheelTimer timer = null;
    
    private ExecutorService threadPoolForSshPing = Executors.newCachedThreadPool();
    
    public void shutdown(){
        if(channelFactory!=null){
            channelFactory.releaseExternalResources();
        }
        if(timer!=null) timer.stop();
    }
    /**
     * Instantiates a new http client store.
     */
    private TcpSshPingResourceStore() {
        init();
    }

    /**
     * Initialize
     */
    public synchronized void init() {
        channelFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        timer = new HashedWheelTimer();
    }

    /**
     * close and clean up the http client, then create the new ones.
     */
    public synchronized void reinit() {

        // first shutdown existing ones.
        shutdown();
        init();
    }
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }
    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }
    public HashedWheelTimer getTimer() {
        return timer;
    }
    public void setTimer(HashedWheelTimer timer) {
        this.timer = timer;
    }

    public ExecutorService getThreadPoolForSshPing() {
        return threadPoolForSshPing;
    }

    public void setThreadPoolForSshPing(ExecutorService threadPoolForSshPing) {
        this.threadPoolForSshPing = threadPoolForSshPing;
    }
 
   

}
