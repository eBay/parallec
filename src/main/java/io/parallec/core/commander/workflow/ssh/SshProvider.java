/*  
Copyright [2013-2015] eBay Software Foundation
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package io.parallec.core.commander.workflow.ssh;

import io.parallec.core.actor.message.ResponseOnSingeRequest;
import io.parallec.core.bean.ssh.SshLoginType;
import io.parallec.core.bean.ssh.SshMeta;
import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.util.PcErrorMsgUtils;
import io.parallec.core.util.PcFileNetworkIoUtils;
import io.parallec.core.util.PcStringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * based on Jsch. tested with real VM with Key/password cases. Run ssh. This is
 * a basic one that works with password. If needed we may replace this
 * SshProvider after defining an interface.
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class SshProvider {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(SshProvider.class);

    /** The ssh meta. */
    private SshMeta sshMeta;

    /** The target host. */
    private String targetHost;

    /**
     * Instantiates a new ssh provider.
     *
     * @param sshMeta
     *            the ssh meta
     * @param targetHost
     *            the target host
     */
    public SshProvider(SshMeta sshMeta, String targetHost) {
        super();
        this.sshMeta = sshMeta;
        this.targetHost = targetHost;
    }

    /**
     * Instantiates a new ssh provider.
     */
    public SshProvider() {
    }

    /** The session. */
    private Session session = null;

    /** The channel. */
    private Channel channel = null;

    /**
     * finally: will close the connection.
     *
     * @return the response on singe request
     */
    public ResponseOnSingeRequest executeSshCommand() {
        ResponseOnSingeRequest sshResponse = new ResponseOnSingeRequest();

        try {
            session = startSshSessionAndObtainSession();
            channel = sessionConnectGenerateChannel(session);
            sshResponse = executeAndGenResponse((ChannelExec) channel);

        } catch (Exception e) {
            sshResponse = genErrorResponse(e);
        } finally {

            if (session != null)
                session.disconnect();
            if (channel != null)
                channel.disconnect();
        }

        return sshResponse;

    }

    /**
     * Start ssh session and obtain session.
     *
     * @return the session
     */
    public Session startSshSessionAndObtainSession() {

        Session session = null;
        try {

            JSch jsch = new JSch();
            if (sshMeta.getSshLoginType() == SshLoginType.KEY) {

                String workingDir = System.getProperty("user.dir");
                String privKeyAbsPath = workingDir + "/"
                        + sshMeta.getPrivKeyRelativePath();
                logger.debug("use privkey: path: " + privKeyAbsPath);

                if (!PcFileNetworkIoUtils.isFileExist(privKeyAbsPath)) {
                    throw new RuntimeException("file not found at "
                            + privKeyAbsPath);
                }

                if (sshMeta.isPrivKeyUsePassphrase()
                        && sshMeta.getPassphrase() != null) {
                    jsch.addIdentity(privKeyAbsPath, sshMeta.getPassphrase());
                } else {
                    jsch.addIdentity(privKeyAbsPath);
                }
            }

            session = jsch.getSession(sshMeta.getUserName(), targetHost,
                    sshMeta.getSshPort());
            if (sshMeta.getSshLoginType() == SshLoginType.PASSWORD) {
                session.setPassword(sshMeta.getPassword());
            }

            session.setConfig("StrictHostKeyChecking", "no");
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
        return session;
    }

    /**
     * Session connect generate channel.
     *
     * @param session
     *            the session
     * @return the channel
     * @throws JSchException
     *             the j sch exception
     */
    public Channel sessionConnectGenerateChannel(Session session)
            throws JSchException {
    	// set timeout
        session.connect(sshMeta.getSshConnectionTimeoutMillis());
        
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(sshMeta.getCommandLine());

        // if run as super user, assuming the input stream expecting a password
        if (sshMeta.isRunAsSuperUser()) {
        	try {
                channel.setInputStream(null, true);

                OutputStream out = channel.getOutputStream();
                channel.setOutputStream(System.out, true);
                channel.setExtOutputStream(System.err, true);
                channel.setPty(true);
                channel.connect();
                
	            out.write((sshMeta.getPassword()+"\n").getBytes());
	            out.flush();
			} catch (IOException e) {
				logger.error("error in sessionConnectGenerateChannel for super user", e);
			}
        } else {
        	channel.setInputStream(null);
        	channel.connect();
        }

        return channel;

    }

    /**
     * Seems there are bad naming in the library the sysout is in
     * channel.getInputStream(); the syserr is in
     * ((ChannelExec)channel).setErrStream(os);
     *
     * @param channel
     *            the channel
     * @return the response on singe request
     */
    public ResponseOnSingeRequest executeAndGenResponse(ChannelExec channel) {
        ResponseOnSingeRequest sshResponse = new ResponseOnSingeRequest();

        InputStream in = null;
        OutputStream outputStreamStdErr = new ByteArrayOutputStream();
        StringBuilder sbStdOut = new StringBuilder();
        try {

            in = channel.getInputStream();
            channel.setErrStream(outputStreamStdErr);

            byte[] tmp = new byte[ParallecGlobalConfig.sshBufferSize];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, ParallecGlobalConfig.sshBufferSize);
                    if (i < 0)
                        break;
                    sbStdOut.append(new String(tmp, 0, i));

                }

                if (channel.isClosed()) {
                    if (in.available() > 0)
                        continue;
                    sshResponse.setFailObtainResponse(false);

                    // exit 0 is good
                    int exitStatus = channel.getExitStatus();
                    sshResponse.setStatusCodeInt(exitStatus);
                    sshResponse.setStatusCode(Integer.toString(exitStatus));
                    break;
                }

                Thread.sleep(ParallecGlobalConfig.sshSleepMIllisBtwReadBuffer);
            }

            sshResponse.setResponseBody(sbStdOut.toString());
            sshResponse.setErrorMessage(outputStreamStdErr.toString());
            sshResponse.setReceiveTimeNow();
        } catch (Exception t) {
            throw new RuntimeException(t);
        }

        return sshResponse;
    }

    /**
     * Gen error response.
     *
     * @param t
     *            the t
     * @return the response on single request
     */
    public ResponseOnSingeRequest genErrorResponse(Exception t) {
        ResponseOnSingeRequest sshResponse = new ResponseOnSingeRequest();
        String displayError = PcErrorMsgUtils.replaceErrorMsg(t.toString());

        sshResponse.setStackTrace(PcStringUtils.printStackTrace(t));
        sshResponse.setErrorMessage(displayError);
        sshResponse.setFailObtainResponse(true);

        logger.error("error in exec SSH. \nIf exection is JSchException: "
                + "Auth cancel and using public key. "
                + "\nMake sure 1. private key full path is right (try sshMeta.getPrivKeyAbsPath()). "
                + "\n2. the user name and key matches  " + t);

        return sshResponse;
    }

    /**
     * Gets the ssh meta.
     *
     * @return the ssh meta
     */
    public SshMeta getSshMeta() {
        return sshMeta;
    }

    /**
     * Sets the ssh meta.
     *
     * @param sshMeta
     *            the new ssh meta
     */
    public void setSshMeta(SshMeta sshMeta) {
        this.sshMeta = sshMeta;
    }

    /**
     * Gets the target host.
     *
     * @return the target host
     */
    public String getTargetHost() {
        return targetHost;
    }

    /**
     * Sets the target host.
     *
     * @param targetHost
     *            the new target host
     */
    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

}
