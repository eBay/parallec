package io.parallec.core.main.ssh;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.parallec.core.TestBase;
import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.commander.workflow.ssh.SshProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.Charsets;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshProviderMockTest extends TestBase {

    public static SshProvider sshProvider;

    public static SshMeta sshMetaPassword = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.PASSWORD, null, passwd, false,
            null, sshConnectionTimeoutMillis, false);
    
    public static SshMeta sshMetaPasswordSuperUser = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.PASSWORD, null, passwd, false,
            null, sshConnectionTimeoutMillis, true);
    
    public static SshMeta sshMetaKey = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.KEY,
            "userdata/fake-privkey.txt", null, false, null,
            sshConnectionTimeoutMillis, false);
    
    public static SshMeta sshMetaKeyWithPassphrase = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.KEY,
            "userdata/fake-privkey.txt", null, true, "changeit",
            sshConnectionTimeoutMillis, false);
    
    public static SshMeta sshMetaKeyNotExist = new SshMeta(commandSshLineValid,
            userName, PORT_DEFAULT, SshLoginType.KEY,
            "userdata/noneexisting.txt", null, false, null,
            sshConnectionTimeoutMillis, false);

    // @Ignore
    @BeforeClass
    public static void setUp() throws Exception {

    }

    @Test
    public void testExecuteSshCommandNoneMock() {

        try {
            sshProvider = new SshProvider(sshMetaPassword, hostIpSample);
            sshProvider.executeSshCommand();
        } catch (Throwable t) {
            logger.info("expected");
        }
    }

    @Test
    public void testExecuteSshCommandSubStepsMock() {

        try {
            sshProvider = new SshProvider(sshMetaPassword, hostIpSample);
            sshProvider.startSshSessionAndObtainSession();
        } catch (Throwable t) {
            logger.info("expected");
        }

        try {
            sshProvider = new SshProvider(sshMetaKey, hostIpSample);
            sshProvider.startSshSessionAndObtainSession();
        } catch (Throwable t) {
            logger.info("expected");
        }

        try {
            sshProvider = new SshProvider(sshMetaKeyNotExist, hostIpSample);
            sshProvider.startSshSessionAndObtainSession();
            
        } catch (Throwable t) {
            logger.info("expected");
        }
   
        try {
            sshProvider = new SshProvider(sshMetaKeyWithPassphrase, hostIpSample);
            sshProvider.startSshSessionAndObtainSession();
        } catch (Throwable t) {
            logger.info("expected");
        }
        
        try {
            sshProvider = new SshProvider(sshMetaKeyWithPassphrase, hostIpSample);
            sshMetaKeyWithPassphrase.setPassphrase(null);
            sshProvider.startSshSessionAndObtainSession();
        } catch (Throwable t) {
            logger.info("expected");
        }
        
        

    }
    
    @Test
    public void sessionConnectGenerateChannelTestWithSuperUser() {

        Session session = mock(Session.class);
        ChannelExec channel = mock(ChannelExec.class);
        OutputStream out = mock(OutputStream.class);
        
        sshProvider = new SshProvider(sshMetaPasswordSuperUser, hostIpSample);
        try {

            when(session.openChannel("exec")).thenReturn(channel);
            when(channel.getOutputStream()).thenReturn(out);
            sshProvider.sessionConnectGenerateChannel(session);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sessionConnectGenerateChannelTest() {

        Session session = mock(Session.class);
        ChannelExec channel = mock(ChannelExec.class);
        sshProvider = new SshProvider(sshMetaKey, hostIpSample);
        try {

            when(session.openChannel("exec")).thenReturn(channel);
            sshProvider.sessionConnectGenerateChannel(session);

        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExecuteAndGenResponse() {

        // ResponseOnSingeReq (Channel channel) {
        ResponseOnSingeRequest sshResponse = new ResponseOnSingeRequest();
        ChannelExec channel = mock(ChannelExec.class);

        sshProvider = new SshProvider(sshMetaKey, hostIpSample);

        String stdoutStr = "Mon Sep 14 21:52:27 UTC 2015";
        InputStream in = new ByteArrayInputStream(
                stdoutStr.getBytes(Charsets.UTF_8));

        try {
            when(channel.getInputStream()).thenReturn(in);
            when(channel.isClosed()).thenReturn(false).thenReturn(true);
            sshResponse = sshProvider.executeAndGenResponse(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(sshResponse.toString());
    }

    @Test
    public void genErrorResponseTest() {
        sshProvider = new SshProvider(sshMetaKey, hostIpSample);
        sshProvider.genErrorResponse(new RuntimeException("fake exception"));
    }

    @Test
    public void executeAndGenResponseThrowsExceptionTest() {

        // ResponseOnSingeReq (Channel channel) {
        ResponseOnSingeRequest sshResponse = new ResponseOnSingeRequest();
        ChannelExec channel = mock(ChannelExec.class);
        sshProvider = new SshProvider(sshMetaKey, hostIpSample);
        String stdoutStr = "Mon Sep 14 21:52:27 UTC 2015";
        InputStream in = new ByteArrayInputStream(
                stdoutStr.getBytes(Charsets.UTF_8));

        try {
            when(channel.getInputStream()).thenReturn(in);
            when(channel.isClosed()).thenReturn(false).thenThrow(
                    new RuntimeException("fake exception"));
            sshResponse = sshProvider.executeAndGenResponse(channel);
        } catch (Throwable e) {
            logger.info("expected exception {}", "String", e);
        }

        logger.info(sshResponse.toString());
    }

}
