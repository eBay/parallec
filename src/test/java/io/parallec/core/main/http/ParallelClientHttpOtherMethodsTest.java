package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpOtherMethodsTest extends TestBase {

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
    public void hitWebsitesInvalidHttpMethods() {

        pc.prepareHttpPut("/validateInternals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("expected wrong HTTP methods {}",
                                res.toString());
                    }
                });

        pc.prepareHttpDelete("/validateInternals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("expected wrong HTTP methods {}",
                                res.toString());
                    }
                });

        pc.prepareHttpHead("/validateInternals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("expected wrong HTTP methods {}",
                                res.toString());
                    }
                });

        pc.prepareHttpOptions("/validateInternals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("expected wrong HTTP methods {}",
                                res.toString());
                    }
                });

    }// end func

}
