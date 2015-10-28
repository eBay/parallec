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


/**
 * The default values for {@link ParallelTaskConfig}  
 *
 */
public class ParallelTaskConfigDefault {

    /** The asst manager retry interval millis. */
    public static long asstManagerRetryIntervalMillis = 250L;

    /** The actor max operation timeout sec. */
    public static int actorMaxOperationTimeoutSec = 15;

    /**
     * The command manager internal timeout and cancel itself time in seconds
     * Note this may need to be adjusted for long polling jobs.
     */
    public static long timeoutInManagerSec = 600;

    /** The timeout the director send to the manager to cancel it from outside. */
    public static long timeoutAskManagerSec = timeoutInManagerSec + 10;

    /** The print http true header map. */
    public static boolean printHttpTrueHeaderMap = true;

    /** The print poller. */
    public static boolean printPoller = true;

    /** The save response to task. */
    public static boolean saveResponseToTask = false;

    /** The auto save log to local. */
    public static boolean autoSaveLogToLocal = false;

    /** The enable capacity aware task scheduler. */
    public static boolean enableCapacityAwareTaskScheduler = false;

    /** The handler execution location default as in Manager after aggregation. */
    public static  HandlerExecutionLocation handlerExecutionLocationDefault = HandlerExecutionLocation.MANAGER;
    /**
     * Instantiates a new parallel task config.
     */
    public ParallelTaskConfigDefault() {
        super();
    }

}
