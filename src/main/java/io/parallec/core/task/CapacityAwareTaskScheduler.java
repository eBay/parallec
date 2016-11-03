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
import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.config.ParallecGlobalConfig;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A task scheduler runnable to check if there are capacity to run a task 
 * from the waitQ.
 * 
 * Note that this scheduler is not enabled by default. 
 * 
 * Enable it by {@link ParallelTaskBuilder#setEnableCapacityAwareTaskScheduler}
 * 
 *  @author Yuanteng (Jeff) Pei
 */
public class CapacityAwareTaskScheduler implements Runnable {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(CapacityAwareTaskScheduler.class);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {

            Queue<ParallelTask> waitQ = ParallelTaskManager.getInstance()
                    .getWaitQ();

            logger.debug(
                    "TASK_WAIT_Q: Current waitQ has task count: {} in Thread scheduler",
                    waitQ.size());

            final ParallelTask task = waitQ.peek();
            if (task != null) {

                int totalUsedCapacityBefore = ParallelTaskManager.getInstance()
                        .getTotalUsedCapacity();

                int capacityThisTask = task.capacityUsed();
                int capacityRemain = ParallecGlobalConfig.maxCapacity
                        - totalUsedCapacityBefore;
                int totalUsedCapacityNew = totalUsedCapacityBefore
                        + capacityThisTask;

                logger.info(
                        "TASK_WAIT_Q: Exists Task in WaitQ with head of queue task id {}"
                                + "....Current used capacity {}, and remaining capacity is {}",
                        task.getTaskId(), totalUsedCapacityBefore,
                        capacityRemain);
                // condition to add to the in progress map
                if (totalUsedCapacityNew <= ParallecGlobalConfig.maxCapacity) {
                    logger.info(
                            "TASK_WAIT_Q: Sufficent capacity. Execute new task from wait queue. Task capacity {}"
                                    + " with total used capacity is now {}, capacityRemain will be {}",
                            capacityThisTask, totalUsedCapacityNew,
                            ParallecGlobalConfig.maxCapacity
                                    - totalUsedCapacityNew);

                    waitQ.poll(); // dequeue
                    Runnable taskRunnable = new Runnable() {
                        @Override
                        public void run() {
                            ParallelTaskManager.getInstance()
                                    .generateUpdateExecuteTask(task);
                        }
                    };
                    new Thread(taskRunnable).start();
                } else {
                    logger.info(
                            "TASK_WAIT_Q: Skip execution new task. Insufficent capacity. "
                                    + "Head of queue task needs capacity {}. However capacityRemain is only {}.",
                            capacityThisTask, capacityRemain);
                }

            }

        } catch (Exception e) {
            logger.error("TASK_WAIT_Q: fail in EvalTaskInWaitQRunner ", e);
        }
    }

}
