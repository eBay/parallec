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
package io.parallec.core.actor.message;

import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;

import java.util.List;
import java.util.Map;


// TODO: Auto-generated Javadoc
/**
 * A single HTTP response for each http request
 * 
 * Note that the failObtainResponse in the response is whether work successfully
 * received. it is not about 200 code.
 * 
 * The Single Response will be in error when the HTTP Worker fail to obtain the
 * response back on condition of 
 * [CANCEL or PROCESS_ON_EXCEPTION or PROCESS_ON_TIMEOUT]
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class ResponseOnSingeRequest {

    /** The response body. */
    // Response attributes
    private String responseBody;

    /** The fail obtain response. */
    private boolean failObtainResponse;

    /** The error message. */
    private String errorMessage;

    /** The stack trace. */
    private String stackTrace;

    /** The status code int. */
    private int statusCodeInt;

    /** The status code. */
    private String statusCode;

    
    /** The response headers: keys are lower cased. */
    private Map<String, List<String>> responseHeaders;
    
    
    /**
     * Checks if is fail obtain response.
     *
     * @return true, if is fail obtain response
     */
    public boolean isFailObtainResponse() {
        return failObtainResponse;
    }

    /**
     * Sets the fail obtain response.
     *
     * @param failObtainResponse
     *            the new fail obtain response
     */
    public void setFailObtainResponse(boolean failObtainResponse) {
        this.failObtainResponse = failObtainResponse;
    }

    /**
     * Sets the response body.
     *
     * @param responseBody
     *            the new response body
     */
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage
     *            the new error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Sets the stack trace.
     *
     * @param stackTrace
     *            the new stack trace
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Sets the status code.
     *
     * @param statusCode
     *            the new status code
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /** The receive time. */
    private String receiveTime = PcConstants.NA;

    /**
     * Instantiates a new response on singe request.
     *
     * @param responseBody the response body
     * @param failObtainResponse the fail obtain response
     * @param errorMessage the error message
     * @param stackTrace the stack trace
     * @param statusCode the status code
     * @param statusCodeInt the status code int
     * @param receiveTime the receive time
     * @param responseHeaders the response headers
     */
    public ResponseOnSingeRequest(String responseBody,
            boolean failObtainResponse, String errorMessage, String stackTrace,
            String statusCode, int statusCodeInt, String receiveTime,  Map<String, List<String>> responseHeaders) {
        super();

        this.responseBody = responseBody;
        this.failObtainResponse = failObtainResponse;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
        this.statusCode = statusCode;
        this.setStatusCodeInt(statusCodeInt);
        this.setReceiveTime(receiveTime);
        this.responseHeaders = responseHeaders;
    }

    /**
     * Instantiates a new response on singe request.
     */
    public ResponseOnSingeRequest() {
        this.responseBody = null;
        this.failObtainResponse = true;
        this.errorMessage = null;
        this.stackTrace = null;
        this.statusCode = null;
        this.setStatusCodeInt(PcConstants.NA_INT);
        this.setReceiveTime(null);
    }

    /**
     * Gets the response body.
     *
     * @return the response body
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the stack trace.
     *
     * @return the stack trace
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the receive time.
     *
     * @return the receive time
     */
    public String getReceiveTime() {
        return receiveTime;
    }

    /**
     * Sets the receive time now.
     */
    public void setReceiveTimeNow() {
        this.receiveTime = PcDateUtils.getNowDateTimeStrStandard();
    }

    /**
     * Sets the receive time.
     *
     * @param receiveTime
     *            the new receive time
     */
    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * Gets the status code int.
     *
     * @return the status code int
     */
    public int getStatusCodeInt() {
        return statusCodeInt;
    }

    /**
     * Sets the status code int.
     *
     * @param statusCodeInt
     *            the new status code int
     */
    public void setStatusCodeInt(int statusCodeInt) {
        this.statusCodeInt = statusCodeInt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ResponseOnSingeReq [responseBody=" + responseBody
                + ", failObtainResponse=" + failObtainResponse
                + ", errorMessage=" + errorMessage + ", stackTrace="
                + stackTrace + ", statusCodeInt=" + statusCodeInt
                + ", statusCode=" + statusCode + ", receiveTime=" + receiveTime
                + "]";
    }

    /**
     * Gets the response headers.
     *
     * @return the response headers
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets the response headers.
     *
     * @param responseHeaders the response headers
     */
    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

}// end subclass