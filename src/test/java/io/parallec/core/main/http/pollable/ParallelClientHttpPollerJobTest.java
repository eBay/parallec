package io.parallec.core.main.http.pollable;

import io.parallec.core.ParallecHeader;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.actor.poll.HttpPollerProcessor;
import io.parallec.core.main.http.pollable.sampleserver.HttpServerThread;
import io.parallec.core.main.http.pollable.sampleserver.ServerWithPollableJobs;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpPollerJobTest extends TestBase {

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

        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    public HttpPollerProcessor generateSampleHttpPoller() {

        // Init the poller
        String pollerType = "CronusAgentPoller";
        String successRegex = ".*\"progress\"\\s*:\\s*(100).*}";
        String failureRegex = ".*\"error\"\\s*:\\s*(.*).*}";
        String jobIdRegex = ".*\"/status/(.*?)\".*";
        String progressRegex = ".*\"progress\"\\s*:\\s*([0-9]*).*}";
        int progressStuckTimeoutSeconds = 600;
        int maxPollError = 5;
        long pollIntervalMillis = 2000L;
        String jobIdPlaceHolder = "$JOB_ID";
        String pollerRequestTemplate = "/status/" + jobIdPlaceHolder;

        HttpPollerProcessor httpPollerProcessor = new HttpPollerProcessor(
                pollerType, successRegex, failureRegex, jobIdRegex,
                progressRegex, progressStuckTimeoutSeconds, pollIntervalMillis,
                pollerRequestTemplate, jobIdPlaceHolder, maxPollError);

        return httpPollerProcessor;
    }

    /**
     * different requests to different target URLs
     * http://www.jeffpei.com/job_b.html http://www.restsuperman.com/job_c.html
     */
    @Test(timeout = 50000)
    public void asyncPollerTest() {

        HttpPollerProcessor httpPollerProcessor = generateSampleHttpPoller();

        ParallelTask task = pc
                .prepareHttpPost("/submitJob")
                .setHttpHeaders(
                        new ParallecHeader().addPair("authorization",
                                ServerWithPollableJobs.AUTH_KEY))
                .setHttpPort(10080).setConcurrency(1500)
                .setTargetHostsFromString("localhost").setHttpPollable(true)
                .setHttpPollerProcessor(httpPollerProcessor)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("getPollingHistory:"
                                + res.getPollingHistory() + " host: "
                                + res.getHost());
                        logger.info(res.toString());
                    }
                });
        logger.info("Task Pretty Print: \n{}", task.prettyPrintInfo());
    }

    @Test(timeout = 20000)
    public void asyncPollerWrongJobIdRegexTest() {

        HttpPollerProcessor httpPollerProcessor = generateSampleHttpPoller();
        httpPollerProcessor.setJobIdRegex(".*\"/statusWrong/(.*?)\".*");
        pc.prepareHttpPost("/submitJob").setHttpPort(10080).setConcurrency(1500)
                .setTargetHostsFromString("localhost").setHttpPollable(true)
                .setHttpPollerProcessor(httpPollerProcessor)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("getPollingHistory:"
                                + res.getPollingHistory() + " host: "
                                + res.getHost());
                        logger.debug(res.toString());
                    }
                });

    }

}
