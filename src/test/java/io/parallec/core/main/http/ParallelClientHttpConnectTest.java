package io.parallec.core.main.http;

import java.util.Map;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTask;
import io.parallec.core.TestBase;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.util.PcStringUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientHttpConnectTest extends TestBase{
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
    public void hitConnectTest() {
    	
        ParallelTask pt = pc
        		.prepareHttpConnect("")
                .setConcurrency(1000)
                .setSaveResponseToTask(true)
                .setTargetHostsFromLineByLineText(FILEPATH_TOP_100,
                        SOURCE_LOCAL).execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                            logger.info("Responose Code:" + res.getStatusCode()
                                    + " host: " + res.getHost()+ " Reponse Content: " +res.getResponseContent());
                    }
                });
        logger.info("Result Summary\n{}",
                PcStringUtils.renderJson(pt.getAggregateResultFullSummary()));

        pt.saveLogToLocal();
    }

    
}
