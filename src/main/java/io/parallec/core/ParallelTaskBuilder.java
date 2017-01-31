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

import io.parallec.core.actor.poll.HttpPollerProcessor;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.ResponseHeaderMeta;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.bean.TargetHostMeta;
import io.parallec.core.bean.TaskRunMode;
import io.parallec.core.bean.ping.PingMeta;
import io.parallec.core.bean.ping.PingMode;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.bean.tcp.TcpMeta;
import io.parallec.core.bean.udp.UdpMeta;
import io.parallec.core.config.HandlerExecutionLocation;
import io.parallec.core.config.ParallelTaskConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.exception.TargetHostsLoadException;
import io.parallec.core.task.ParallelTaskManager;
import io.parallec.core.task.ParallelTaskState;
import io.parallec.core.task.RequestReplacementType;
import io.parallec.core.task.TaskErrorMeta;
import io.parallec.core.task.TaskErrorMeta.TaskErrorType;
import io.parallec.core.taskbuilder.targethosts.ITargetHostsBuilder;
import io.parallec.core.taskbuilder.targethosts.TargetHostsBuilder;
import io.parallec.core.util.PcConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.channel.ChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.ning.http.client.AsyncHttpClient;


// TODO: Auto-generated Javadoc
/**
 * 
 * This class stores all the metadata to build the ParallelTask. It is a Parallec key class.
 * 
 * Build the parallel task and then execute it after a validation. During the
 * validation, certain missing parameters will use the default values. execute()
 * is the key function
 *
 * @author Yuanteng (Jeff) Pei
 */
public class ParallelTaskBuilder {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(ParallelTaskBuilder.class);

    /** The metadata about the HTTP request. */
    private HttpMeta httpMeta = new HttpMeta();

    /** The ssh meta. must be initialized here to avoid NPE */
    private SshMeta sshMeta = new SshMeta();

    /** The TCP request meta data. */
    private TcpMeta tcpMeta = new TcpMeta();
    
    /** The UDP request meta data. */
    private UdpMeta udpMeta = new UdpMeta();
    
    /** The ping meta data. */
    private PingMeta pingMeta = new PingMeta();
    
    /** The target host meta data. */
    private TargetHostMeta targetHostMeta;

    /** The replacement var map node specific. */
    private final Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();

    /** The replacement var map. */
    private Map<String, String> replacementVarMap = new HashMap<String, String>();

    /** The request replacement type. */
    private RequestReplacementType requestReplacementType = RequestReplacementType.NO_REPLACEMENT;

    /** The target hosts. */
    private List<String> targetHosts = new ArrayList<String>();

    /** The targetHostBuilder. */
    private ITargetHostsBuilder targetHostBuilder = new TargetHostsBuilder();

    /** The response context. */
    private Map<String, Object> responseContext = new HashMap<String, Object>();

    /** The mode. */
    public TaskRunMode mode = TaskRunMode.SYNC;

    /** The config using default values. will never be null */
    private ParallelTaskConfig config = new ParallelTaskConfig();

    /** The request protocol. */
    private RequestProtocol requestProtocol = null;

    /** The concurrency. */
    private int concurrency = 0;


