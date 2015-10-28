package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.util.PcStringUtils;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelClientHttpTop500WebsiteTest extends TestBase {

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
     * Add throttling of 50 when need to hit around 500 web sites testing wait
     * and retry part
     */
    @Test
    public void hitTop500WebsitesThrottling() {

        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(500)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_500,
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
                Thread.sleep(100L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
                pc.logHealth();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("Result Summary\n{}",
                PcStringUtils.renderJson(pt.getAggregateResultCountSummary()));

    }// end func

    /**
     * Local ISP seems cannot be more than 500 concurrency or will fail
     */
    // @Test
    @Ignore
    public void hitTop1000WebsitesThrottling() {

        long startTime = System.currentTimeMillis();
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(500)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_1000,
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
                pc.logHealth();
                // System.err.println(String.format("POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                // pt.getProgress(), pt.getTaskId() ));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();

        String secondElapsedStr = new Double((endTime - startTime) / 1000.0)
                .toString();

        logger.info("Result Summary\n{}",
                PcStringUtils.renderJson(pt.getAggregateResultCountSummary()));
        logger.info("Hit 10K website use {} seconds.", secondElapsedStr);

    }

}
