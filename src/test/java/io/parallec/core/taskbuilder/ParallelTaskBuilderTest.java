package io.parallec.core.taskbuilder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ParallelTaskBuilder;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.bean.HttpMeta;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.exception.TargetHostsLoadException;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpMethod;

import java.util.Arrays;
import java.util.Map;

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
        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .getHttpMeta();
        pc.prepareHttpGet("/userdata/$STATE/sample_weather_$ZIP.txt")
                .setHttpMeta(new HttpMeta());

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
    public void testExecuteMock() throws Exception {

        ParallelTaskBuilder tb = mock(ParallelTaskBuilder.class);
        HttpMeta meta = new HttpMeta();
        meta.initValuesNa();
        meta.setHttpMethod(HttpMethod.GET);
        meta.setRequestPort("80");

        ParallecResponseHandler handler = new ParallecResponseHandler() {
            @Override
            public void onCompleted(ResponseOnSingleTask res,
                    Map<String, Object> responseContext) {
                logger.info("test:" + res.toString());
            }
        };
        try {
            when(tb.getMode()).thenThrow(new RuntimeException());
            when(tb.execute(handler)).thenCallRealMethod();
            tb.setHttpMeta(meta);
            tb.execute(handler);

        } catch (Exception e) {
            logger.info("expected NPE error " + e);
        }
    }

    @Test
    public void testExecuteWorkerHandlerException() throws Exception {
        ParallelTaskBuilder tb = new ParallelTaskBuilder();

        tb.setTargetHostsFromString("www.google.com");
        HttpMeta meta = new HttpMeta();
        meta.initValuesNa();
        meta.setHttpMethod(HttpMethod.GET);
        meta.setRequestPort("80");
        tb.setHttpMeta(meta);
        tb.handleInWorker();

        ParallecResponseHandler handler = new ParallecResponseHandler() {
            @Override
            public void onCompleted(ResponseOnSingleTask res,
                    Map<String, Object> responseContext) {
                throw new RuntimeException();
            }
        };

        try {
            tb.execute(handler);

        } catch (Exception e) {
            logger.info("expected error " + e);
        }
    }

    @Test
    public void testSetReplaceVarMapToSingleTargetSingleVar() throws Exception {
        ParallelTaskBuilder tb = new ParallelTaskBuilder();

        // test setReplaceVarMapToSingleTargetSingleVar
        tb.setReplaceVarMapToSingleTargetSingleVar(null, null, null);

        tb.setReplaceVarMapToSingleTargetSingleVar(null,
                Arrays.asList("a", null, "c"), "replace");

        tb.setRunAsSuperUser(true);
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
        
        try {
            tb.setTargetHostsFromCmsQueryUrl("", "", "token");
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

        tb.setAsyncHttpClient(HttpClientStore.getInstance()
                .getCurrentDefaultClient());
        try {

            tb.validation();
        } catch (ParallelTaskInvalidException e) {
            logger.info("expected error " + e);
        }
    }

}
