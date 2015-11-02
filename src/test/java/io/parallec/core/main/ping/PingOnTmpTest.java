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
package io.parallec.core.main.ping;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.monitor.MonitorProvider;
import io.parallec.core.util.PcStringUtils;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * The most basic test with hitting the same URL at 3 different websites.
 * require Internet access for testing.
 * 
 * <p>
 * This example shows 1. Basic request construction 2. how to use response
 * context to pass value during the response handler out to a global space
 * </p>
 */
public class PingOnTmpTest extends TestBase {

    /** The pc. */
    private static ParallelClient pc;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    /**
     * Shutdown.
     *
     * @throws Exception
     *             the exception
     */
    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }
    
    /**
     * Ping websites min async.
     */
    @Ignore
    // @Test
    public void pingWebsitesMinBasicSyncOn1500() {

        ParallelTask task = pc.preparePing().setConcurrency(1500)
                .setPingNumRetries(0)
                .setTargetHostsFromLineByLineText(FILEPATH_TMP1, SOURCE_LOCAL)
                // .setTargetHostsFromString(
                // "www.parallec.io www.jeffpei.com www.restcommander.com bad.c21tom")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

        logger.info("Task Pretty Print: \n{}",
                PcStringUtils.renderJson(task.getAggregateResultCountSummary()));
        logger.info("Total Duration: " + task.getDurationSec());
    }// end func

    
    /**
     * Ping websites min async.
     */
    @Ignore
    // @Test
    public void pingWebsitesMinAsync() {

        ParallelTask task = pc.preparePing().async().setConcurrency(1500)
                .setTargetHostsFromLineByLineText(FILEPATH_TMP1, SOURCE_LOCAL)
                // .setTargetHostsFromString(
                // "www.parallec.io www.jeffpei.com www.restcommander.com bad.c21tom")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

        while (!task.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println("LIVE_THREAD_COUNT: "
                        + MonitorProvider.getInstance().getLiveThreadCount());
                // System.err.println(String.format("POLL_JOB_PROGRESS (%.5g%%)",
                // task.getProgress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Task Pretty Print: \n{}",
                PcStringUtils.renderJson(task.getAggregateResultCountSummary()));

    }// end func
}
