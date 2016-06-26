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

import io.parallec.core.ParallelTask;
import io.parallec.core.actor.ActorConfig;
import io.parallec.core.actor.ExecutionManager;
import io.parallec.core.actor.message.InitialRequestToManager;
import io.parallec.core.actor.message.ResponseFromManager;
import io.parallec.core.bean.StrStrMap;
import io.parallec.core.commander.workflow.InternalDataProvider;
import io.parallec.core.commander.workflow.VarReplacementProvider;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.task.TaskErrorMeta.TaskErrorType;
import io.parallec.core.util.DaemonThreadFactory;
import io.parallec.core.util.PcDateUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * 
 * The class to manage the current running tasks and wait queue.  (Singleton) 
 * {@link #generateUpdateExecuteTask } is the key function to execute a ParallelTask.
 * 
 * It has access to the waiting task queue and the currently running map of ParallelTasks.
 * 
 * @author Yuanteng (Jeff) Pei
 * @author Teng Song 
 * 
 */
public class ParallelTaskManager {

    /** The logger. */
    // init in constructor
    private static Logger logger;

    /** The Constant instance. */
    private final static ParallelTaskManager instance = new ParallelTaskManager();

    /** The scheduler. */
    private ScheduledExecutorService scheduler;

    /** The wait q. */
    private final Queue<ParallelTask> waitQ = new ConcurrentLinkedQueue<ParallelTask>();
   
    /** The inprogress task map. */
    // Key is JobID; this is the one before completes
    private final ConcurrentHashMap<String, ParallelTask> inprogressTaskMap = new ConcurrentHashMap<String, ParallelTask>();

    
    /**
     * Gets the single instance of ParallelTaskManager.
     *
     * @return single instance of ParallelTaskManager
     */
    public static ParallelTaskManager getInstance() {
        return instance;
    }
    /**
     * Instantiates a new parallel task manager.
     */
    private ParallelTaskManager() {
        super();

        // as a singleton this may be unnecessary; added just for sonar
        synchronized (this) {
            logger = LoggerFactory.getLogger(ParallelTaskManager.class);
        }
        logger.info("Initialized ParallelTaskManager...");

    }

    /**
     * as it is daemon thread
     * 
     * TODO when release external resources should shutdown the scheduler.
     */
    public synchronized void initTaskSchedulerIfNot() {

        if (scheduler == null) {
            scheduler = Executors
                    .newSingleThreadScheduledExecutor(DaemonThreadFactory
                            .getInstance());
            CapacityAwareTaskScheduler runner = new CapacityAwareTaskScheduler();
            scheduler.scheduleAtFixedRate(runner,
                    ParallecGlobalConfig.schedulerInitDelay,
                    ParallecGlobalConfig.schedulerCheckInterval,
                    TimeUnit.MILLISECONDS);
            logger.info("initialized daemon task scheduler to evaluate waitQ tasks.");
            
        }
    }
    
    /**
     * Shutdown task scheduler.
     */
    public synchronized void shutdownTaskScheduler(){
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("shutdowned the task scheduler. No longer accepting new tasks");
            scheduler = null;
        }
    }

    /**
     * Gets the task from in progress map.
     *
     * @param jobId
     *            the job id
     * @return the task from in progress map
     */
    public ParallelTask getTaskFromInProgressMap(String jobId) {
        if (!inprogressTaskMap.containsKey(jobId))
            return null;
        return inprogressTaskMap.get(jobId);
    }

    /**
     * get current total used capacity.
     *
     * @return the total used capacity
     */
    public int getTotalUsedCapacity() {
        int totalCapacity = 0;
        for (Entry<String, ParallelTask> entry : inprogressTaskMap.entrySet()) {
            ParallelTask task = entry.getValue();
            if (task != null)
                totalCapacity += task.capacityUsed();

        }
        return totalCapacity;
    }

    /**
     * Gets the remaining capacity.
     *
     * @return the remaining capacity
     */
    public int getRemainingCapacity() {

        return ParallecGlobalConfig.maxCapacity - getTotalUsedCapacity();
    }

    /**
     * when create new job, always add this to the queue.
     *
     * @param jobId
     *            the job id
     * @param task
     *            the task
     */
    public synchronized void addTaskToInProgressMap(String jobId,
            ParallelTask task) {
        inprogressTaskMap.put(jobId, task);
    }

    /**
     * Removes the task from in progress map.
     *
     * @param jobId
     *            the job id
     */
    public synchronized void removeTaskFromInProgressMap(String jobId) {
        inprogressTaskMap.remove(jobId);
    }

    /**
     * also clean the waitQ.
     */
    public synchronized void cleanInprogressJobMap() {
        inprogressTaskMap.clear();
    }

    /**
     * Clean wait task queue.
     */
    public synchronized void cleanWaitTaskQueue() {

        for (ParallelTask task : waitQ) {
            task.setState(ParallelTaskState.COMPLETED_WITH_ERROR);
            task.getTaskErrorMetas().add(
                    new TaskErrorMeta(TaskErrorType.USER_CANCELED, "NA"));
            logger.info(
                    "task {} removed from wait q. This task has been marked as USER CANCELED.",
                    task.getTaskId());

        }

        waitQ.clear();
    }

    /**
     * Removes the task from wait q.
     *
     * @param taskTobeRemoved
     *            the task tobe removed
     * @return true, if successful
     */
    public synchronized boolean removeTaskFromWaitQ(ParallelTask taskTobeRemoved) {
        boolean removed = false;
        for (ParallelTask task : waitQ) {
            if (task.getTaskId() == taskTobeRemoved.getTaskId()) {

                task.setState(ParallelTaskState.COMPLETED_WITH_ERROR);
                task.getTaskErrorMetas().add(
                        new TaskErrorMeta(TaskErrorType.USER_CANCELED, "NA"));
                logger.info(
                        "task {} removed from wait q. This task has been marked as USER CANCELED.",
                        task.getTaskId());
                removed = true;
            }
        }

        return removed;
    }

    /**
     * key function to execute a parallel task.
     *
     * @param task            the parallel task
     * @return the batch response from manager
     */
    public ResponseFromManager generateUpdateExecuteTask(ParallelTask task) {

        // add to map now; as can only pass final
        ParallelTaskManager.getInstance().addTaskToInProgressMap(
                task.getTaskId(), task);
        logger.info("Added task {} to the running inprogress map...",
                task.getTaskId());

        boolean useReplacementVarMap = false;
        boolean useReplacementVarMapNodeSpecific = false;
        Map<String, StrStrMap> replacementVarMapNodeSpecific = null;
        Map<String, String> replacementVarMap = null;

        ResponseFromManager batchResponseFromManager = null;

        switch (task.getRequestReplacementType()) {
        case UNIFORM_VAR_REPLACEMENT:
            useReplacementVarMap = true;
            useReplacementVarMapNodeSpecific = false;
            replacementVarMap = task.getReplacementVarMap();
            break;
        case TARGET_HOST_SPECIFIC_VAR_REPLACEMENT:
            useReplacementVarMap = false;
            useReplacementVarMapNodeSpecific = true;
            replacementVarMapNodeSpecific = task
                    .getReplacementVarMapNodeSpecific();
            break;
        case NO_REPLACEMENT:
            useReplacementVarMap = false;
            useReplacementVarMapNodeSpecific = false;
            break;
        default:
            logger.error("error request replacement type. default as no replacement");
        }// end switch

        // generate content in nodedata
        InternalDataProvider dp = InternalDataProvider.getInstance();
        dp.genNodeDataMap(task);

        VarReplacementProvider.getInstance()
                .updateRequestWithReplacement(task, useReplacementVarMap,
                        replacementVarMap, useReplacementVarMapNodeSpecific,
                        replacementVarMapNodeSpecific);

        batchResponseFromManager = 
                sendTaskToExecutionManager(task);

        removeTaskFromInProgressMap(task.getTaskId());
        logger.info(
                "Removed task {} from the running inprogress map... "
                        + ". This task should be garbage collected if there are no other pointers.",
                task.getTaskId());
        return batchResponseFromManager;

    }// end func.

    /**
     * Gets the wait q.
     *
     * @return the wait q
     */
    public Queue<ParallelTask> getWaitQ() {
        return waitQ;
    }
    
    /**
     * Gets the inprogress task map.
     *
     * @return the inprogress task map
     */
    public Map<String, ParallelTask> getInprogressTaskMap() {
        return inprogressTaskMap;
    }
    
    
    /**
     * Send parallel task to execution manager.
     *
     * @param task
     *            the parallel task
     * @return the batch response from manager
     */
    @SuppressWarnings("deprecation")
    public ResponseFromManager sendTaskToExecutionManager(ParallelTask task) {

        ResponseFromManager commandResponseFromManager = null;
        ActorRef executionManager = null;
        try {
            // Start new job
            logger.info("!!STARTED sendAgentCommandToManager : "
                    + task.getTaskId() + " at "
                    + PcDateUtils.getNowDateTimeStr());

            executionManager = ActorConfig.createAndGetActorSystem().actorOf(
                    Props.create(ExecutionManager.class, task),
                    "ExecutionManager-" + task.getTaskId());

            final FiniteDuration duration = Duration.create(task.getConfig()
                    .getTimeoutAskManagerSec(), TimeUnit.SECONDS);
            // Timeout timeout = new
            // Timeout(FiniteDuration.parse("300 seconds"));
            Future<Object> future = Patterns.ask(executionManager,
                    new InitialRequestToManager(task), new Timeout(duration));

            // set ref
            task.executionManager = executionManager;

            commandResponseFromManager = (ResponseFromManager) Await.result(
                    future, duration);

            logger.info("!!COMPLETED sendTaskToExecutionManager : "
                    + task.getTaskId() + " at "
                    + PcDateUtils.getNowDateTimeStr()
                    + "  \t\t  GenericResponseMap in future size: "
                    + commandResponseFromManager.getResponseCount());

        } catch (Exception ex) {
            logger.error("Exception in sendTaskToExecutionManager {} details {}: ",
                    ex, ex);

        } finally {
            // stop the manager
            if (executionManager != null && !executionManager.isTerminated()) {
                ActorConfig.createAndGetActorSystem().stop(executionManager);
            }

            if (task.getConfig().isAutoSaveLogToLocal()) {
                task.saveLogToLocal();
            }

        }

        return commandResponseFromManager;

    }// end func.
}
