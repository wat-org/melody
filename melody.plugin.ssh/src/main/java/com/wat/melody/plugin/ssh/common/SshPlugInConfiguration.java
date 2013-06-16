package com.wat.melody.plugin.ssh.common;

import java.io.File;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.keypair.exception.IllegalKeyPairSizeException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryPathException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.ssh.IKnownHostsRepository;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.exception.KnownHostsException;
import com.wat.melody.common.ssh.exception.KnownHostsRepositoryPathException;
import com.wat.melody.common.ssh.impl.KnownHostsRepository;
import com.wat.melody.common.ssh.impl.KnownHostsRepositoryPath;
import com.wat.melody.common.ssh.impl.SshSessionConfiguration;
import com.wat.melody.common.ssh.types.CompressionLevel;
import com.wat.melody.common.ssh.types.CompressionType;
import com.wat.melody.common.ssh.types.ConnectionTimeout;
import com.wat.melody.common.ssh.types.ProxyType;
import com.wat.melody.common.ssh.types.ReadTimeout;
import com.wat.melody.common.ssh.types.ServerAliveInterval;
import com.wat.melody.common.ssh.types.ServerAliveMaxCount;
import com.wat.melody.common.ssh.types.exception.IllegalCompressionLevelException;
import com.wat.melody.common.ssh.types.exception.IllegalCompressionTypeException;
import com.wat.melody.common.ssh.types.exception.IllegalProxyTypeException;
import com.wat.melody.common.ssh.types.exception.IllegalServerAliveMaxCountException;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.plugin.ssh.common.exception.SshPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshPlugInConfiguration implements IPlugInConfiguration,
		ISshSessionConfiguration {

	public static SshPlugInConfiguration get(IProcessorManager pm)
			throws PlugInConfigurationException {
		return (SshPlugInConfiguration) pm
				.getPluginConfiguration(SshPlugInConfiguration.class);
	}

	private static KeyPairSize createKeyPairSize(int size) {
		try {
			return KeyPairSize.parseInt(size);
		} catch (IllegalKeyPairSizeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a KeyPairSize with value '" + size + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	// CONFIGURATION DIRECTIVES DEFAULT VALUES
	public static final String DEFAULT_KNOWNHOSTS = ".ssh/known_hosts";
	public static final String DEFAULT_KEYPAIR_REPO = ".ssh/";
	public static final KeyPairSize DEFAULT_KEYPAIR_SIZE = createKeyPairSize(2048);
	public static final boolean DEFAULT_MGMT_ENABLE = true;

	// MANDATORY CONFIGURATION DIRECTIVE

	// OPTIONNAL CONFIGURATION DIRECTIVE
	public static final String KNOWN_HOSTS = "ssh.knownhosts";
	public static final String KEYPAIR_REPO = "ssh.keypair.repository";
	public static final String KEYPAIR_SIZE = "ssh.keypair.size";

	public static final String COMPRESSION_TYPE = "ssh.compression.type";
	public static final String COMPRESSION_LEVEL = "ssh.compression.level";

	public static final String CONNECTION_TIMEOUT = "ssh.conn.socket.connect.timeout";
	public static final String READ_TIMEOUT = "ssh.conn.socket.read.timeout";
	public static final String SERVER_ALIVE_MAX_COUNT = "ssh.conn.serveralive.countmax";
	public static final String SERVER_ALIVE_INTERVAL = "ssh.conn.serveralive.interval";

	public static final String PROXY_TYPE = "ssh.conn.proxy.type";
	public static final String PROXY_HOST = "ssh.conn.proxy.host";
	public static final String PROXY_PORT = "ssh.conn.proxy.port";

	public static final String MGMT_ENABLE = "ssh.management.enable";
	public static final String MGMT_LOGIN = "ssh.management.master.user";
	public static final String MGMT_KEYPAIRNAME = "ssh.management.master.key";
	public static final String MGMT_PASSWORD = "ssh.management.master.pass";

	private String _configurationFilePath;
	private KeyPairRepositoryPath _keyPairRepo;
	private KeyPairSize _keyPairSize = DEFAULT_KEYPAIR_SIZE;
	private ISshSessionConfiguration _sshSessionConfiguration;
	private Boolean _mgmtEnable = DEFAULT_MGMT_ENABLE;
	private String _mgmtLogin;
	private KeyPairName _mgmtKeyPairName;
	private String _mgmtPassword;

	public SshPlugInConfiguration() {
		setSshSessionConfiguration(new SshSessionConfiguration());
	}

	@Override
	public String getFilePath() {
		return _configurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an Ssh Plug-In " + "Configuration file path).");
		}
		_configurationFilePath = fp;
	}

	private ISshSessionConfiguration getSshSessionConfiguration() {
		return _sshSessionConfiguration;
	}

	private void setSshSessionConfiguration(ISshSessionConfiguration fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshSessionConfiguration.class.getCanonicalName() + ".");
		}
		_sshSessionConfiguration = fp;
	}

	@Override
	public void load(PropertySet ps) throws PlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		setFilePath(ps.getSourceFile());

		loadKeyPairRepo(ps);
		loadKeyPairSize(ps);

		loadKnownHosts(ps);
		loadCompressionLevel(ps);
		loadCompressionType(ps);
		loadConnectionTimeout(ps);
		loadReadTimeout(ps);
		loadServerAliveCountMax(ps);
		loadServerAliveInterval(ps);
		loadProxyType(ps);
		loadProxyHost(ps);
		loadProxyPort(ps);

		loadMgmtEnable(ps);
		loadMgmtMasterUser(ps);
		loadMgmtMasterKey(ps);
		loadMgmtMasterPass(ps);

		validate();
	}

	private void loadKeyPairRepo(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KEYPAIR_REPO)) {
			return;
		}
		try {
			setKeyPairRepositoryPath(ps.get(KEYPAIR_REPO));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_REPO), Ex);
		}
	}

	private void loadKeyPairSize(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KEYPAIR_SIZE)) {
			return;
		}
		try {
			setKeyPairSize(ps.get(KEYPAIR_SIZE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_SIZE), Ex);
		}
	}

	private void loadKnownHosts(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KNOWN_HOSTS)) {
			return;
		}
		try {
			setKnownHosts(ps.get(KNOWN_HOSTS));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KNOWN_HOSTS), Ex);
		}
	}

	private void loadCompressionLevel(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(COMPRESSION_LEVEL)) {
			return;
		}
		try {
			setCompressionLevel(ps.get(COMPRESSION_LEVEL));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_LEVEL), Ex);
		}
	}

	private void loadCompressionType(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(COMPRESSION_TYPE)) {
			return;
		}
		try {
			setCompressionType(ps.get(COMPRESSION_TYPE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_TYPE), Ex);
		}
	}

	private void loadConnectionTimeout(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(CONNECTION_TIMEOUT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, CONNECTION_TIMEOUT), Ex);
		}
	}

	private void loadReadTimeout(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(READ_TIMEOUT)) {
			return;
		}
		try {
			setReadTimeout(ps.get(READ_TIMEOUT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, READ_TIMEOUT), Ex);
		}
	}

	private void loadServerAliveCountMax(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_MAX_COUNT)) {
			return;
		}
		try {
			setServerAliveCountMax(ps.get(SERVER_ALIVE_MAX_COUNT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_MAX_COUNT),
					Ex);
		}
	}

	private void loadServerAliveInterval(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_INTERVAL)) {
			return;
		}
		try {
			setServerAliveInterval(ps.get(SERVER_ALIVE_INTERVAL));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_INTERVAL),
					Ex);
		}
	}

	private void loadProxyType(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_TYPE)) {
			return;
		}
		try {
			setProxyType(ps.get(PROXY_TYPE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_TYPE), Ex);
		}
	}

	private void loadProxyHost(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_HOST)) {
			return;
		}
		try {
			setProxyHost(ps.get(PROXY_HOST));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_HOST), Ex);
		}
	}

	private void loadProxyPort(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_PORT)) {
			return;
		}
		try {
			setProxyPort(ps.get(PROXY_PORT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_PORT), Ex);
		}
	}

	private void loadMgmtEnable(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_ENABLE)) {
			return;
		}
		try {
			setMgmtEnable(ps.get(MGMT_ENABLE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_ENABLE), Ex);
		}
	}

	private void loadMgmtMasterUser(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_LOGIN)) {
			return;
		}
		try {
			setManagementLogin(ps.get(MGMT_LOGIN));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_LOGIN), Ex);
		}
	}

	private void loadMgmtMasterKey(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_KEYPAIRNAME)) {
			return;
		}
		try {
			setManagementKeyPairName(ps.get(MGMT_KEYPAIRNAME));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_KEYPAIRNAME), Ex);
		}
	}

	private void loadMgmtMasterPass(PropertySet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_PASSWORD)) {
			return;
		}
		try {
			setManagementPassword(ps.get(MGMT_PASSWORD));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_PASSWORD), Ex);
		}
	}

	private void validate() throws SshPlugInConfigurationException {
		/*
		 * TODO : try to connect to the proxy, if set.
		 */
		if (getKnownHosts() == null) {
			setKnownHosts(DEFAULT_KNOWNHOSTS);
		}
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(DEFAULT_KEYPAIR_REPO);
		}
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepo;
	}

	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepoPath) {
		if (keyPairRepoPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepositoryPath.class.getCanonicalName()
					+ " (the default KeyPair Repository Path).");
		}
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepo = keyPairRepoPath;
		return previous;
	}

	public KeyPairRepositoryPath setKeyPairRepositoryPath(File keyPairRepoPath)
			throws SshPlugInConfigurationException {
		if (keyPairRepoPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + File.class.getCanonicalName()
					+ " (the default KeyPair Repository Path).");
		}
		if (!keyPairRepoPath.isAbsolute()) {
			// Resolve from this configuration File's parent location
			keyPairRepoPath = new File(new File(getFilePath()).getParent(),
					keyPairRepoPath.getPath());
		}
		try {
			return setKeyPairRepositoryPath(new KeyPairRepositoryPath(
					keyPairRepoPath));
		} catch (KeyPairRepositoryPathException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public KeyPairRepositoryPath setKeyPairRepositoryPath(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the default KeyPair Repository Path).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKeyPairRepositoryPath(new File(val));
	}

	public KeyPairSize getKeyPairSize() {
		return _keyPairSize;
	}

	public KeyPairSize setKeyPairSize(KeyPairSize keyPairSize) {
		if (keyPairSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairSize.class.getCanonicalName()
					+ ".");
		}
		KeyPairSize previous = getKeyPairSize();
		_keyPairSize = keyPairSize;
		return previous;
	}

	public KeyPairSize setKeyPairSize(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the default KeyPair Size).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setKeyPairSize(KeyPairSize.parseString(val));
		} catch (IllegalKeyPairSizeException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public IKnownHostsRepository getKnownHosts() {
		return getSshSessionConfiguration().getKnownHosts();
	}

	@Override
	public IKnownHostsRepository setKnownHosts(IKnownHostsRepository knownHosts) {
		return getSshSessionConfiguration().setKnownHosts(knownHosts);

	}

	public IKnownHostsRepository setKnownHosts(
			KnownHostsRepositoryPath keyPairRepoPath)
			throws KnownHostsException {
		if (keyPairRepoPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KnownHostsRepositoryPath.class.getCanonicalName() + ".");
		}
		return setKnownHosts(KnownHostsRepository
				.getKnownHostsRepository(keyPairRepoPath));
	}

	public IKnownHostsRepository setKnownHosts(File knownHosts)
			throws SshPlugInConfigurationException {
		if (knownHosts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + File.class.getCanonicalName()
					+ " (the KnownHosts Repository Path).");
		}
		if (!knownHosts.isAbsolute()) {
			// Resolve from this configuration File's parent location
			knownHosts = new File(new File(getFilePath()).getParent(),
					knownHosts.getPath());
		}
		try {
			return setKnownHosts(new KnownHostsRepositoryPath(
					knownHosts.getPath()));
		} catch (KnownHostsRepositoryPathException | KnownHostsException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public IKnownHostsRepository setKnownHosts(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the KnownHosts Repository Path).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKnownHosts(new File(val));
	}

	@Override
	public CompressionLevel getCompressionLevel() {
		return getSshSessionConfiguration().getCompressionLevel();
	}

	@Override
	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel) {
		return getSshSessionConfiguration().setCompressionLevel(
				compressionLevel);
	}

	public CompressionLevel setCompressionLevel(String val)
			throws SshPlugInConfigurationException {
		try {
			return setCompressionLevel(CompressionLevel.parseString(val));
		} catch (IllegalCompressionLevelException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public CompressionType getCompressionType() {
		return getSshSessionConfiguration().getCompressionType();
	}

	@Override
	public CompressionType setCompressionType(CompressionType compressionType) {
		return getSshSessionConfiguration().setCompressionType(compressionType);
	}

	public CompressionType setCompressionType(String val)
			throws SshPlugInConfigurationException {
		try {
			return setCompressionType(CompressionType.parseString(val));
		} catch (IllegalCompressionTypeException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public Timeout getConnectionTimeout() {
		return getSshSessionConfiguration().getConnectionTimeout();
	}

	@Override
	public Timeout setConnectionTimeout(ConnectionTimeout ival) {
		return getSshSessionConfiguration().setConnectionTimeout(ival);
	}

	public Timeout setConnectionTimeout(String val)
			throws SshPlugInConfigurationException {
		try {
			return setConnectionTimeout(ConnectionTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public Timeout getReadTimeout() {
		return getSshSessionConfiguration().getReadTimeout();
	}

	@Override
	public Timeout setReadTimeout(ReadTimeout ival) {
		return getSshSessionConfiguration().setReadTimeout(ival);
	}

	public Timeout setReadTimeout(String val)
			throws SshPlugInConfigurationException {
		try {
			return setReadTimeout(ReadTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public ServerAliveMaxCount getServerAliveMaxCount() {
		return getSshSessionConfiguration().getServerAliveMaxCount();
	}

	@Override
	public ServerAliveMaxCount setServerAliveMaxCount(ServerAliveMaxCount ival) {
		return getSshSessionConfiguration().setServerAliveMaxCount(ival);
	}

	public ServerAliveMaxCount setServerAliveCountMax(String val)
			throws SshPlugInConfigurationException {
		try {
			return setServerAliveMaxCount(ServerAliveMaxCount.parseString(val));
		} catch (IllegalServerAliveMaxCountException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public Timeout getServerAliveInterval() {
		return getSshSessionConfiguration().getServerAliveInterval();
	}

	@Override
	public Timeout setServerAliveInterval(ServerAliveInterval ival) {
		return getSshSessionConfiguration().setServerAliveInterval(ival);
	}

	public Timeout setServerAliveInterval(String val)
			throws SshPlugInConfigurationException {
		try {
			return setServerAliveInterval(ServerAliveInterval.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public ProxyType getProxyType() {
		return getSshSessionConfiguration().getProxyType();
	}

	@Override
	public ProxyType setProxyType(ProxyType val) {
		return getSshSessionConfiguration().setProxyType(val);
	}

	public ProxyType setProxyType(String val)
			throws SshPlugInConfigurationException {
		try {
			return setProxyType(ProxyType.parseString(val));
		} catch (IllegalProxyTypeException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public Host getProxyHost() {
		return getSshSessionConfiguration().getProxyHost();
	}

	@Override
	public Host setProxyHost(Host val) {
		return getSshSessionConfiguration().setProxyHost(val);
	}

	public Host setProxyHost(String val) throws SshPlugInConfigurationException {
		try {
			return setProxyHost(Host.parseString(val));
		} catch (IllegalHostException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	@Override
	public Port getProxyPort() {
		return getSshSessionConfiguration().getProxyPort();
	}

	@Override
	public Port setProxyPort(Port port) {
		return getSshSessionConfiguration().setProxyPort(port);
	}

	public Port setProxyPort(String val) throws SshPlugInConfigurationException {
		try {
			return setProxyPort(Port.parseString(val));
		} catch (IllegalPortException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public boolean getMgmtEnable() {
		return _mgmtEnable;
	}

	public boolean setMgmtEnable(boolean val) {
		boolean previous = getMgmtEnable();
		_mgmtEnable = val;
		return previous;
	}

	public boolean setMgmtEnable(String val)
			throws SshPlugInConfigurationException {
		try {
			return setMgmtEnable(Bool.parseString(val));
		} catch (IllegalBooleanException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	/**
	 * @return the ssh management master user. Cannot be null.
	 */
	public String getManagementLogin() {
		return _mgmtLogin;
	}

	public String setManagementLogin(String mgmtMasterUser)
			throws SshPlugInConfigurationException {
		if (mgmtMasterUser == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the management user's login).");
		}
		if (mgmtMasterUser.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getManagementLogin();
		_mgmtLogin = mgmtMasterUser;
		return previous;
	}

	/**
	 * @return the keypair name of the ssh management master user. Can be null,
	 *         when the connection as ssh management master user should be done
	 *         without keypair.
	 */
	public KeyPairName getManagementKeyPairName() {
		return _mgmtKeyPairName;
	}

	public KeyPairName setMgmtMasterKey(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ " (the management user's keypair name).");
		}
		KeyPairName previous = getManagementKeyPairName();
		_mgmtKeyPairName = keyPairName;
		return previous;
	}

	public KeyPairName setManagementKeyPairName(String val)
			throws SshPlugInConfigurationException {
		try {
			return setMgmtMasterKey(KeyPairName.parseString(val));
		} catch (IllegalKeyPairNameException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	/**
	 * @return the password of the ssh management master user, or the password
	 *         of the keypair of the ssh management master user. Can be null, if
	 *         the keypair of the ssh management master user is defined and if
	 *         this key don't have a passphrase.
	 */
	public String getManagementPassword() {
		return _mgmtPassword;
	}

	public String setManagementPassword(String mgmtMasterPass)
			throws SshPlugInConfigurationException {
		if (mgmtMasterPass == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the management user's password or keypair "
					+ " passphrase).");
		}
		if (mgmtMasterPass.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getManagementLogin();
		_mgmtPassword = mgmtMasterPass;
		return previous;
	}

}