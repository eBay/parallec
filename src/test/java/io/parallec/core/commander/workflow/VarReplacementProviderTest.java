package io.parallec.core.commander.workflow;

import io.parallec.core.FilterRegex;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.bean.StrStrMap;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VarReplacementProviderTest  extends TestBase {


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
    public void testVarReplacementProviderWithNullMap() {
        
        Map<String, StrStrMap> replacementVarMapNodeSpecific = new HashMap<String, StrStrMap>();
        replacementVarMapNodeSpecific.put("www.jeffpei.com",
                new StrStrMap());
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
}
