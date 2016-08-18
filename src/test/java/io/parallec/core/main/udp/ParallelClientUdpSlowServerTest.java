package io.parallec.core.main.udp;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;
import io.parallec.core.main.udp.sampleserver.UdpServerThread;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParallelClientUdpSlowServerTest extends TestBase {

    private static ParallelClient pc;
    private static UdpServerThread serverThread;

    @BeforeClass
    public static void setUp() throws Exception {
        pc = new ParallelClient();
        boolean similateSlowResponse = true;
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
     * should throw exception of idle timeout.
     * 
     * UDP server will sleep 5 seconds.  however this 
     */
    @Test(timeout = 50000)
    public void UdpTestWithSlowServer() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareUdp("hadoopmonudp").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setUdpPort(10091)
                .setResponseContext(responseContext)
                .setUdpIdleTimeoutSec(1)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.getResponseContent() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());
                        responseContext.put("respErrMsg",
                                res.getErrorMessage());

                    }

                });

        String respErrMsg = (String) responseContext.get("respErrMsg");
        Asserts.check(respErrMsg.contains("UDP idle (read) timeout"),
                "fail.UdpTest with slow server idle timeout flow");
       
    }
    

    /**
     * should throw exception of idle timeout.
     * 
     * UDP server will sleep 5 seconds.  now set idle timeout to be 12 sec
     */
    @Test(timeout = 50000)
    public void UdpTestWithSlowServerWithLongerIdleTimeout() {
        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareUdp("hadoopmonudp").setConcurrency(300)
                .setTargetHostsFromString("localhost")
                .setUdpPort(10091)
                .setResponseContext(responseContext)
                .setUdpIdleTimeoutSec(10)
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
        Asserts.check(resp.contains("hadoopmonudp"),
                "fail.UdpTest with slow server longer idle timeout flow");
       
    }
 
   
}
