package com.wat.melody.plugin.telnet.common;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.Melody;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.bool.exception.IllegalBooleanException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.telnet.ITelnetSessionConfiguration;
import com.wat.melody.common.telnet.impl.TelnetSessionConfiguration;
import com.wat.melody.common.telnet.types.ConnectionRetry;
import com.wat.melody.common.telnet.types.ConnectionTimeout;
import com.wat.melody.common.telnet.types.ReadTimeout;
import com.wat.melody.common.telnet.types.ReceiveBufferSize;
import com.wat.melody.common.telnet.types.SendBufferSize;
import com.wat.melody.common.telnet.types.SoLinger;
import com.wat.melody.common.telnet.types.exception.IllegalConnectionRetryException;
import com.wat.melody.common.telnet.types.exception.IllegalReceiveBufferSizeException;
import com.wat.melody.common.telnet.types.exception.IllegalSendBufferSizeException;
import com.wat.melody.common.telnet.types.exception.IllegalSoLingerException;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.plugin.telnet.common.exception.TelnetPlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetPlugInConfiguration implements IPlugInConfiguration,
		ITelnetSessionConfiguration {

	public static TelnetPlugInConfiguration get()
			throws PlugInConfigurationException {
		return (TelnetPlugInConfiguration) Melody.getContext()
				.getProcessorManager()
				.getPluginConfiguration(TelnetPlugInConfiguration.class);
	}

	// MANDATORY CONFIGURATION DIRECTIVE

	// OPTIONNAL CONFIGURATION DIRECTIVE
	public static final String CONNECTION_RETRY = "telnet.conn.socket.connect.retry";
	public static final String CONNECTION_TIMEOUT = "telnet.conn.socket.connect.timeout";
	public static final String READ_TIMEOUT = "telnet.conn.socket.read.timeout";
	public static final String SO_LINGER = "telnet.conn.socket.solinger";
	public static final String TCP_NODELAY = "telnet.conn.socket.tcpnodelay";
	public static final String SEND_BUFFER_SIZE_HINT = "telnet.conn.socket.buffer.size.send";
	public static final String RECEIVE_BUFFER_SIZE_HINTS = "telnet.conn.socket.buffer.size.receive";

	private String _configurationFilePath;
	private ITelnetSessionConfiguration _telnetSessionConfiguration;

	public TelnetPlugInConfiguration() {
		setTelnetSessionConfiguration(new TelnetSessionConfiguration());
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

	private ITelnetSessionConfiguration getTelnetSessionConfiguration() {
		return _telnetSessionConfiguration;
	}

	private void setTelnetSessionConfiguration(ITelnetSessionConfiguration fp) {
		if (fp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetSessionConfiguration.class.getCanonicalName()
					+ ".");
		}
		_telnetSessionConfiguration = fp;
	}

	@Override
	public void load(PropertySet ps) throws PlugInConfigurationException {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		setFilePath(ps.getSourceFile());

		loadConnectionTimeout(ps);
		loadConnectionRetry(ps);

		loadReadTimeout(ps);
		loadSoLinger(ps);
		loadTcpNoDelay(ps);
		loadSendBufferSize(ps);
		loadReceiveBufferSize(ps);

		validate();
	}

	private void loadConnectionTimeout(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(CONNECTION_TIMEOUT)) {
			return;
		}
		try {
			setConnectionTimeout(ps.get(CONNECTION_TIMEOUT));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, CONNECTION_TIMEOUT), Ex);
		}
	}

	private void loadConnectionRetry(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(CONNECTION_RETRY)) {
			return;
		}
		try {
			setConnectionRetry(ps.get(CONNECTION_RETRY));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, CONNECTION_RETRY), Ex);
		}
	}

	private void loadReadTimeout(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(READ_TIMEOUT)) {
			return;
		}
		try {
			setReadTimeout(ps.get(READ_TIMEOUT));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, READ_TIMEOUT), Ex);
		}
	}

	private void loadSoLinger(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(SO_LINGER)) {
			return;
		}
		try {
			setSoLinger(ps.get(SO_LINGER));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SO_LINGER), Ex);
		}
	}

	private void loadTcpNoDelay(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(TCP_NODELAY)) {
			return;
		}
		try {
			setTcpNoDelay(ps.get(TCP_NODELAY));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TCP_NODELAY), Ex);
		}
	}

	private void loadSendBufferSize(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(SEND_BUFFER_SIZE_HINT)) {
			return;
		}
		try {
			setSendBufferSize(ps.get(SEND_BUFFER_SIZE_HINT));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, SEND_BUFFER_SIZE_HINT),
					Ex);
		}
	}

	private void loadReceiveBufferSize(PropertySet ps)
			throws TelnetPlugInConfigurationException {
		if (!ps.containsKey(RECEIVE_BUFFER_SIZE_HINTS)) {
			return;
		}
		try {
			setReceiveBufferSize(ps.get(RECEIVE_BUFFER_SIZE_HINTS));
		} catch (TelnetPlugInConfigurationException Ex) {
			throw new TelnetPlugInConfigurationException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					RECEIVE_BUFFER_SIZE_HINTS), Ex);
		}
	}

	private void validate() throws TelnetPlugInConfigurationException {
		// nothing to do
	}

	@Override
	public ConnectionTimeout getConnectionTimeout() {
		return getTelnetSessionConfiguration().getConnectionTimeout();
	}

	@Override
	public ConnectionTimeout setConnectionTimeout(ConnectionTimeout val) {
		return getTelnetSessionConfiguration().setConnectionTimeout(val);
	}

	public ConnectionTimeout setConnectionTimeout(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setConnectionTimeout(ConnectionTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public ConnectionRetry getConnectionRetry() {
		return getTelnetSessionConfiguration().getConnectionRetry();
	}

	@Override
	public ConnectionRetry setConnectionRetry(ConnectionRetry val) {
		return getTelnetSessionConfiguration().setConnectionRetry(val);
	}

	public ConnectionRetry setConnectionRetry(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setConnectionRetry(ConnectionRetry.parseString(val));
		} catch (IllegalConnectionRetryException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public ReadTimeout getReadTimeout() {
		return getTelnetSessionConfiguration().getReadTimeout();
	}

	@Override
	public ReadTimeout setReadTimeout(ReadTimeout val) {
		return getTelnetSessionConfiguration().setReadTimeout(val);
	}

	public ReadTimeout setReadTimeout(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setReadTimeout(ReadTimeout.parseString(val));
		} catch (IllegalTimeoutException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public SoLinger getSoLinger() {
		return getTelnetSessionConfiguration().getSoLinger();
	}

	@Override
	public SoLinger setSoLinger(SoLinger val) {
		return getTelnetSessionConfiguration().setSoLinger(val);
	}

	public SoLinger setSoLinger(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setSoLinger(SoLinger.parseString(val));
		} catch (IllegalSoLingerException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public boolean getTcpNoDelay() {
		return getTelnetSessionConfiguration().getTcpNoDelay();
	}

	@Override
	public boolean setTcpNoDelay(boolean val) {
		return getTelnetSessionConfiguration().setTcpNoDelay(val);
	}

	public boolean setTcpNoDelay(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setTcpNoDelay(Bool.parseString(val));
		} catch (IllegalBooleanException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public SendBufferSize getSendBufferSize() {
		return getTelnetSessionConfiguration().getSendBufferSize();
	}

	@Override
	public SendBufferSize setSendBufferSize(SendBufferSize val) {
		return getTelnetSessionConfiguration().setSendBufferSize(val);
	}

	public SendBufferSize setSendBufferSize(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setSendBufferSize(SendBufferSize.parseString(val));
		} catch (IllegalSendBufferSizeException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

	@Override
	public ReceiveBufferSize getReceiveBufferSize() {
		return getTelnetSessionConfiguration().getReceiveBufferSize();
	}

	@Override
	public ReceiveBufferSize setReceiveBufferSize(ReceiveBufferSize val) {
		return getTelnetSessionConfiguration().setReceiveBufferSize(val);
	}

	public ReceiveBufferSize setReceiveBufferSize(String val)
			throws TelnetPlugInConfigurationException {
		try {
			return setReceiveBufferSize(ReceiveBufferSize.parseString(val));
		} catch (IllegalReceiveBufferSizeException Ex) {
			throw new TelnetPlugInConfigurationException(Ex);
		}
	}

}