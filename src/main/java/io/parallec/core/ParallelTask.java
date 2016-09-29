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

import io.parallec.core.actor.message.CancelTaskOnHostRequest;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.message.type.ExecutionManagerMsgType;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.SetAndCount;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.config.ParallelTaskConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.task.ParallelTaskBean;
import io.parallec.core.task.ParallelTaskManager;
import io.parallec.core.task.ParallelTaskState;
import io.parallec.core.task.RequestReplacementType;
import io.parallec.core.task.TaskErrorMeta;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcStringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.ning.http.client.AsyncHttpClient;

/**
 * The key class represents a onetime execution on multiple requests. It
 * contains all the task and request metadata, target hosts, configs, and the
 * responses.
 * 
 * A ParallelTask is the returned object from the
 * {@link ParallelTaskBuilder#execute}
 * 
 * <ul>
 * <li>The metadata on this whole task, including config, running state,
 * progress, request count, task id.&nbsp;</li>
 * <li>The results of the task:&nbsp;parallelTaskResult,&nbsp;&nbsp;which is a
 * hashmap of each target host map with its response. There is also a received
 * count</li>
 * <li>Detailed request metadata on HTTP/SSH/PING/TCP. &nbsp;The async http
 * client used for this task ( you may replace it with your own )</li>
 * <li>Target host list</li>
 * <li>The user defined response handler</li>
 * <li>A actorRef pointer to the command manager so that you may use it to
 * cancel the whole task or those requests&nbsp;that match a sublist of target
 * host lists.</li>
 * </ul>
 * 
 *
 * @author Yuanteng (Jeff) Pei
 */
public class ParallelTask {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(ParallelTask.class);

    /** The config. */
    private ParallelTaskConfig config = new ParallelTaskConfig();

    /** TODO: The start time. */
    private long submitTime;

    /** The execute start time. */
    private long executeStartTime;

    /** The end time. */
    private long executionEndTime;

    /** The duration sec. */
    private double durationSec;

    /** The request num. */
    private int requestNum;

    /** The request num actual. */
    private int requestNumActual;

    /** The responsed num. */
    private volatile int responsedNum = 0;

    /** The task error metas. */
    private final List<TaskErrorMeta> taskErrorMetas = new ArrayList<TaskErrorMeta>();

    /** The response context. */
    // cannot be final. must be able to be replacable to pass data out.
    private Map<String, Object> responseContext = new HashMap<String, Object>();

    /** The state. */
    private ParallelTaskState state = ParallelTaskState.WAITING;;

    /** The task id. */
    private String taskId;

    /** The handler. */
    private ParallecResponseHandler handler;

    /** The aggregate result map. */
    private final Map<String, LinkedHashSet<String>> aggregateResultMap = new ConcurrentHashMap<String, LinkedHashSet<String>>();

    /**
     * The parallel task result: a hashmap to store the request parameters, host
     * name, ResponseOnSingleTask. Note that by default, the response content is
     * not saved into the ResponseOnSingleTask. Unless the user changes the
     * config by calling {@link ParallelTaskBuilder#setSaveResponseToTask}
     * 
     * */
    private Map<String, NodeReqResponse> parallelTaskResult = new ConcurrentHashMap<String, NodeReqResponse>();

    /** The http meta. */
    private HttpMeta httpMeta;

    /** The target host meta. */
    private TargetHostMeta targetHostMeta;

    /** The ssh meta. */
    private SshMeta sshMeta;

    /** The TCP meta. */
    private TcpMeta tcpMeta;

    /** The UDP meta. */
    private UdpMeta udpMeta;

    /** The ping meta. */
    private PingMeta pingMeta;

    /**
     * The command manager. if private: getter/setter: openpojo unit test will
     * fail.
     */
    public ActorRef executionManager = null;

    /** The replacement var map node specific. */
    private final Map<String, StrStrMap> replacementVarMapNodeSpecific = new ConcurrentHashMap<String, StrStrMap>();

    /** The replacement var map. */
    private final Map<String, String> replacementVarMap = new ConcurrentHashMap<String, String>();

