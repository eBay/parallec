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
import io.parallec.core.bean.ping.PingMode;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.util.PcStringUtils;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The most basic test with hitting the same URL at 3 different websites.
 * require Internet access for testing.
 * 
 * <p>
 * This example shows 1. Basic request construction 2. how to use response
 * context to pass value during the response handler out to a global space
 * </p>
 */
public class PingTest extends TestBase {

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
     * Ping websites min async. default mode need ROOT/Admin 
     */
    @Test
    public void pingWebsitesMinBasicSync() {

        ParallelTask task = pc.preparePing().setConcurrency(100)
                 .setTargetHostsFromString(
                 "www.parallec.io www.jeffpei.com www.restcommander.com bad.c21tom")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

        logger.info("Task Pretty Print: \n{}",
                PcStringUtils.renderJson(task.getAggregateResultFullSummary()));
        logger.info("Total Duration: " + task.getDurationSec());
    }// end func
    
    
    @Ignore
    public void pingWebsitesMoreOptionsForever() {
        for(int i=1; i<100; i++){
            pingWebsitesMoreOptionsHelper();
        }
    }
    
    public void pingWebsitesMoreOptionsHelper() {

        ParallelTask task = pc.preparePing().setConcurrency(100)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL)
                 .setPingMode(PingMode.PROCESS)
                 .setPingNumRetries(1)
                 .setPingTimeoutMillis(2000)
                 
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

        logger.info("Task Pretty Print: \n{}",
                PcStringUtils.renderJson(task.getAggregateResultFullSummary()));
        logger.info("Total Duration: " + task.getDurationSec());
    }// end func
    
    @Test
    public void pingWebsitesMoreOptions() {

        ParallelTask task = pc.preparePing().setConcurrency(100)
                 .setTargetHostsFromString(
                 "www.parallec.io www.jeffpei.com www.restcommander.com 192.168.99.11")
                 .setPingMode(PingMode.PROCESS)
                 .setPingNumRetries(2)
                 .setPingTimeoutMillis(2000)
                 
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

        logger.info("Task Pretty Print: \n{}",
                PcStringUtils.renderJson(task.getAggregateResultFullSummary()));
        logger.info("Total Duration: " + task.getDurationSec());
    }// end func
    
    
    @Test
    public void testInvalidPoller() {
        try {
            pc.preparePing().setConcurrency(100)
            .setHttpPollable(true)
            .setTargetHostsFromString(
            "www.parallec.io www.jeffpei.com www.restcommander.com bad.c21tom")
           .execute(new ParallecResponseHandler() {
               @Override
               public void onCompleted(ResponseOnSingleTask res,
                       Map<String, Object> responseContext) {
                   
               }
           });
        } catch (ParallelTaskInvalidException e) {

            logger.info("EXPECTED Exception {}", e.getLocalizedMessage());
        }
    }
 
}
