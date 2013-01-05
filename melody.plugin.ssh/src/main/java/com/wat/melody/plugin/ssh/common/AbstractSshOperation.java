package com.wat.melody.plugin.ssh.common;

import java.io.IOException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.utils.LogThreshold;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.jsch.JSchHelper;
import com.wat.melody.plugin.ssh.common.jsch.LoggerOutputStream;
import com.wat.melody.plugin.ssh.common.jsch.SshConnectionDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractSshOperation implements ITask, SshConnectionDatas {

	/**
	 * XML attribute in the SD which define the remote account to connect to
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * XML attribute in the SD which is either the password of the remote
	 * account, or the password of the keypair.
	 */
	public static final String PASS_ATTR = "password";

	/**
	 * XML attribute in the SD which define the ip of the remote system to
	 * connect to
	 */
	public static final String HOST_ATTR = "host";

	/**
	 * XML attribute in the SD which define the port of the ssh daemon on the
	 * remote system to connect to
	 */
	public static final String PORT_ATTR = "port";

	/**
	 * XML attribute in the SD which define how the remote system to connect to
	 * should be trusted. 'false' means that the remote system must have been
	 * previously registered in the knownhosts file in order to connect to.
	 * 'true' means that the remote system to connect to will be automatically
	 * trusted, even if it is not registered in the knownhosts file.
	 */
	public static final String TRUST_ATTR = "trust";

	/**
	 * XML attribute in the SD which define the path of the keypair repository
	 * which contains the keypair use to connect to the remote system.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keyPairRepository";

	/**
	 * XML attribute in the SD which define the name of the keypair to use to
	 * connect to the remote system; The keypair will be found/created in the
	 * keypair repositoy.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";

	private ITaskContext moContext;
	private SshPlugInConfiguration moPluginConf;
	private String msLogin;
	private String msPassword;
	private Host moHost;
	private Port moPort;
	private Boolean mbTrust;
	private KeyPairRepository moKeyPairRepository;
	private KeyPairName moKeyPairName;

	public AbstractSshOperation() {
		initContext();
		initPluginConf();
		initKeyPairRepository();
		initKeyPairName();
		initLogin();
		initPassword();
		initHost();
		setPort(Port.SSH);
		initTrust();
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private void initKeyPairRepository() {
		moKeyPairRepository = null;
	}

	private void initKeyPairName() {
		moKeyPairName = null;
	}

	private void initLogin() {
		msLogin = null;
	}

	private void initPassword() {
		msPassword = null;
	}

	private void initHost() {
		moHost = null;
	}

	private void initTrust() {
		mbTrust = false;
	}

	@Override
	public void validate() throws SshException {
		if (getKeyPairName() == null) {
			return;
		}
		if (getKeyPairRepository() == null) {
			setKeyPairRepository(getPluginConf().getKeyPairRepo());
		}
		KeyPairRepository kpr = getKeyPairRepository();
		try {
			if (!kpr.containsKeyPair(getKeyPairName())) {
				kpr.createKeyPair(getKeyPairName(), getPluginConf()
						.getKeyPairSize(), getPassphrase());
			}
		} catch (IOException Ex) {
			throw new RuntimeException(Ex);
		}
		try {
			getPluginConf()
					.addIdentity(kpr.getPrivateKeyFile(getKeyPairName()));
		} catch (JSchException Ex) {
			throw new SshException(Ex);
		}

		if (getPassword() == null && getKeyPairName() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_PASSWORD_OR_PK_ATTR, PASS_ATTR,
					KEYPAIR_NAME_ATTR));
		}
	}

	/**
	 * <p>
	 * Open a ssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * 
	 * @throws SshException
	 */
	public Session openSession() throws SshException {
		return JSchHelper.openSession(this, getPluginConf());
	}

	/**
	 * <p>
	 * Open a sftp channel.
	 * </p>
	 * 
	 * @return the opened sftp channel.
	 * 
	 * @throws SshException
	 */
	public ChannelSftp openSftpChannel(Session session) throws SshException {
		return JSchHelper.openSftpChannel(session, getPluginConf());
	}

	public int execSshCommand(Session session, String sCommand,
			String outputPrefix) throws SshException, InterruptedException {
		LoggerOutputStream out = new LoggerOutputStream(outputPrefix
				+ " [STDOUT]", LogThreshold.DEBUG);
		LoggerOutputStream err = new LoggerOutputStream(outputPrefix
				+ " [STDERR]", LogThreshold.ERROR);
		return JSchHelper.execSshCommand(session, sCommand, out, err);
	}

	public int execSshCommand(String sCommand, String outputPrefix)
			throws SshException, InterruptedException {
		Session session = openSession();
		try {
			return execSshCommand(session, sCommand, outputPrefix);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	@Override
	public String toString() {
		return "{ host:" + getHost() + ", port:" + getPort() + ", user:"
				+ getLogin() + ", password:" + getPassword() + ", keypairname:"
				+ getKeyPairName() + " }";
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	/**
	 * <p>
	 * Set the {@link ITaskContext} of this object with the given
	 * {@link ITaskContext} and retrieve the Ssh Plug-In
	 * {@link SshPlugInConfiguration}.
	 * </p>
	 * 
	 * @param p
	 *            is the {@link ITaskContext} to set.
	 * 
	 * @throws SshException
	 *             if an error occurred while retrieving the Ssh Plug-In
	 *             {@link SshPlugInConfiguration}.
	 * @throws IllegalArgumentException
	 *             if the given {@link ITaskContext} is <tt>null</tt>.
	 */
	@Override
	public void setContext(ITaskContext p) throws SshException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(SshPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new SshException(Ex);
		}

	}

	protected SshPlugInConfiguration getPluginConf() {
		return moPluginConf;
	}

	public SshPlugInConfiguration setPluginConf(SshPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		SshPlugInConfiguration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	public String getLogin() {
		return msLogin;
	}

	@Attribute(name = LOGIN_ATTR, mandatory = true)
	public String setLogin(String sLogin) throws SshException {
		if (sLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (sLogin.trim().length() == 0) {
			throw new SshException(Messages.bind(
					Messages.SshEx_EMPTY_LOGIN_ATTR, sLogin));
		}
		String previous = getLogin();
		msLogin = sLogin;
		return previous;
	}

	public Host getHost() {
		return moHost;
	}

	@Attribute(name = HOST_ATTR, mandatory = true)
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Host previous = getHost();
		moHost = host;
		return previous;
	}

	public Port getPort() {
		return moPort;
	}

	@Attribute(name = PORT_ATTR)
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Port previous = getPort();
		moPort = port;
		return previous;
	}

	@Override
	public String getPassword() {
		return msPassword;
	}

	@Attribute(name = PASS_ATTR)
	public String setPassword(String sPassword) {
		if (sPassword == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassword();
		msPassword = sPassword;
		return previous;
	}

	public boolean getTrust() {
		return mbTrust;
	}

	@Attribute(name = TRUST_ATTR)
	public boolean setTrust(boolean b) {
		boolean previous = getTrust();
		mbTrust = b;
		return previous;
	}

	@Override
	public KeyPairRepository getKeyPairRepository() {
		return moKeyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepository setKeyPairRepository(
			KeyPairRepository keyPairRepository) {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		KeyPairRepository previous = getKeyPairRepository();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	@Override
	public KeyPairName getKeyPairName() {
		return moKeyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		KeyPairName previous = getKeyPairName();
		moKeyPairName = keyPairName;
		return previous;
	}

	@Override
	public String getPassphrase() {
		return msPassword;
	}

	/**
	 * @param message
	 *            ignored
	 * 
	 * @return true always
	 */
	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	/**
	 * @param passwordPrompt
	 *            ignored
	 * 
	 * @return true always
	 */
	@Override
	public boolean promptPassword(String passwordPrompt) {
		return true;
	}

	/**
	 * @param message
	 *            ignored
	 * 
	 * @return the value of trustAllCertificates
	 */
	@Override
	public boolean promptYesNo(String message) {
		return getTrust();
	}

	/**
	 * @param message
	 *            ignored
	 */
	@Override
	public void showMessage(String message) {
	}

}
