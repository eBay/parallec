package io.parallec.core.bean.ssh;

import io.parallec.core.config.ParallecGlobalConfig;
import io.parallec.core.exception.ParallelTaskInvalidException;

/**
 * Another ssh metadata for jump host.
 *
 * @author Lucien Chan
 *
 */
public class SshJumpHostMeta {
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

    /** The passphrase. */
    private String passphrase;

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
     * Instantiates a new ssh meta.
     *
     * @param userName the user name
     * @param sshPort the ssh port
     * @param sshLoginType the ssh login type
     * @param privKeyRelativePath the priv key relative path
     * @param password the password
     * @param privKeyUsePassphrase the priv key use passphrase
     * @param passphrase the passphrase
     * @param sshConnectionTimeoutMillis the ssh connection timeout millis
     */
    public SshJumpHostMeta(String userName, int sshPort,
                   SshLoginType sshLoginType, String privKeyRelativePath,
                   String password, boolean privKeyUsePassphrase, String passphrase,
                   int sshConnectionTimeoutMillis, boolean runAsSuperUser) {
        super();
        this.userName = userName;
        this.sshPort = sshPort;
        this.sshLoginType = sshLoginType;
        this.privKeyRelativePath = privKeyRelativePath;
        this.password = password;
        this.privKeyUsePassphrase = privKeyUsePassphrase;
        this.passphrase = passphrase;
    }

    /**
     * Instantiates a new ssh meta. timeout using the default one:
     * sshConnectionTimeoutMillis
     */
    public SshJumpHostMeta() {
        this.userName = null;
        this.sshPort = 22;
        this.sshLoginType = null;
        this.privKeyRelativePath = null;
        this.password = null;
        this.privKeyUsePassphrase = false;
        this.passphrase = null;
    };

    /**
     * Validation.
     *
     * @return true, if successful
     * @throws ParallelTaskInvalidException
     *             the parallel task invalid exception
     */
    public boolean validation() throws ParallelTaskInvalidException {
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
        String privKeyAbsPath = workingDir + "/" + getPrivKeyRelativePath();
        return privKeyAbsPath;
    }
}