    /**
     * Instantiates a new parallel task builder.
     */
    public ParallelTaskBuilder() {
        super();
        logger.info("Initialized task builder with default config");
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
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setResponseContext(
            Map<String, Object> responseContext) {
        if (responseContext != null)
            this.responseContext = responseContext;
        else
            logger.error("context cannot be null. skip set.");
        return this;
    }

    /**
     * Call response handler in operation worker (in parallel before aggregation).
     * Handle the user defined onComplete() function in worker before aggregation (handle in parallel).
     * Be cautious on concurrency control if save the response to a common data store
     * Also when you definte the concurrency control, take into account of the time needed to hander the response.
     *
     * @return the parallel task builder
     */
    public ParallelTaskBuilder handleInWorker() {
        this.config.setHandlerExecutionLocation(HandlerExecutionLocation.OPERATION_WORKER);
        return this;
    }
    
    /**
     * Call response handler in manager (in a sequence after aggregation) Default mode. In this mode, will trigger the user defined response hander
     * after response is passed back from worker to manager. 
     * 
     * This is the default mode. Be cautious on using long blocking operation in the handler onComplete() function
     * 
     * Since it is handled in manager, a long sync operation may block the whole flow 
     * because each response will need to go through here. 
     * 
     * @return the parallel task builder
     */
    public ParallelTaskBuilder handleInManager() {
        this.config.setHandlerExecutionLocation(HandlerExecutionLocation.MANAGER);
        return this;
    }
    
    /**
     * Execute the task asynchronously.
     *
     * @return the parallel task builder
     */
    public ParallelTaskBuilder async() {
        this.mode = TaskRunMode.ASYNC;
        return this;
    }

    /**
     * Execute the task synchronously (the default run mode).
     *
     * @return the parallel task builder
     */
    public ParallelTaskBuilder sync() {
        this.mode = TaskRunMode.SYNC;
        return this;
    }

    /**
     * Sets the mode.
     *
     * @param mode
     *            the new mode
     */
    public void setMode(TaskRunMode mode) {
        this.mode = mode;
    }

    /**
     * key function. first validate if the ACM has adequate data; then execute
     * it after the validation. the new ParallelTask task guareetee to have the
     * targethost meta and command meta not null
     *
     * @param handler
     *            the handler
     * @return the parallel task
     */

    public ParallelTask execute(ParallecResponseHandler handler) {

        ParallelTask task = new ParallelTask();

        try {
            targetHostMeta = new TargetHostMeta(targetHosts);

            final ParallelTask taskReal = new ParallelTask(requestProtocol,
                    concurrency, httpMeta, targetHostMeta, sshMeta, tcpMeta, udpMeta, pingMeta,
                    handler, responseContext, 
                    replacementVarMapNodeSpecific, replacementVarMap,
                    requestReplacementType, 
                    config);

            task = taskReal;

            logger.info("***********START_PARALLEL_HTTP_TASK_"
                    + task.getTaskId() + "***********");

            // throws ParallelTaskInvalidException
            task.validateWithFillDefault();

            task.setSubmitTime(System.currentTimeMillis());

            if (task.getConfig().isEnableCapacityAwareTaskScheduler()) {

                //late initialize the task scheduler
                ParallelTaskManager.getInstance().initTaskSchedulerIfNot();
                // add task to the wait queue
                ParallelTaskManager.getInstance().getWaitQ().add(task);

                logger.info("Enabled CapacityAwareTaskScheduler. Submitted task to waitQ in builder.. "
                        + task.getTaskId());

            } else {

                logger.info(
                        "Disabled CapacityAwareTaskScheduler. Immediately execute task {} ",
                        task.getTaskId());

                Runnable director = new Runnable() {

                    public void run() {
                        ParallelTaskManager.getInstance()
                                .generateUpdateExecuteTask(taskReal);
                    }
                };
                new Thread(director).start();
            }

            if (this.getMode() == TaskRunMode.SYNC) {
                logger.info("Executing task {} in SYNC mode...  ",
                        task.getTaskId());

                while (task != null && !task.isCompleted()) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        logger.error("InterruptedException " + e);
                    }
                }
            }
        } catch (ParallelTaskInvalidException ex) {

            logger.info("Request is invalid with missing parts. Details: "
                    + ex.getMessage() + " Cannot execute at this time. "
                    + " Please review your request and try again.\nCommand:"
                    + httpMeta.toString());

            task.setState(ParallelTaskState.COMPLETED_WITH_ERROR);
            task.getTaskErrorMetas().add(
                    new TaskErrorMeta(TaskErrorType.VALIDATION_ERROR,
                            "validation eror"));

        } catch (Exception t) {
            logger.error("fail task builder. Unknown error: " + t, t);
            task.setState(ParallelTaskState.COMPLETED_WITH_ERROR);
            task.getTaskErrorMetas().add(
                    new TaskErrorMeta(TaskErrorType.UNKNOWN, "unknown eror",
                            t));
        }

