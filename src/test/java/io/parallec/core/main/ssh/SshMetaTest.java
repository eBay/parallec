package io.parallec.core.main.ssh;

import io.parallec.core.TestBase;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.commander.workflow.ssh.SshProvider;
import io.parallec.core.exception.ParallelTaskInvalidException;

import org.junit.Test;

public class SshMetaTest extends TestBase {
    public static SshProvider sshProvider;

    public static SshMeta sshMetaPassword = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.PASSWORD, null, passwd, false,
            null, sshConnectionTimeoutMillis, false);
    public static SshMeta sshMetaKey = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.KEY,
            "userdata/fake-privkey.txt", null, false, null,
            sshConnectionTimeoutMillis, false);
    public static SshMeta sshMetaKeyNotExist = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.KEY,
            "userdata/noneexisting.txt", null, false, null,
            sshConnectionTimeoutMillis, false);

    private static void resetPass() {
        sshMetaPassword = new SshMeta(commandSshLineValid, userName,
                PORT_DEFAULT, SshLoginType.PASSWORD, null, passwd, false, null,
                sshConnectionTimeoutMillis, false);
    }

    private static void resetKey() {

        sshMetaKey = new SshMeta(commandSshLineValid, userName, PORT_DEFAULT,
                SshLoginType.KEY, "userdata/fake-privkey.txt", null, false,
                null, sshConnectionTimeoutMillis, false);
    }

    private static void expectException(SshMeta meta) {

        try {
            meta.validation();
        } catch (ParallelTaskInvalidException t) {
            logger.info("expected " + t);
        }

    }

    @Test
    public void Validationtest() throws Exception {
        sshMetaPassword.setCommandLine(null);

        expectException(sshMetaPassword);
        resetPass();

        sshMetaPassword.setUserName(null);
        expectException(sshMetaPassword);
        resetPass();

        sshMetaPassword.setSshLoginType(null);
        expectException(sshMetaPassword);
        resetPass();

        sshMetaPassword.setPassword(null);
        expectException(sshMetaPassword);
        resetPass();

        sshMetaKey.setPrivKeyRelativePath(null);
        expectException(sshMetaKey);
        resetKey();

        sshMetaKey.setPrivKeyUsePassphrase(true);
        sshMetaKey.setPassphrase(null);
        expectException(sshMetaKey);
        resetKey();

        sshMetaKey.getPrivKeyAbsPath();
    }
}
