package com.wat.melody.plugin.ssh.common;

import java.io.IOException;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.impl.LoggerOutputStream;
import com.wat.melody.common.ssh.impl.SshConnectionDatas;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.ssh.impl.SshUserDatas;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractSshOperation implements ITask {

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
	 * XML attribute in the SD which define the remote account to connect to
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * XML attribute in the SD which is either the password of the remote
	 * account, or the password of the keypair.
	 */
	public static final String PASS_ATTR = "password";

	/**
	 * XML attribute in the SD which define the path of the keypair repository
	 * which contains the keypair used to connect to the remote system.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * XML attribute in the SD which define the name of the keypair to use to
	 * connect to the remote system; The keypair will be found/created in the
	 * keypair repositoy.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	private ITaskContext moContext;
	private SshPlugInConfiguration moPluginConf;
	private ISshUserDatas moUserDatas;
	private ISshConnectionDatas moCnxDatas;

	public AbstractSshOperation() {
		initContext();
		initPluginConf();
		setUserDatas(new SshUserDatas());
		setConnectionDatas(new SshConnectionDatas());
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private ISshUserDatas getUserDatas() {
		return moUserDatas;
	}

	private ISshUserDatas setUserDatas(ISshUserDatas ud) {
		ISshUserDatas previous = getUserDatas();
		moUserDatas = ud;
		return previous;
	}

	private ISshConnectionDatas getConnectionDatas() {
		return moCnxDatas;
	}

	private ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd) {
		ISshConnectionDatas previous = getConnectionDatas();
		moCnxDatas = cd;
		return previous;
	}

	@Override
	public void validate() throws SshException {
		if (getPassword() == null && getKeyPairName() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_PASSWORD_OR_PK_ATTR, PASS_ATTR,
					KEYPAIR_NAME_ATTR));
		}
		if (getKeyPairName() == null) {
			return;
		}
		if (getKeyPairRepository() == null) {
			setKeyPairRepository(getPluginConf().getKeyPairRepositoryPath());
		}
		KeyPairRepositoryPath kprp = getKeyPairRepository();
		KeyPairRepository kpr = KeyPairRepository.getKeyPairRepository(kprp);
		if (!kpr.containsKeyPair(getKeyPairName())) {
			try {
				kpr.createKeyPair(getKeyPairName(), getPluginConf()
						.getKeyPairSize(), getPassword());
			} catch (IOException Ex) {
				throw new SshException(Ex);
			}
		}
	}

	/**
	 * <p>
	 * Open a ssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * @throws InterruptedException
	 * @throws SshSessionException
	 * 
	 * @throws SshException
	 */
	public ISshSession openSession() throws SshException, InterruptedException {
		ISshSession session = createSession();
		try {
			session.connect();
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		}
		return session;
	}

	/**
	 * Can be override by subclasses to provide another ISshSession.
	 */
	protected ISshSession createSession() {
		ISshSession session = new SshSession();
		session.setUserDatas(getUserDatas());
		session.setConnectionDatas(getConnectionDatas());
		session.setSessionConfiguration(getPluginConf());
		return session;
	}

	public int execSshCommand(String sCommand, boolean requiretty,
			String outputPrefix) throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			LoggerOutputStream out = new LoggerOutputStream(outputPrefix
					+ " [STDOUT]", LogThreshold.DEBUG);
			LoggerOutputStream err = new LoggerOutputStream(outputPrefix
					+ " [STDERR]", LogThreshold.ERROR);
			return session.execRemoteCommand(sCommand, requiretty, out, err);
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
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

	public Host getHost() {
		return getConnectionDatas().getHost();
	}

	@Attribute(name = HOST_ATTR, mandatory = true)
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		return getConnectionDatas().setHost(host);
	}

	public Port getPort() {
		return getConnectionDatas().getPort();
	}

	@Attribute(name = PORT_ATTR)
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		return getConnectionDatas().setPort(port);
	}

	public String getLogin() {
		return getUserDatas().getLogin();
	}

	public boolean getTrust() {
		return getConnectionDatas().getTrust();
	}

	@Attribute(name = TRUST_ATTR)
	public boolean setTrust(boolean b) {
		return getConnectionDatas().setTrust(b);
	}

	@Attribute(name = LOGIN_ATTR, mandatory = true)
	public String setLogin(String sLogin) {
		if (sLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a login).");
		}
		return getUserDatas().setLogin(sLogin);
	}

	public String getPassword() {
		return getUserDatas().getPassword();
	}

	@Attribute(name = PASS_ATTR)
	public String setPassword(String sPassword) {
		return getUserDatas().setPassword(sPassword);
	}

	public KeyPairRepositoryPath getKeyPairRepository() {
		return getUserDatas().getKeyPairRepositoryPath();
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepository(
			KeyPairRepositoryPath keyPairRepository) {
		return getUserDatas().setKeyPairRepositoryPath(keyPairRepository);
	}

	public KeyPairName getKeyPairName() {
		return getUserDatas().getKeyPairName();
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		return getUserDatas().setKeyPairName(keyPairName);
	}

}