package io.parallec.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientTest extends TestBase {
    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    @Test
    public void testReInit() {

        pc.releaseExternalResources();

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc
                .prepareHttpGet("/validateInternals.html")
                .setConcurrency(1700)
                .handleInWorker()
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
                        logger.debug(res.toString());

                    }
                });

        for (Object o : responseContext.values()) {
            Double cpuDouble = Double.parseDouble((String) o);
            Asserts.check(cpuDouble <= 100.0 && cpuDouble >= 0.0,
                    " Fail to extract cpu values");
        }

    }
}
