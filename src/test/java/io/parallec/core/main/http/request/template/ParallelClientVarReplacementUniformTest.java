package io.parallec.core.main.http.request.template;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelClientVarReplacementUniformTest extends TestBase {

    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    // TODO
    @Ignore
    public void testVarReplacementTargetHostSpecificHttpHeaderReplacement() {

    }

    /**
     * same requests to different target URLs
     * 
     * demonstrate the uniform variable replacement
     * 
     * here: the "$URL_VARIABLE" is replaced by validateInternals
     */
    @Test
    public void hitWebsitesMinTargeUniformReplacement() {

        Map<String, String> replacementVarMap = new HashMap<String, String>();
        replacementVarMap.put("URL_VARIABLE", "validateInternals");

        pc.prepareHttpGet("/$URL_VARIABLE.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .setReplacementVarMap(replacementVarMap)
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
                        Double cpuDouble = Double.parseDouble(cpu);
                        Asserts.check(cpuDouble <= 100.0 && cpuDouble >= 0.0,
                                " Fail to extract cpu values");
                    }
                });

    }

}
