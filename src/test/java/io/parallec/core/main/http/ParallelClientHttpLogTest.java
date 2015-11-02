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
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.util.PcStringUtils;

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
public class ParallelClientHttpLogTest extends TestBase {

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
     * Hit websites min sync.
     */
    @Test
    public void hitWebsitesMinSync() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        ParallelTask pt = pc
                .prepareHttpGet("/validateInternals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(
                                ".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(
                                ".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>.*")
                                .filter(res.getResponseContent());

                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu);
                        metricMap.put("MemoryUsage", memory);

                        logger.info("cpu:" + cpu + " memory: " + memory
                                + " host: " + res.getHost());
                        responseContext.put(res.getHost(), cpu);

                        // logger.info(res.toString());

                    }
                });
        logger.info("Response details: "
                + PcStringUtils.renderJson(pt.getParallelTaskResult()));
        for (Object o : responseContext.values()) {

            Double cpuDouble = Double.parseDouble((String) o);
            Asserts.check(cpuDouble <= 100.0 && cpuDouble >= 0.0,
                    " Fail to extract cpu values");
        }

    }

}
