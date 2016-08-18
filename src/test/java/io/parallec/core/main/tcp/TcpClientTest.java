package io.parallec.core.main.tcp;

import io.parallec.core.ParallelClient;
import io.parallec.core.TestBase;
import io.parallec.core.main.tcp.sampleserver.TcpServerThread;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

public class TcpClientTest extends TestBase {

    private static ParallelClient pc;
    private static TcpServerThread serverThread;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();

        serverThread = new TcpServerThread(false);
        serverThread.start();
        
        //add sleep to make sure the server starts first
        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }

    @AfterClass
    public static void shutdown() throws Exception {
        pc.releaseExternalResources();
        serverThread.setShutdown(true);

        try {
            Thread.sleep(500L);
        } catch (Exception e) {
            ;
        }
    }


    /**
     * different requests to different target URLs
     * http://www.jeffpei.com/job_b.html http://www.restsuperman.com/job_c.html
     */
    //@Test(timeout = 50000)
    @Ignore
    public void telClientTest() {
        try {
            TcpProviderPoc.getInstance().request();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }


}
