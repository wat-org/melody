package com.wat.melody.plugin.aws.ec2.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.wat.melody.api.IPluginConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.plugin.aws.ec2.common.exception.ConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Configuration implements IPluginConfiguration, AWSCredentials {

	public static final String NAME = "AWS.EC2";

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

	// MANDATORY CONFIGURATION DIRECTIVE
	public static final String EC2_ACCESS_KEY = "accessKey";
	public static final String EC2_SECRET_KEY = "secretKey";

	// OPTIONNAL CONFIGURATION DIRECTIVE
	public static final String EC2_CONNECTION_TIMEOUT = "aws.conn.socket.connect.timeout";
	public static final String EC2_READ_TIMEOUT = "aws.conn.socket.read.timeout";
	public static final String EC2_CONNECTION_RETRY = "aws.conn.socket.max.error.retry";
	public static final String EC2_SEND_BUFFER_SIZE_HINT = "aws.conn.socket.buffer.size.send";
	public static final String EC2_RECEIVE_BUFFER_SIZE_HINTS = "aws.conn.socket.buffer.size.receive";
	public static final String EC2_MAX_CONNECTION = "aws.conn.max.pool.size";

	public static final String EC2_PROTOCOL = "aws.conn.http.protocol";
	public static final String EC2_USER_AGENT = "aws.conn.http.useragent";

	public static final String EC2_PROXY_HOST = "aws.conn.proxy.host";
	public static final String EC2_PROXY_PORT = "aws.conn.proxy.port";
	public static final String EC2_PROXY_USERNAME = "aws.conn.proxy.username";
	public static final String EC2_PROXY_PASSWORD = "aws.conn.proxy.password";

	private String msConfigurationFilePath;
	private String msAccessKey;
	private String msSecretKey;
	private ClientConfiguration moCC;
	private Map<String, AmazonEC2> moPooledEc2s;

	public Configuration() {
		setCC(new ClientConfiguration());
		setPooledEc2s(new Hashtable<String, AmazonEC2>());
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
	public void load(PropertiesSet ps) throws ConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		setFilePath(ps.getFilePath());

		loadAccessKey(ps);
		loadSecretKey(ps);
		loadConnectionTimeout(ps);
		loadSocketTimeout(ps);
		loadMaxErrorRetry(ps);
		loadMaxConnections(ps);
		loadSocketSendBufferSizeHints(ps);
		loadSocketReceiveBufferSizeHints(ps);
		loadProtocol(ps);
		loadUserAgent(ps);
		loadProxyHost(ps);
		loadProxyPort(ps);
		loadProxyUsername(ps);
		loadProxyPassword(ps);

		validate();
	}

	private void loadAccessKey(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_ACCESS_KEY)) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, EC2_ACCESS_KEY));
		}
		try {
			setAccessKey(ps.get(EC2_ACCESS_KEY));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_ACCESS_KEY), Ex);
		}
	}

	private void loadSecretKey(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_SECRET_KEY)) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, EC2_SECRET_KEY));
		}
		try {
			setSecretKey(ps.get(EC2_SECRET_KEY));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_SECRET_KEY), Ex);
		}
	}

	private void loadConnectionTimeout(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(EC2_CONNECTION_TIMEOUT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_CONNECTION_TIMEOUT),
					Ex);
		}
	}

	private void loadSocketTimeout(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_READ_TIMEOUT)) {
			return;
		}
		try {
			setSocketTimeout(ps.get(EC2_READ_TIMEOUT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_READ_TIMEOUT), Ex);
		}
	}

	private void loadMaxErrorRetry(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_CONNECTION_RETRY)) {
			return;
		}
		try {
			setMaxErrorRetry(ps.get(EC2_CONNECTION_RETRY));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_CONNECTION_RETRY),
					Ex);
		}
	}

	private void loadMaxConnections(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_MAX_CONNECTION)) {
			return;
		}
		try {
			setMaxConnections(ps.get(EC2_MAX_CONNECTION));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_MAX_CONNECTION), Ex);
		}
	}

	private void loadSocketSendBufferSizeHints(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_SEND_BUFFER_SIZE_HINT)) {
			return;
		}
		try {
			setSocketSendBufferSizeHints(ps.get(EC2_SEND_BUFFER_SIZE_HINT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					EC2_SEND_BUFFER_SIZE_HINT), Ex);
		}
	}

	private void loadSocketReceiveBufferSizeHints(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_RECEIVE_BUFFER_SIZE_HINTS)) {
			return;
		}
		try {
			setSocketReceiveBufferSizeHints(ps
					.get(EC2_RECEIVE_BUFFER_SIZE_HINTS));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					EC2_RECEIVE_BUFFER_SIZE_HINTS), Ex);
		}
	}

	private void loadProtocol(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_PROTOCOL)) {
			return;
		}
		try {
			setProtocol(ps.get(EC2_PROTOCOL));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_PROTOCOL), Ex);
		}
	}

	private void loadUserAgent(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_USER_AGENT)) {
			return;
		}
		try {
			setUserAgent(ps.get(EC2_USER_AGENT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_USER_AGENT), Ex);
		}
	}

	private void loadProxyHost(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_PROXY_HOST)) {
			return;
		}
		try {
			setProxyHost(ps.get(EC2_PROXY_HOST));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_PROXY_HOST), Ex);
		}
	}

	private void loadProxyPort(PropertiesSet ps) throws ConfigurationException {
		if (!ps.containsKey(EC2_PROXY_PORT)) {
			return;
		}
		try {
			setProxyPort(ps.get(EC2_PROXY_PORT));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_PROXY_PORT), Ex);
		}
	}

	private void loadProxyUsername(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_PROXY_USERNAME)) {
			return;
		}
		try {
			setProxyUsername(ps.get(EC2_PROXY_USERNAME));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_PROXY_USERNAME), Ex);
		}
	}

	private void loadProxyPassword(PropertiesSet ps)
			throws ConfigurationException {
		if (!ps.containsKey(EC2_PROXY_PASSWORD)) {
			return;
		}
		try {
			setProxyPassword(ps.get(EC2_PROXY_PASSWORD));
		} catch (ConfigurationException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, EC2_PROXY_PASSWORD), Ex);
		}
	}

	private void validate() throws ConfigurationException {
		try {
			Common.validate(new AmazonEC2Client(this, getCC()));
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().equalsIgnoreCase("AuthFailure")) {
				throw new ConfigurationException(
						Messages.bind(Messages.ConfEx_INVALID_AWS_CREDENTIALS,
								new Object[] { getAWSAccessKeyId(),
										getAWSSecretKey(), EC2_ACCESS_KEY,
										EC2_SECRET_KEY, getFilePath() }));
			} else {
				throw new ConfigurationException(Messages.ConfEx_VALIDATION, Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new ConfigurationException(Messages.ConfEx_VALIDATION, Ex);
		}
	}

	public ClientConfiguration getCC() {
		return moCC;
	}

	public ClientConfiguration setCC(ClientConfiguration cg) {
		if (cg == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ClientConfiguration.");
		}
		ClientConfiguration previous = getCC();
		moCC = cg;
		return previous;
	}

	public Map<String, AmazonEC2> getPooledEc2s() {
		return moPooledEc2s;
	}

	public Map<String, AmazonEC2> setPooledEc2s(Map<String, AmazonEC2> ec2s) {
		if (ec2s == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Map<String, AmazonEC2> (a map which "
					+ "contains pooled AmazonEc2, by region).");
		}
		Map<String, AmazonEC2> previous = getPooledEc2s();
		moPooledEc2s = ec2s;
		return previous;
	}

	@Override
	public String getAWSAccessKeyId() {
		return msAccessKey;
	}

	public String setAccessKey(String accessKey) throws ConfigurationException {
		if (accessKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS accesKey).");
		}
		if (accessKey.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getAWSAccessKeyId();
		msAccessKey = accessKey;
		return previous;
	}

	@Override
	public String getAWSSecretKey() {
		return msSecretKey;
	}

	public String setSecretKey(String secretKey) throws ConfigurationException {
		if (secretKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS secretKey).");
		}
		if (secretKey.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getAWSSecretKey();
		msSecretKey = secretKey;
		return previous;
	}

	public int getConnectionTimeout() {
		return getCC().getConnectionTimeout();
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

	public int setConnectionTimeout(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, ival));
		}
		int previous = getCC().getConnectionTimeout();
		getCC().setConnectionTimeout(ival);
		return previous;
	}

	public int getSocketTimeout() {
		return getCC().getSocketTimeout();
	}

	public int setSocketTimeout(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a socket timeout).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, val));
		}
	}

	public int setSocketTimeout(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, ival));
		}
		int previous = getCC().getSocketTimeout();
		getCC().setSocketTimeout(ival);
		return previous;
	}

	public int getMaxErrorRetry() {
		return getCC().getMaxErrorRetry();
	}

	public int setMaxErrorRetry(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a retry attempts).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setMaxErrorRetry(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_RETRY, val));
		}
	}

	public int setMaxErrorRetry(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_RETRY, ival));
		}
		int previous = getCC().getMaxErrorRetry();
		getCC().setMaxErrorRetry(ival);
		return previous;
	}

	public int getMaxConnections() {
		return getCC().getMaxConnections();
	}

	public int setMaxConnections(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer or zero (a maximum connection amount).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setMaxConnections(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_MAX_CONN, val));
		}
	}

	public int setMaxConnections(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_MAX_CONN, ival));
		}
		int previous = getCC().getMaxConnections();
		getCC().setMaxConnections(ival);
		return previous;
	}

	public int getSocketSendBufferSizeHints() {
		return getCC().getSocketBufferSizeHints()[0];
	}

	public int setSocketSendBufferSizeHints(String val)
			throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer (a Send Buffer Size).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketSendBufferSizeHints(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SEND_BUFFSIZE, val));
		}
	}

	public int setSocketSendBufferSizeHints(int ival)
			throws ConfigurationException {
		if (ival <= 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SEND_BUFFSIZE, ival));
		}
		int previous = getSocketSendBufferSizeHints();
		getCC().setSocketBufferSizeHints(ival,
				getSocketReceiveBufferSizeHints());
		return previous;
	}

	public int getSocketReceiveBufferSizeHints() {
		return getCC().getSocketBufferSizeHints()[1];
	}

	public int setSocketReceiveBufferSizeHints(String val)
			throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents an positive "
					+ "Integer (a Receive Buffer Size).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketReceiveBufferSizeHints(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_RECEIVE_BUFFSIZE, val));
		}
	}

	public int setSocketReceiveBufferSizeHints(int ival)
			throws ConfigurationException {
		if (ival <= 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_RECEIVE_BUFFSIZE, ival));
		}
		int previous = getSocketReceiveBufferSizeHints();
		getCC().setSocketBufferSizeHints(getSocketSendBufferSizeHints(), ival);
		return previous;
	}

	public Protocol getProtocol() {
		return getCC().getProtocol();
	}

	public Protocol setProtocol(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a Protocol "
					+ "(Accepted values are '" + Protocol.values().toString()
					+ "').");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		Protocol previous = getCC().getProtocol();
		if (val.equalsIgnoreCase(Protocol.HTTP.toString())) {
			getCC().setProtocol(Protocol.HTTP);
		} else if (val.equalsIgnoreCase(Protocol.HTTPS.toString())) {
			getCC().setProtocol(Protocol.HTTPS);
		} else {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_PROTOCOL, val, Protocol.values()
							.toString()));
		}
		return previous;
	}

	public String getUserAgent() {
		return getCC().getUserAgent();
	}

	public String setUserAgent(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a User Agent).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getCC().getUserAgent();
		getCC().setUserAgent(val);
		return previous;
	}

	public String getProxyHost() {
		return getCC().getProxyHost();
	}

	public String setProxyHost(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Proxy Host FQDN or IP "
					+ "address).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			InetAddress.getByName(val);
		} catch (UnknownHostException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_PROXY_HOST, val), Ex);
		}
		String previous = getCC().getProxyHost();
		getCC().setProxyHost(val);
		return previous;
	}

	public int getProxyPort() {
		return getCC().getProxyPort();
	}

	public int setProxyPort(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a positive "
					+ "Integer (a Proxy Port).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setProxyPort(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_PROXY_PORT, val));
		}
	}

	public int setProxyPort(int ival) throws ConfigurationException {
		if (ival < 0) {
			throw new ConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_PROXY_PORT, ival));
		}
		int previous = getCC().getProxyPort();
		getCC().setProxyPort(ival);
		return previous;
	}

	public String getProxyUsername() {
		return getCC().getProxyUsername();
	}

	public String setProxyUsername(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Proxy Username).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getCC().getProxyUsername();
		getCC().setProxyUsername(val);
		return previous;
	}

	public String getProxyPassword() {
		return getCC().getProxyPassword();
	}

	public String setProxyPassword(String val) throws ConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Proxy Password).");
		}
		if (val.trim().length() == 0) {
			throw new ConfigurationException(Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getCC().getProxyPassword();
		getCC().setProxyPassword(val);
		return previous;
	}

	/**
	 * <p>
	 * Get a <code>AmazonEC2</code> object which is already configured for the
	 * requested region.
	 * </p>
	 * 
	 * @param region
	 *            is the requested region.
	 * 
	 * @return a <code>AmazonEC2</code> object which is already configured for
	 *         the requested region, or <code>null</code> if the requested
	 *         region is not valid.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 */
	public AmazonEC2 getAmazonEC2(String region) {
		if (region == null) {
			return null;
		}
		AmazonEC2 ec2 = null;
		if (getPooledEc2s().containsKey(region)) {
			ec2 = getPooledEc2s().get(region);
		}
		if (ec2 == null) {
			ec2 = new AmazonEC2Client(this, getCC());
			String ep = Common.getEndpoint(ec2, region);
			if (ep == null) {
				return null;
			}
			ec2.setEndpoint(ep);
			getPooledEc2s().put(region, ec2);
		}
		return ec2;
	}

}