package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.actor.ExecutionManagerTest;
import io.parallec.core.task.ParallelTaskState;

import java.util.Arrays;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO Testing the enabled capacity control
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class ParallelTaskCancelOnTargetHostsTest extends TestBase {

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
    public void cancelSingleHostAfter200MillisGoodAndBadHostName() {
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose Code:" + res.getStatusCode()
                                + " host: " + res.getHost());
                    }
                });
        boolean hasCanceled = false;
        boolean cancelSuccess = false;
        while (!pt.isCompleted()) {
            try {
                Thread.sleep(200L);
                if (!hasCanceled) {
                    logger.info("try to cancel on target");
                    cancelSuccess = pt.cancelOnTargetHosts(Arrays.asList(
                            "badhostName", "www.walmart.com"));
                    hasCanceled = true;
                }
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Asserts.check(cancelSuccess == true,
                "fail cancelSingleHostAfter200Millis");
        pt.saveLogToLocal();
    }

    @Test
    public void cancelSingleHostAfterDone() {
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose Code:" + res.getStatusCode()
                                + " host: " + res.getHost());
                    }
                });
        while (!pt.isCompleted()) {
            try {
                Thread.sleep(1000L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pt.cancelOnTargetHosts(Arrays.asList("www.walmart.com"));
    }

    @Test
    public void testCancelOnHostException() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.setState(null);
        task.cancelOnTargetHosts(Arrays.asList("target"));
    }

    @Test
    public void testCancelOnHostNullManager() {
        ParallelTask task = ExecutionManagerTest.genParallelTask();
        task.setState(ParallelTaskState.IN_PROGRESS);
        task.executionManager = null;
        task.cancelOnTargetHosts(Arrays.asList("target"));
    }

}
