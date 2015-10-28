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
package io.parallec.core;

import io.parallec.core.bean.TaskRequest;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * this includes the request
 * each target host will have one.
 * 
 * {@link ResponseOnSingleTask#isError()} is true: means fail to receive resposne 
 *
 * @author Yuanteng Jeff Pei
 * 
 */
public class ResponseOnSingleTask {

    /** The request. */
    // Request attributes
    private TaskRequest request;

    /** The response content. */
    private String responseContent;
    // for pollable job: the complete time. otherwise it is the receive time for
    /** The receive time. */
    // the http response
    private String receiveTime = PcConstants.NA;

    /** The receive time in manager. */
    private String receiveTimeInManager = PcConstants.NA;

    /** The error. */
    private Boolean error = null;

    /** The error message. */
    private String errorMessage = PcConstants.NA;

    /** The stack trace. */
    private String stackTrace = PcConstants.NA;

    /** The status code. */
    private String statusCode = PcConstants.NA;

    /** The status code int. */
    private int statusCodeInt = PcConstants.NA_INT;

    /**
     * The operation time millis. From when the Operation worker received the
     * request, to when operation worker finish and return back the response on
     * single task
     */
    private long operationTimeMillis;

    /** The polling history map. */
    // for polling history 201412
    final private Map<String, String> pollingHistoryMap = new LinkedHashMap<String, String>();

    /**
     * Gets the polling history map.
     *
     * @return the polling history map
     */
    public Map<String, String> getPollingHistoryMap() {
        return pollingHistoryMap;
    }

    /**
     * Gets the polling history.
     *
     * @return the polling history
     */
    public String getPollingHistory() {
        return PcStringUtils.renderJson(pollingHistoryMap);
    }

    /**
     * Instantiates a new response on single task.
     */
    public ResponseOnSingleTask() {
        super();
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
     * Sets the receive time.
     *
     * @param receiveTime
     *            the new receive time
     */
    public void setReceiveTime(String receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * Gets the response content.
     *
     * @return the response content
     */
    public String getResponseContent() {
        return responseContent;
    }

    /**
     * Sets the response content.
     *
     * @param responseContent
     *            the new response content
     */
    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }

    /**
     * Gets the receive time in manager.
     *
     * @return the receive time in manager
     */
    public String getReceiveTimeInManager() {
        return receiveTimeInManager;
    }

    /**
     * Sets the receive time in manager.
     *
     * @param receiveTimeInManager
     *            the new receive time in manager
     */
    public void setReceiveTimeInManager(String receiveTimeInManager) {
        this.receiveTimeInManager = receiveTimeInManager;
    }

    /**
     * Checks if is error.
     *
     * @return true, if is error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Sets the error.
     *
     * @param error
     *            the new error
     */
    public void setError(boolean error) {
        this.error = error;
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
     * Sets the stack trace.
     *
     * @param stackTrace
     *            the new stack trace
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Gets the operation time millis.
     *
     * @return the operation time millis
     */
    public long getOperationTimeMillis() {
        return operationTimeMillis;
    }

    /**
     * Sets the operation time millis.
     *
     * @param operationTimeMillis
     *            the new operation time millis
     */
    public void setOperationTimeMillis(long operationTimeMillis) {
        this.operationTimeMillis = operationTimeMillis;
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
     * Sets the status code.
     *
     * @param statusCode
     *            the new status code
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public TaskRequest getRequest() {
        return request;
    }

    /**
     * Sets the request.
     *
     * @param request
     *            the new request
     */
    public void setRequest(TaskRequest request) {
        this.request = request;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return this.request == null ? PcConstants.NA : this.request.getHost();
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
     * Sets the error message.
     *
     * @param errorMessage
     *            the new error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SingleTaskResponse [request=" + request + ", responseContent="
                + responseContent + ", receiveTime=" + receiveTime
                + ", receiveTimeInManager=" + receiveTimeInManager + ", error="
                + error + ", errorMessage=" + errorMessage + ", stackTrace="
                + stackTrace + ", statusCode=" + statusCode
                + ", operationTimeMillis=" + operationTimeMillis
                + ", pollingHistoryMap=" + pollingHistoryMap + "]";
    }

    public int getStatusCodeInt() {
        return statusCodeInt;
    }

    public void setStatusCodeInt(int statusCodeInt) {
        this.statusCodeInt = statusCodeInt;
    }

}