    /** The request replacement type. */
    private RequestReplacementType requestReplacementType = RequestReplacementType.NO_REPLACEMENT;

    /** The request protocol. */
    private RequestProtocol requestProtocol;

    /** The concurrency. */
    private int concurrency;

    // end member var

    /**
     * Instantiates a new parallel task.
     */
    public ParallelTask() {
        this.setTaskId(generateTaskId());
        this.responsedNum = 0;
        this.requestNum = 0;
        this.state = ParallelTaskState.WAITING;

        // use default config
        this.config = new ParallelTaskConfig();

    }

    /**
     * Instantiates a new parallel task.
     *
     * @param requestProtocol
     *            the request protocol
     * @param concurrency
     *            the concurrency
     * @param httpMeta
     *            the http meta
     * @param targetHostMeta
     *            the target host meta
     * @param sshMeta
     *            the ssh meta
     * @param tcpMeta
     *            the tcp meta
     * @param udpMeta
     *            the udp meta
     * @param pingMeta
     *            the ping meta
     * @param handler
     *            the handler
     * @param responseContext
     *            the response context
     * @param replacementVarMapNodeSpecific
     *            the replacement var map node specific
     * @param replacementVarMap
     *            the replacement var map
     * @param requestReplacementType
     *            the request replacement type
     * @param config
     *            the config
     */
    public ParallelTask(RequestProtocol requestProtocol, int concurrency,
            HttpMeta httpMeta, TargetHostMeta targetHostMeta, SshMeta sshMeta,
            TcpMeta tcpMeta, UdpMeta udpMeta, PingMeta pingMeta,
            ParallecResponseHandler handler,
            Map<String, Object> responseContext,
            Map<String, StrStrMap> replacementVarMapNodeSpecific,
            Map<String, String> replacementVarMap,
            RequestReplacementType requestReplacementType,
            ParallelTaskConfig config

    ) {
        this.requestProtocol = requestProtocol;
        this.concurrency = concurrency;
        this.targetHostMeta = targetHostMeta;
        // set taskid / requestNum must be after set target hosts meta;
        // as it is using the target hosts count
        this.taskId = this.generateTaskId();
        this.requestNum = targetHostMeta.getHosts().size();
        // make it the same as init num
        this.requestNumActual = requestNum;

        this.httpMeta = httpMeta;
        this.tcpMeta = tcpMeta;
        this.udpMeta = udpMeta;
        this.sshMeta = sshMeta;
        this.pingMeta = pingMeta;
        this.handler = handler;

        this.responsedNum = 0;
        this.state = ParallelTaskState.WAITING;
        if (responseContext != null)
            this.responseContext = responseContext;

        this.replacementVarMapNodeSpecific
                .putAll(replacementVarMapNodeSpecific);
        this.replacementVarMap.putAll(replacementVarMap);
        this.requestReplacementType = requestReplacementType;

        this.config = config;
    }

    /**
     * Cancel on target hosts.
     *
     * @param targetHosts
     *            the target hosts
     * @return true, if successful
     */
    @SuppressWarnings("deprecation")
    public boolean cancelOnTargetHosts(List<String> targetHosts) {

        boolean success = false;

        try {

            switch (state) {

            case IN_PROGRESS:
                if (executionManager != null
                        && !executionManager.isTerminated()) {
                    executionManager.tell(new CancelTaskOnHostRequest(
                            targetHosts), executionManager);
                    logger.info(
                            "asked task to stop from running on target hosts with count {}...",
                            targetHosts.size());
                } else {
                    logger.info("manager already killed or not exist.. NO OP");
                }
                success = true;
                break;
            case COMPLETED_WITHOUT_ERROR:
            case COMPLETED_WITH_ERROR:
            case WAITING:
                logger.info("will NO OP for cancelOnTargetHost as it is not in IN_PROGRESS state");
                success = true;
                break;
            default:
                break;

            }

        } catch (Exception e) {
            logger.error(
                    "cancel task {} on hosts with count {} error with exception details ",
                    this.getTaskId(), targetHosts.size(), e);
        }

        return success;
    }

