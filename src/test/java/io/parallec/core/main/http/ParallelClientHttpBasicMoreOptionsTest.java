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
package io.parallec.core.main.http;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.RequestProtocol;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.config.ParallecGlobalConfig;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class ParallelClientHttpBasicMoreOptionsTest extends TestBase {

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
     * basic but auto save response to log .setAutoSaveLogToLocal(true)
     * .setEnableCapacityAwareTaskScheduler(true) .setSaveResponseToTask(true)
     */
    @Test
    public void hitWebsitesMinSync() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        ParallelTask task = pc.prepareHttpGet("/validateInternals.html").setConcurrency(1700)
                .setResponseContext(responseContext)
                .setTargetHostsFromString("www.parallec.io www.jeffpei.com www.restcommander.com")

                .setSaveResponseToTask(true).setAutoSaveLogToLocal(true).setEnableCapacityAwareTaskScheduler(true)
                // .setSaveResponseToTask(true)

                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res, Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());

                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu);
                        metricMap.put("MemoryUsage", memory);

                        logger.info("cpu:" + cpu + " memory: " + memory + " host: " + res.getHost());
                        responseContext.put(res.getHost(), cpu);

                        logger.info(res.toString());

                    }
                });

        for (Object o : responseContext.values()) {

            Double cpuDouble = Double.parseDouble((String) o);
            Asserts.check(cpuDouble <= 100.0 && cpuDouble >= 0.0, " Fail to extract cpu values");
        }

        logger.info("Task Pretty Print: \n{}", task.prettyPrintInfo());
        logger.info("Aggregated results: \n{}", task.getAggregatedResultHumanStr());
    }

    /**
     * Test hitting none unicode websites assuming www.rakuten.co.jp still uses
     * the following none unicode encoding. If www.rakuten.co.jp changes the
     * encoding to utf-8, this test may still pass but not serve the purpose.
     * Content-Type: text/html; charset=EUC-JP
     */
    @Test
    public void hitNoneUnicodeWebsite() {
        ParallecGlobalConfig.httpResponseBodyCharsetUsesResponseContentType = true;
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareHttpGet("/")
        .setResponseContext(responseContext)
        .setProtocol(RequestProtocol.HTTPS)
        .setHttpPort(443)
        .setTargetHostsFromString("www.rakuten.co.jp")
        .setSaveResponseToTask(true)
        .setAutoSaveLogToLocal(true)
        .setEnableCapacityAwareTaskScheduler(true)
        .execute(new ParallecResponseHandler() {
            @Override
            public void onCompleted(ResponseOnSingleTask res, Map<String, Object> responseContext) {
                responseContext.put("content", res.getResponseContent());
                logger.info("resultTest:getStatusCode " + res.getStatusCode());
            }
        });
        System.out.println(responseContext.get("content"));
        Asserts.check(responseContext.get("content").toString().contains("楽天"),
                " Fail to get response from none unicode sites");
        ParallecGlobalConfig.httpResponseBodyCharsetUsesResponseContentType = false;

    }
}
