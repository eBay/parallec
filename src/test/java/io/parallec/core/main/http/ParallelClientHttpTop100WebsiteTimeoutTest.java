package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.config.ParallelTaskConfig;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpTop100WebsiteTimeoutTest extends TestBase {

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
    public void hitTop100WebsitesTimeout() {
        // timeout early
        ParallelTaskConfig config = new ParallelTaskConfig();
        config.setTimeoutInManagerSec(1);

        ParallelTask pt = pc
                .prepareHttpGet("")
                .setConcurrency(1000)
                .setSaveResponseToTask(true)
                .setConfig(config)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose Code:" + res.getStatusCode()
                                + " host: " + res.getHost());
                    }
                });
        logger.info("completed {} out of {} tasks in early timeout.",
                pt.getResponsedNum(), pt.getRequestNum());

    }

}
