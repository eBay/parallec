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
package io.parallec.core.actor;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelTask;
import io.parallec.core.RequestProtocol;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.actor.message.CancelTaskOnHostRequest;
import io.parallec.core.actor.message.InitialRequestToManager;
import io.parallec.core.actor.message.NodeReqResponse;
import io.parallec.core.actor.message.RequestToBatchSenderAsstManager;
import io.parallec.core.actor.message.ResponseCountToBatchSenderAsstManager;
import io.parallec.core.actor.message.ResponseFromManager;
import io.parallec.core.actor.message.type.ExecutionManagerMsgType;
import io.parallec.core.actor.message.type.OperationWorkerMsgType;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.ResponseHeaderMeta;
import io.parallec.core.bean.SingleTargetTaskStatus;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.bean.TaskRequest;
import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.commander.workflow.InternalDataProvider;
import io.parallec.core.config.HandlerExecutionLocation;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ExecutionManagerExecutionException;
import io.parallec.core.exception.ExecutionManagerExecutionException.ManagerExceptionType;
import io.parallec.core.resources.HttpMethod;
import io.parallec.core.task.ParallelTaskState;
import io.parallec.core.task.TaskErrorMeta;
import io.parallec.core.task.TaskErrorMeta.TaskErrorType;
import io.parallec.core.util.PcConstants;
import io.parallec.core.util.PcDateUtils;
import io.parallec.core.util.PcHttpUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncHttpClient;

