package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpInvalidInputTest extends TestBase {

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
    public void hitWebsitesUrlWithValidation() {

        // miss target hosts

        boolean validate = pc.prepareHttpGet(" /validateInternals.html ")
                .setConcurrency(1700)
                // .setTargetHostsFromString("www.parallec.io www.jeffpei.com www.restcommander.com")
                .validation();
        System.out.println("validation: " + validate);

        boolean validate2 = pc
                .prepareHttpGet(" /validateInternals.html ")
                .setConcurrency(1700)
                .async()
                .sync()
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .validation();
        System.out.println("validation2: " + validate2);
    }

    @Test
    public void hitWebsitesUrlEndingWSInput() {

        // hitWebsitesUrlEndingWSInput
        pc.prepareHttpGet(" /validateInternals.html ")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.parallec.io www.jeffpei.com www.restcommander.com")
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info(res.toString());
                    }
                });

    }

    @Test
    public void hitWebsitesInvalidInput() {

        // miss target hosts
        pc.prepareHttpGet("/validateInternals.html").setConcurrency(1700)

        .execute(new ParallecResponseHandler() {

            @Override
            public void onCompleted(ResponseOnSingleTask res,
                    Map<String, Object> responseContext) {
            }
        });

        // space in between URL
        pc.prepareHttpGet("/validate Internals.html")
                .setConcurrency(1700)
                .setTargetHostsFromString(
                        "www.restcommander.com www.jeffpei.com")
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                    }
                });

        // space in between single target host

        List<String> badHostList = new ArrayList<String>();
        badHostList.add("www.restco mmander.com");
        pc.prepareHttpGet("/validateInternals.html").setConcurrency(1700)
                .setTargetHostsFromList(badHostList)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                    }
                });

    }

}
