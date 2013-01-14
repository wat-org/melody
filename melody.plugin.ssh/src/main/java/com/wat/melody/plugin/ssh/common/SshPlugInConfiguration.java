package com.wat.melody.plugin.ssh.common;

import java.io.File;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.ssh.CompressionLevel;
import com.wat.melody.common.ssh.CompressionType;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.KnownHostsFile;
import com.wat.melody.common.ssh.ProxyType;
import com.wat.melody.common.ssh.exception.IllegalCompressionLevelException;
import com.wat.melody.common.ssh.exception.IllegalCompressionTypeException;
import com.wat.melody.common.ssh.exception.IllegalProxyTypeException;
import com.wat.melody.common.ssh.exception.IllegalSshSessionConfigurationException;
import com.wat.melody.common.ssh.exception.KnownHostsFileException;
import com.wat.melody.common.ssh.impl.SshSessionConfiguration;
import com.wat.melody.common.utils.GenericTimeout;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Timeout;
import com.wat.melody.common.utils.exception.IllegalTimeoutException;
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

	// CONFIGURATION DIRECTIVES DEFAULT VALUES
	public final String DEFAULT_KNOWNHOSTS = ".ssh/known_hosts";
	public final String DEFAULT_KEYPAIR_REPO = ".ssh/";

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

	private String msConfigurationFilePath;
	private KeyPairRepository moKeyPairRepo;
	private int miKeyPairSize = 2048;
	private ISshSessionConfiguration moSshSessionConfiguration;
	private Boolean mbMgmtEnable = true;
	private String moMgmtLogin = "root";
	private KeyPairName moMgmtKeyPairName;
	private String moMgmtPassword;

	public SshPlugInConfiguration() {
		setSshSessionConfiguration(new SshSessionConfiguration());
	}

	@Override
	public String getFilePath() {
		return msConfigurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS EC2 Plug-In "
					+ "Configuration file path).");
		}
		msConfigurationFilePath = fp;
	}

	private ISshSessionConfiguration getSshSessionConfiguration() {
		return moSshSessionConfiguration;
	}

	private void setSshSessionConfiguration(ISshSessionConfiguration fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshSessionConfiguration.class.getCanonicalName() + ".");
		}
		moSshSessionConfiguration = fp;
	}

	@Override
	public void load(PropertiesSet ps) throws PlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		setFilePath(ps.getFilePath());

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

	private void loadKeyPairRepo(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KEYPAIR_REPO)) {
			return;
		}
		try {
			setKeyPairRepo(ps.get(KEYPAIR_REPO));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_REPO), Ex);
		}
	}

	private void loadKeyPairSize(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KEYPAIR_SIZE)) {
			return;
		}
		try {
			setKeyPairSize(ps.get(KEYPAIR_SIZE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_SIZE), Ex);
		}
	}

	private void loadKnownHosts(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(KNOWN_HOSTS)) {
			return;
		}
		try {
			setKnownHosts(ps.get(KNOWN_HOSTS));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KNOWN_HOSTS), Ex);
		}
	}

	private void loadCompressionLevel(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(COMPRESSION_LEVEL)) {
			return;
		}
		try {
			setCompressionLevel(ps.get(COMPRESSION_LEVEL));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_LEVEL), Ex);
		}
	}

	private void loadCompressionType(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(COMPRESSION_TYPE)) {
			return;
		}
		try {
			setCompressionType(ps.get(COMPRESSION_TYPE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_TYPE), Ex);
		}
	}

	private void loadConnectionTimeout(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(CONNECTION_TIMEOUT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, CONNECTION_TIMEOUT), Ex);
		}
	}

	private void loadReadTimeout(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(READ_TIMEOUT)) {
			return;
		}
		try {
			setReadTimeout(ps.get(READ_TIMEOUT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, READ_TIMEOUT), Ex);
		}
	}

	private void loadServerAliveCountMax(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_MAX_COUNT)) {
			return;
		}
		try {
			setServerAliveCountMax(ps.get(SERVER_ALIVE_MAX_COUNT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_MAX_COUNT),
					Ex);
		}
	}

	private void loadServerAliveInterval(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_INTERVAL)) {
			return;
		}
		try {
			setServerAliveInterval(ps.get(SERVER_ALIVE_INTERVAL));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_INTERVAL),
					Ex);
		}
	}

	private void loadProxyType(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_TYPE)) {
			return;
		}
		try {
			setProxyType(ps.get(PROXY_TYPE));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_TYPE), Ex);
		}
	}

	private void loadProxyHost(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_HOST)) {
			return;
		}
		try {
			setProxyHost(ps.get(PROXY_HOST));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_HOST), Ex);
		}
	}

	private void loadProxyPort(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(PROXY_PORT)) {
			return;
		}
		try {
			setProxyPort(ps.get(PROXY_PORT));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_PORT), Ex);
		}
	}

	private void loadMgmtEnable(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_ENABLE)) {
			return;
		}
		setMgmtEnable(ps.get(MGMT_ENABLE));
	}

	private void loadMgmtMasterUser(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_LOGIN)) {
			return;
		}
		try {
			setManagementLogin(ps.get(MGMT_LOGIN));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_LOGIN), Ex);
		}
	}

	private void loadMgmtMasterKey(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_KEYPAIRNAME)) {
			return;
		}
		try {
			setManagementKeyPairName(ps.get(MGMT_KEYPAIRNAME));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_KEYPAIRNAME), Ex);
		}
	}

	private void loadMgmtMasterPass(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_PASSWORD)) {
			return;
		}
		try {
			setManagementPassword(ps.get(MGMT_PASSWORD));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
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
		if (getKeyPairRepo() == null) {
			setKeyPairRepo(DEFAULT_KEYPAIR_REPO);
		}
	}

	public KeyPairRepository getKeyPairRepo() {
		return moKeyPairRepo;
	}

	public KeyPairRepository setKeyPairRepo(KeyPairRepository keyPairRepo) {
		if (keyPairRepo == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Directory (a KeyPair Repo path).");
		}
		KeyPairRepository previous = getKeyPairRepo();
		moKeyPairRepo = keyPairRepo;
		return previous;
	}

	public KeyPairRepository setKeyPairRepo(File keyPairRepo)
			throws SshPlugInConfigurationException {
		if (keyPairRepo == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Directory (a KeyPair Repo path).");
		}
		if (!keyPairRepo.isAbsolute()) {
			// Resolve from this configuration File's parent location
			keyPairRepo = new File(new File(getFilePath()).getParent(),
					keyPairRepo.getPath());
		}
		try {
			return setKeyPairRepo(new KeyPairRepository(keyPairRepo));
		} catch (KeyPairRepositoryException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public File setKeyPairRepo(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Directory (a KeyPair Repo path).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKeyPairRepo(new File(val));
	}

	public int getKeyPairSize() {
		return miKeyPairSize;
	}

	public int setKeyPairSize(int ival) throws SshPlugInConfigurationException {
		if (ival < 1024) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KEYPAIR_SIZE, ival));
		}
		int previous = getKeyPairSize();
		miKeyPairSize = ival;
		return previous;
	}

	public int setKeyPairSize(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer > 1024 (a KeyPair Size).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setKeyPairSize(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KEYPAIR_SIZE, val));
		}
	}

	public KnownHostsFile getKnownHosts() {
		return getSshSessionConfiguration().getKnownHosts();
	}

	public KnownHostsFile setKnownHosts(KnownHostsFile knownHosts) {
		return getSshSessionConfiguration().setKnownHosts(knownHosts);

	}

	public KnownHostsFile setKnownHosts(File knownHosts)
			throws SshPlugInConfigurationException {
		if (knownHosts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a KnownHosts File).");
		}
		if (!knownHosts.isAbsolute()) {
			// Resolve from this configuration File's parent location
			knownHosts = new File(new File(getFilePath()).getParent(),
					knownHosts.getPath());
		}
		try {
			return setKnownHosts(new KnownHostsFile(knownHosts.getPath()));
		} catch (KnownHostsFileException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KNOWNHOSTS, knownHosts), Ex);
		}
	}

	public KnownHostsFile setKnownHosts(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a KnownHosts File path).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKnownHosts(new File(val));
	}

	public CompressionLevel getCompressionLevel() {
		return getSshSessionConfiguration().getCompressionLevel();
	}

	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel) {
		return getSshSessionConfiguration().setCompressionLevel(
				compressionLevel);
	}

	public CompressionLevel setCompressionLevel(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an "
					+ "Integer >=0 and <=6 (a CompressionLevel).");
		}
		try {
			return setCompressionLevel(CompressionLevel.parseString(val));
		} catch (IllegalCompressionLevelException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public CompressionType getCompressionType() {
		return getSshSessionConfiguration().getCompressionType();
	}

	public CompressionType setCompressionType(CompressionType compressionType) {
		return getSshSessionConfiguration().setCompressionType(compressionType);
	}

	public CompressionType setCompressionType(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a CompressionType).");
		}
		try {
			return CompressionType.parseString(val);
		} catch (IllegalCompressionTypeException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public Timeout getConnectionTimeout() {
		return getSshSessionConfiguration().getConnectionTimeout();
	}

	public Timeout setConnectionTimeout(GenericTimeout ival) {
		return getSshSessionConfiguration().setConnectionTimeout(ival);
	}

	public Timeout setConnectionTimeout(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a connection timeout).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setConnectionTimeout(GenericTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, val));
		}
	}

	public Timeout getReadTimeout() {
		return getSshSessionConfiguration().getReadTimeout();
	}

	public Timeout setReadTimeout(GenericTimeout ival) {
		return getSshSessionConfiguration().setReadTimeout(ival);
	}

	public Timeout setReadTimeout(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a read timeout).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setReadTimeout(GenericTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, val));
		}
	}

	public int getServerAliveCountMax() {
		return getSshSessionConfiguration().getServerAliveCountMax();
	}

	public int setServerAliveCountMax(int ival)
			throws IllegalSshSessionConfigurationException {
		return getSshSessionConfiguration().setServerAliveCountMax(ival);
	}

	public int setServerAliveCountMax(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a server alive count max).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setServerAliveCountMax(Integer.parseInt(val));
		} catch (IllegalSshSessionConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, val));
		}
	}

	public Timeout getServerAliveInterval() {
		return getSshSessionConfiguration().getServerAliveInterval();
	}

	public Timeout setServerAliveInterval(GenericTimeout ival) {
		return getSshSessionConfiguration().setServerAliveInterval(ival);
	}

	public Timeout setServerAliveInterval(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a server alive interval).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setServerAliveInterval(GenericTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL, val));
		}
	}

	public ProxyType getProxyType() {
		return getSshSessionConfiguration().getProxyType();
	}

	public ProxyType setProxyType(ProxyType val) {
		return getSshSessionConfiguration().setProxyType(val);
	}

	public ProxyType setProxyType(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a proxy type).");
		}
		try {
			return setProxyType(ProxyType.parseString(val));
		} catch (IllegalProxyTypeException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public Host getProxyHost() {
		return getSshSessionConfiguration().getProxyHost();
	}

	public Host setProxyHost(Host val) {
		return getSshSessionConfiguration().setProxyHost(val);
	}

	public Host setProxyHost(String val) throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a proxy host).");
		}
		try {
			return setProxyHost(Host.parseString(val));
		} catch (IllegalHostException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public Port getProxyPort() {
		return getSshSessionConfiguration().getProxyPort();
	}

	public Port setProxyPort(Port port) {
		return getSshSessionConfiguration().setProxyPort(port);
	}

	public Port setProxyPort(String val) throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a Port).");
		}
		if (val.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setProxyPort(Port.parseString(val));
		} catch (IllegalPortException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	public boolean getMgmtEnable() {
		return mbMgmtEnable;
	}

	public boolean setMgmtEnable(boolean val) {
		boolean previous = getMgmtEnable();
		mbMgmtEnable = val;
		return previous;
	}

	public boolean setMgmtEnable(String val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a boolean).");
		}
		return setMgmtEnable(Boolean.parseBoolean(val));
	}

	/**
	 * 
	 * @return the ssh management master user. Cannot be null.
	 */
	public String getManagementLogin() {
		return moMgmtLogin;
	}

	public String setManagementLogin(String mgmtMasterUser)
			throws SshPlugInConfigurationException {
		if (mgmtMasterUser == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a user.");
		}
		if (mgmtMasterUser.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getManagementLogin();
		moMgmtLogin = mgmtMasterUser;
		return previous;
	}

	/**
	 * 
	 * @return the keypair name of the ssh management master user. Can be null,
	 *         when the connection as ssh management master user should be done
	 *         without keypair.
	 */
	public KeyPairName getManagementKeyPairName() {
		return moMgmtKeyPairName;
	}

	public KeyPairName setMgmtMasterKey(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		KeyPairName previous = getManagementKeyPairName();
		moMgmtKeyPairName = keyPairName;
		return previous;
	}

	public KeyPairName setManagementKeyPairName(String val)
			throws SshPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a "
					+ KeyPairName.class.getCanonicalName() + ".");
		}
		try {
			return setMgmtMasterKey(KeyPairName.parseString(val));
		} catch (IllegalKeyPairNameException Ex) {
			throw new SshPlugInConfigurationException(Ex);
		}
	}

	/**
	 * 
	 * @return the password of the ssh management master user, or the password
	 *         of the keypair of the ssh management master user. Can be null, if
	 *         the keypair of the ssh management master user is defined and if
	 *         this key don't have a passphrase.
	 */
	public String getManagementPassword() {
		return moMgmtPassword;
	}

	public String setManagementPassword(String mgmtMasterPass)
			throws SshPlugInConfigurationException {
		if (mgmtMasterPass == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a password.");
		}
		if (mgmtMasterPass.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getManagementLogin();
		moMgmtPassword = mgmtMasterPass;
		return previous;
	}

}
