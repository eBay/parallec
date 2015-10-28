package io.parallec.core.taskbuilder;

import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.TestBase;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.exception.TargetHostsLoadException;
import io.parallec.core.resources.HttpClientStore;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelTaskBuilderTest extends TestBase {

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
    public void builderFuncTest() throws Exception {

        String jsonPath = "$.sample.small-target-hosts[*].hostName";

        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .setTargetHostsFromJsonPath(jsonPath, URL_JSON_PATH, SOURCE_URL);
        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt").getHttpMeta();
        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt").setHttpMeta(
                new HttpMeta());

    }

    @Test
    public void testInvalid() throws Exception {
        ParallelTaskBuilder tb = new ParallelTaskBuilder();
 
        
        try {

            tb.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected error " + e);
        }
    }
    
    @Test
    public void testSetterGetters() throws Exception {
        ParallelTaskBuilder tb = new ParallelTaskBuilder();
        tb.setResponseContext(null);
        try {
            tb.setTargetHostsFromCmsQueryUrl("", "");
        } catch (TargetHostsLoadException e) {
            logger.info("expected error " + e);
        }
        tb.getTargetHostMeta();
        tb.setTargetHostMeta(null);
        tb.setSshPort(22);

        tb.setSshLoginType(SshLoginType.KEY);
        tb.setSshPrivKeyRelativePath("");
        tb.setSshPrivKeyRelativePathWtihPassphrase("", "");
        
        tb.setTcpChannelFactory(null);
        tb.setTcpConnectTimeoutMillis(1000);
        tb.setTcpIdleTimeoutSec(5);
        tb.handleInManager();
        tb.handleInWorker();
        tb.sync();
        tb.getAsyncHttpClient();
        tb.setHttpEntityBody("test");
        tb.setTargetHostsFromList(Arrays.asList("www.parallec.io"));
        
        tb.setAsyncHttpClient(HttpClientStore.getInstance().getCurrentDefaultClient());
        try {

            tb.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected error " + e);
        }
    }

}
