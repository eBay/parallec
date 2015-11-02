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

import io.parallec.core.actor.message.ContinueToSendToBatchSenderAsstManager;
import io.parallec.core.actor.message.RequestToBatchSenderAsstManager;
import io.parallec.core.actor.message.ResponseCountToBatchSenderAsstManager;
import io.parallec.core.actor.message.type.OperationWorkerMsgType;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.util.PcDateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;


/**
 * 
 * The assistant manager is for sending out requests in batch;
 * 
 * @author Yuanteng (Jeff) Pei
 * 
 */
public class AssistantExecutionManager extends UntypedActor {

    /** The logger. */
    private static Logger logger = LoggerFactory
            .getLogger(AssistantExecutionManager.class);

    private long asstManagerRetryIntervalMillis;

    /** The response count. */
    protected int responseCount = 0;

    /** The request total count. */
    protected int requestTotalCount = 0;

    /** The start time. */
    protected long startTime = System.currentTimeMillis();

    /** The end time. */
    protected long endTime = -1L;

    /** The original manager. */
    protected ActorRef originalManager = null;

    /** The workers. */
    protected List<ActorRef> workers = new ArrayList<ActorRef>();

    /** The max concurrency adjusted. */
    protected int maxConcurrencyAdjusted = ParallecGlobalConfig.concurrencyDefault;

    /** The processed worker count. */
    protected int processedWorkerCount = 0;

    protected String taskId = null;

    protected String taskIdTrim = null;

    /**
     * Instantiates a new assistant execution manager.
     */
    public AssistantExecutionManager() {
    };

    /**
     * Note that if there is sleep in this method.
     *
     * @param stopCount
     *            the stop count
     */

    public void sendMessageUntilStopCount(int stopCount) {

        // always send with valid data.
        for (int i = processedWorkerCount; i < workers.size(); ++i) {
            ActorRef worker = workers.get(i);
            try {

                /**
                 * !!! This is a must; without this sleep; stuck occured at 5K.
                 * AKKA seems cannot handle too much too fast message send out.
                 */
                Thread.sleep(1L);

            } catch (InterruptedException e) {
                logger.error("sleep exception " + e + " details: ", e);
            }

            // send as if the sender is the origin manager; so reply back to
            // origin manager
            worker.tell(OperationWorkerMsgType.PROCESS_REQUEST, originalManager);

            processedWorkerCount++;

            if (processedWorkerCount > stopCount) {
                return;
            }

            logger.debug("REQ_SENT: {} / {} taskId {}", 
                 processedWorkerCount, requestTotalCount, taskIdTrim);

        }// end for loop
    }

    /**
     * Wait and retry.
     */
    public void waitAndRetry() {
        ContinueToSendToBatchSenderAsstManager continueToSendToBatchSenderAsstManager = new ContinueToSendToBatchSenderAsstManager(
                processedWorkerCount);

        logger.debug("NOW WAIT Another " + asstManagerRetryIntervalMillis
                + " MS. at " + PcDateUtils.getNowDateTimeStrStandard());
        getContext()
                .system()
                .scheduler()
                .scheduleOnce(
                        Duration.create(asstManagerRetryIntervalMillis,
                                TimeUnit.MILLISECONDS), getSelf(),
                        continueToSendToBatchSenderAsstManager,
                        getContext().system().dispatcher(), getSelf());
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    public void onReceive(Object message) {

        // Start all workers
        if (message instanceof RequestToBatchSenderAsstManager) {

            // clear responseMap

            RequestToBatchSenderAsstManager request = (RequestToBatchSenderAsstManager) message;
            originalManager = getSender();
            taskId = request.getTaskId();
            asstManagerRetryIntervalMillis = request
                    .getAsstManagerRetryIntervalMillis();
            // assumption: at least 12
            taskIdTrim = taskId.length() <= 12 ? taskId
                    : taskId.substring(taskId.length() - 12,
                            taskId.length());
            workers = request.getWorkers();
            maxConcurrencyAdjusted = request.getMaxConcurrency();

            requestTotalCount = workers.size();

            sendMessageUntilStopCount(maxConcurrencyAdjusted);

            // if not completed; will schedule a continue send msg
            if (processedWorkerCount < requestTotalCount) {

                waitAndRetry();
                return;
            } else {
                logger.info("Now finished sending all needed messages. Done job of ASST Manager at "
                        + PcDateUtils.getNowDateTimeStrStandard());
                return;
            }
        } else if (message instanceof ContinueToSendToBatchSenderAsstManager) {

            // now reaching the end; have processed all of them, just waiting
            // the response to come back
            int notProcessedNodeCount = requestTotalCount
                    - processedWorkerCount;
            if (notProcessedNodeCount <= 0) {
                logger.info("!Finished sending all msg in ASST MANAGER at "
                        + PcDateUtils.getNowDateTimeStrStandard()

                        + " STOP doing wait and retry.");
                return;
            }

            int extraSendCount = maxConcurrencyAdjusted
                    - (processedWorkerCount - responseCount);

            if (extraSendCount > 0) {
                logger.info("HAVE ROOM to send extra of : " + extraSendCount
                        + " MSG. now Send at "
                        + PcDateUtils.getNowDateTimeStrStandard());

                sendMessageUntilStopCount(processedWorkerCount + extraSendCount);
                waitAndRetry();
            } else {
                logger.info("NO ROOM to send extra. Windowns is full. extraSendCount is negative: "
                        + extraSendCount
                        + " reschedule now at "
                        + PcDateUtils.getNowDateTimeStrStandard());
                waitAndRetry();

            }
        } else if (message instanceof ResponseCountToBatchSenderAsstManager) {

            responseCount = ((ResponseCountToBatchSenderAsstManager) message)
                    .getResponseCount();

            logger.debug("RECV IN batchSenderAsstManager FROM ExecutionManager responseCount: "
                    + responseCount);

        }
    }

}