/**
 * This is the Akka Actor that executes the ParallelTask.
 * 
 * Will create a list of operation workers to match each of the target hosts.
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class ExecutionManager extends UntypedActor {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(ExecutionManager.class);

    /** The response count. */
    protected int responseCount = 0;

    /** The request count. */
    protected int requestCount = 0;

    /** The start time. */
    protected long startTime = System.currentTimeMillis();

    /** The was issued cancel. */
    protected boolean wasIssuedCancel = false;

    /** The end time. */
    protected long endTime = -1L;

    /** The director. */
    protected ActorRef director = null;

    /** The workers. */
    protected final Map<String, ActorRef> workers = new LinkedHashMap<String, ActorRef>();

    /** The batch sender asst manager. */
    protected ActorRef batchSenderAsstManager = null;

    /** The response map. */
    protected final Map<String, ResponseOnSingleTask> responseMap = new HashMap<String, ResponseOnSingleTask>();

    /** The task id. */
    protected String taskId = null;

    /** The task id trim. */
    protected String taskIdTrim = null;

    /** The timeout message cancellable. */
    protected Cancellable timeoutMessageCancellable = null;

    /** The async http client global. */
    protected AsyncHttpClient asyncHttpClientGlobal = null;

    /** The parallel task. */
    protected ParallelTask task = null;

    /** The command metadata. */
    protected HttpMeta httpMeta = null;

    /** The node group metadata. */
    protected TargetHostMeta targetHostMeta = null;

    /**
     * Instantiates a new command manager.
     *
     * @param task
     *            the task
     */
    public ExecutionManager(ParallelTask task) {
        this.task = task;
    }

    /** The Constant REDUCE_LEN. */
    public static final int REDUCE_LEN = 12;

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) {
        try {
            // Start all workers
            if (message instanceof InitialRequestToManager) {
                director = getSender();
                logger.info("parallec task state : "
                        + ParallelTaskState.IN_PROGRESS.toString());
                task.setState(ParallelTaskState.IN_PROGRESS);
                task.setExecuteStartTime(startTime);

                taskId = task.getTaskId();

                // assumption: at least REDUCE_LEN
                taskIdTrim = taskId.length() <= REDUCE_LEN ? taskId : taskId
                        .substring(taskId.length() - REDUCE_LEN,
                                taskId.length());

                httpMeta = task.getHttpMeta();
                targetHostMeta = task.getTargetHostMeta();
                final RequestProtocol requestProtocol = task
                        .getRequestProtocol();

                // Get request parameters to construct a REST CALL
                final String requestUrlPrefixOrig = httpMeta
                        .getRequestUrlPostfix();
                final HttpMethod httpMethod = httpMeta.getHttpMethod();

                String requestPortStrOrig = httpMeta.getRequestPort();

                final boolean pollable = httpMeta.isPollable();

                final int maxConcurrency = task.getConcurrency();

                Map<String, NodeReqResponse> nodeDataMapValid = task
                        .getParallelTaskResult();

                logger.info("Before Safety Check: total entry count: "
                        + nodeDataMapValid.size());

                Map<String, NodeReqResponse> nodeDataMapValidSafe = new HashMap<String, NodeReqResponse>();

                InternalDataProvider.getInstance()
                        .filterUnsafeOrUnnecessaryRequest(nodeDataMapValid,
                                nodeDataMapValidSafe);

                logger.info(
                        "After Safety Check: total entry count in nodeDataMapValidSafe: {}",
                        nodeDataMapValidSafe.size());

                logger.debug("maxConcurrency : {}", maxConcurrency);

                // after filter, no duplicated target hosts for this count.
                requestCount = nodeDataMapValidSafe.size();
                task.setRequestNumActual(requestCount);

                logger.info("!Obtain command request for target host meta id "
                        + targetHostMeta.getTargetHostId() + "  with count: "
                        + requestCount);
                // If there are no target hosts in the incoming message, send a
                // message back
                if (requestCount <= 0) {
                    getSender().tell(new ResponseFromManager(requestCount),
                            getSelf());
                    logger.info("req count <=0. return");
                    // not really error. just no task to run
                    reply(ParallelTaskState.COMPLETED_WITHOUT_ERROR,
                            new RuntimeException(
                                    "ReqCount after trim is 0. Return."));
                    return;
                }

                int sentRequestCounter = 0;

                asyncHttpClientGlobal = task.getAsyncHttpClient();

                final AsyncHttpClient asyncHttpClient = asyncHttpClientGlobal;

                // always send with valid safe data.
                for (Entry<String, NodeReqResponse> entry : nodeDataMapValidSafe
                        .entrySet()) {

                    final String targetHost = entry.getKey();
                    NodeReqResponse nodeReqResponse = entry.getValue();

                    final String requestContentOrig = nodeReqResponse
                            .getRequestParameters().get(
                                    PcConstants.REQUEST_BODY_PLACE_HOLDER);

                    final String requestContent = NodeReqResponse
                            .replaceStrByMap(
                                    nodeReqResponse.getRequestParameters(),
                                    requestContentOrig);
                    final String resourcePath = NodeReqResponse
                            .replaceStrByMap(
                                    nodeReqResponse.getRequestParameters(),
                                    requestUrlPrefixOrig);
                    // add support for port replacement
                    final String requestPortStr = NodeReqResponse
                            .replaceStrByMap(
                                    nodeReqResponse.getRequestParameters(),
                                    requestPortStrOrig);
                    int requestPort = 80;
                    try {
                        requestPort = Integer.parseInt(requestPortStr);
                    } catch (NumberFormatException nfe) {
                        logger.error(
                                "Error parsing replacable port with NumberFormatException. "
                                        + "No valid port for host {}. Now use default port 80",
                                targetHost);
                    }
                    // only pass when it is not in manager
                    final ParallecResponseHandler handler = task.getConfig()
                            .getHandlerExecutionLocation() == HandlerExecutionLocation.MANAGER ? null
                            : task.getHandler();
                    final Map<String, Object> responseContext = task
                            .getConfig().getHandlerExecutionLocation() == HandlerExecutionLocation.MANAGER ? null
                            : task.getResponseContext();
                    Map<String, String> httpHeaderMapLocal = new HashMap<String, String>();
                    httpHeaderMapLocal.putAll(httpMeta.getHeaderMetadata()
                            .getHeaderMap());

                    // 3rd, add the dynamic part ; generic var based
                    // replacement.
                    PcHttpUtils.replaceHttpHeaderMapNodeSpecific(
                            httpHeaderMapLocal,
                            nodeReqResponse.getRequestParameters());

                    /**
                     * If want to print to check
                     */
                    if (task.getConfig().isPrintHttpTrueHeaderMap()) {

                        for (Entry<String, String> headerEntry : httpHeaderMapLocal
                                .entrySet()) {
                            String headerKey = headerEntry.getKey();
                            String headerValue = headerEntry.getValue();

                            nodeReqResponse
                                    .getRequestParameters()
                                    .put(PcConstants.REQUEST_PARAMETER_HTTP_HEADER_PREFIX
                                            + headerKey, headerValue);
                        }

                    }

                    if (task.getConfig().isPrintPoller()) {
                        // put the one before encoding
                        nodeReqResponse.getRequestParameters().put(
                                PcConstants.NODE_REQUEST_NEED_POLLER,
                                Boolean.toString(pollable));
                    }

                    /**
                     * 20140310: END add pass HTTP header into Operation worker
                     * as part of the
                     */

                    String targetHostNew = nodeReqResponse
                            .getRequestParameters()
                            .get(PcConstants.UNIFORM_TARGET_HOST_VAR_WHEN_CHECK);
                    if (targetHostNew != null) {
                        nodeReqResponse.getRequestParameters().put(
                                PcConstants.NODE_REQUEST_TRUE_TARGET_NODE,
                                targetHostNew);
                    }

                    final String hostUniform = (targetHostNew == null) ? null
                            : targetHostNew;

                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_STATUS,
                            SingleTargetTaskStatus.IN_PROGRESS.toString());

                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_TRUE_CONTENT,
                            requestContent);

                    // put the one before encoding
                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_TRUE_URL, resourcePath);

                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_TRUE_PORT,
                            Integer.toString(requestPort));

                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_HTTP_HEADER_META,
                            httpMeta.getHeaderMetadata().getHeaderStr());

                    long prepareRequestTime = System.currentTimeMillis();

                    String prepareRequestTimeStr = PcDateUtils
                            .getDateTimeStrStandard(new Date(prepareRequestTime));
                    nodeReqResponse.getRequestParameters().put(
                            PcConstants.NODE_REQUEST_PREPARE_TIME,
                            prepareRequestTimeStr);

                    final SshMeta sshMeta = task.getSshMeta();
                    final TcpMeta tcpMeta = task.getTcpMeta();
                    final UdpMeta udpMeta = task.getUdpMeta();

                    final PingMeta pingMeta = task.getPingMeta();
                    final ResponseHeaderMeta responseHeaderMeta = task
                            .getHttpMeta().getResponseHeaderMeta();

                    logger.debug("REQUEST GENERATED: "
                            + (sentRequestCounter + 1)
                            + " / "
                            + requestCount
                            + " after "
                            + Double.toString((prepareRequestTime - startTime) / 1000.0)
                            + " secs" + ":  (NOT SEND YET) " + targetHost
                            + " at " + prepareRequestTimeStr);

                    ActorRef worker = getContext().system().actorOf(
                            Props.create(OperationWorker.class,
                                    new TaskRequest(task.getConfig()
                                            .getActorMaxOperationTimeoutSec(),
                                            requestProtocol, targetHost,
                                            hostUniform, requestPort,
                                            resourcePath, requestContent,
                                            httpMethod, pollable,
                                            httpHeaderMapLocal, handler,
                                            responseContext, sshMeta, tcpMeta,
                                            udpMeta, pingMeta,
                                            responseHeaderMeta),
                                    asyncHttpClient, task.getHttpMeta()
                                            .getHttpPollerProcessor()

                            ));

                    workers.put(targetHost, worker);

                    ++sentRequestCounter;

                }// end for loop

                final RequestToBatchSenderAsstManager requestToBatchSenderAsstManager = new RequestToBatchSenderAsstManager(
                        taskId, task.getConfig()
                                .getAsstManagerRetryIntervalMillis(),
                        new ArrayList<ActorRef>(workers.values()), getSelf(),
                        maxConcurrency);

                batchSenderAsstManager = getContext().system().actorOf(
                        Props.create(AssistantExecutionManager.class),
                        "RequestToBatchSenderAsstManager-"
                                + UUID.randomUUID().toString());

                batchSenderAsstManager.tell(requestToBatchSenderAsstManager,
                        getSelf());

                final FiniteDuration timeOutDuration = Duration
                        .create(task.getConfig().getTimeoutInManagerSec(),
                                TimeUnit.SECONDS);
                timeoutMessageCancellable = getContext()
                        .system()
                        .scheduler()
                        .scheduleOnce(timeOutDuration, getSelf(),
                                ExecutionManagerMsgType.OPERATION_TIMEOUT,
                                getContext().system().dispatcher(), getSelf());

                logger.debug(
                        "Scheduled TIMEOUT_IN_MANAGER_SCONDS OPERATION_TIMEOUT after SEC {} ",
                        task.getConfig().getTimeoutInManagerSec());
            } else if (message instanceof ResponseOnSingleTask) {

                ResponseOnSingleTask taskResponse = (ResponseOnSingleTask) message;

                this.responseCount += 1;
                task.setResponsedNum(responseCount);

                /**
                 * add feedback of current responseCount to asst manager
                 * ResponseCountToBatchSenderAsstManager
                 */
                final ResponseCountToBatchSenderAsstManager responseCountToBatchSenderAsstManager = new ResponseCountToBatchSenderAsstManager(
                        this.responseCount);

                batchSenderAsstManager.tell(
                        responseCountToBatchSenderAsstManager, getSelf());

                logger.debug("Send batchSenderAsstManager to responseCountToBatchSenderAsstManager: "
                        + this.responseCount);

                String hostName = taskResponse.getRequest().getHost();
                if (responseMap.containsKey(hostName)) {
                    logger.error("ERROR: duplicate response received {}", hostName);
                }
                responseMap.put(hostName, taskResponse);

                String responseSummary = taskResponse.isError() ? "FAIL_GET_RESPONSE: "
                        + taskResponse.getErrorMessage()
                        : taskResponse.getStatusCode();
                Map<String, LinkedHashSet<String>> resultMap = task
                        .getAggregateResultMap();
                if (resultMap.containsKey(responseSummary)) {
                    resultMap.get(responseSummary).add(hostName);
                } else {
                    LinkedHashSet<String> set = new LinkedHashSet<String>();
                    set.add(hostName);
                    resultMap.put(responseSummary, set);
                }

                // save response to result map
                NodeReqResponse nrr = task.getParallelTaskResult()
                        .get(hostName);
                nrr.setSingleTaskResponse(taskResponse);

                String responseTrim = taskResponse.getResponseContent() == null ? null
                        : taskResponse.getResponseContent().trim();
                String displayResponse = (Strings.isNullOrEmpty(responseTrim)) ? "EMPTY"
                        : responseTrim
                                .substring(
                                        0,
                                        Math.min(
                                                PcConstants.AGNET_RESPONSE_MAX_RESPONSE_DISPLAY_BYTE,
                                                responseTrim.length()));

                long responseReceiveTime = System.currentTimeMillis();
                // %.5g%n
                double progressPercent = (double) (responseCount)
                        / (double) (requestCount) * 100.0;
                String responseReceiveTimeStr = PcDateUtils
                        .getDateTimeStrStandard(new Date(responseReceiveTime));
                String secondElapsedStr = Double
                        .toString((responseReceiveTime - startTime) / 1000.0);

                // log the first/ last 5 percent; then sample the middle
                if (requestCount < ParallecGlobalConfig.logAllResponseIfTotalLessThan
                        || responseCount <= ParallecGlobalConfig.logAllResponseBeforeInitCount
                        || progressPercent < ParallecGlobalConfig.logAllResponseBeforePercent
                        || progressPercent > ParallecGlobalConfig.logAllResponseAfterPercent
                        || responseCount
                                % ParallecGlobalConfig.logResponseInterval == 0) {
                    // percent is escaped using percent sign; hostName
                    logger.info(String
                            .format("\n[%d]__RESP_RECV_IN_MGR %d (+%d) / %d (%.5g%%)  "
                                    + "AFT %s S @ %s @ %s , TaskID : %s , CODE: %s, RESP_BRIEF: %s %s",
                                    responseCount,
                                    responseCount,
                                    requestCount - responseCount,
                                    requestCount,
                                    progressPercent,
                                    secondElapsedStr,
                                    hostName,
                                    responseReceiveTimeStr,
                                    taskIdTrim,
                                    taskResponse.getStatusCode(),
                                    displayResponse,
                                    taskResponse.getErrorMessage() == null ? ""
                                            : ", ERR: "
                                                    + taskResponse
                                                            .getErrorMessage()));
                }

                nrr.getRequestParameters().put(PcConstants.NODE_REQUEST_STATUS,
                        SingleTargetTaskStatus.COMPLETED.toString());

                if (task.getConfig().getHandlerExecutionLocation() == HandlerExecutionLocation.MANAGER
                        && task != null && task.getHandler() != null) {
                    try {
                        // logger.debug("HANDLE In manager: " +
                        // taskResponse.getHost());
                        task.getHandler().onCompleted(taskResponse,
                                task.getResponseContext());
                    } catch (Exception t) {
                        logger.error(
                                "Error handling onCompleted in manager for response: {} Error {}",
                                taskResponse.toString(),
                                t.getLocalizedMessage());
                    }
                }

                if (!task.getConfig().isSaveResponseToTask()) {
                    taskResponse.setResponseContent(PcConstants.NOT_SAVED);
                    taskResponse.setResponseHeaders(null);
                    logger.debug("Erased single task response content and response headers to save space.");
                }

                if (this.responseCount == this.requestCount) {
                    if (wasIssuedCancel) {
                        ExecutionManagerExecutionException exCanceled = new ExecutionManagerExecutionException(
                                "ExecutionManager: task was canceled by user",
                                ManagerExceptionType.CANCEL);
                        reply(ParallelTaskState.COMPLETED_WITH_ERROR,
                                exCanceled);
                    } else {
                        reply(ParallelTaskState.COMPLETED_WITHOUT_ERROR, null);
                    }

                }// end when all requests have resonponse

            } else if (message instanceof CancelTaskOnHostRequest) {
                CancelTaskOnHostRequest msg = (CancelTaskOnHostRequest) message;
                cancelRequestAndWorkerOnHost(msg.getTargetHosts());

            } else if (message instanceof ExecutionManagerMsgType) {
                switch ((ExecutionManagerMsgType) message) {

                // this will immediately return. not waiting for the op workers
                // reply
                case OPERATION_TIMEOUT:
                    cancelRequestAndWorkers();
                    String msg = "Execution manager timeout on whole Parallel Task.";
                    ExecutionManagerExecutionException ex = new ExecutionManagerExecutionException(
                            msg,
                            ManagerExceptionType.TIMEOUT);
                    logger.error(msg);
                    reply(ParallelTaskState.COMPLETED_WITH_ERROR, ex);
                    
                    break;
                // this will wait for the works to reply.
                case CANCEL:
                    cancelRequestAndWorkers();
                    wasIssuedCancel = true;
                    break;
                default:
                    break;
                }
            } else {
                logger.error("Unhandled: " + message);
                unhandled(message);
            }

        } catch (Exception t) {
            logger.error("Command Manager error: " + t + " trace: ", t);

            // not to terminate: but add error details. Will exit after timeout
            // or cancel
            task.getTaskErrorMetas().add(
                    new TaskErrorMeta(TaskErrorType.COMMAND_MANAGER_ERROR,
                            t == null ? "NA" : t.getLocalizedMessage()));
            reply(ParallelTaskState.COMPLETED_WITH_ERROR, t);

        }

    }// end func

    /**
     * reply will terminal the actor and return the results.
     *
     * @param state
     *            the state
     * @param t
     *            the exception
     */
    @SuppressWarnings("deprecation")
    private void reply(ParallelTaskState state, Exception t) {

        task.setState(state);

        logger.info("task.state : " + task.getState().toString());

        logger.info("task.totalJobNumActual : " + task.getRequestNumActual()
                + " InitCount: " + task.getRequestNum());
        logger.info("task.response received Num {} ", task.getResponsedNum());

        if (state == ParallelTaskState.COMPLETED_WITH_ERROR) {
            task.getTaskErrorMetas().add(
                    new TaskErrorMeta(TaskErrorType.COMMAND_MANAGER_ERROR,
                            t == null ? "NA" : t.getLocalizedMessage()));

            String curTimeStr = PcDateUtils.getNowDateTimeStrStandard();
            logger.info("COMPLETED_WITH_ERROR.  " + this.requestCount
                    + " at time: " + curTimeStr);
//TODO
            // #47
            if (t instanceof ExecutionManagerExecutionException
                    && ((ExecutionManagerExecutionException) t).getType() == ManagerExceptionType.TIMEOUT) {

                for (Entry<String, NodeReqResponse> entry : task
                        .getParallelTaskResult().entrySet()) {

                    // no response yet
                    if (entry.getValue() != null
                            && entry.getValue().getSingleTaskResponse() == null) {
                        ResponseOnSingleTask response = new ResponseOnSingleTask();
                        response.setReceiveTimeInManager(curTimeStr);
                        response.setError(true);
                        response.setErrorMessage(t.getLocalizedMessage()+" Response was not received");
                        response.setReceiveTime(curTimeStr);
                        entry.getValue().setSingleTaskResponse(response);
                        logger.info("Found empty response for {}",
                                entry.getKey());
                    }
                }
            }//end if

        } else {
            logger.info("SUCCESSFUL GOT ON ALL RESPONSES: Received all the expected messages. Count matches: "
                    + this.requestCount
                    + " at time: "
                    + PcDateUtils.getNowDateTimeStrStandard());

        }

        ResponseFromManager batchResponseFromManager = new ResponseFromManager(
                responseMap.size());

        responseMap.clear();
        director.tell(batchResponseFromManager, getSelf());

        // Send message to the future with the result
        endTime = System.currentTimeMillis();

        task.setExecutionEndTime(endTime);

        double durationSec = (endTime - startTime) / 1000.0;
        task.setDurationSec(durationSec);
        logger.info("\nTime taken to get all responses back : " + durationSec
                + " secs");
        task.setExecutionEndTime(endTime);
        for (ActorRef worker : workers.values()) {
            getContext().stop(worker);
        }
        workers.clear();

        if (batchSenderAsstManager != null
                && !batchSenderAsstManager.isTerminated()) {
            getContext().stop(batchSenderAsstManager);
        }

        if (timeoutMessageCancellable != null) {
            timeoutMessageCancellable.cancel();
        }

        if (getSelf() != null && !getSelf().isTerminated()) {
            getContext().stop(getSelf());
        }
    }

    /**
     * Cancel request and workers.
     */
    @SuppressWarnings("deprecation")
    private void cancelRequestAndWorkers() {

        for (ActorRef worker : workers.values()) {
            if (worker != null && !worker.isTerminated()) {
                worker.tell(OperationWorkerMsgType.CANCEL, getSelf());
            }
        }

        logger.info("ExecutionManager sending cancelPendingRequest at time: "
                + PcDateUtils.getNowDateTimeStr());
    }

    /**
     * Cancel request and worker on host.
     *
     * @param targetHosts
     *            the target hosts
     */
    @SuppressWarnings("deprecation")
    private void cancelRequestAndWorkerOnHost(List<String> targetHosts) {

        List<String> validTargetHosts = new ArrayList<String>(workers.keySet());
        validTargetHosts.retainAll(targetHosts);
        logger.info("targetHosts for cancel: Total: {}"
                + " Valid in current manager with worker threads: {}",
                targetHosts.size(), validTargetHosts.size());

        for (String targetHost : validTargetHosts) {

            ActorRef worker = workers.get(targetHost);

            if (worker != null && !worker.isTerminated()) {
                worker.tell(OperationWorkerMsgType.CANCEL, getSelf());
                logger.info("Submitted CANCEL request on Host {}", targetHost);
            } else {
                logger.info(
                        "Did NOT Submitted "
                                + "CANCEL request on Host {} as worker on this host is null or already killed",
                        targetHost);
            }

        }

    }

}
