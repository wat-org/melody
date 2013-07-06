package com.wat.melody.plugin.ssh.common;

import java.io.IOException;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.common.messages.Msg;
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
	 * Defines the remote system ip or fqdn.
	 */
	public static final String HOST_ATTR = "host";

	/**
	 * Defines the remote system port (e.g. the port of ssh daemon on the remote
	 * system).
	 */
	public static final String PORT_ATTR = "port";

	/**
	 * Defines how the remote system to connect to should be trusted.
	 * <ul>
	 * <li>'false' means that the remote system must have been previously
	 * registered in the knownhosts file in order to connect to ;</li>
	 * <li>'true' means that the remote system to connect to will be
	 * automatically trusted, even if it is not registered in the knownhosts
	 * file ;</li>
	 * </ul>
	 */
	public static final String TRUST_ATTR = "trust";

	/**
	 * Defines the user to connect with on the remote system.
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * Defines the password of the user used to connect to the remote system. If
	 * a keypair is provide, defines the keypair's passphrase.
	 */
	public static final String PASS_ATTR = "password";

	/**
	 * Defines the path of the keypair repository which contains the keypair
	 * used to connect to the remote system.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * Defines the name of the keypair - relative to the keypair-repository - to
	 * use to connect to the remote system. If the keypair-repository doesn't
	 * contains such keypair, it will be automatically created. If a passphrase
	 * was provided, the keypair will be encrypted with it.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	private ISshUserDatas _userDatas;
	private ISshConnectionDatas _cnxDatas;

	public AbstractSshOperation() {
		setUserDatas(new SshUserDatas());
		setConnectionDatas(new SshConnectionDatas());
	}

	private ISshUserDatas getUserDatas() {
		return _userDatas;
	}

	private ISshUserDatas setUserDatas(ISshUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshUserDatas.class.getCanonicalName() + ".");
		}
		ISshUserDatas previous = getUserDatas();
		_userDatas = ud;
		return previous;
	}

	private ISshConnectionDatas getConnectionDatas() {
		return _cnxDatas;
	}

	private ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd) {
		if (cd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshConnectionDatas.class.getCanonicalName() + ".");
		}
		ISshConnectionDatas previous = getConnectionDatas();
		_cnxDatas = cd;
		return previous;
	}

	@Override
	public void validate() throws SshException {
		if (getPassword() == null && getKeyPairName() == null) {
			throw new SshException(Msg.bind(
					Messages.BaseEx_MISSING_PASSWORD_OR_PK_ATTR, PASS_ATTR,
					KEYPAIR_NAME_ATTR));
		}
		if (getKeyPairName() == null) {
			return;
		}
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(getSshPlugInConf()
					.getKeyPairRepositoryPath());
		}
		KeyPairRepositoryPath kprp = getKeyPairRepositoryPath();
		KeyPairRepository kpr = KeyPairRepository.getKeyPairRepository(kprp);
		try {
			kpr.createKeyPair(getKeyPairName(), getSshPlugInConf()
					.getKeyPairSize(), getPassword());
		} catch (IllegalPassphraseException Ex) {
			if (getPassword() == null) {
				throw new SshException(Msg.bind(
						Messages.BaseEx_MISSING_PASSPHRASE_ATTR,
						getKeyPairName(), PASS_ATTR));
			} else {
				throw new SshException(Msg.bind(
						Messages.BaseEx_INVALID_PASSPHRASE_ATTR,
						getKeyPairName(), PASS_ATTR));
			}
		} catch (IOException Ex) {
			throw new SshException(Ex);
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
	 * @throws InterruptedException
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
	 * Can be override by subclasses to enhance behavior of {@link ISshSession}.
	 */
	protected ISshSession createSession() throws SshException {
		ISshSession session;
		session = new SshSession(getUserDatas(), getConnectionDatas());
		session.setSessionConfiguration(getSshPlugInConf());
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

	protected SshPlugInConfiguration getSshPlugInConf() throws SshException {
		try {
			return SshPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new SshException(Ex);
		}
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

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return getUserDatas().getKeyPairRepositoryPath();
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
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