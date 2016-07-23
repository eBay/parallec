package io.parallec.core.main.http.request.template;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecHeader;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.bean.StrStrMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelClientVarReplacementHostSpecificTest extends TestBase {

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
     * TODO 20160721 git issue #25
     * different requests to different ports  
     * http://www.jeffpei.com:80/job_b.html http://www.portquiz.com:8080/job_b.html
     */
    @Test
    public void hitWebsitesMinTargetHostSpecificPortReplacement() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("portquiz.net",
                new StrStrMap().addPair("PORT", "8080"));
        replacementVarMapNodeSpecific.put("www.jeffpei.com",
                new StrStrMap().addPair("PORT", "80"));

        pc.prepareHttpGet("/job_b.html")
                .setHttpPortReplaceable("$PORT")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "portquiz.net www.jeffpei.com")
                .setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        if(res.getRequest().getHost()=="portquiz.net"){
                            Assert.assertTrue( 
                                    res.getStatusCodeInt()==404);
                        }else if(res.getRequest().getHost()=="www.jeffpei.com"){
                            Assert.assertTrue(
                                    res.getStatusCodeInt()==200);
                        }
                        logger.info(res.toString());
                    }
                });

    }
    
    /**
     * trigger the NumberFormatException in execution manager. 
     */
    @Test
    public void hitWebsitesMinTargetHostSpecificPortReplacementErrorCase() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("portquiz.net",
                new StrStrMap().addPair("PORT", "8080"));
        replacementVarMapNodeSpecific.put("www.jeffpei.com",
                new StrStrMap().addPair("PORT", "80"));

        pc.prepareHttpGet("/job_b.html")
                .setHttpPortReplaceable("$PORT")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "localhost www.jeffpei.com")
                .setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(""+res.isError());
                    }
                });

    }
    
    
    
    /**
     * different requests to different target URLs
     * http://www.jeffpei.com/job_b.html http://www.restsuperman.com/job_c.html
     */
    @Test
    public void hitWebsitesMinTargetHostSpecificReplacement() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("www.parallec.io",
                new StrStrMap().addPair("JOB_ID", "job_a"));
        replacementVarMapNodeSpecific.put("www.jeffpei.com",
                new StrStrMap().addPair("JOB_ID", "job_b"));
        replacementVarMapNodeSpecific.put("www.restcommander.com",
                new StrStrMap().addPair("JOB_ID", "job_c"));

        pc.prepareHttpGet("/$JOB_ID.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String extractedString = new FilterRegex(
                                ".*<td>JobProgress</td>\\s*<td>(.*?)</td>[\\s\\S]*")
                                .filter(res.getResponseContent());
                        logger.info("ExtracedString: progress:"
                                + extractedString + " host: " + res.getHost());
                        logger.debug(res.toString());
                    }
                });

    }

    /**
     * note that the target host must match the key this is the new way new
     * http://www.parallec.io/userdata/sample_weather_48824.txt
     * http://www.parallec.io/userdata/sample_weather_95037.txt
     */
    @Test
    public void differentRequestsToSameTargetHost() {

        Map<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("temp", null);

        pc.prepareHttpGet("/userdata/sample_weather_$ZIP.txt")
                .setHttpHeaders(
                        new ParallecHeader().addPair("content-type",
                                "text/xml;charset=UTF-8"))
                .setConcurrency(1700)
                .setReplaceVarMapToSingleTargetSingleVar("ZIP", Arrays.asList("95037","48824"),
                        "www.parallec.io")
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String temp = new FilterRegex("(.*)").filter(res
                                .getResponseContent());
                        logger.info("\n!!Temperature: " + temp
                                + " TargetHost: " + res.getHost());
                        logger.debug(res.toString());

                        responseContext.put("temp", temp);
                    }
                });

        int tempGlobal = Integer.parseInt((String) responseContext.get("temp"));
        Asserts.check(
                tempGlobal <= 100 && tempGlobal >= 0,
                " Fail to extract output from sample weather API. Fail different request to same server test");

    }

    @Test
    public void differentRequestsToSameTargetHostMultiVarInvalidReplaceEmptyTarget() {

        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .setReplaceVarMapToSingleTarget(null, null);

        List<StrStrMap> replacementVarMapList = new ArrayList<StrStrMap>();
        replacementVarMapList.add(new StrStrMap());
        replacementVarMapList.add(null);

        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .setReplaceVarMapToSingleTarget(replacementVarMapList, "test");

    }

    /**
     * Use this api if need to match multiple variables new
     * http://www.parallec.io/userdata/MI/sample_weather_48824.txt
     * http://www.parallec.io/userdata/CA/sample_weather_95037.txt
     */
    @Test
    public void differentRequestsToSameTargetHostMultiVar() {

        List<StrStrMap> replacementVarMapList = new ArrayList<StrStrMap>();
        replacementVarMapList.add(new StrStrMap().addPair("ZIP", "95037")
                .addPair("STATE", "CA"));
        replacementVarMapList.add(new StrStrMap().addPair("ZIP", "48824")
                .addPair("STATE", "MI"));

        Map<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("temp", null);

        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .setHttpHeaders(
                        new ParallecHeader().addPair("content-type",
                                "text/xml;charset=UTF-8"))
                .setConcurrency(1700)
                .setReplaceVarMapToSingleTarget(replacementVarMapList,
                        "www.parallec.io")

                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String temp = new FilterRegex("(.*)").filter(res
                                .getResponseContent());
                        logger.info("\n!!Temperature: " + temp
                                + " TargetHost: " + res.getHost());
                        logger.debug(res.toString());

                        responseContext.put("temp", temp);
                    }
                });

        int tempGlobal = Integer.parseInt((String) responseContext.get("temp"));
        Asserts.check(
                tempGlobal <= 100 && tempGlobal >= 0,
                " Fail to extract output from sample weather API. Fail different request to same server test");

    }

    /**
     * note that the target host must match the key
     * 
     * new http://www.parallec.io/userdata/sample_weather_48824.txt
     * http://www.parallec.io/userdata/sample_weather_95037.txt
     */
    @Test
    public void differentRequestsToSameTargetHostWithMap() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();

        replacementVarMapNodeSpecific.put("api1",
                new StrStrMap().addPair("ZIP", "95037"));
        replacementVarMapNodeSpecific.put("api2",
                new StrStrMap().addPair("ZIP", "48824"));

        Map<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("temp", null);

        pc.prepareHttpGet("/userdata/sample_weather_$ZIP.txt")
                .setHttpHeaders(
                        new ParallecHeader().addPair("content-type",
                                "text/xml;charset=UTF-8"))
                .setConcurrency(1700)
                .setTargetHostsFromString("api1 api2")
                .setReplaceVarMapToSingleTargetFromMap(
                        replacementVarMapNodeSpecific, "www.parallec.io")
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String temp = new FilterRegex("(.*)").filter(res
                                .getResponseContent());
                        logger.info("\n!!Temperature: " + temp
                                + " TargetHost: " + res.getHost());
                        logger.debug(res.toString());

                        responseContext.put("temp", temp);
                    }
                });

        int tempGlobal = Integer.parseInt((String) responseContext.get("temp"));
        Asserts.check(
                tempGlobal <= 100 && tempGlobal >= 0,
                " Fail to extract output from sample weather API. Fail different request to same server test");

    }

    /**
     * 
     * this wsf.cdyne.com has been instable
     * 
     * http://www.restcommander.com/usecase.html#usecase-different-servers
     * 
     * note that this has dependency of the web service of wsf.cdyne.com is up.
     * 
     * 
     * Note that this require internet access
     * 
     * DNS slow in QA CI: wsf.cdyne.com 4.59.146.111
     */
    @Ignore
    public void differentRequestsToSameTargetHostOriginal() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();

        replacementVarMapNodeSpecific.put("95037",
                new StrStrMap().addPair("ZIP", "95037"));
        replacementVarMapNodeSpecific.put("48824",
                new StrStrMap().addPair("ZIP", "48824"));

        Map<String, Object> responseContext = new HashMap<String, Object>();
        responseContext.put("city", null);

        pc.prepareHttpPost("/WeatherWS/Weather.asmx")
                .setHttpHeaders(
                        new ParallecHeader().addPair("content-type",
                                "text/xml;charset=UTF-8"))
                .setConcurrency(1700)
                .setTargetHostsFromString("95037 48824")
                .setReplaceVarMapToSingleTargetFromMap(
                        replacementVarMapNodeSpecific, "wsf.cdyne.com")
                .setHttpEntityBody(
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:weat=\"http://ws.cdyne.com/WeatherWS/\">"
                                + "<soapenv:Header/><soapenv:Body><weat:GetCityWeatherByZIP><weat:ZIP>$ZIP</weat:ZIP>"
                                + "</weat:GetCityWeatherByZIP></soapenv:Body></soapenv:Envelope>")
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        String city = new FilterRegex(".*<City>(.*?)</City>.*")
                                .filter(res.getResponseContent());
                        String temperature = new FilterRegex(
                                ".*<Temperature>(.*?)</Temperature>.*")
                                .filter(res.getResponseContent());
                        logger.info("\n!!ExtracedString: City:" + city
                                + " Temperature: " + temperature
                                + " TargetHost: " + res.getHost());
                        logger.debug(res.toString());

                        responseContext.put("city", city);
                    }
                });

        String cityGlobal = (String) responseContext.get("city");
        Asserts.check(
                cityGlobal.equalsIgnoreCase("Morgan Hill")
                        || cityGlobal.equalsIgnoreCase("East Lansing"),
                " Fail to extract output from weather API. Fail different request to same server test");

    }

    /**
     * Test with diable request when need to disable the request.
     */
    @Test
    public void hitWebsitesMinTargetHostSpecificReplacementWithNA() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("www.parallec.io",
                new StrStrMap().addPair("JOB_ID", "job_a"));
        replacementVarMapNodeSpecific.put("www.jeffpei.com", new StrStrMap()
                .addPair("JOB_ID", "job_b").addPair("NA", "NA"));
        replacementVarMapNodeSpecific.put("www.restcommander.com",
                new StrStrMap().addPair("JOB_ID", "job_c"));

        ParallelTask task = pc
                .prepareHttpGet("/$JOB_ID.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {

                    }
                });

        logger.info(task.toString());

        Asserts.check(task.getRequestNum() == 3
                && task.getRequestNumActual() == 2,
                "NA is not able to disable the request");

    }

    /**
     * test all requests are disabled
     */
    @Test
    public void hitWebsitesMinTargetHostSpecificReplacementWithAllNA() {

        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("www.parallec.io", new StrStrMap()
                .addPair("JOB_ID", "job_a").addPair("NA", "NA"));
        replacementVarMapNodeSpecific.put("www.jeffpei.com", new StrStrMap()
                .addPair("JOB_ID", "job_b").addPair("NA", "NA"));
        replacementVarMapNodeSpecific.put("www.restcommander.com",
                new StrStrMap().addPair("JOB_ID", "job_c").addPair("NA", "NA"));

        ParallelTask task = pc
                .prepareHttpGet("/$JOB_ID.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .setReplacementVarMapNodeSpecific(replacementVarMapNodeSpecific)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {

                    }
                });

        logger.info(task.toString());

        Asserts.check(task.getRequestNum() == 3
                && task.getRequestNumActual() == 0,
                "NA is not able to disable the request");

    }

}
