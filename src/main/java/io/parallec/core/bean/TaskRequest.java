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
package io.parallec.core.bean;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.RequestProtocol;
import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.resources.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * This is the request send to the operation worker. 
 * It contains the actual request that has been replaced if there are variables defined.
 * 
 * @author Yuanteng (Jeff) Pei 
 */
public class TaskRequest {

    /** The logger. */
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(TaskRequest.class);

    /** The actor max operation timeout sec. */
    private final int actorMaxOperationTimeoutSec;

    /** The resource path. */
    private final String resourcePath;

    /** The request content. */
    private final String requestContent;

    /** The http method. */
    private final HttpMethod httpMethod;

    /** The pollable. */
    private final boolean pollable;

    /** The http header map. */
    private final Map<String, String> httpHeaderMap = new HashMap<String, String>();

    /** The protocol. */
    private final RequestProtocol protocol;

    /** The host. */
    private final String host;
    // 20130917 ASSUMPTION: WHEN IT IS NOT NULL: THIS ONE WILL REPLACE THE host
    /** The host uniform. */
    // to send the HTTP REQUEST.
    private final String hostUniform;

    /** The port. */
    private final int port;

    /** The ssh meta. */
    private final SshMeta sshMeta;

    /** The tcp meta. */
    private final TcpMeta tcpMeta;

    /** The tcp meta. */
    private final UdpMeta udpMeta;
    
    /** The ping meta. */
    private final PingMeta pingMeta;
    
    /** The handler. */
    private final ParallecResponseHandler handler;
    
    /** The response context. */
    private final Map<String, Object> responseContext;
    
    /** The response header meta. */
    private final ResponseHeaderMeta responseHeaderMeta;

    /**
     * Instantiates a new task request.
     *
     * @param actorMaxOperationTimeoutSec the actor max operation timeout sec
     * @param protocol the protocol
     * @param host the host
     * @param hostUniform the host uniform
     * @param port the port
     * @param resourcePath the resource path
     * @param requestContent the request content
     * @param httpMethod the http method
     * @param pollable the pollable
     * @param httpHeaderMap the http header map
     * @param handler the handler
     * @param responseContext the response context
     * @param sshMeta the ssh meta
     * @param tcpMeta the tcp meta
     * @param udpMeta the udp meta
     * @param pingMeta the ping meta
     * @param responseHeaderMeta the response header meta
     */
    public TaskRequest(

            // add for config
            int actorMaxOperationTimeoutSec,
            RequestProtocol protocol, String host, String hostUniform,
            int port, String resourcePath, String requestContent,
            HttpMethod httpMethod, boolean pollable,
            Map<String, String> httpHeaderMap, 
            ParallecResponseHandler handler, 
            Map<String, Object> responseContext,
            SshMeta sshMeta, TcpMeta tcpMeta, UdpMeta udpMeta, PingMeta pingMeta, ResponseHeaderMeta responseHeaderMeta

    ) {
        this.actorMaxOperationTimeoutSec = actorMaxOperationTimeoutSec;
        this.protocol = protocol;
        this.host = host;
        this.hostUniform = hostUniform;
        this.port = port;
        this.pollable = pollable;
        if (httpHeaderMap != null)
            this.httpHeaderMap.putAll(httpHeaderMap);

        this.resourcePath = resourcePath;
        this.requestContent = requestContent;
        this.httpMethod = httpMethod;

        this.handler = handler;
        this.responseContext = responseContext;
        this.sshMeta = sshMeta;
        this.tcpMeta = tcpMeta;
        this.udpMeta = udpMeta;
        this.pingMeta = pingMeta;
        this.responseHeaderMeta = responseHeaderMeta;

    }

    /**
     * Gets the resource path.
     *
     * @return the resource path
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Gets the http method.
     *
     * @return the http method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the post data.
     *
     * @return the post data
     */
    public String getPostData() {
        return requestContent;
    }

    /**
     * Gets the request content.
     *
     * @return the request content
     */
    public String getRequestContent() {
        return requestContent;
    }

    /**
     * Checks if is pollable.
     *
     * @return true, if is pollable
     */
    public boolean isPollable() {
        return pollable;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TaskRequest [actorMaxOperationTimeoutSec="
                + actorMaxOperationTimeoutSec + ", resourcePath="
                + resourcePath + ", requestContent=" + requestContent
                + ", httpMethod=" + httpMethod + ", pollable=" + pollable
                + ", httpHeaderMap=" + httpHeaderMap + ", protocol=" + protocol
                + ", host=" + host + ", hostUniform=" + hostUniform + ", port="
                + port + ", sshMeta=" + sshMeta + ", tcpMeta=" + tcpMeta
                + ", udpMeta=" + udpMeta + ", pingMeta=" + pingMeta
                + ", handler=" + handler + ", responseContext="
                + responseContext + ", responseHeaderMeta="
                + responseHeaderMeta + "]";
    }

    /**
     * Gets the http header map.
     *
     * @return the http header map
     */
    public Map<String, String> getHttpHeaderMap() {
        return httpHeaderMap;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public RequestProtocol getProtocol() {
        return protocol;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the host uniform.
     *
     * @return the host uniform
     */
    public String getHostUniform() {
        return hostUniform;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the ssh meta.
     *
     * @return the ssh meta
     */
    public SshMeta getSshMeta() {
        return sshMeta;
    }

    /**
     * Gets the actor max operation timeout sec.
     *
     * @return the actor max operation timeout sec
     */
    public int getActorMaxOperationTimeoutSec() {
        return actorMaxOperationTimeoutSec;
    }

    /**
     * Gets the tcp meta.
     *
     * @return the tcp meta
     */
    public TcpMeta getTcpMeta() {
        return tcpMeta;
    }

    /**
     * Gets the ping meta.
     *
     * @return the ping meta
     */
    public PingMeta getPingMeta() {
        return pingMeta;
    }

    /**
     * Gets the handler.
     *
     * @return the handler
     */
    public ParallecResponseHandler getHandler() {
        return handler;
    }

    /**
     * Gets the response context.
     *
     * @return the response context
     */
    public Map<String, Object> getResponseContext() {
        return responseContext;
    }

    /**
     * Gets the udp meta.
     *
     * @return the udp meta
     */
    public UdpMeta getUdpMeta() {
        return udpMeta;
    }

    /**
     * Gets the response header meta.
     *
     * @return the response header meta
     */
    public ResponseHeaderMeta getResponseHeaderMeta() {
        return responseHeaderMeta;
    }

}
