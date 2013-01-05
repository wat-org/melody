package com.wat.melody.plugin.ssh.common;

import java.io.File;

import org.apache.commons.codec.binary.Base64;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;
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
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.plugin.ssh.common.exception.IllegalCompressionLevelException;
import com.wat.melody.plugin.ssh.common.exception.IllegalCompressionTypeException;
import com.wat.melody.plugin.ssh.common.exception.IllegalProxyTypeException;
import com.wat.melody.plugin.ssh.common.exception.SshPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshPlugInConfiguration implements IPlugInConfiguration {

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
	public static final String MGMT_MASTER_USER = "ssh.management.master.user";
	public static final String MGMT_MASTER_KEY = "ssh.management.master.key";
	public static final String MGMT_MASTER_PASS = "ssh.management.master.pass";

	private String msConfigurationFilePath;
	private JSch moJSch;
	private File moKnownHosts;
	private KeyPairRepository moKeyPairRepo;
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

	private Boolean mbMgmtEnable = true;
	private String moMgmtMasterUser = "root";
	private KeyPairName moMgmtMasterKey;
	private String moMgmtMasterPass;

	public SshPlugInConfiguration() {
		setJSch(new JSch());
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
	public void load(PropertiesSet ps) throws PlugInConfigurationException {
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

		loadMgmtEnable(ps);
		loadMgmtMasterUser(ps);
		loadMgmtMasterKey(ps);
		loadMgmtMasterPass(ps);

		validate();
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
		if (!ps.containsKey(MGMT_MASTER_USER)) {
			return;
		}
		try {
			setMgmtMasterUser(ps.get(MGMT_MASTER_USER));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_MASTER_USER), Ex);
		}
	}

	private void loadMgmtMasterKey(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_MASTER_KEY)) {
			return;
		}
		try {
			setMgmtMasterKey(ps.get(MGMT_MASTER_KEY));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_MASTER_KEY), Ex);
		}
	}

	private void loadMgmtMasterPass(PropertiesSet ps)
			throws SshPlugInConfigurationException {
		if (!ps.containsKey(MGMT_MASTER_PASS)) {
			return;
		}
		try {
			setMgmtMasterPass(ps.get(MGMT_MASTER_PASS));
		} catch (SshPlugInConfigurationException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MGMT_MASTER_PASS), Ex);
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
			return;
		}
		getJSch().getHostKeyRepository().remove(host, null);
	}

	public JSch getJSch() {
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

	public File setKnownHosts(File knownHosts)
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
			Tools.validateFilePath(knownHosts.getPath());
			getJSch().setKnownHosts(knownHosts.getPath());
		} catch (IllegalFileException | IllegalDirectoryException
				| JSchException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_KNOWNHOSTS, knownHosts), Ex);
		}
		File previous = getKnownHosts();
		moKnownHosts = knownHosts;
		return previous;
	}

	public File setKnownHosts(String val)
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

	public CompressionLevel getCompressionLevel() {
		return miCompressionLevel;
	}

	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel) {
		if (compressionLevel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a CompressionLevel).");
		}
		CompressionLevel previous = getCompressionLevel();
		miCompressionLevel = compressionLevel;
		return previous;
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

	public int getConnectionTimeout() {
		return miConnectionTimeout;
	}

	public int setConnectionTimeout(int ival)
			throws SshPlugInConfigurationException {
		if (ival < 0) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, ival));
		}
		int previous = getConnectionTimeout();
		miConnectionTimeout = ival;
		return previous;
	}

	public int setConnectionTimeout(String val)
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
			return setConnectionTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, val));
		}
	}

	public int getReadTimeout() {
		return miReadTimeout;
	}

	public int setReadTimeout(int ival) throws SshPlugInConfigurationException {
		if (ival < 0) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, ival));
		}
		int previous = getReadTimeout();
		miReadTimeout = ival;
		return previous;
	}

	public int setReadTimeout(String val)
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
			return setReadTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, val));
		}
	}

	public int getServerAliveCountMax() {
		return miServerAliveCountMax;
	}

	public int setServerAliveCountMax(int ival)
			throws SshPlugInConfigurationException {
		if (ival < 0) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, ival));
		}
		int previous = getServerAliveCountMax();
		miServerAliveCountMax = ival;
		return previous;
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
		} catch (NumberFormatException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, val));
		}
	}

	public int getServerAliveInterval() {
		return miServerAliveInterval;
	}

	public int setServerAliveInterval(int ival)
			throws SshPlugInConfigurationException {
		if (ival < 0) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL, ival));
		}
		int previous = getServerAliveInterval();
		miServerAliveInterval = ival;
		return previous;
	}

	public int setServerAliveInterval(String val)
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
			return setServerAliveInterval(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new SshPlugInConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_INTERVAL, val));
		}
	}

	public ProxyType getProxyType() {
		return moProxyType;
	}

	public ProxyType setProxyType(ProxyType val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ProxyType.");
		}
		ProxyType previous = getProxyType();
		moProxyType = val;
		return previous;
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
		return moProxyHost;
	}

	public Host setProxyHost(Host val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Host.");
		}
		Host previous = getProxyHost();
		moProxyHost = val;
		return previous;
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
		return moProxyPort;
	}

	public Port setProxyPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Port.");
		}
		Port previous = getProxyPort();
		moProxyPort = port;
		return previous;
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
	public String getMgmtMasterUser() {
		return moMgmtMasterUser;
	}

	public String setMgmtMasterUser(String mgmtMasterUser)
			throws SshPlugInConfigurationException {
		if (mgmtMasterUser == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a user.");
		}
		if (mgmtMasterUser.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getMgmtMasterUser();
		moMgmtMasterUser = mgmtMasterUser;
		return previous;
	}

	/**
	 * 
	 * @return the keypair name of the ssh management master user. Can be null,
	 *         when the connection as ssh management master user should be done
	 *         without keypair.
	 */
	public KeyPairName getMgmtMasterKey() {
		return moMgmtMasterKey;
	}

	public KeyPairName setMgmtMasterKey(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		KeyPairName previous = getMgmtMasterKey();
		moMgmtMasterKey = keyPairName;
		return previous;
	}

	public KeyPairName setMgmtMasterKey(String val)
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
	public String getMgmtMasterPass() {
		return moMgmtMasterPass;
	}

	public String setMgmtMasterPass(String mgmtMasterPass)
			throws SshPlugInConfigurationException {
		if (mgmtMasterPass == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a password.");
		}
		if (mgmtMasterPass.trim().length() == 0) {
			throw new SshPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getMgmtMasterUser();
		moMgmtMasterPass = mgmtMasterPass;
		return previous;
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