        logger.info("***********FINISH_PARALLEL_HTTP_TASK_" + task.getTaskId()
                + "***********");
        return task;

    }// end func.

    /**
     * add some validation to see if this miss anything.
     *
     * @return true, if successful
     * @throws ParallelTaskInvalidException
     *             the parallel task invalid exception
     */

    public boolean validation() throws ParallelTaskInvalidException {

        ParallelTask task = new ParallelTask();
        targetHostMeta = new TargetHostMeta(targetHosts);

        task = new ParallelTask(requestProtocol, concurrency, httpMeta,
                targetHostMeta, sshMeta, tcpMeta, udpMeta, pingMeta, null, responseContext,
                 replacementVarMapNodeSpecific,
                replacementVarMap, requestReplacementType, config);
        boolean valid = false;

        try {
            valid = task.validateWithFillDefault();
        } catch (ParallelTaskInvalidException e) {
            logger.info("task is invalid " + e);
        }

        return valid;

    }


    /**
     * Sets the protocol.
     *
     * @param protocol
     *            the protocol
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setProtocol(RequestProtocol protocol) {
        this.requestProtocol = protocol;
        return this;
    }


    /**
     * Sets the max concurrency.
     *
     * @param concurrency the concurrency
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setConcurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;

    }

    
    /**
     * Sets the body.
     *
     * @param body
     *            the body
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setHttpEntityBody(String body) {
        this.httpMeta.setEntityBody(body);
        return this;
    }

    /**
     * this will create the adhoc header.
     *
     * @param pheader
     *            the pheader
     * @return the parallel task builder
     */

    public ParallelTaskBuilder setHttpHeaders(ParallecHeader pheader) {
        this.httpMeta.setHeaderMetadata(pheader);
        return this;

    }


    /**
     * Sets the port.
     *
     * @param port
     *            the port
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setHttpPort(int port) {
        this.httpMeta.setRequestPort(Integer.toString(port));
        return this;

    }

    
    /**
     * Sets the port variable name such as $PORT.
     *
     * @param portVar            the port
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setHttpPortReplaceable(String portVar) {
        this.httpMeta.setRequestPort(portVar);
        return this;

    }

 

    /**
     * Sets the async http client.
     *
     * @param asyncHttpClient
     *            the async http client
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setAsyncHttpClient(
            AsyncHttpClient asyncHttpClient) {
        this.httpMeta.setAsyncHttpClient(asyncHttpClient);
        return this;
    }

    /**
     * Gets the HttpMeta.
     *
     * @return the httpMeta
     */
    public HttpMeta getHttpMeta() {
        return httpMeta;
    }

    /**
     * Sets the HttpMeta.
     *
     * @param httpMeta
     *            the new httpMeta
     */
    public void setHttpMeta(HttpMeta httpMeta) {
        this.httpMeta = httpMeta;
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
     * Gets the target hosts.
     *
     * @return the target hosts
     */
    public List<String> getTargetHosts() {
        return targetHosts;
    }


    /**
     * Sets the target hosts from list.
     *
     * @param targetHosts
     *            the target hosts
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTargetHostsFromList(List<String> targetHosts) {

        this.targetHosts = targetHostBuilder.setTargetHostsFromList(targetHosts);
        return this;
    }



    /**
     * Sets the target hosts from string.
     *
     * @param targetHostsStr
     *            the target hosts str
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTargetHostsFromString(String targetHostsStr) {

        this.targetHosts = targetHostBuilder.setTargetHostsFromString(targetHostsStr);

        return this;
    }


    /**
     * Sets the target hosts from json path.
     *
     * @param jsonPath
     *            the json path
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the parallel task builder
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public ParallelTaskBuilder setTargetHostsFromJsonPath(String jsonPath,
            String sourcePath, HostsSourceType sourceType)
            throws TargetHostsLoadException {

        this.targetHosts = targetHostBuilder.setTargetHostsFromJsonPath(jsonPath, sourcePath,
                sourceType);
        return this;

    }


    /**
     * Sets the target hosts from line by line text.
     *
     * @param sourcePath
     *            the source path
     * @param sourceType
     *            the source type
     * @return the parallel task builder
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public ParallelTaskBuilder setTargetHostsFromLineByLineText(
            String sourcePath, HostsSourceType sourceType)
            throws TargetHostsLoadException {

        this.targetHosts = targetHostBuilder.setTargetHostsFromLineByLineText(sourcePath,
                sourceType);
        return this;
    }


    /**
     * Sets the target hosts from CMS query url. 
     * Will use label as projection, will not use authorization token
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @return the parallel task builder
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */
    public ParallelTaskBuilder setTargetHostsFromCmsQueryUrl(String cmsQueryUrl)
            throws TargetHostsLoadException {

        this.targetHosts = targetHostBuilder.setTargetHostsFromCmsQueryUrl(cmsQueryUrl);
        return this;
    }

    /**
     * Sets the target hosts from CMS query url. Will not use authorization token.
     * CMS: configuration-management-service. 
     * A.k.a. YiDB: http://www.yidb.org/
     * 
     * Parallec supports CMS query
     * 
     * http://ccoetech.ebay.com/cms-configuration-management-service-based-
     * mongodb
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @param projection
     *            the projection
     * @return the parallel task builder
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */

    public ParallelTaskBuilder setTargetHostsFromCmsQueryUrl(
            String cmsQueryUrl, String projection)
            throws TargetHostsLoadException {
        this.targetHosts = targetHostBuilder.setTargetHostsFromCmsQueryUrl(cmsQueryUrl,
                projection);
        return this;
    }
    
    /**
     * CMS: configuration-management-service. 
     * A.k.a. YiDB: http://www.yidb.org/
     * 
     * Parallec supports CMS query
     * 
     * http://ccoetech.ebay.com/cms-configuration-management-service-based-
     * mongodb
     *
     * @param cmsQueryUrl
     *            the cms query url
     * @param projection
     *            the projection
     * @param token
     *            the CMS authorization token if needed
     * @return the parallel task builder
     * @throws TargetHostsLoadException
     *             the target hosts load exception
     */

    public ParallelTaskBuilder setTargetHostsFromCmsQueryUrl(
            String cmsQueryUrl, String projection, String token)
            throws TargetHostsLoadException {
        this.targetHosts = targetHostBuilder.setTargetHostsFromCmsQueryUrl(cmsQueryUrl,
                projection, token);
        return this;
    }

    /**
     * Gets the TargetHostsBuilder.
     *
     * @return the thb
     */
    public ITargetHostsBuilder getTargetHostBuilder() {
        return targetHostBuilder;
    }

    /**
     * Sets the TargetHostsBuilder.
     *
     * @param thb
     *            the new thb
     */
    public void setTargetHostBuilder(ITargetHostsBuilder thb) {
        this.targetHostBuilder = thb;
    }

    /**
     * Gets the mode of either sync or async.
     *
     * @return the mode
     */
    public TaskRunMode getMode() {
        return mode;
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
     * @param targetHostMeta the new target host meta
     */
    public void setTargetHostMeta(
            TargetHostMeta targetHostMeta) {
        this.targetHostMeta = targetHostMeta;
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
     * Sets the replacement var map node specific.
     *
     * @param replacementVarMapNodeSpecific
     *            the replacement var map node specific
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setReplacementVarMapNodeSpecific(
            Map<String, StrStrMap> replacementVarMapNodeSpecific) {
        this.replacementVarMapNodeSpecific.clear();
        this.replacementVarMapNodeSpecific
                .putAll(replacementVarMapNodeSpecific);

        this.requestReplacementType = RequestReplacementType.TARGET_HOST_SPECIFIC_VAR_REPLACEMENT;
        logger.info("Set requestReplacementType as {}"
                + requestReplacementType.toString());
        return this;
    }


    /**
     * Sets the replace var map to single target from map.
     *
     * @param replacementVarMapNodeSpecific
     *            the replacement var map node specific
     * @param uniformTargetHost
     *            the uniform target host
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setReplaceVarMapToSingleTargetFromMap(
            Map<String, StrStrMap> replacementVarMapNodeSpecific,
            String uniformTargetHost) {
        setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific);

        if (Strings.isNullOrEmpty(uniformTargetHost)) {
            logger.error("uniform target host is empty or null. skip setting.");
            return this;
        }
        for (Entry<String, StrStrMap> entry : replacementVarMapNodeSpecific
                .entrySet()) {

            if (entry.getValue() != null) {
                entry.getValue().addPair(PcConstants.UNIFORM_TARGET_HOST_VAR,
                        uniformTargetHost);
            }
        }
        return this;
    }

    /**
     * Sets the replace var map to single target single var.
     *
     * @param variable
     *            the variable
     * @param replaceList
     *            : the list of strings that will replace the variable
     * @param uniformTargetHost
     *            the uniform target host
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setReplaceVarMapToSingleTargetSingleVar(
            String variable, List<String> replaceList, String uniformTargetHost) {

        if (Strings.isNullOrEmpty(uniformTargetHost)) {
            logger.error("uniform target host is empty or null. skil setting.");
            return this;
        }
        this.replacementVarMapNodeSpecific.clear();
        this.targetHosts.clear();
        int i = 0;
        for (String replace : replaceList) {
            if (replace == null){
                logger.error("null replacement.. skip");
                continue;
            }
            String hostName = PcConstants.API_PREFIX + i;

            replacementVarMapNodeSpecific.put(
                    hostName,
                    new StrStrMap().addPair(variable, replace).addPair(
                            PcConstants.UNIFORM_TARGET_HOST_VAR,
                            uniformTargetHost));
            targetHosts.add(hostName);
            ++i;
        }
        this.requestReplacementType = RequestReplacementType.TARGET_HOST_SPECIFIC_VAR_REPLACEMENT;
        logger.info(
                "Set requestReplacementType as {} for single target. Will disable the set target hosts."
                        + "Also Simulated "
                        + "Now Already set targetHost list with size {}. \nPLEASE NOT TO SET TARGET HOSTS AGAIN WITH THIS API.",
                requestReplacementType.toString(), targetHosts.size());

        return this;
    }

    /**
     * Sets the replace var map to single target.
     *
     * @param replacementVarMapList
     *            the replacement var map list
     * @param uniformTargetHost
     *            the uniform target host
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setReplaceVarMapToSingleTarget(
            List<StrStrMap> replacementVarMapList, String uniformTargetHost) {

        if (Strings.isNullOrEmpty(uniformTargetHost)) {
            logger.error("uniform target host is empty or null. skil setting.");
            return this;
        }
        this.replacementVarMapNodeSpecific.clear();
        this.targetHosts.clear();
        int i = 0;
        for (StrStrMap ssm : replacementVarMapList) {
            if (ssm == null)
                continue;
            String hostName = PcConstants.API_PREFIX + i;
            ssm.addPair(PcConstants.UNIFORM_TARGET_HOST_VAR, uniformTargetHost);
            replacementVarMapNodeSpecific.put(hostName, ssm);
            targetHosts.add(hostName);
            ++i;
        }
        this.requestReplacementType = RequestReplacementType.TARGET_HOST_SPECIFIC_VAR_REPLACEMENT;
        logger.info(
                "Set requestReplacementType as {} for single target. Will disable the set target hosts."
                        + "Also Simulated "
                        + "Now Already set targetHost list with size {}. \nPLEASE NOT TO SET TARGET HOSTS AGAIN WITH THIS API.",
                requestReplacementType.toString(), targetHosts.size());

        return this;
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
     * Sets the replacement var map.
     *
     * @param replacementVarMap
     *            the replacement var map
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setReplacementVarMap(
            Map<String, String> replacementVarMap) {
        this.replacementVarMap = replacementVarMap;

        // TODO Check and warning of overwriting
        // set as uniform
        this.requestReplacementType = RequestReplacementType.UNIFORM_VAR_REPLACEMENT;
        return this;
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
     * @param requestReplacementType            the new request replacement type
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setRequestReplacementType(
            RequestReplacementType requestReplacementType) {
        this.requestReplacementType = requestReplacementType;
        return this;
    }

    /**
     * Sets the HTTP pollable.
     * if the task is for an async API that needs to poll progress.
     * 
     * @param isPollable
     *            the is pollable
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setHttpPollable(boolean isPollable) {
        this.httpMeta.setPollable(isPollable);

        return this;
    }
    
    

    /**
     * Sets the HTTP response header meta data.
     * Can define a list of keys would like to retrieve from the response headers. 
     * Or when getAll is true: will get all the key value pair, regardless of the keys list provided.
     * 
     * @param responseHeaderMeta the response header meta
     * @return the parallel task builder
     */
    public ParallelTaskBuilder saveResponseHeaders(ResponseHeaderMeta responseHeaderMeta) {
        this.httpMeta.setResponseHeaderMeta(responseHeaderMeta);
        return this;
    }

    
    

    /**
     * Sets the HTTP poller processor to handle Async API.
     * Will auto enable the pollable mode with this call
     *
     * @param httpPollerProcessor
     *            the http poller processor
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setHttpPollerProcessor(
            HttpPollerProcessor httpPollerProcessor) {
        this.httpMeta.setHttpPollerProcessor(httpPollerProcessor);
        this.httpMeta.setPollable(true);
        return this;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "PTaskBuilder [acm=" + httpMeta + ", nodeGroupSourceMetadata="
                + targetHostMeta + ", replacementVarMapNodeSpecific="
                + replacementVarMapNodeSpecific + ", replacementVarMap="
                + replacementVarMap + ", requestReplacementType="
                + requestReplacementType + ", targetHosts=" + targetHosts
                + ", thb=" + targetHostBuilder 
                + ", responseContext=" + responseContext
                + ", mode=" + mode + "]";
    }

    /**
     * Sets the ssh command line.
     * @param commandLine
     *            the command line
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshCommandLine(String commandLine) {
        this.sshMeta.setCommandLine(commandLine);
        return this;
    }
    
    /**
     * Sets the ssh runAsSuperUser.
     *
     * @param runAsSuperUser            the runAsSuperUser
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setRunAsSuperUser(Boolean runAsSuperUser) {
        this.sshMeta.setRunAsSuperUser(runAsSuperUser);
        return this;
    }

    /**
     * Sets the ssh port.
     *
     * @param sshPort
     *            the ssh port
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshPort(int sshPort) {
        this.sshMeta.setSshPort(sshPort);
        return this;
    }

    /**
     * Sets the ssh user name.
     *
     * @param userName
     *            the user name
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshUserName(String userName) {
        this.sshMeta.setUserName(userName);
        return this;
    }


    /**
     * Sets the ssh password.
     *
     * @param password
     *            the password
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshPassword(String password) {
        this.sshMeta.setPassword(password);
        this.sshMeta.setSshLoginType(SshLoginType.PASSWORD);
        return this;
    }

    /**
     * Sets the ssh login type.
     *
     * @param sshLoginType
     *            the ssh login type
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshLoginType(SshLoginType sshLoginType) {
        this.sshMeta.setSshLoginType(sshLoginType);
        return this;
    }


    /**
     * Sets the ssh priv key relative path. 
     * Note that must be relative path for now.
     * This default to no need of passphrase for the private key.
     * Will also auto set the login type to key based.
     * 
     * @param privKeyRelativePath
     *            the priv key relative path
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshPrivKeyRelativePath(
            String privKeyRelativePath) {
        this.sshMeta.setPrivKeyRelativePath(privKeyRelativePath);
        this.sshMeta.setSshLoginType(SshLoginType.KEY);
        return this;
    }

 
    /**
     * Sets the ssh priv key relative path wtih passphrase.
     *
     * @param privKeyRelativePath the priv key relative path
     * @param passphrase the passphrase
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshPrivKeyRelativePathWtihPassphrase(
            String privKeyRelativePath, String passphrase) {
        this.sshMeta.setPrivKeyRelativePath(privKeyRelativePath);
        this.sshMeta.setPrivKeyUsePassphrase(true);
        this.sshMeta.setPassphrase(passphrase);
        this.sshMeta.setSshLoginType(SshLoginType.KEY);
        return this;
    }


    /**
     * Sets the ssh connection timeout millis.
     *
     * @param sshConnectionTimeoutMillis
     *            the ssh connection timeout millis
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSshConnectionTimeoutMillis(
            int sshConnectionTimeoutMillis) {
        this.sshMeta.setSshConnectionTimeoutMillis(sshConnectionTimeoutMillis);
        return this;
    }

    /**
     * Sets the tcp port.
     *
     * @param tcpPort
     *            the tcp port
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTcpPort(int tcpPort) {
        this.tcpMeta.setTcpPort(tcpPort);
        return this;
    }

    /**
     * Sets the tcp connect timeout millis.
     *
     * @param tcpConnectTimeoutMillis
     *            the tcp connect timeout millis
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTcpConnectTimeoutMillis(
            int tcpConnectTimeoutMillis) {
        this.tcpMeta.setTcpConnectTimeoutMillis(tcpConnectTimeoutMillis);
        return this;
    }
    
    /**
     * Sets the tcp idle timeout sec.
     *
     * @param tcpIdleTimeoutSec the tcp idle timeout sec
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTcpIdleTimeoutSec(
            int tcpIdleTimeoutSec) {
        this.tcpMeta.setTcpIdleTimeoutSec(tcpIdleTimeoutSec);
        return this;
    }

    /**
     * Sets the tcp channel factory.
     *
     * @param channelFactory
     *            the channel factory
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setTcpChannelFactory(
            ChannelFactory channelFactory) {
        this.tcpMeta.setChannelFactory(channelFactory);
        return this;
    }
    
    
    /**
     * Sets the udp port.
     *
     * @param udpPort
     *            the udp port
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setUdpPort(int udpPort) {
        this.udpMeta.setUdpPort(udpPort);
        return this;
    }

    /**
     * Sets the udp read(idle) timeout millis.
     *
     * @param udpIdleTimeoutSec
     *            the udp read timeout millis
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setUdpIdleTimeoutSec(
            int udpIdleTimeoutSec) {
        this.udpMeta.setUdpIdleTimeoutSec(udpIdleTimeoutSec);
        return this;
    }

    /**
     * Sets the config.
     *
     * @param config
     *            the config
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setConfig(ParallelTaskConfig config) {
        this.config = config;
        return this;
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
     * Sets the save response to task.
     *
     * @param saveResponseToTask
     *            the save response to task
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setSaveResponseToTask(boolean saveResponseToTask) {
        this.config.setSaveResponseToTask(saveResponseToTask);
        return this;
    }

    /**
     * Sets the enable capacity aware task scheduler.
     * 
     * OPTIONAL. DEFAULT: false. 
     * @param enableCapacityAwareTaskScheduler
     *            the enable capacity aware task scheduler
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setEnableCapacityAwareTaskScheduler(
            boolean enableCapacityAwareTaskScheduler) {
        this.config
                .setEnableCapacityAwareTaskScheduler(enableCapacityAwareTaskScheduler);
        return this;
    }

    /**
     * Sets the auto save log to local.
     * Will auto save logs to the local file system. 
     * 
     * The logs by default are written to path "userdata/task/logs" folder.
     * 
     * Note that it is user's responsibility to clearn these logs.
     * 
     * OPTIONAL. DEFAULT: false. 
     * @param autoSaveLogToLocal
     *            the auto save log to local
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setAutoSaveLogToLocal(boolean autoSaveLogToLocal) {
        this.config.setAutoSaveLogToLocal(autoSaveLogToLocal);
        return this;
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
     * Gets the concurrency.
     *
     * @return the concurrency
     */
    public int getConcurrency() {
        return concurrency;
    }

    
    /**
     * Sets the ping mode. Process or INET_ADDRESS_REACHABLE based.
     * Default as InetAddress mode. InetAddress requires Root privilege. 
     *
     * @param mode the mode
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setPingMode(PingMode mode) {
        this.pingMeta.setMode(mode);
        return this;
    }

    /**
     * Sets the ping timeout millis.
     *
     * @param pingTimeoutMillis the ping timeout millis
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setPingTimeoutMillis(int pingTimeoutMillis) {
        this.pingMeta.setPingTimeoutMillis(pingTimeoutMillis);
        return this;
    }

    /**
     * Sets the ping num retries.
     *
     * @param numRetries the num retries
     * @return the parallel task builder
     */
    public ParallelTaskBuilder setPingNumRetries(int numRetries) {
        this.pingMeta.setNumRetries(numRetries);
        return this;
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
     * Sets the udp meta.
     *
     * @param udpMeta the new udp meta
     */
    public void setUdpMeta(UdpMeta udpMeta) {
        this.udpMeta = udpMeta;
    }
    

    
}
