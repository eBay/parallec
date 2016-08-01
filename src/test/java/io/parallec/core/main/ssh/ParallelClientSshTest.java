package io.parallec.core.main.ssh;

import io.parallec.core.ParallecResponseHandler;
import io.parallec.core.ParallelClient;
import io.parallec.core.ResponseOnSingleTask;
import io.parallec.core.TestBase;

import java.util.HashMap;
import java.util.Map;

import io.parallec.core.bean.ssh.SshJumpHostMeta;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import org.apache.http.util.Asserts;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ParallelClientSshTest extends TestBase {

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
    public void sshWorkerJumpHostTest(){
        Map<String, Object> responseContext = new HashMap<String, Object>();

        SshJumpHostMeta jumpMeta = new SshJumpHostMeta();
        jumpMeta.setUserName(userName);
        jumpMeta.setSshLoginType(SshLoginType.PASSWORD);
        jumpMeta.setPassword(passwd);
        pc.prepareSsh().setConcurrency(150)
                .setTargetHostsFromString(LOCALHOST)
                .setSshLoginType(SshLoginType.PASSWORD)
                .setSshUserName(userName)
                .setSshPassword(passwd)
                .setSshCommandLine("df -h; ds; ")
                .setSshJumpHost(hostIpSample)
                .setSshJumpHostMeta(jumpMeta)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {
                    @Override
                    public void onCompleted(ResponseOnSingleTask res, Map<String, Object> responseContext){
                        responseContext.put("response", res.toString());
                        responseContext.put("host", res.getHost());
                        responseContext.put("errmsg", res.getErrorMessage());
                    }
                });
        logger.info(responseContext.get("response").toString());
        pc.releaseExternalResources();
    }

    @Test
    public void sshWorkerFakeVmPasswordTest() {

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareSsh().setConcurrency(500)
                .setTargetHostsFromString(hostIpSample)
                .setSshCommandLine("df -h; ds; ").setSshUserName(userName)
                .setSshPassword(passwd).setSshConnectionTimeoutMillis(5000)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.toString() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());
                        responseContext.put("errorMessage",
                                res.getErrorMessage());

                    }

                });

        String errMessage = (String) responseContext.get("errorMessage");
        Asserts.check(errMessage.contains("socket is not established"),
                "fail. error is not expected. not sure if ssh flow was executed");

    }// end func

    @Test
    public void sshWorkerFakeVmPasswordInvalidPollerTest() {

        Map<String, Object> responseContext = new HashMap<String, Object>();
        pc.prepareSsh().setConcurrency(300)
                .setTargetHostsFromString(hostIpSample)
                .setSshCommandLine("df -h; ds; ").setSshUserName(userName)
                .setHttpPollable(true).setSshPassword(passwd)
                .setResponseContext(responseContext)
                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.toString() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());

                    }

                });

    }// end func

    // @Test
    @Ignore
    public void sshWorkerRealVmPasswordTest() {
        pc.prepareSsh().setConcurrency(300)
                .setTargetHostsFromString(SshProviderRealTest.vmIp)
                .setSshCommandLine("df -h; ds; ").setSshUserName("parallec")
                .setSshPassword("parallec")

                .execute(new ParallecResponseHandler() {

                    @Override
                    public void onCompleted(ResponseOnSingleTask res,
                            Map<String, Object> responseContext) {
                        logger.info("Responose:" + res.toString() + " host: "
                                + res.getHost() + " errmsg: "
                                + res.getErrorMessage());
                    }
                });
    }

}
