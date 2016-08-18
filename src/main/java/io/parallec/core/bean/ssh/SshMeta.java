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
package io.parallec.core.bean.ssh;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;



// TODO: Auto-generated Javadoc
/**
 * all ssh metadata except for the target host name. also those timeout configs
 * and
 * 
 * @author Yuanteng (Jeff) Pei
 *
 */
public class SshMeta {

    /** The command line. */
    private String commandLine;

    /** The user name. */
    private String userName;

    /** The ssh port. */
    private int sshPort;

    /** The ssh login type. */
    // key or password based
    private SshLoginType sshLoginType;

    /** The priv key relative path. */
    private String privKeyRelativePath;

    /** The password. */
    private String password;

    /** The priv key use passphrase. */
    private boolean privKeyUsePassphrase;
    
    /** The priv key for runAsSuperUser. */
    private boolean runAsSuperUser;

    /** The passphrase. */
    private String passphrase;

    /** The ssh connection timeout millis. */
    // ssh
    private int sshConnectionTimeoutMillis;

    /**
     * Checks if is priv key use passphrase.
     *
     * @return true, if is priv key use passphrase
     */
    public boolean isPrivKeyUsePassphrase() {
        return privKeyUsePassphrase;
    }

    /**
     * Sets the priv key use passphrase.
     *
     * @param privKeyUsePassphrase
     *            the new priv key use passphrase
     */
    public void setPrivKeyUsePassphrase(boolean privKeyUsePassphrase) {
        this.privKeyUsePassphrase = privKeyUsePassphrase;
    }

    /**
     * Gets the passphrase.
     *
     * @return the passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Sets the passphrase.
     *
     * @param passphrase
     *            the new passphrase
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    
    /**
     * Checks if is run as super user.
     *
     * @return true, if is run as super user
     */
    public boolean isRunAsSuperUser() {
		return runAsSuperUser;
	}

    /**
     * Sets the runAsSuperUser.
     *
     * @param runAsSuperUser
     *            the new runAsSuperUser
     */
    public void setRunAsSuperUser(boolean runAsSuperUser) {
		this.runAsSuperUser = runAsSuperUser;
	}


    /**
     * Instantiates a new ssh meta.
     *
     * @param commandLine the command line
     * @param userName the user name
     * @param sshPort the ssh port
     * @param sshLoginType the ssh login type
     * @param privKeyRelativePath the priv key relative path
     * @param password the password
     * @param privKeyUsePassphrase the priv key use passphrase
     * @param passphrase the passphrase
     * @param sshConnectionTimeoutMillis the ssh connection timeout millis
     * @param runAsSuperUser if the ssh will be run as superuser.
     */
    public SshMeta(String commandLine, String userName, int sshPort,
            SshLoginType sshLoginType, String privKeyRelativePath,
            String password, boolean privKeyUsePassphrase, String passphrase,
            int sshConnectionTimeoutMillis, boolean runAsSuperUser) {
        super();
        this.commandLine = commandLine;
        this.userName = userName;
        this.sshPort = sshPort;
        this.sshLoginType = sshLoginType;
        this.privKeyRelativePath = privKeyRelativePath;
        this.password = password;
        this.privKeyUsePassphrase = privKeyUsePassphrase;
        this.passphrase = passphrase;
        this.sshConnectionTimeoutMillis = sshConnectionTimeoutMillis;
        this.runAsSuperUser = runAsSuperUser;
    }

    /**
     * Instantiates a new ssh meta. timeout using the default one:
     * sshConnectionTimeoutMillis
     */
    public SshMeta() {

        this.commandLine = null;
        this.userName = null;
        this.sshPort = 22;
        this.sshLoginType = null;
        this.privKeyRelativePath = null;
        this.password = null;
        this.privKeyUsePassphrase = false;
        this.passphrase = null;
        this.runAsSuperUser = false;
        this.sshConnectionTimeoutMillis = ParallecGlobalConfig.sshConnectionTimeoutMillisDefault;

    };

	/**
     * Validation.
     *
     * @return true, if successful
     * @throws ParallelTaskInvalidException
     *             the parallel task invalid exception
     */
    public boolean validation() throws ParallelTaskInvalidException {

        if (this.commandLine == null) {
            throw new ParallelTaskInvalidException(
                    "commandSshLine is null for ssh");
        }
        if (this.sshLoginType == null) {
            throw new ParallelTaskInvalidException(
                    "sshLoginType is null for ssh");
        }

        if (this.userName == null) {
            throw new ParallelTaskInvalidException("userName is null for ssh. "
                    + "UserName is required for both key/password based login");
        }


        if (this.sshLoginType == SshLoginType.PASSWORD && password == null) {
            throw new ParallelTaskInvalidException(
                    "use password but it is null for ssh");
        }
        if (this.sshLoginType == SshLoginType.KEY
                && this.privKeyRelativePath == null) {
            throw new ParallelTaskInvalidException(
                    "use public key but private key path is null for ssh");
        }

        if (this.sshLoginType == SshLoginType.KEY
                && this.privKeyUsePassphrase && this.passphrase == null) {
            throw new ParallelTaskInvalidException(
                    "use public key and also with private key passphrase but it is null for ssh");
        }

        return true;

    }

    /**
     * Gets the command line.
     *
     * @return the command line
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * Sets the command line.
     *
     * @param commandLine
     *            the new command line
     */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the ssh login type.
     *
     * @return the ssh login type
     */
    public SshLoginType getSshLoginType() {
        return sshLoginType;
    }

    /**
     * Sets the ssh login type.
     *
     * @param sshLoginType
     *            the new ssh login type
     */
    public void setSshLoginType(SshLoginType sshLoginType) {
        this.sshLoginType = sshLoginType;
    }

    /**
     * Gets the ssh port.
     *
     * @return the ssh port
     */
    public int getSshPort() {
        return sshPort;
    }

    /**
     * Sets the ssh port.
     *
     * @param sshPort
     *            the new ssh port
     */
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    /**
     * Gets the priv key relative path.
     *
     * @return the priv key relative path
     */
    public String getPrivKeyRelativePath() {
        return privKeyRelativePath;
    }

    /**
     * Sets the priv key relative path.
     *
     * @param privKeyRelativePath
     *            the new priv key relative path
     */
    public void setPrivKeyRelativePath(String privKeyRelativePath) {
        this.privKeyRelativePath = privKeyRelativePath;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName
     *            the new user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the priv key abs path.
     *
     * @return the priv key abs path
     */
    public String getPrivKeyAbsPath() {

        String workingDir = System.getProperty("user.dir");
        return workingDir + "/" + getPrivKeyRelativePath();
    }

    /**
     * Gets the ssh connection timeout millis.
     *
     * @return the ssh connection timeout millis
     */
    public int getSshConnectionTimeoutMillis() {
        return sshConnectionTimeoutMillis;
    }

    /**
     * Sets the ssh connection timeout millis.
     *
     * @param sshConnectionTimeoutMillis the new ssh connection timeout millis
     */
    public void setSshConnectionTimeoutMillis(int sshConnectionTimeoutMillis) {
        this.sshConnectionTimeoutMillis = sshConnectionTimeoutMillis;
    }

}
