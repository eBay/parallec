package io.parallec.core.main.ssh;

import io.parallec.core.TestBase;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.commander.workflow.ssh.SshProvider;

import org.junit.Ignore;

public class SshProviderRealTest extends TestBase {

    private static int PORT_DEFAULT = 22;

    public static String vmIp = "52.25.191.169";

    @Ignore
    // @Test
    public void testExecuteSshCommandRealHostPassword() {

        SshMeta sshMeta = new SshMeta("date; ifconfig; ds", "parallec",
                PORT_DEFAULT, SshLoginType.PASSWORD, null, "parallec", false,
                null, sshConnectionTimeoutMillis, false);

        SshProvider instance = new SshProvider(sshMeta, vmIp);
        ResponseOnSingeRequest response = instance.executeSshCommand();
        logger.info(response.toString());
    }

    // @Test
    @Ignore
    public void testExecuteSshCommandRealHostKey() {

        SshMeta sshMeta = new SshMeta(" ps -ef; ds; ifconfig", "ubuntu",
                PORT_DEFAULT, SshLoginType.KEY, "userdata/vm-keys.pem",
                null, false, null, sshConnectionTimeoutMillis, false);
        SshProvider instance = new SshProvider(sshMeta, vmIp);
        ResponseOnSingeRequest response = instance.executeSshCommand();
        logger.info("PrivKey absolute path: " + sshMeta.getPrivKeyAbsPath());
        logger.info(response.toString());
    }

}
