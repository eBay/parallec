package io.parallec.core.main.http;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.util.PcFileNetworkIoUtils;
import io.parallec.core.util.PcStringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelClientHttpTop100WebsiteTest extends TestBase {

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
    public void hitTop100WebsitesMin() {

        ParallelTask pt = pc
                .prepareHttpGet("")
                .setConcurrency(1000)
                .setSaveResponseToTask(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                            logger.info("Responose Code:" + res.getStatusCode()
                                    + " host: " + res.getHost());
                    }
                });
        logger.info("Result Summary\n{}",
                PcStringUtils.renderJson(pt.getAggregateResultFullSummary()));

        pt.saveLogToLocal();
    }

    @Ignore
    // @Test
    public void hitTop100WebsitesViaIps() {

        List<String> hostNames = PcFileNetworkIoUtils
                .getListFromLineByLineText(FILEPATH_TOP_100, SOURCE_LOCAL);

        List<String> hostIps = new ArrayList<String>();
        int count = 0;
        for (String hostName : hostNames) {
            logger.info("get ip for host # {}", ++count);
            InetAddress address;
            try {
                address = InetAddress.getByName(hostName);
                hostIps.add(address.getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        ParallelTask pt = pc.prepareHttpGet("").setConcurrency(1000)
                .setTargetHostsFromList(hostIps)
                // .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                // SOURCE_LOCAL)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose Code:" + res.getStatusCode()
                                + " host: " + res.getHost());
                    }
                });
        logger.info("Result Summary\n{}",
                PcStringUtils.renderJson(pt.getAggregateResultCountSummary()));

    }

}
