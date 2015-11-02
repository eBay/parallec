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
package io.parallec.core.config;

import io.parallec.core.ParallelTask;
import io.parallec.core.ParallelTaskBuilder;

/**
 * Configuration class to use with a {@link ParallelTask}. 
 * 
 * This can be overwritten for each task. 
 * 
 * Configs about various timeout, whether to auto save logs / save responses.
 * whether to enable the capacity scheduler.
 * 
 * Some of the most useful ones we directly have APIs by the {@link ParallelTaskBuilder} 
 * 
 * will load from the default value from {@link ParallelTaskConfigDefault}
 *
 */
public class ParallelTaskConfig {

    public ParallelTaskConfig() {
        super();
        this.asstManagerRetryIntervalMillis = ParallelTaskConfigDefault.asstManagerRetryIntervalMillis;
        this.actorMaxOperationTimeoutSec = ParallelTaskConfigDefault.actorMaxOperationTimeoutSec;
        this.timeoutInManagerSec = ParallelTaskConfigDefault.timeoutInManagerSec;
        this.timeoutAskManagerSec = ParallelTaskConfigDefault.timeoutAskManagerSec;
        this.printHttpTrueHeaderMap = ParallelTaskConfigDefault.printHttpTrueHeaderMap;
        this.printPoller = ParallelTaskConfigDefault.printPoller;
        this.saveResponseToTask = ParallelTaskConfigDefault.saveResponseToTask;
        this.enableCapacityAwareTaskScheduler = ParallelTaskConfigDefault.enableCapacityAwareTaskScheduler;
        this.autoSaveLogToLocal = ParallelTaskConfigDefault.autoSaveLogToLocal;
        this.handlerExecutionLocation = ParallelTaskConfigDefault.handlerExecutionLocationDefault;
    }

    /** The asst manager retry interval millis. */
    private long asstManagerRetryIntervalMillis;

    /** The actor max operation timeout sec. */
    private int actorMaxOperationTimeoutSec;

    /**
     * The command manager internal timeout and cancel itself time in seconds
     * Note this may need to be adjusted for long polling jobs.
     */
    private long timeoutInManagerSec;

    /** The timeout the director send to the manager to cancel it from outside. */
    private long timeoutAskManagerSec;

    /** The print http true header map. */
    private boolean printHttpTrueHeaderMap;

    private boolean printPoller;

    private boolean saveResponseToTask;

    /**
     * The enable capacity protection. When enabled, a parallec task may not be
     * immediately executed. Instead, it is submitted to a wait q. Every 0.5
     * second, a deamon thread (SingleThreadScheduledExecutor) is used to check
     * if there is capacity to put the new node in.
     * 
     * a parallel task capacity is calculated as Min(task.maxConcurrency,
     * requestNum)
     * 
     * <p>
     * details check ParallelTaskManager initTaskScheduler
     * </p>
     * */
    private boolean enableCapacityAwareTaskScheduler;

    private boolean autoSaveLogToLocal;
    
    private HandlerExecutionLocation handlerExecutionLocation;
    
    

    public long getAsstManagerRetryIntervalMillis() {
        return asstManagerRetryIntervalMillis;
    }

    public void setAsstManagerRetryIntervalMillis(
            long asstManagerRetryIntervalMillis) {
        this.asstManagerRetryIntervalMillis = asstManagerRetryIntervalMillis;
    }

    public int getActorMaxOperationTimeoutSec() {
        return actorMaxOperationTimeoutSec;
    }

    public void setActorMaxOperationTimeoutSec(int actorMaxOperationTimeoutSec) {
        this.actorMaxOperationTimeoutSec = actorMaxOperationTimeoutSec;
    }

    public long getTimeoutInManagerSec() {
        return timeoutInManagerSec;
    }

    public void setTimeoutInManagerSec(long timeoutInManagerSec) {
        this.timeoutInManagerSec = timeoutInManagerSec;
    }

    public long getTimeoutAskManagerSec() {
        return timeoutAskManagerSec;
    }

    public void setTimeoutAskManagerSec(long timeoutAskManagerSec) {
        this.timeoutAskManagerSec = timeoutAskManagerSec;
    }

    public boolean isPrintHttpTrueHeaderMap() {
        return printHttpTrueHeaderMap;
    }

    public void setPrintHttpTrueHeaderMap(boolean printHttpTrueHeaderMap) {
        this.printHttpTrueHeaderMap = printHttpTrueHeaderMap;
    }

    public boolean isPrintPoller() {
        return printPoller;
    }

    public void setPrintPoller(boolean printPoller) {
        this.printPoller = printPoller;
    }

    public boolean isSaveResponseToTask() {
        return saveResponseToTask;
    }

    public void setSaveResponseToTask(boolean saveResponseToTask) {
        this.saveResponseToTask = saveResponseToTask;
    }

    public boolean isEnableCapacityAwareTaskScheduler() {
        return enableCapacityAwareTaskScheduler;
    }

    public void setEnableCapacityAwareTaskScheduler(
            boolean enableCapacityAwareTaskScheduler) {
        this.enableCapacityAwareTaskScheduler = enableCapacityAwareTaskScheduler;
    }

    public boolean isAutoSaveLogToLocal() {
        return autoSaveLogToLocal;
    }

    public void setAutoSaveLogToLocal(boolean autoSaveLogToLocal) {
        this.autoSaveLogToLocal = autoSaveLogToLocal;
    }

    public HandlerExecutionLocation getHandlerExecutionLocation() {
        return handlerExecutionLocation;
    }

    public void setHandlerExecutionLocation(HandlerExecutionLocation handlerExecutionLocation) {
        this.handlerExecutionLocation = handlerExecutionLocation;
    }

    @Override
    public String toString() {
        return "ParallelTaskConfig [asstManagerRetryIntervalMillis="
                + asstManagerRetryIntervalMillis
                + ", actorMaxOperationTimeoutSec="
                + actorMaxOperationTimeoutSec + ", timeoutInManagerSec="
                + timeoutInManagerSec + ", timeoutAskManagerSec="
                + timeoutAskManagerSec + ", printHttpTrueHeaderMap="
                + printHttpTrueHeaderMap + ", printPoller=" + printPoller
                + ", saveResponseToTask=" + saveResponseToTask
                + ", enableCapacityAwareTaskScheduler="
                + enableCapacityAwareTaskScheduler + ", autoSaveLogToLocal="
                + autoSaveLogToLocal + ", handlerExecutionLocation="
                + handlerExecutionLocation + "]";
    }

    
}
