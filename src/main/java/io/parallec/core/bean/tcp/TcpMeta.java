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
package io.parallec.core.bean.tcp;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.resources.TcpUdpSshPingResourceStore;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
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
public class TcpMeta {
    
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(TcpMeta.class);

    public static final ChannelHandler stringDecoder = new StringDecoder();
    public static final ChannelHandler stringEncoder = new StringEncoder();
    

    
    /** The command */
    private String command;

    /** The tcp port. */
    private Integer tcpPort;

    private Integer tcpConnectTimeoutMillis;

    private Integer tcpIdleTimeoutSec;
    
    private ChannelFactory channelFactory;
    
    public TcpMeta(String command, int tcpPort, int tcpConnectTimeoutMillis,
            int tcpIdleTimeoutSec,
            ChannelFactory channelFactory) {
        super();
        this.command = command;
        this.tcpPort = tcpPort;
        this.tcpConnectTimeoutMillis = tcpConnectTimeoutMillis;
        this.tcpIdleTimeoutSec = tcpIdleTimeoutSec;
        this.channelFactory = channelFactory;
        
    }


    public TcpMeta() {
        super();
        this.command=null;
        this.tcpPort=null;
        this.channelFactory=null;
        this.tcpConnectTimeoutMillis= null;
        this.tcpIdleTimeoutSec=null;
        
    }

    public boolean validation() throws ParallelTaskInvalidException {

        if (this.command == null) {
            throw new ParallelTaskInvalidException(
                    "command is null for TCP");
        }

        if (this.tcpPort == null) {
            throw new ParallelTaskInvalidException("tcpPort is null. please set");
        }
        
        if (this.tcpConnectTimeoutMillis == null) {
            logger.info("SET DEFAULT TCP CONNECT TIMEOUT: TCP tcpConnectTimeoutMillis is set as default");
            this.tcpConnectTimeoutMillis= ParallecGlobalConfig.tcpConnectTimeoutMillisDefault;
        }
        
        if (this.tcpIdleTimeoutSec == null) {
            logger.info("SET DEFAULT TCP IDLE TIMEOUT: TCP tcpIdleTimeoutSec is set as default");
            this.tcpIdleTimeoutSec= ParallecGlobalConfig.tcpIdleTimeoutSecDefault;
        }
        
        if (this.channelFactory == null) {
            logger.info("SET DEFAULT TCP NETTY CHANNEL FACTORY: TCP channelFactory is set as default");
            this.channelFactory=TcpUdpSshPingResourceStore.getInstance().getChannelFactory();
        }

        return true;

    }
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getTcpConnectTimeoutMillis() {
        return tcpConnectTimeoutMillis;
    }

    public void setTcpConnectTimeoutMillis(int tcpConnectTimeoutMillis) {
        this.tcpConnectTimeoutMillis = tcpConnectTimeoutMillis;
    }


    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }


    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }


    public Integer getTcpIdleTimeoutSec() {
        return tcpIdleTimeoutSec;
    }


    public void setTcpIdleTimeoutSec(Integer tcpIdleTimeoutSec) {
        this.tcpIdleTimeoutSec = tcpIdleTimeoutSec;
    }

 

}
