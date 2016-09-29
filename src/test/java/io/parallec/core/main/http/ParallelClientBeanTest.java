package io.parallec.core.main.http;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.resources.HttpClientType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientBeanTest extends TestBase {

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
    public void parallecClient() {
        pc.reinitHttpClients();
        pc.cleanInprogressJobMap();
        pc.cleanWaitTaskQueue();
        pc.setCustomClientFast(null);
        pc.setCustomClientSlow(null);
        pc.setHttpClientTypeCurrentDefault(HttpClientType.EMBED_FAST);
        pc.getRunningJobCount();

    }

}
