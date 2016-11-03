package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Yuanteng (Jeff) Pei
 *
 */
public class ParallelTaskCancelWholeTaskTest extends TestBase {

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
    public void cancelAfter100ms() {
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(300)
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
        while (!pt.isCompleted()) {
            try {
                Thread.sleep(100L);
                if (!hasCanceled)
                    pt.cancel(false);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void cancelAfterDone() {
        // $NULL_UR
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(300)
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
                Thread.sleep(2000L);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pt.cancel(true);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void cancelAfter2SecSync() {
        
        logger.info("START cancelAfter2SecSync ");
        ParallelTask pt = pc
                .prepareHttpGet("")
                .async()
                .setConcurrency(300)
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
                // travis ci is quick; can often finish the whole task within 1 sec.
                Thread.sleep(500L);
                pt.cancel(true);
                System.err.println(String.format(
                        "POLL_JOB_PROGRESS (%.5g%%)  PT jobid: %s",
                        pt.getProgress(), pt.getTaskId()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        pt.saveLogToLocal();
        
        logger.info("COMPLETED cancelAfter2SecSync ");
    }

}
