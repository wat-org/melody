package com.wat.melody.plugin.aws.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.Provider;
import java.util.Arrays;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.wat.cloud.aws.ec2.AwsEc2Cloud;
import com.wat.cloud.aws.ec2.AwsEc2PooledConnection;
import com.wat.cloud.aws.s3.AwsS3PooledConnection;
import com.wat.cloud.aws.s3.StorageMode;
import com.wat.cloud.aws.s3.StorageModeConverter;
import com.wat.cloud.aws.s3.exception.IllegalStorageModeException;
import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.Melody;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.plugin.aws.common.exception.AwsPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsPlugInConfiguration implements IPlugInConfiguration,
		AWSCredentials {

	public static AwsPlugInConfiguration get()
			throws PlugInConfigurationException {
		return (AwsPlugInConfiguration) Melody.getContext()
				.getProcessorManager()
				.getPluginConfiguration(AwsPlugInConfiguration.class);
	}

	// MANDATORY CONFIGURATION DIRECTIVE
	public static final String AWS_ACCESS_KEY = "accessKey";
	public static final String AWS_SECRET_KEY = "secretKey";

	// OPTIONNAL CONFIGURATION DIRECTIVE
	public static final String AWS_CONNECTION_TIMEOUT = "aws.conn.socket.connect.timeout";
	public static final String AWS_READ_TIMEOUT = "aws.conn.socket.read.timeout";
	public static final String AWS_CONNECTION_RETRY = "aws.conn.socket.max.error.retry";
	public static final String AWS_SEND_BUFFER_SIZE_HINT = "aws.conn.socket.buffer.size.send";
	public static final String AWS_RECEIVE_BUFFER_SIZE_HINTS = "aws.conn.socket.buffer.size.receive";
	public static final String AWS_MAX_CONNECTION = "aws.conn.max.pool.size";

	public static final String AWS_PROTOCOL = "aws.conn.http.protocol";
	public static final String AWS_USER_AGENT = "aws.conn.http.useragent";

	public static final String AWS_PROXY_HOST = "aws.conn.proxy.host";
	public static final String AWS_PROXY_PORT = "aws.conn.proxy.port";
	public static final String AWS_PROXY_USERNAME = "aws.conn.proxy.username";
	public static final String AWS_PROXY_PASSWORD = "aws.conn.proxy.password";

	public static final String AWS_CLIENT_SIDE_ENCRYPTION_STORAGE_MODE = "aws.encryption.clientside.storagemode";
	public static final String AWS_CLIENT_SIDE_ENCRYPTION_PROVIDER = "aws.encryption.clientside.provider";

	private String _configurationFilePath;
	private String _accessKey;
	private String _secretKey;
	private ClientConfiguration _clientConfiguration;
	private CryptoConfiguration _cryptoConfiguration;

	public AwsPlugInConfiguration() {
		setClientConf(new ClientConfiguration());
		setCryptoConf(new CryptoConfiguration());
	}

	@Override
	public String getFilePath() {
		return _configurationFilePath;
	}

	private void setFilePath(String fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an AWS Plug-In Configuration file path).");
		}
		_configurationFilePath = fp;
	}

	@Override
	public void load(PropertySet ps) throws AwsPlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		setFilePath(ps.getSourceFile());

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
		loadClientSideEncyptionStorageMode(ps);
		loadClientSideEncyptionProvider(ps);

		validate();
	}

	private void loadAccessKey(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_ACCESS_KEY)) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, AWS_ACCESS_KEY));
		}
		try {
			setAccessKey(ps.get(AWS_ACCESS_KEY));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_ACCESS_KEY), Ex);
		}
	}

	private void loadSecretKey(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_SECRET_KEY)) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, AWS_SECRET_KEY));
		}
		try {
			setSecretKey(ps.get(AWS_SECRET_KEY));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_SECRET_KEY), Ex);
		}
	}

	private void loadConnectionTimeout(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(AWS_CONNECTION_TIMEOUT));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_CONNECTION_TIMEOUT),
					Ex);
		}
	}

	private void loadSocketTimeout(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_READ_TIMEOUT)) {
			return;
		}
		try {
			setSocketTimeout(ps.get(AWS_READ_TIMEOUT));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_READ_TIMEOUT), Ex);
		}
	}

	private void loadMaxErrorRetry(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_CONNECTION_RETRY)) {
			return;
		}
		try {
			setMaxErrorRetry(ps.get(AWS_CONNECTION_RETRY));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_CONNECTION_RETRY),
					Ex);
		}
	}

	private void loadMaxConnections(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_MAX_CONNECTION)) {
			return;
		}
		try {
			setMaxConnections(ps.get(AWS_MAX_CONNECTION));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_MAX_CONNECTION), Ex);
		}
	}

	private void loadSocketSendBufferSizeHints(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_SEND_BUFFER_SIZE_HINT)) {
			return;
		}
		try {
			setSocketSendBufferSizeHints(ps.get(AWS_SEND_BUFFER_SIZE_HINT));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					AWS_SEND_BUFFER_SIZE_HINT), Ex);
		}
	}

	private void loadSocketReceiveBufferSizeHints(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_RECEIVE_BUFFER_SIZE_HINTS)) {
			return;
		}
		try {
			setSocketReceiveBufferSizeHints(ps
					.get(AWS_RECEIVE_BUFFER_SIZE_HINTS));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					AWS_RECEIVE_BUFFER_SIZE_HINTS), Ex);
		}
	}

	private void loadProtocol(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_PROTOCOL)) {
			return;
		}
		try {
			setProtocol(ps.get(AWS_PROTOCOL));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_PROTOCOL), Ex);
		}
	}

	private void loadUserAgent(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_USER_AGENT)) {
			return;
		}
		try {
			setUserAgent(ps.get(AWS_USER_AGENT));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_USER_AGENT), Ex);
		}
	}

	private void loadProxyHost(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_PROXY_HOST)) {
			return;
		}
		try {
			setProxyHost(ps.get(AWS_PROXY_HOST));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_PROXY_HOST), Ex);
		}
	}

	private void loadProxyPort(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_PROXY_PORT)) {
			return;
		}
		try {
			setProxyPort(ps.get(AWS_PROXY_PORT));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_PROXY_PORT), Ex);
		}
	}

	private void loadProxyUsername(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_PROXY_USERNAME)) {
			return;
		}
		try {
			setProxyUsername(ps.get(AWS_PROXY_USERNAME));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_PROXY_USERNAME), Ex);
		}
	}

	private void loadProxyPassword(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_PROXY_PASSWORD)) {
			return;
		}
		try {
			setProxyPassword(ps.get(AWS_PROXY_PASSWORD));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, AWS_PROXY_PASSWORD), Ex);
		}
	}

	private void loadClientSideEncyptionStorageMode(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_CLIENT_SIDE_ENCRYPTION_STORAGE_MODE)) {
			return;
		}
		try {
			setClientSideEncryptionStorageMode(ps
					.get(AWS_CLIENT_SIDE_ENCRYPTION_STORAGE_MODE));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					AWS_CLIENT_SIDE_ENCRYPTION_STORAGE_MODE), Ex);
		}
	}

	private void loadClientSideEncyptionProvider(PropertySet ps)
			throws AwsPlugInConfigurationException {
		if (!ps.containsKey(AWS_CLIENT_SIDE_ENCRYPTION_PROVIDER)) {
			return;
		}
		try {
			setClientSideEncryptionProvider(ps
					.get(AWS_CLIENT_SIDE_ENCRYPTION_PROVIDER));
		} catch (AwsPlugInConfigurationException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					AWS_CLIENT_SIDE_ENCRYPTION_PROVIDER), Ex);
		}
	}

	private void validate() throws AwsPlugInConfigurationException {
		try {
			AwsEc2Cloud.validate(new AmazonEC2Client(this, getClientConf()));
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().equalsIgnoreCase("AuthFailure")) {
				throw new AwsPlugInConfigurationException(Msg.bind(
						Messages.ConfEx_INVALID_AWS_CREDENTIALS,
						getAWSAccessKeyId(), getAWSSecretKey(), AWS_ACCESS_KEY,
						AWS_SECRET_KEY, getFilePath()));
			} else {
				throw new AwsPlugInConfigurationException(
						Messages.ConfEx_VALIDATION, Ex);
			}
		} catch (AmazonClientException Ex) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_VALIDATION, Ex);
		}
	}

	public ClientConfiguration getClientConf() {
		return _clientConfiguration;
	}

	public ClientConfiguration setClientConf(ClientConfiguration cc) {
		if (cc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ClientConfiguration.class.getCanonicalName() + ".");
		}
		ClientConfiguration previous = getClientConf();
		_clientConfiguration = cc;
		return previous;
	}

	public CryptoConfiguration getCryptoConf() {
		return _cryptoConfiguration;
	}

	public CryptoConfiguration setCryptoConf(CryptoConfiguration cc) {
		if (cc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ CryptoConfiguration.class.getCanonicalName() + ".");
		}
		CryptoConfiguration previous = getCryptoConf();
		_cryptoConfiguration = cc;
		return previous;
	}

	@Override
	public String getAWSAccessKeyId() {
		return _accessKey;
	}

	public String setAccessKey(String accessKey)
			throws AwsPlugInConfigurationException {
		if (accessKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the AWS Acces Key ID).");
		}
		if (accessKey.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getAWSAccessKeyId();
		_accessKey = accessKey;
		return previous;
	}

	@Override
	public String getAWSSecretKey() {
		return _secretKey;
	}

	public String setSecretKey(String secretKey)
			throws AwsPlugInConfigurationException {
		if (secretKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the AWS Secret Key).");
		}
		if (secretKey.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getAWSSecretKey();
		_secretKey = secretKey;
		return previous;
	}

	public int getConnectionTimeout() {
		return getClientConf().getConnectionTimeout();
	}

	public int setConnectionTimeout(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the connection timeout; a positive Integer or "
					+ "zero).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setConnectionTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, val));
		}
	}

	public int setConnectionTimeout(int ival)
			throws AwsPlugInConfigurationException {
		if (ival < 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_CONNECTION_TIMEOUT, ival));
		}
		int previous = getConnectionTimeout();
		getClientConf().setConnectionTimeout(ival);
		return previous;
	}

	public int getSocketTimeout() {
		return getClientConf().getSocketTimeout();
	}

	public int setSocketTimeout(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the socket timeout; a positive Integer or zero).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketTimeout(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, val));
		}
	}

	public int setSocketTimeout(int ival)
			throws AwsPlugInConfigurationException {
		if (ival < 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_READ_TIMEOUT, ival));
		}
		int previous = getSocketTimeout();
		getClientConf().setSocketTimeout(ival);
		return previous;
	}

	public int getMaxErrorRetry() {
		return getClientConf().getMaxErrorRetry();
	}

	public int setMaxErrorRetry(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the retry attempts; a positive Integer or zero).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setMaxErrorRetry(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_RETRY, val));
		}
	}

	public int setMaxErrorRetry(int ival)
			throws AwsPlugInConfigurationException {
		if (ival < 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_RETRY, ival));
		}
		int previous = getMaxErrorRetry();
		getClientConf().setMaxErrorRetry(ival);
		return previous;
	}

	public int getMaxConnections() {
		return getClientConf().getMaxConnections();
	}

	public int setMaxConnections(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the maximum connection amount; a positive Integer "
					+ "or zero).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setMaxConnections(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_MAX_CONN, val));
		}
	}

	public int setMaxConnections(int ival)
			throws AwsPlugInConfigurationException {
		if (ival < 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_MAX_CONN, ival));
		}
		int previous = getMaxConnections();
		getClientConf().setMaxConnections(ival);
		return previous;
	}

	public int getSocketSendBufferSizeHints() {
		return getClientConf().getSocketBufferSizeHints()[0];
	}

	public int setSocketSendBufferSizeHints(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Send Buffer Size; a positive Integer).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketSendBufferSizeHints(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_SEND_BUFFSIZE, val));
		}
	}

	public int setSocketSendBufferSizeHints(int ival)
			throws AwsPlugInConfigurationException {
		if (ival <= 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_SEND_BUFFSIZE, ival));
		}
		int previous = getSocketSendBufferSizeHints();
		getClientConf().setSocketBufferSizeHints(ival,
				getSocketReceiveBufferSizeHints());
		return previous;
	}

	public int getSocketReceiveBufferSizeHints() {
		return getClientConf().getSocketBufferSizeHints()[1];
	}

	public int setSocketReceiveBufferSizeHints(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Receive Buffer Size; a positive Integer).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setSocketReceiveBufferSizeHints(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_RECEIVE_BUFFSIZE, val));
		}
	}

	public int setSocketReceiveBufferSizeHints(int ival)
			throws AwsPlugInConfigurationException {
		if (ival <= 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_RECEIVE_BUFFSIZE, ival));
		}
		int previous = getSocketReceiveBufferSizeHints();
		getClientConf().setSocketBufferSizeHints(
				getSocketSendBufferSizeHints(), ival);
		return previous;
	}

	public Protocol getProtocol() {
		return getClientConf().getProtocol();
	}

	public Protocol setProtocol(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a Protocol; accepted values are '"
					+ Arrays.asList(Protocol.values()) + "').");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		Protocol previous = getProtocol();
		if (val.equalsIgnoreCase(Protocol.HTTP.toString())) {
			getClientConf().setProtocol(Protocol.HTTP);
		} else if (val.equalsIgnoreCase(Protocol.HTTPS.toString())) {
			getClientConf().setProtocol(Protocol.HTTPS);
		} else {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_PROTOCOL, val, Protocol.values()
							.toString()));
		}
		return previous;
	}

	public String getUserAgent() {
		return getClientConf().getUserAgent();
	}

	public String setUserAgent(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the User Agent).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getUserAgent();
		getClientConf().setUserAgent(val);
		return previous;
	}

	public String getProxyHost() {
		return getClientConf().getProxyHost();
	}

	public String setProxyHost(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Proxy Host FQDN or IP address used to connect"
					+ " to AWS).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			InetAddress.getByName(val);
		} catch (UnknownHostException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_PROXY_HOST, val), Ex);
		}
		String previous = getProxyHost();
		getClientConf().setProxyHost(val);
		return previous;
	}

	public int getProxyPort() {
		return getClientConf().getProxyPort();
	}

	public int setProxyPort(String val) throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Proxy Port used to connect to AWS; "
					+ "a positive Integer).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		try {
			return setProxyPort(Integer.parseInt(val));
		} catch (NumberFormatException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_PROXY_PORT, val));
		}
	}

	public int setProxyPort(int ival) throws AwsPlugInConfigurationException {
		if (ival < 0) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_PROXY_PORT, ival));
		}
		int previous = getProxyPort();
		getClientConf().setProxyPort(ival);
		return previous;
	}

	public String getProxyUsername() {
		return getClientConf().getProxyUsername();
	}

	public String setProxyUsername(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Proxy Username used to connect to AWS).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getProxyUsername();
		getClientConf().setProxyUsername(val);
		return previous;
	}

	public String getProxyPassword() {
		return getClientConf().getProxyPassword();
	}

	public String setProxyPassword(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the Proxy Password used to connect to AWS).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		String previous = getProxyPassword();
		getClientConf().setProxyPassword(val);
		return previous;
	}

	public StorageMode getClientSideEncryptionStorageMode() {
		return StorageModeConverter.convert(getCryptoConf().getStorageMode());
	}

	public StorageMode setClientSideEncryptionStorageMode(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the client side encryption storage mode).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		// validate input
		StorageMode sm = null;
		try {
			sm = StorageMode.parseString(val);
		} catch (IllegalStorageModeException Ex) {
			throw new AwsPlugInConfigurationException(Ex);
		}
		StorageMode previous = getClientSideEncryptionStorageMode();
		getCryptoConf().setStorageMode(StorageModeConverter.convert(sm));
		return previous;
	}

	public Provider getClientSideEncryptionProvider() {
		return getCryptoConf().getCryptoProvider();
	}

	public Provider setClientSideEncryptionProvider(String val)
			throws AwsPlugInConfigurationException {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the canomical class name of a Security Provider, "
					+ "used to perform client side encryption).");
		}
		if (val.trim().length() == 0) {
			throw new AwsPlugInConfigurationException(
					Messages.ConfEx_EMPTY_DIRECTIVE);
		}
		// validate input
		Provider p = null;
		try {
			p = (Provider) Class.forName(val).getConstructor().newInstance();
		} catch (ClassNotFoundException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_PROVIDER_CNF, val));
		} catch (NoClassDefFoundError Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_PROVIDER_NCDF, val, Ex.getMessage()
							.replaceAll("/", ".")));
		} catch (NoSuchMethodException | IllegalAccessException
				| InstantiationException | ClassCastException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_PROVIDER_IS, val,
					Provider.class.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new AwsPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_PROVIDER_IE, val), Ex.getCause());
		}
		Provider previous = getClientSideEncryptionProvider();
		getCryptoConf().setCryptoProvider(p);
		return previous;
	}

	/**
	 * @param region
	 *            is the requested region.
	 * 
	 * @return an {@link AmazonEC2} object connected to the requested region, or
	 *         <tt>null</tt> if the requested region is not valid.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given region is <tt>null</tt>.
	 * @throws AmazonServiceException
	 *             if the operation fails (ex: credentials not valid).
	 * @throws AmazonClientException
	 *             if the operation fails (ex: network error).
	 */
	public AmazonEC2 getAwsEc2Connection(String region) {
		return AwsEc2PooledConnection.getPooledConnection(region, this,
				getClientConf());
	}

	/**
	 * @return an {@link AmazonS3} object.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails (ex: credentials not valid).
	 * @throws AmazonClientException
	 *             if the operation fails (ex: network error).
	 */
	public AmazonS3 getAwsS3Connection() {
		return AwsS3PooledConnection.getPooledConnection(this, getClientConf(),
				null, null);
	}

	/**
	 * @param kprp
	 *            specifies the key-pair repository path, where stand the
	 *            key-pair.
	 * @param kpn
	 *            specifies the key-pair name of the desired key-pair which will
	 *            be used for the client-side encryption. If the key-pair
	 *            doesn't exists in the given key-pair repository, it will be
	 *            created.
	 * @param kpn
	 *            specifies the passphrase of the desired key-pair.
	 * 
	 * @return an {@link AmazonS3} object, which perform client side encryption.
	 * 
	 * @throws IllegalArgumentException
	 *             if either the repo, the name or the size is <tt>null</tt>.
	 * @throws IOException
	 *             if an IO error occurred while reading/creating the
	 *             {@link KeyPair} in this Repository.
	 * @throws IllegalPassphraseException
	 *             if the key already exists but the given pass-phrase is not
	 *             correct (the key can't be decrypted).
	 * @throws AmazonServiceException
	 *             if the operation fails (ex: credentials not valid).
	 * @throws AmazonClientException
	 *             if the operation fails (ex: network error).
	 */
	public AmazonS3 getAwsS3Connection(KeyPairRepositoryPath kprp,
			KeyPairName kpn, KeyPairSize kps, String passphrase)
			throws IOException, IllegalPassphraseException {
		KeyPairRepository kpr = KeyPairRepository.getKeyPairRepository(kprp);
		KeyPair kp = kpr.createKeyPair(kpn, kps, passphrase);
		EncryptionMaterials enc = new EncryptionMaterials(kp);
		return AwsS3PooledConnection.getPooledConnection(this, getClientConf(),
				enc, getCryptoConf());
	}

}