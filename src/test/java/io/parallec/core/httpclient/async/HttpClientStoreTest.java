package io.parallec.core.httpclient.async;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.resources.HttpClientStore;
import io.parallec.core.resources.HttpClientType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpClientStoreTest extends TestBase {

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
    public void testDirectorForException() {
        try {
            HttpClientStore.getInstance().init();

            HttpClientStore.getInstance().getClientByType(HttpClientType.EMBED_FAST);
            HttpClientStore.getInstance().getCustomClientFast();
            HttpClientStore.getInstance().getCustomClientSlow();
            HttpClientStore.getInstance().getEmbedClientFast();
            HttpClientStore.getInstance().getEmbedClientSlow();
            HttpClientStore.getInstance().setHttpClientTypeCurrentDefault(HttpClientType.CUSTOM_FAST);
        } catch (Exception ex) {
            logger.error("Expected Exception : " + ex);
        }
    }// end func

}
