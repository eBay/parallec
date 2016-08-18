package io.parallec.core.main.udp;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.exception.ParallelTaskInvalidException;
import io.parallec.core.main.udp.sampleserver.UdpServerThread;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientUdpBasicTest extends TestBase {

    private static ParallelClient pc;
    private static UdpServerThread serverThread;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
        boolean similateSlowResponse = false;
        serverThread = new UdpServerThread(similateSlowResponse);
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
     * http://www.jeffpei.com/job_b.html http://www.restsuperman.com/job_c.html
     */
    @Test(timeout = 50000)
    public void UdpTest() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareUdp("hadoopmonudp").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setUdpPort(10091)
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
        Asserts.check(resp.contains("AT_UDP_SERVER"),
                "fail.UdpTest with whole PC flow");

       
    }
    
    @Test(timeout = 50000)
    public void UdpExpectedRefusedConnectionTest() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareUdp("hadoopmon").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setUdpPort(10099)
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
    public void UdpTestFunctionsExceptions() {
        
        try{
            
            pc.prepareUdp("hadoopmonudp").setConcurrency(300)
            .setTargetHostsFromString("localhost")
            .setHttpPollable(true)
            .validation()
            ;
        }catch(ParallelTaskInvalidException e){
            logger.info("EXPECTED error" + e.getLocalizedMessage());
        }

       
    }

}
