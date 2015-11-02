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
package io.parallec.core.task;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelTask;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.poll.HttpPollerProcessor;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.util.BeanMapper;
import io.parallec.core.util.PcDateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;


/**
 * The Class ParallelTaskBean. this is for serialization and see as a snapshot
 * 
 * @author Yuanteng (Jeff) Pei
 */
public class ParallelTaskBean {

    /**
     * Instantiates a new parallel task bean.
     *
     * @param task
     *            the task
     */
    public ParallelTaskBean(ParallelTask task) {
        super();
        BeanMapper.copy(task, this);
        this.parallelTaskResult.putAll(task.getParallelTaskResult());
        this.taskErrorMetas.addAll(task.getTaskErrorMetas());
        this.replacementVarMap.putAll(task.getReplacementVarMap());
        this.state = task.getState().toString();

        this.submitTime = PcDateUtils.getDateTimeStrStandard(new Date(task
                .getSubmitTime()));
        this.executeStartTime = PcDateUtils.getDateTimeStrStandard(new Date(
                task.getExecuteStartTime()));
        this.executionEndTime = PcDateUtils.getDateTimeStrStandard(new Date(
                task.getExecutionEndTime()));

    }

    /** The time stamp. */
    private String timeStamp;

    /** TODO: The start time. */
    private String submitTime;

    /** The execute start time. */
    private String executeStartTime;

    /** The end time. */
    private String executionEndTime;

    /** The duration sec. */
    private double durationSec;

    /** The request num. */
    private Integer requestNum = null;

    /** The request num actual. */
    private Integer requestNumActual = null;

    /** The response received number. */
    private volatile Integer responsedNum = null;

    /** The task error metas. */
    private final List<TaskErrorMeta> taskErrorMetas = new ArrayList<TaskErrorMeta>();

    /** The response context. */
    // cannot be final. must be able to be replacable to pass data out.
    private Map<String, Object> responseContext = new HashMap<String, Object>();

    /** The state. */
    private String state;

    /** The job id. */
    private String taskId;

    /** The handler. */
    private ParallecResponseHandler handler;

    /** The parallel task result. */
    private Map<String, NodeReqResponse> parallelTaskResult = new HashMap<String, NodeReqResponse>();

    /** The command meta. */
    private HttpMeta commandMeta;

    /** The target host meta. */
    private TargetHostMeta targetHostMeta;

    /** The ssh meta. */
    private SshMeta sshMeta;

    /** The ssh meta. */
    private TcpMeta tcpMeta;
    
    /** The executionManager. */
    public ActorRef executionManager = null;

    /** The replacement var map node specific. */
    private final Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();

    /** The replacement var map. */
    private final Map<String, String> replacementVarMap = new HashMap<String, String>();

    /** The request replacement type. */
    private RequestReplacementType requestReplacementType = RequestReplacementType.NO_REPLACEMENT;

    /** The is pollable. */
    private boolean isPollable = false;

    /** The http poller processor. */
    private HttpPollerProcessor httpPollerProcessor = null;

    /**
     * Gets the time stamp.
     *
     * @return the time stamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the time stamp.
     *
     * @param timeStamp
     *            the new time stamp
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Gets the request num.
     *
     * @return the request num
     */
    public Integer getRequestNum() {
        return requestNum;
    }

    /**
     * Sets the request num.
     *
     * @param requestNum
     *            the new request num
     */
    public void setRequestNum(Integer requestNum) {
        this.requestNum = requestNum;
    }

    /**
     * Gets the request num actual.
     *
     * @return the request num actual
     */
    public Integer getRequestNumActual() {
        return requestNumActual;
    }

    /**
     * Sets the request num actual.
     *
     * @param requestNumActual
     *            the new request num actual
     */
    public void setRequestNumActual(Integer requestNumActual) {
        this.requestNumActual = requestNumActual;
    }

    /**
     * Gets the responsed num.
     *
     * @return the responsed num
     */
    public Integer getResponsedNum() {
        return responsedNum;
    }

