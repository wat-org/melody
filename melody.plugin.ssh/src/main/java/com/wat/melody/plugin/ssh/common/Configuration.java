package com.wat.melody.plugin.ssh.common;

import java.io.File;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.wat.melody.api.IPluginConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.PluginConfigurationException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.plugin.ssh.Upload;
import com.wat.melody.plugin.ssh.common.exception.ConfigurationException;
import com.wat.melody.plugin.ssh.common.exception.IllegalCompressionLevelException;
import com.wat.melody.plugin.ssh.common.exception.IllegalCompressionTypeException;
import com.wat.melody.plugin.ssh.common.exception.IllegalProxyTypeException;
import com.wat.melody.plugin.ssh.common.exception.SshException;

public class Configuration implements IPluginConfiguration {

	private static Log log = LogFactory.getLog(Configuration.class);

	public static final String NAME = "SSH";

	public static Configuration get(IProcessorManager pm)
			throws ConfigurationException {
		Map<String, IPluginConfiguration> pcs = pm.getPluginConfigurations();
		IPluginConfiguration pc = null;
		pc = pcs.get(NAME);
		if (pc == null) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_NOT_REGISTERED, NAME));
		}
		try {
			return (Configuration) pc;
		} catch (ClassCastException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_REGISTRATION_ERROR,
					new Object[] { NAME,
							IPluginConfiguration.PLUGIN_CONF_CLASS,
							pc.getFilePath(),
							Configuration.class.getCanonicalName() }));
		}
	}

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

	private String msConfigurationFilePath;
	private JSch moJSch;
	private File moKnownHosts;
	private File moKeyPairRepo;
	private int miKeyPairSize = 2048;
	private CompressionLevel miCompressionLevel = CompressionLevel.NONE;
	private CompressionType msCompressionType = CompressionType.NONE;
	private int miConnectionTimeout = 15000;
	private int miReadTimeout = 60000;
	private int miServerAliveCountMax = 1;
	private int miServerAliveInterval = 10000;
	private ProxyType moProxyType;
	private Host moProxyHost;
	private Port moProxyPort;

	public Configuration() {
		setJSch(new JSch());
	}

	@Override
	public String getName() {
		return NAME;
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

	@Override
	public void load(PropertiesSet ps) throws PluginConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		setFilePath(ps.getFilePath());

		loadKnownHosts(ps);
		loadKeyPairRepo(ps);
		loadKeyPairSize(ps);
		loadCompressionLevel(ps);
		loadCompressionType(ps);
		loadConnectionTimeout(ps);
		loadReadTimeout(ps);
		loadServerAliveCountMax(ps);
		loadServerAliveInterval(ps);
		loadProxyType(ps);
		loadProxyHost(ps);
		loadProxyPort(ps);

		validate();
	}

	private void loadKnownHosts(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(KNOWN_HOSTS)) {
			return;
		}
		try {
			setKnownHosts(ps.get(KNOWN_HOSTS));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KNOWN_HOSTS), Ex);
		}
	}

	private void loadKeyPairRepo(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(KEYPAIR_REPO)) {
			return;
		}
		try {
			setKeyPairRepo(ps.get(KEYPAIR_REPO));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_REPO), Ex);
		}
	}

	private void loadKeyPairSize(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(KEYPAIR_SIZE)) {
			return;
		}
		try {
			setKeyPairSize(ps.get(KEYPAIR_SIZE));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, KEYPAIR_SIZE), Ex);
		}
	}

	private void loadCompressionLevel(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(COMPRESSION_LEVEL)) {
			return;
		}
		try {
			setCompressionLevel(ps.get(COMPRESSION_LEVEL));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_LEVEL), Ex);
		}
	}

	private void loadCompressionType(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(COMPRESSION_TYPE)) {
			return;
		}
		try {
			setCompressionType(ps.get(COMPRESSION_TYPE));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, COMPRESSION_TYPE), Ex);
		}
	}

	private void loadConnectionTimeout(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(CONNECTION_TIMEOUT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, CONNECTION_TIMEOUT), Ex);
		}
	}

	private void loadReadTimeout(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(READ_TIMEOUT)) {
			return;
		}
		try {
			setReadTimeout(ps.get(READ_TIMEOUT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, READ_TIMEOUT), Ex);
		}
	}

	private void loadServerAliveCountMax(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_MAX_COUNT)) {
			return;
		}
		try {
			setServerAliveCountMax(ps.get(SERVER_ALIVE_MAX_COUNT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_MAX_COUNT),
					Ex);
		}
	}

	private void loadServerAliveInterval(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(SERVER_ALIVE_INTERVAL)) {
			return;
		}
		try {
			setServerAliveInterval(ps.get(SERVER_ALIVE_INTERVAL));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SERVER_ALIVE_INTERVAL),
					Ex);
		}
	}

	private void loadProxyType(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(PROXY_TYPE)) {
			return;
		}
		try {
			setProxyType(ps.get(PROXY_TYPE));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_TYPE), Ex);
		}
	}

	private void loadProxyHost(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(PROXY_HOST)) {
			return;
		}
		try {
			setProxyHost(ps.get(PROXY_HOST));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_HOST), Ex);
		}
	}

	private void loadProxyPort(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(PROXY_PORT)) {
			return;
		}
		try {
			setProxyPort(ps.get(PROXY_PORT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROXY_PORT), Ex);
		}
	}

	private void validate() throws ConfigurationException {
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

	public void addIdentity(File privkeyFile) throws JSchException {
		getJSch().addIdentity(privkeyFile.getPath());
	}

	public boolean existsKnownHostsHostKey(String host, byte[] bHostKey) {
		int res = getJSch().getHostKeyRepository().check(host, bHostKey);
		return res == HostKeyRepository.OK || res == HostKeyRepository.CHANGED;
	}

	public HostKey getKnownHostsHostKey(String host) {
		HostKey[] hks = getJSch().getHostKeyRepository().getHostKey(host, null);
		try {
			return hks[0];
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return null;
		}
	}

	public boolean addKnownHostsHost(ITaskContext context, Host host, Port p,
			long timeout) throws SshException, InterruptedException {
		Upload upload = new Upload();

		upload.setContext(context);
		upload.setTrust(true);
		upload.setHost(host);
		upload.setPort(p);

		try {
			upload.setLogin("melody");
		} catch (SshException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "login of the Ssh connection to 'melody'. "
					+ "Because this login is harcoded, it must be valid. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;
		boolean enablementDone = true;

		while (true) {
			// Don't upload anything, just connect.
			try {
				Session session = upload.openSession();
				ChannelSftp channel = upload.openSftpChannel(session);
				channel.disconnect();
				session.disconnect();
				break;
			} catch (Throwable Ex) {
				if (Ex.getCause() == null || Ex.getCause().getMessage() == null) {
					throw new SshException(Ex);
				} else if (Ex.getCause().getMessage()
						.indexOf("Incorrect credentials") != -1) {
					// connection succeed
					break;
				} else if (Ex.getCause().getMessage().indexOf("refused") == -1
						&& Ex.getCause().getMessage().indexOf("timeout") == -1
						&& Ex.getCause().getMessage().indexOf("No route") == -1) {
					throw new SshException(Ex);
				}
			}
			log.debug(Messages.bind(Messages.SshMsg_WAIT_FOR_MANAGEMENT, host
					.getValue().getHostAddress(), p.getValue()));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				enablementDone = false;
				break;
			}
		}
		return enablementDone;
	}

	/**
	 * 
	 * @param host
	 * @param sHostKey
	 * 
	 * @throws JSchException
	 *             if the given key is not an RSA or DSA key.
	 */
	public void addKnownHostsHostKey(String host, String sHostKey)
			throws JSchException {
		if (sHostKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an HostKey).");
		}
		byte[] key = Base64.decodeBase64(sHostKey.getBytes());
		if (existsKnownHostsHostKey(host, key)) {
			return;
		}
		HostKey hk = new HostKey(host, key);
		getJSch().getHostKeyRepository().add(hk, new AnwserYes());
	}

	public void removeKnownHostsHostKey(String host) {
		if (host == null) {
			host = "";
		}
		getJSch().getHostKeyRepository().remove(host, null);
	}

	private JSch getJSch() {
		return moJSch;
	}

	private JSch setJSch(JSch jsch) {
		JSch previous = getJSch();
		moJSch = jsch;
		return previous;
	}

	public File getKnownHosts() {
		return moKnownHosts;
	}

	public File setKnownHosts(File knownHosts) throws ConfigurationException {
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
			Tools.validateFilePath(knownHosts.getPath());
			getJSch().setKnownHosts(knownHosts.getPath());
		} catch (IllegalFileException | IllegalDirectoryException
				| JSchException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KNOWNHOSTS, knownHosts), Ex);
		}
		File previous = getKnownHosts();
		moKnownHosts = knownHosts;
		return previous;
	}

	public File setKnownHosts(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a KnownHosts File path).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKnownHosts(new File(val));
	}

	public File getKeyPairRepo() {
		return moKeyPairRepo;
	}

	public File setKeyPairRepo(File keyPairRepo) throws ConfigurationException {
		if (keyPairRepo == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Directory (a KeyPair Repo).");
		}
		if (!keyPairRepo.isAbsolute()) {
			// Resolve from this configuration File's parent location
			keyPairRepo = new File(new File(getFilePath()).getParent(),
					keyPairRepo.getPath());
		}
		try {
			Tools.validateDirExists(keyPairRepo.getPath());
		} catch (IllegalDirectoryException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KEYPAIR_REPO, keyPairRepo), Ex);
		}
		File previous = getKnownHosts();
		moKeyPairRepo = keyPairRepo;
		return previous;
	}

	public File setKeyPairRepo(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Directory (a KeyPair Repo path).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		return setKeyPairRepo(new File(val));
	}

	public int getKeyPairSize() {
		return miKeyPairSize;
	}

	public int setKeyPairSize(int ival) throws ConfigurationException {
		if (ival < 1024) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KEYPAIR_SIZE, ival));
		}
		int previous = getKeyPairSize();
		miKeyPairSize = ival;
		return previous;
	}

	public int setKeyPairSize(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer > 1024 (a KeyPair Size).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setKeyPairSize(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KEYPAIR_SIZE, val));
		}
	}

	public CompressionLevel getCompressionLevel() {
		return miCompressionLevel;
	}

	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel) throws ConfigurationException {
		if (compressionLevel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a CompressionLevel).");
		}
		CompressionLevel previous = getCompressionLevel();
		miCompressionLevel = compressionLevel;
		return previous;
	}

	public CompressionLevel setCompressionLevel(String val)
			throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an "
					+ "Integer >=0 and <=6 (a CompressionLevel).");
		}
		try {
			return setCompressionLevel(CompressionLevel.parseString(val));
		} catch (IllegalCompressionLevelException Ex) {
			throw new ConfigurationException(Ex);
		}
	}

	public CompressionType getCompressionType() {
		return msCompressionType;
	}

	public CompressionType setCompressionType(CompressionType compressionType) {
		if (compressionType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a CompressionType).");
		}
		CompressionType previous = getCompressionType();
		msCompressionType = compressionType;
		return previous;
	}

	public CompressionType setCompressionType(String val)
			throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a CompressionType).");
		}
		try {
			return CompressionType.parseString(val);
		} catch (IllegalCompressionTypeException Ex) {
			throw new ConfigurationException(Ex);
		}
	}

	public int getConnectionTimeout() {
		return miConnectionTimeout;
	}

	public int setConnectionTimeout(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, ival));
		}
		int previous = getConnectionTimeout();
		miConnectionTimeout = ival;
		return previous;
	}

	public int setConnectionTimeout(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a connection timeout).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setConnectionTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, val));
		}
	}

	public int getReadTimeout() {
		return miReadTimeout;
	}

	public int setReadTimeout(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, ival));
		}
		int previous = getReadTimeout();
		miReadTimeout = ival;
		return previous;
	}

	public int setReadTimeout(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a read timeout).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setReadTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, val));
		}
	}

	public int getServerAliveCountMax() {
		return miServerAliveCountMax;
	}

	public int setServerAliveCountMax(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, ival));
		}
		int previous = getServerAliveCountMax();
		miServerAliveCountMax = ival;
		return previous;
	}

	public int setServerAliveCountMax(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a server alive count max).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setServerAliveCountMax(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, val));
		}
	}

	public int getServerAliveInterval() {
		return miServerAliveInterval;
	}

	public int setServerAliveInterval(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL, ival));
		}
		int previous = getServerAliveInterval();
		miServerAliveInterval = ival;
		return previous;
	}

	public int setServerAliveInterval(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a server alive interval).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setServerAliveInterval(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL, val));
		}
	}

	public ProxyType getProxyType() {
		return moProxyType;
	}

	public ProxyType setProxyType(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a proxy type).");
		}
		try {
			return setProxyType(ProxyType.parseString(val));
		} catch (IllegalProxyTypeException Ex) {
			throw new ConfigurationException(Ex);
		}
	}

	public ProxyType setProxyType(ProxyType val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ProxyType.");
		}
		ProxyType previous = getProxyType();
		moProxyType = val;
		return previous;
	}

	public Host getProxyHost() {
		return moProxyHost;
	}

	public Host setProxyHost(Host val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Host.");
		}
		Host previous = getProxyHost();
		moProxyHost = val;
		return previous;
	}

	public Host setProxyHost(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a proxy host).");
		}
		try {
			return setProxyHost(Host.parseString(val));
		} catch (IllegalHostException Ex) {
			throw new ConfigurationException(Ex);
		}
	}

	public Port getProxyPort() {
		return moProxyPort;
	}

	public Port setProxyPort(Port port) throws ConfigurationException {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Port.");
		}
		Port previous = getProxyPort();
		moProxyPort = port;
		return previous;
	}

	public Port setProxyPort(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a Port).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setProxyPort(Port.parseString(val));
		} catch (IllegalPortException Ex) {
			throw new ConfigurationException(Ex);
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
	public Session openSession(AbstractSshOperation base) throws SshException {
		Session session = null;
		try {
			session = getJSch().getSession(base.getLogin(),
					base.getHost().getValue().getHostAddress(),
					base.getPort().getValue());
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Ssh Session. "
					+ "Because all parameters have already been validated, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		session.setUserInfo(base);

		session.setServerAliveCountMax(getServerAliveCountMax());

		try {
			session.setServerAliveInterval(getServerAliveInterval());
		} catch (JSchException Ex) {
			throw new SshException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL,
					getServerAliveInterval()), Ex);
		}

		try {
			session.setTimeout(getReadTimeout());
		} catch (JSchException Ex) {
			throw new SshException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT,
					getServerAliveInterval()), Ex);
		}

		session.setConfig("compression.s2c", getCompressionType().getValue());
		session.setConfig("compression.c2s", getCompressionType().getValue());
		session.setConfig("compression_level", getCompressionLevel().getValue());

		if (getProxyType() != null) {
			/*
			 * TODO : hande proxy parameters
			 */
		}

		try {
			session.connect(getConnectionTimeout());
		} catch (JSchException Ex) {
			if (Ex.getMessage() != null && Ex.getMessage().indexOf("Auth") == 0) {
				// will match message 'Auth cancel' and 'Auth fail'
				Ex = new JSchException("Incorrect credentials.", Ex);
			}
			throw new SshException(Messages.bind(
					Messages.SshEx_FAILED_TO_CONNECT, new Object[] {
							base.getHost().getValue().getHostAddress(),
							base.getPort().getValue(), base.getLogin() }), Ex);
		}

		return session;
	}

}

class AnwserYes implements UserInfo {
	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	@Override
	public boolean promptPassword(String passwordPrompt) {
		return true;
	}
}