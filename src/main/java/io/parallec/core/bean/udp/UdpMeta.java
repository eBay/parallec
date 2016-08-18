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
package io.parallec.core.bean.udp;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * all ssh metadata except for the target host name. also those timeout configs
 * and
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class UdpMeta {
    
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(UdpMeta.class);

    public static final ChannelHandler stringDecoder = new StringDecoder();
    public static final ChannelHandler stringEncoder = new StringEncoder();
    

    /** The command */
    private String command;

    /** The udp port. */
    private Integer udpPort;

    private Integer udpIdleTimeoutSec;
    
    private DatagramChannelFactory channelFactory;
    
    public UdpMeta(String command, int udpPort, int udpIdleTimeoutSec,
            DatagramChannelFactory channelFactory) {
        super();
        this.command = command;
        this.udpPort = udpPort;
        this.udpIdleTimeoutSec = udpIdleTimeoutSec;
        this.channelFactory = channelFactory;
        
    }


    public UdpMeta() {
        super();
        this.command=null;
        this.udpPort=null;
        this.channelFactory=null;
        this.udpIdleTimeoutSec= null;
        
    }

    public boolean validation() throws ParallelTaskInvalidException {

        if (this.command == null) {
            throw new ParallelTaskInvalidException(
                    "command is null for UDP");
        }

        if (this.udpPort == null) {
            throw new ParallelTaskInvalidException("udpPort is null. please set");
        }
        
        if (this.udpIdleTimeoutSec == null) {
            logger.info("SET DEFAULT UDP CONNECT TIMEOUT: UDP connectTimeoutMillis is set as default");
            this.udpIdleTimeoutSec= ParallecGlobalConfig.udpIdleTimeoutSecDefault;
        }
        
        
        if (this.channelFactory == null) {
            logger.info("SET DEFAULT UDP NETTY CHANNEL FACTORY: UDP channelFactory is set as default");
            this.channelFactory=TcpUdpSshPingResourceStore.getInstance().getDatagramChannelFactory();
        }

        return true;

    }
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }


    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }


    public void setChannelFactory(DatagramChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }


    public Integer getUdpPort() {
        return udpPort;
    }


    public void setUdpPort(Integer udpPort) {
        this.udpPort = udpPort;
    }


    public Integer getUdpIdleTimeoutSec() {
        return udpIdleTimeoutSec;
    }


    public void setUdpIdleTimeoutSec(Integer udpIdleTimeoutSec) {
        this.udpIdleTimeoutSec = udpIdleTimeoutSec;
    }


    @Override
    public String toString() {
        return "UdpMeta [command=" + command + ", udpPort=" + udpPort
                + ", udpIdleTimeoutSec=" + udpIdleTimeoutSec
                + ", channelFactory=" + channelFactory + "]";
    }




 

}
