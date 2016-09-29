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

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.bean.ResponseHeaderMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing the response header
 * 
 */
public class ParallelClientHttpResponseHeaderTest extends TestBase {

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
     * Hit websites and get all headers
     */
    @Test
    public void hitWebsitesMinSyncWithResponseHeadersAll() {

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareHttpGet("/validateInternals.html")
                .setConcurrency(1700)
                .handleInWorker()
                .saveResponseHeaders(new ResponseHeaderMeta(null, true))
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {

                        Map<String, List<String>> responseHeaders = res
                                .getResponseHeaders();
                        for (Entry<String, List<String>> entry : responseHeaders
                                .entrySet()) {
                            logger.info(
                                    "response header (lowed case key): {} - {}",
                                    entry.getKey(), entry.getValue());
                        }
                        responseContext.put(res.getHost(),
                                responseHeaders.size());
                        logger.debug(res.toString());

                    }
                });

        for (Object o : responseContext.values()) {
            int headerKeySize = Integer.parseInt((String) o);
            Asserts.check(headerKeySize > 0, " Fail to extract http header");
        }
        // logger.info("Task Pretty Print: \n{}", task.prettyPrintInfo());
    }

    /**
     * Hit websites min sync and get response headers with certain list of keys.
     * 
     * the x-github-request-id key is not present in amazone S3 responses. So
     * the response header size should be [3,2,2]
     * 
     * note that the key set provided in ResponseHeaderMeta in request can be case insensitive. However in the
     * response headers map returned in the ResponseOnSingleTask, all keys have been lower case for easy access.
     * 
     */
    @Test
    public void hitWebsitesMinSyncWithResponseHeadersSubset() {

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareHttpGet("/validateInternals.html")
                .setConcurrency(1700)
                .handleInWorker()
                .saveResponseHeaders(
                        new ResponseHeaderMeta(Arrays.asList("Content-Type",
                                "server", "x-github-request-id"), false))
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {

                        Map<String, List<String>> responseHeaders = res
                                .getResponseHeaders();
                        for (Entry<String, List<String>> entry : responseHeaders
                                .entrySet()) {
                            logger.info(
                                    "response header (lowed case key): {} - {}",
                                    entry.getKey(), entry.getValue());
                        }
                        responseContext.put(res.getHost(),
                                responseHeaders.size());
                        logger.debug(res.toString());

                    }
                });

        for (Object o : responseContext.values()) {
            int headerKeySize = Integer.parseInt((String) o);
            Asserts.check(headerKeySize >= 2 && headerKeySize <= 3,
                    " Fail to extract http header subset");
        }
        // logger.info("Task Pretty Print: \n{}", task.prettyPrintInfo());
    }

}