    /**
     * Cancel.
     *
     * @param sync
     *            the sync
     * @return true, if successful
     */
    @SuppressWarnings("deprecation")
    public boolean cancel(boolean sync) {

        boolean success = false;

        try {
            switch (state) {
            case WAITING:
                ParallelTaskManager.getInstance().removeTaskFromWaitQ(this);
                this.state = ParallelTaskState.COMPLETED_WITHOUT_ERROR;
                success = true;
                break;
            case IN_PROGRESS:
                if (executionManager != null
                        && !executionManager.isTerminated()) {

                    executionManager.tell(ExecutionManagerMsgType.CANCEL,
                            executionManager);
                    logger.info(
                            "Asked parallel task {} to stop from running...",
                            this.taskId);

                    if (sync) {
                        logger.info("Run cancel in SYNC mode... waiting for task to finish...");
                        while (!isCompleted()) {
                            try {
                                Thread.sleep(100L);
                            } catch (InterruptedException e) {
                                logger.error(" task {} interrupted ", this.taskId);
                            }
                        }
                        logger.info("Task completed! Cancellation is completed.");
                    } else {
                        logger.info("Run cancel in ASYNC mode... will now return...");
                    }

                } else {
                    logger.info("manager already killed or not exist..");
                }
                this.state = ParallelTaskState.COMPLETED_WITH_ERROR;
                success = true;
                break;
            case COMPLETED_WITHOUT_ERROR:
            case COMPLETED_WITH_ERROR:
                logger.info("task are already in completed state..no operation...");
                success = true;
                break;
            default:
                break;

            }

        } catch (Exception e) {
            logger.error("cancel task {} error with exception details ",
                    this.getTaskId(), e);
        }

        return success;
    }

    /**
     * Capacity used.
     *
     * @return the int
     */
    public int capacityUsed() {
        return Math.min(this.requestNum, this.getConcurrency());
    }

    /**
     * will do validation. for empty will either throw exception or fill with
     * default values
     *
     * @return true, if successful
     * @throws ParallelTaskInvalidException
     *             the parallel task invalid exception
     */
    public boolean validateWithFillDefault()
            throws ParallelTaskInvalidException {

        /**
         * for the client: will use the current default. note that that can be
         * overwritten!
         */

        // validate if there are no hosts anymore
        if (this.targetHostMeta.getHosts().isEmpty()) {
            throw new ParallelTaskInvalidException(
                    "Empty targetHosts! Please set target hosts and try again...return..");
        }
        if (requestProtocol == null) {
            requestProtocol = RequestProtocol.HTTP;
            logger.info("USE DEFAULT HTTP PROTOCOL: Missing Protocol HTTP/HTTPS. SET protocol as default HTTP");
        }

        // concurrency check range
        if (this.getConcurrency() <= 0
                || this.getConcurrency() > ParallecGlobalConfig.maxCapacity) {
            logger.info("USE DEFAULT CONCURRENCY: User did not specify max concurrency "
                    + "or its out of max allowed capacity: "
                    + ParallecGlobalConfig.concurrencyDefault);
            this.setConcurrency(ParallecGlobalConfig.concurrencyDefault);
        }

        if (this.config == null) {
            logger.info("USE DEFAULT CONFIG: User did not specify"
                    + " config for task/actor timeout etc. ");
            this.config = new ParallelTaskConfig();
        }

        // check if ssh
        if (this.requestProtocol == RequestProtocol.SSH) {

            // this will throw ParallelTaskInvalidException
            this.sshMeta.validation();

            if (this.getConcurrency() > ParallecGlobalConfig.concurrencySshLimit) {
                logger.info("SSH CONCURRENCY LIMIT is lower. Apply value as: "
                        + ParallecGlobalConfig.concurrencySshLimit);
                this.setConcurrency(ParallecGlobalConfig.concurrencySshLimit);
            }
            if (this.httpMeta.isPollable())
                throw new ParallelTaskInvalidException(
                        "Not support pollable job with SSH.");

            this.httpMeta.initValuesNa();
            // remove tcp object
            this.tcpMeta = null;
            // remove ping object
            this.pingMeta = null;
            // remove udp object
            this.udpMeta = null;
        } else if (this.requestProtocol == RequestProtocol.PING) {

            if (this.httpMeta.isPollable())
                throw new ParallelTaskInvalidException(
                        "Not support pollable job with PING.");
            this.httpMeta.initValuesNa();

            this.pingMeta.validation();
            // remove ssh object
            this.sshMeta = null;
            // remove tcp object
            this.tcpMeta = null;
            // remove udp object
            this.udpMeta = null;

            // TCP
        } else if (this.requestProtocol == RequestProtocol.TCP) {
            if (this.httpMeta.isPollable())
                throw new ParallelTaskInvalidException(
                        "Not support pollable job with TCP.");
            this.httpMeta.initValuesNa();

            this.tcpMeta.validation();
            // remove ssh object
            this.sshMeta = null;
            // remove ping object
            this.pingMeta = null;
            // remove udp object
            this.udpMeta = null;
            // UDP
        } else if (this.requestProtocol == RequestProtocol.UDP) {
            if (this.httpMeta.isPollable())
                throw new ParallelTaskInvalidException(
                        "Not support pollable job with UDP.");
            this.httpMeta.initValuesNa();

            this.udpMeta.validation();
            // remove tcp object
            this.tcpMeta = null;
            // remove ssh object
            this.sshMeta = null;
            // remove ping object
            this.pingMeta = null;
            // HTTP/HTTPS
        } else {

            this.httpMeta.validation();
            // remove ssh object
            this.sshMeta = null;
            // remove tcp object
            this.tcpMeta = null;
            // remove ping object
            this.pingMeta = null;
            // remove udp object
            this.udpMeta = null;
        }// end else

        return true;
    }// end func

