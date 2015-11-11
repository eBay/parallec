package io.parallec.core.main.http.request.template;

import io.parallec.core.ParallecHeader;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.main.http.pollable.sampleserver.HttpServerThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientVarReplacementHostSpecificHeaderTest extends
        TestBase {

    private static ParallelClient pc;
    private static HttpServerThread serverThread;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
        serverThread = new HttpServerThread();
        serverThread.start();
        
        //add sleep to make sure the server starts first
        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }        
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
        serverThread.setShutdown(true);
    }

    /**
     * Testing the replacement in the header values. the sample server would
     * response with the header values
     */
    @Test
    public void differentRequestsToSameTargetHostWithHeaderReplacement() {
        List<String> replaceList = new ArrayList<String>();
        replaceList.add("111");
        replaceList.add("222");

        Map<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("temp", null);

        pc.prepareHttpGet("/testHeaders")
                .setHttpHeaders(new ParallecHeader().addPair("sample", "$SAMPLE"))
                .setConcurrency(1700)
                .setHttpPort(10080)
                .setReplaceVarMapToSingleTargetSingleVar("SAMPLE", replaceList,
                        "localhost").setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());

                        responseContext.put("sampleValue",
                                res.getResponseContent());
                    }
                });

        int tempGlobal = Integer.parseInt((String) responseContext
                .get("sampleValue"));
        Asserts.check(
                tempGlobal <= 222 && tempGlobal >= 111,
                " Fail to extract o Fail different request to same server with header replacement test");

    }

}
