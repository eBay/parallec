package io.parallec.core.main.http;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.util.PcDateUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpFromCmsAsyncTest extends TestBase {

    private static ParallelClient pc;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
    }

    /**
     * With CMS query; async timeout 15 seconds
     * Added token
     */
    @Test(timeout = 15000)
    public void hitCmsQuerySinglePageWithoutTokenAsync() {

        // http://ccoetech.ebay.com/cms-configuration-management-service-based-mongodb
        String cmsQueryUrl = URL_CMS_QUERY_SINGLE_PAGE;
        ParallelTask pt = pc.prepareHttpGet("/validateInternals.html")
                .setTargetHostsFromCmsQueryUrl(cmsQueryUrl, "label")
                .setConcurrency(1700).async()
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(
                                ".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(
                                ".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());

                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu);
                        metricMap.put("MemoryUsage", memory);
                        metricMap.put("LastUpdated",
                                PcDateUtils.getNowDateTimeStrStandard());
                        metricMap.put("NodeGroupType", "OpenSource");

                        logger.info("cpu:" + cpu + " host: " + res.getHost());
                    }
                });
        logger.info(pt.toString());
        Asserts.check(pt.getRequestNum() == 3, "fail to load all target hosts");
        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format("POLL_JOB_PROGRESS (%.5g%%)",
                        pt.getProgress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }    
    
    
    /**
     * With CMS query; async timeout 15 seconds
     * Added token
     */
    @Test(timeout = 15000)
    public void hitCmsQuerySinglePageWithTokenAsync() {

        // http://ccoetech.ebay.com/cms-configuration-management-service-based-mongodb
        String cmsQueryUrl = URL_CMS_QUERY_SINGLE_PAGE;
        ParallelTask pt = pc.prepareHttpGet("/validateInternals.html")
                .setTargetHostsFromCmsQueryUrl(cmsQueryUrl, "label", "someToken")
                .setConcurrency(1700).async()
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(
                                ".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(
                                ".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());

                        Map<String, Object> metricMap = new HashMap<String, Object>();
                        metricMap.put("CpuUsage", cpu);
                        metricMap.put("MemoryUsage", memory);
                        metricMap.put("LastUpdated",
                                PcDateUtils.getNowDateTimeStrStandard());
                        metricMap.put("NodeGroupType", "OpenSource");

                        logger.info("cpu:" + cpu + " host: " + res.getHost());
                    }
                });
        logger.info(pt.toString());
        Asserts.check(pt.getRequestNum() == 3, "fail to load all target hosts");
        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format("POLL_JOB_PROGRESS (%.5g%%)",
                        pt.getProgress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * With CMS(YiDB) query; async timeout 15 seconds CMS Example:
     * http://ccoetech
     * .ebay.com/cms-configuration-management-service-based-mongodb
     */
    @Test(timeout = 15000)
    public void hitCmsQueryMultiPageAsync() {

        String cmsQueryUrl = URL_CMS_QUERY_MULTI_PAGE;
        ParallelTask pt = pc.prepareHttpGet("/validateInternals.html")
                .setTargetHostsFromCmsQueryUrl(cmsQueryUrl)
                .setConcurrency(1700).async()
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String cpu = new FilterRegex(
                                ".*<td>CPU-Usage-Percent</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        String memory = new FilterRegex(
                                ".*<td>Memory-Used-KB</td>\\s*<td>(.*?)</td>[\\s\\S]*")
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

        logger.info(pt.toString());
        Asserts.check(pt.getRequestNum() == 3, "fail to load all target hosts");

        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format("POLL_JOB_PROGRESS (%.5g%%)",
                        pt.getProgress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