    /**
     * Gen job id.
     *
     * @return the string
     */
    public String generateTaskId() {
        final String uuid = UUID.randomUUID().toString().substring(0, 12);
        int size = this.targetHostMeta == null ? 0 : this.targetHostMeta
                .getHosts().size();
        return "PT_" + size + "_"
                + PcDateUtils.getNowDateTimeStrConciseNoZone() + "_" + uuid;
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     */
    public Double getProgress() {

        if (state.equals(ParallelTaskState.IN_PROGRESS)) {
            if (requestNum != 0) {
                return 100.0 * ((double) responsedNum / (double) requestNumActual);
            } else {
                return 0.0;
            }
        }

        if (state.equals(ParallelTaskState.WAITING)) {
            return 0.0;
        }

        // fix task if fail validation, still try to poll progress 0901
        if (state.equals(ParallelTaskState.COMPLETED_WITH_ERROR)
                || state.equals(ParallelTaskState.COMPLETED_WITHOUT_ERROR)) {
            return 100.0;
        }

        return 0.0;

    }

    /**
     * state==ParallelTaskState.COMPLETED_WITHOUT_ERROR ||
     * state==ParallelTaskState.COMPLETED_WITH_ERROR;
     *
     * @return true, if is completed
     */
    public boolean isCompleted() {
        return state == ParallelTaskState.COMPLETED_WITHOUT_ERROR
                || state == ParallelTaskState.COMPLETED_WITH_ERROR;
    }

    /**
     * Gets the async http client.
     *
     * @return the async http client
     */
    public AsyncHttpClient getAsyncHttpClient() {
        return this.httpMeta.getAsyncHttpClient();
    }

    /**
     * Sets the async http client.
     *
     * @param asyncHttpClient
     *            the new async http client
     */
    public void setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {
        this.httpMeta.setAsyncHttpClient(asyncHttpClient);
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
     * Gets the task error metas.
     *
     * @return the task error metas
     */
    public List<TaskErrorMeta> getTaskErrorMetas() {
        return taskErrorMetas;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ParallelTask [config=" + config + ", submitTime=" + submitTime
                + ", executeStartTime=" + executeStartTime
                + ", executionEndTime=" + executionEndTime + ", durationSec="
                + durationSec + ", requestNum=" + requestNum
                + ", requestNumActual=" + requestNumActual + ", responsedNum="
                + responsedNum + ", taskErrorMetas=" + taskErrorMetas
                + ", responseContext=" + responseContext + ", state=" + state
                + ", taskId=" + taskId + ", handler=" + handler
                + ", aggregateResultMap=" + aggregateResultMap
                + ", parallelTaskResult=" + parallelTaskResult + ", httpMeta="
                + httpMeta + ", targetHostMeta=" + targetHostMeta
                + ", sshMeta=" + sshMeta + ", tcpMeta=" + tcpMeta
                + ", pingMeta=" + pingMeta + ", executionManager="
                + executionManager + ", replacementVarMapNodeSpecific="
                + replacementVarMapNodeSpecific + ", replacementVarMap="
                + replacementVarMap + ", requestReplacementType="
                + requestReplacementType + ", requestProtocol="
                + requestProtocol + ", concurrency=" + concurrency + "]";
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
     * Gets the response context.
     *
     * @return the response context
     */
    public Map<String, Object> getResponseContext() {
        return responseContext;
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
     * Gets the command meta.
     *
     * @return the command meta
     */
    public HttpMeta getHttpMeta() {
        return httpMeta;

    }

    /**
     * Sets the command meta.
     *
     * @param httpMeta
     *            the new http meta
     */
    public void setHttpMeta(HttpMeta httpMeta) {
        this.httpMeta = httpMeta;
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
     * Gets the config.
     *
     * @return the config
     */
    public ParallelTaskConfig getConfig() {
        return config;
    }

    /**
     * Sets the config.
     *
     * @param config
     *            the new config
     */
    public void setConfig(ParallelTaskConfig config) {
        this.config = config;
    }

    /**
     * Pretty print info.
     *
     * @return the string
     */
    public String prettyPrintInfo() {
        return PcStringUtils.renderJson(new ParallelTaskBean(this));
    }

    /**
     * Save log to local.
     *
     * @param path
     *            the path
     * @return true, if successful
     */
    public boolean saveLogToLocal(String path) {
        String content = PcStringUtils.renderJson(new ParallelTaskBean(this));
        File file = new File(path);
        boolean success = false;
        try {
            FileUtils.writeStringToFile(file, content);
            success = true;
        } catch (IOException e) {
            logger.error("error writing parallel task to path {} details ",
                    path, e);
        }
        logger.info(
                "Save parallel task {} log  to disk at path {}. Success?: {}",
                this.taskId, path, success);
        return success;
    }

    /**
     * Save log to local.
     *
     * @return true, if successful
     */
    public boolean saveLogToLocal() {
        String path = ParallecGlobalConfig.taskLogFolderWithSlash + this.taskId
                + ParallecGlobalConfig.taskLogPostfix;
        return saveLogToLocal(path);
    }

    /**
     * Gets the execution end time.
     *
     * @return the execution end time
     */
    public long getExecutionEndTime() {
        return executionEndTime;
    }

    /**
     * Sets the execution end time.
     *
     * @param executionEndTime
     *            the new execution end time
     */
    public void setExecutionEndTime(long executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    /**
     * Aggregate results to see the status code distribution with target hosts.
     *
     * @return the aggregateResultMap
     */
    public Map<String, SetAndCount> getAggregateResultFullSummary() {

        Map<String, SetAndCount> summaryMap = new ConcurrentHashMap<String, SetAndCount>();

        for (Entry<String, LinkedHashSet<String>> entry : aggregateResultMap
                .entrySet()) {
            summaryMap.put(entry.getKey(), new SetAndCount(entry.getValue()));
        }

        return summaryMap;
    }

    /**
     * Gets the aggregated result human str.
     *
     * @return the aggregated result human str
     */
    public String getAggregatedResultHumanStr() {
        return PcStringUtils.getAggregatedResultHuman(aggregateResultMap);
    }

    /**
     * Gets the aggregate result count summary. only list the counts for brief
     * understanding
     *
     * @return the aggregate result count summary
     */
    public Map<String, Integer> getAggregateResultCountSummary() {

        Map<String, Integer> summaryMap = new LinkedHashMap<String, Integer>();

        for (Entry<String, LinkedHashSet<String>> entry : aggregateResultMap
                .entrySet()) {
            summaryMap.put(entry.getKey(), entry.getValue().size());
        }

        return summaryMap;
    }

    /**
     * Gets the aggregate result map.
     *
     * @return the aggregate result map
     */
    public Map<String, LinkedHashSet<String>> getAggregateResultMap() {
        return aggregateResultMap;
    }

    /**
     * Gets the duration sec.
     *
     * @return the duration sec
     */
    public double getDurationSec() {
        return durationSec;
    }

    /**
     * Sets the duration sec.
     *
     * @param durationSec
     *            the new duration sec
     */
    public void setDurationSec(double durationSec) {
        this.durationSec = durationSec;
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
     * Sets the tcp meta.
     *
     * @param tcpMeta
     *            the new tcp meta
     */
    public void setTcpMeta(TcpMeta tcpMeta) {
        this.tcpMeta = tcpMeta;
    }

    /**
     * Gets the request protocol.
     *
     * @return the request protocol
     */
    public RequestProtocol getRequestProtocol() {
        return requestProtocol;
    }

    /**
     * Sets the request protocol.
     *
     * @param requestProtocol
     *            the new request protocol
     */
    public void setRequestProtocol(RequestProtocol requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

    /**
     * Gets the concurrency.
     *
     * @return the concurrency
     */
    public int getConcurrency() {
        return concurrency;
    }

    /**
     * Sets the concurrency.
     *
     * @param concurrency
     *            the new concurrency
     */
    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    /**
     * Gets the submit time.
     *
     * @return the submit time
     */
    public long getSubmitTime() {
        return submitTime;
    }

    /**
     * Sets the submit time.
     *
     * @param submitTime
     *            the new submit time
     */
    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * Gets the execute start time.
     *
     * @return the execute start time
     */
    public long getExecuteStartTime() {
        return executeStartTime;
    }

    /**
     * Sets the execute start time.
     *
     * @param executeStartTime
     *            the new execute start time
     */
    public void setExecuteStartTime(long executeStartTime) {
        this.executeStartTime = executeStartTime;
    }

    /**
     * Gets the request num.
     *
     * @return the request num
     */
    public int getRequestNum() {
        return requestNum;
    }

    /**
     * Sets the request num.
     *
     * @param requestNum
     *            the new request num
     */
    public void setRequestNum(int requestNum) {
        this.requestNum = requestNum;
    }

    /**
     * Gets the request num actual.
     *
     * @return the request num actual
     */
    public int getRequestNumActual() {
        return requestNumActual;
    }

    /**
     * Sets the request num actual.
     *
     * @param requestNumActual
     *            the new request num actual
     */
    public void setRequestNumActual(int requestNumActual) {
        this.requestNumActual = requestNumActual;
    }

    /**
     * Gets the responsed num.
     *
     * @return the responsed num
     */
    public int getResponsedNum() {
        return responsedNum;
    }

    /**
     * Sets the responsed num.
     *
     * @param responsedNum
     *            the new responsed num
     */
    public void setResponsedNum(int responsedNum) {
        this.responsedNum = responsedNum;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public ParallelTaskState getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state
     *            the new state
     */
    public void setState(ParallelTaskState state) {
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
     * Gets the ping meta.
     *
     * @return the ping meta
     */
    public PingMeta getPingMeta() {
        return pingMeta;
    }

    /**
     * Sets the ping meta.
     *
     * @param pingMeta
     *            the new ping meta
     */
    public void setPingMeta(PingMeta pingMeta) {
        this.pingMeta = pingMeta;
    }

    public UdpMeta getUdpMeta() {
        return udpMeta;
    }

    public void setUdpMeta(UdpMeta udpMeta) {
        this.udpMeta = udpMeta;
    }

}
