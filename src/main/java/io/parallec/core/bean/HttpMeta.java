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

import io.parallec.core.ParallecHeader;
import io.parallec.core.actor.poll.HttpPollerProcessor;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.util.PcConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;

/**
 * The metadata about the HTTP request (url/port/header/concurrency etc) It does
 * not include the target hosts information.
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class HttpMeta {
    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(HttpMeta.class);
    /** The http method. */
    private HttpMethod httpMethod;

    /** The request url postfix. */
    private String requestUrlPostfix;

    /** The request entityBody */
    private String entityBody;

    /** The request port. */
    private String requestPort;

    /** The parallec header. */
    private ParallecHeader parallecHeader;

    /** The http poller processor. */
    private HttpPollerProcessor httpPollerProcessor = null;

    // 0910
    /** The is pollable. */
    // pollable
    private boolean isPollable = false;

    /** The async http client. */
    private AsyncHttpClient asyncHttpClient;

    /**
     * Instantiates a new command meta.
     */
    // when init PTask Builder
    public HttpMeta() {
        this.httpMethod = null;
        this.requestUrlPostfix = null;
        this.entityBody = null;
        this.requestPort = null;
        this.parallecHeader = null;
        this.httpPollerProcessor = null;
        this.isPollable = false;
        this.asyncHttpClient = null;
    };

    /**
     * for none HTTP type: set some default for HTTP (not used) to avoid NPE.
     */
    public void initValuesNa() {
        // just set some default as not used
        setRequestPort("0");
        setHeaderMetadata(new ParallecHeader());
        setHttpMethod(HttpMethod.NA);
        setEntityBody(PcConstants.COMMAND_VAR_DEFAULT_REQUEST_CONTENT);
        setRequestUrlPostfix("");
        this.httpPollerProcessor = null;
        this.isPollable = false;
        this.asyncHttpClient = null;
    }



    /**
     * Validation.
     *
     * @return true, if successful
     * @throws ParallelTaskInvalidException
     *             the parallel task invalid exception
     */
    public boolean validation() throws ParallelTaskInvalidException {

        if (this.getAsyncHttpClient() == null) {
            logger.info("USE DEFAULT HTTP CLIENT: Did not set special asyncHttpClient, will use the current default one: "
                    + HttpClientStore.getInstance()
                            .getHttpClientTypeCurrentDefault().toString());
            this.asyncHttpClient = HttpClientStore.getInstance()
                    .getCurrentDefaultClient();
        }

        
        if (this.getHttpMethod() == null)
            throw new ParallelTaskInvalidException("Missing getHttpMethod!");
        if (this.getHeaderMetadata() == null) {
            logger.info("USE DEFAULT EMPTY HEADER: Did not specify HTTP header. Will use empty header."
                    + " Use .setHeaders to add headers");
            this.setHeaderMetadata(new ParallecHeader());
        }
        // if null it is OK, just set as the default
        if (this.getEntityBody() == null)
                               setEntityBody(PcConstants.COMMAND_VAR_DEFAULT_REQUEST_CONTENT);

        if (this.getRequestPort() == null) {
            setRequestPort("80");
            logger.info("USE DEFAULT PORT: Missing port. SET default port to be 80");
        }

        if (this.getRequestUrlPostfix() == null
                || this.getRequestUrlPostfix().trim().isEmpty()) {
            setRequestUrlPostfix("");
            logger.info("USE DEFAULT URL: RequestUrlPostfix is null or empty. SET as empty \"\". e.g. just want to GET http://parallec.io");
        }
        if (this.isPollable() == true &&
                this.getHttpPollerProcessor() == null) {
                throw new ParallelTaskInvalidException(
                        "set pollable but httpPollerProcessor is null!! Invalid. please set httpPollerProcessor() ");
        }

        return true;

    }
    


    /**
     * Gets the request port.
     *
     * @return the request port
     */
    public String getRequestPort() {
        return requestPort;
    }

    /**
     * Sets the request port.
     *
     * @param requestPort
     *            the new request port
     */
    public void setRequestPort(String requestPort) {
        this.requestPort = requestPort;
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
     * Sets the http method.
     *
     * @param httpMethod
     *            the new http method
     */
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Gets the request url postfix.
     *
     * @return the request url postfix
     */
    public String getRequestUrlPostfix() {
        return requestUrlPostfix;
    }

    /**
     * will trim the leading and trailing whitespace if not null.
     *
     * @param requestUrlPostfix
     *            the new request url postfix
     */
    public void setRequestUrlPostfix(String requestUrlPostfix) {
        this.requestUrlPostfix = requestUrlPostfix != null ? requestUrlPostfix
                .trim() : null;
    }

    /**
     * Gets the request content template.
     *
     * @return the request content template
     */
    public String getEntityBody() {
        return entityBody;
    }

    public void setEntityBody(String entityBody) {
        this.entityBody = entityBody;
    }


    @Override
    public String toString() {
        return "HttpMeta [httpMethod=" + httpMethod + ", requestUrlPostfix="
                + requestUrlPostfix + ", entityBody="
                + entityBody + ", requestPort=" + requestPort
                + ", parallecHeader=" + parallecHeader + "]";
    }

    /**
     * Replace full request content.
     *
     * @param requestContentTemplate
     *            the request content template
     * @param replacementString
     *            the replacement string
     * @return the string
     */
    public static String replaceFullRequestContent(
            String requestContentTemplate, String replacementString) {
        return (requestContentTemplate.replace(
                PcConstants.COMMAND_VAR_DEFAULT_REQUEST_CONTENT,
                replacementString));
    }

    /**
     * Replace default full request content.
     *
     * @param requestContentTemplate
     *            the request content template
     * @return the string
     */
    // only replace AGENT_COMMAND_VAR_DEFAULT_REQUEST_CONTENT by ""
    public static String replaceDefaultFullRequestContent(
            String requestContentTemplate) {
        return replaceFullRequestContent(requestContentTemplate, "");
    }

    /**
     * Gets the header metadata.
     *
     * @return the header metadata
     */
    public ParallecHeader getHeaderMetadata() {
        return parallecHeader;
    }

    /**
     * Sets the header metadata.
     *
     * @param parallecHeader
     *            the new header metadata
     */
    public void setHeaderMetadata(ParallecHeader parallecHeader) {
        this.parallecHeader = parallecHeader;
    }

    public HttpMeta(HttpMethod httpMethod, String requestUrlPostfix,
            String entityBody, String requestPort, ParallecHeader parallecHeader) {
        super();
        this.httpMethod = httpMethod;
        this.requestUrlPostfix = requestUrlPostfix;
        this.entityBody = entityBody;
        this.requestPort = requestPort;
        this.parallecHeader = parallecHeader;
    }

    public HttpPollerProcessor getHttpPollerProcessor() {
        return httpPollerProcessor;
    }

    public void setHttpPollerProcessor(HttpPollerProcessor httpPollerProcessor) {
        this.httpPollerProcessor = httpPollerProcessor;
    }

    public boolean isPollable() {
        return isPollable;
    }

    public void setPollable(boolean isPollable) {
        this.isPollable = isPollable;
    }

    public AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public void setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

}
