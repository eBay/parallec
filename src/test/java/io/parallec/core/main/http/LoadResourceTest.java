package io.parallec.core.main.http;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoadResourceTest extends TestBase {

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
    public void testLoadResource() {

        Reader r = new InputStreamReader(getClass().getClassLoader()
                .getResourceAsStream("actorconfig.conf"));
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1;)
                sw.write(buffer, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = sw.toString();
        System.out.println(str);
    }


}
