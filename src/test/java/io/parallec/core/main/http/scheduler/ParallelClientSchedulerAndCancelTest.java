package io.parallec.core.main.http.scheduler;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.task.ParallelTaskManager;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientSchedulerAndCancelTest extends TestBase {

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
     * test Insufficient capacity:
     * 
     * submit 2 jobs each using 100, total is 120. so only 1 can run.
     */
    @Test(timeout = 120000)
    public void hitTop100WebsitesCapacityProtection() {

        ParallecGlobalConfig.maxCapacity = 120;
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK1: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        ParallelTask pt2 = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK2: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (!pt2.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT2 jobid: %s",
                        pt2.getProgress(), pt2.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ParallecGlobalConfig.maxCapacity = 2500;
        ParallelTaskManager.getInstance().shutdownTaskScheduler();
    }// end func

    /**
     * test Insufficient capacity with cancellation of the 2nd task while it is
     * in waiting state.
     * 
     * submit 2 jobs each using 100, total is 120. so only 1 can run.
     */
    @Test(timeout = 60000)
    public void hitTop100WebsitesCapacityProtectionCancel2ndTask() {

        ParallelTaskManager.getInstance().shutdownTaskScheduler();
        
        ParallecGlobalConfig.maxCapacity = 120;
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK1: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        ParallelTask pt2 = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK2: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));

                if (pt.getProgress() > 30.0) {
                    pt2.cancel(true);
                    logger.info("cancel 2nd task");

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (!pt2.isCompleted()) {
            try {
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT2 jobid: %s",
                        pt2.getProgress(), pt2.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info(pt2.prettyPrintInfo());

        ParallecGlobalConfig.maxCapacity = 2500;
    }

    /**
     * test Insufficient capacity with cancellation of the 2nd task while it is
     * in waiting state.
     * 
     * submit 2 jobs each using 100, total is 120. so only 1 can run.
     */
    @Test(timeout = 60000)
    public void hitTop100WebsitesCapacityProtectionCancelBothTask() {

        ParallecGlobalConfig.maxCapacity = 120;
        pc.prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK1: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });

        ParallelTask pt2 = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(100)
                .setEnableCapacityAwareTaskScheduler(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("TASK2: Responose Code:"
                                + res.getStatusCode() + " host: "
                                + res.getHost());
                    }
                });
        ParallelTaskManager.getInstance().cleanWaitTaskQueue();

        logger.info(pt2.prettyPrintInfo());

        ParallecGlobalConfig.maxCapacity = 2500;
    }

}