    /**
     * Sets the responsed num.
     *
     * @param responsedNum
     *            the new responsed num
     */
    public void setResponsedNum(Integer responsedNum) {
        this.responsedNum = responsedNum;
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
     * Sets the response context.
     *
     * @param responseContext
     *            the response context
     */
    public void setResponseContext(Map<String, Object> responseContext) {
        this.responseContext = responseContext;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state
     *            the new state
     */
    public void setState(String state) {
        this.state = state;
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
     * Sets the handler.
     *
     * @param handler
     *            the new handler
     */
    public void setHandler(ParallecResponseHandler handler) {
        this.handler = handler;
    }

    /**
     * Gets the parallel task result.
     *
     * @return the parallel task result
     */
    public Map<String, NodeReqResponse> getParallelTaskResult() {
        return parallelTaskResult;
    }

    /**
     * Sets the parallel task result.
     *
     * @param parallelTaskResult
     *            the parallel task result
     */
    public void setParallelTaskResult(
            Map<String, NodeReqResponse> parallelTaskResult) {
        this.parallelTaskResult = parallelTaskResult;
    }

    /**
     * Gets the command meta.
     *
     * @return the command meta
     */
    public HttpMeta getCommandMeta() {
        return commandMeta;
    }

    /**
     * Sets the command meta.
     *
     * @param commandMeta
     *            the new command meta
     */
    public void setCommandMeta(HttpMeta commandMeta) {
        this.commandMeta = commandMeta;
    }

    /**
     * Gets the target host meta.
     *
     * @return the target host meta
     */
    public TargetHostMeta getTargetHostMeta() {
        return targetHostMeta;
    }

    /**
     * Sets the target host meta.
     *
     * @param targetHostMeta
     *            the new target host meta
     */
    public void setTargetHostMeta(TargetHostMeta targetHostMeta) {
        this.targetHostMeta = targetHostMeta;
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
     * Sets the ssh meta.
     *
     * @param sshMeta
     *            the new ssh meta
     */
    public void setSshMeta(SshMeta sshMeta) {
        this.sshMeta = sshMeta;
    }

    /**
     * Gets the request replacement type.
     *
     * @return the request replacement type
     */
    public RequestReplacementType getRequestReplacementType() {
        return requestReplacementType;
    }

    /**
     * Sets the request replacement type.
     *
     * @param requestReplacementType
     *            the new request replacement type
     */
    public void setRequestReplacementType(
            RequestReplacementType requestReplacementType) {
        this.requestReplacementType = requestReplacementType;
    }

    /**
     * Checks if is pollable.
     *
     * @return true, if is pollable
     */
    public boolean isPollable() {
        return isPollable;
    }

    /**
     * Sets the pollable.
     *
     * @param isPollable
     *            the new pollable
     */
    public void setPollable(boolean isPollable) {
        this.isPollable = isPollable;
    }

    /**
     * Gets the http poller processor.
     *
     * @return the http poller processor
     */
    public HttpPollerProcessor getHttpPollerProcessor() {
        return httpPollerProcessor;
    }

    /**
     * Sets the http poller processor.
     *
     * @param httpPollerProcessor
     *            the new http poller processor
     */
    public void setHttpPollerProcessor(HttpPollerProcessor httpPollerProcessor) {
        this.httpPollerProcessor = httpPollerProcessor;
    }

    /**
     * Gets the task error metas.
     *
     * @return the task error metas
     */
    public List<TaskErrorMeta> getTaskErrorMetas() {
        return taskErrorMetas;
    }

    /**
     * Gets the replacement var map node specific.
     *
     * @return the replacement var map node specific
     */
    public Map<String, StrStrMap> getReplacementVarMapNodeSpecific() {
        return replacementVarMapNodeSpecific;
    }

    /**
     * Gets the replacement var map.
     *
     * @return the replacement var map
     */
    public Map<String, String> getReplacementVarMap() {
        return replacementVarMap;
    }

    /**
     * Gets the task id.
     *
     * @return the task id
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Sets the task id.
     *
     * @param taskId
     *            the new task id
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getExecuteStartTime() {
        return executeStartTime;
    }

    public void setExecuteStartTime(String executeStartTime) {
        this.executeStartTime = executeStartTime;
    }

    public String getExecutionEndTime() {
        return executionEndTime;
    }

    public void setExecutionEndTime(String executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    public double getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(double durationSec) {
        this.durationSec = durationSec;
    }

    public TcpMeta getTcpMeta() {
        return tcpMeta;
    }

    public void setTcpMeta(TcpMeta tcpMeta) {
        this.tcpMeta = tcpMeta;
    }

}
