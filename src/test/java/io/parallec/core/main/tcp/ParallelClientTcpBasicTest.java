package io.parallec.core.main.tcp;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.main.tcp.sampleserver.TcpServerThread;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientTcpBasicTest extends TestBase {

    private static ParallelClient pc;
    private static TcpServerThread serverThread;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();

        serverThread = new TcpServerThread(false);
        serverThread.start();

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
    @Test(timeout = 50000)
    public void TcpTest() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareTcp("hadoopmon").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setTcpPort(10081)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.getResponseContent() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());
                        responseContext.put("resp",
                                res.getResponseContent());

                    }

                });

        String resp = (String) responseContext.get("resp");
        Asserts.check(resp.contains("AT_TCP_SERVER"),
                "fail.TcpTest with whole PC flow");

       
    }
    
    @Test(timeout = 50000)
    public void TcpExpectedRefusedConnectionTest() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareTcp("hadoopmon").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setTcpPort(10099)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.getResponseContent() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());
                        responseContext.put("resp",
                                res.getStatusCode());

                    }

                });

        String resp = (String) responseContext.get("resp");
        Asserts.check(resp.contains("FAILURE"),
                "fail.TcpTest with expected wrong port");

       
    }
    
    @Test
    public void TcpTestFunctionsExceptions() {
        
        try{
            
            pc.prepareTcp("hadoopmon").setConcurrency(300)
            .setTargetHostsFromString("localhost")
            .setHttpPollable(true)
            .validation()
            ;
        }catch(ParallelTaskInvalidException e){
            logger.info("EXPECTED error" + e.getLocalizedMessage());
        }

       
    }

}
