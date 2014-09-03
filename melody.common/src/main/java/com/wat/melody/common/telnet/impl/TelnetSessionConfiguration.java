package com.wat.melody.common.telnet.impl;

import com.wat.melody.common.telnet.ITelnetSessionConfiguration;
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

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetSessionConfiguration implements ITelnetSessionConfiguration {

	private static ConnectionTimeout createConnectionTimeout(int timeout) {
		try {
			return ConnectionTimeout.parseInt(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ConnectionTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ConnectionRetry createConnectionRetry(int connretry) {
		try {
			return ConnectionRetry.parseInt(connretry);
		} catch (IllegalConnectionRetryException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ConnectionRetry with value '" + connretry + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ReadTimeout createReadTimeout(int timeout) {
		try {
			return ReadTimeout.parseInt(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ReadTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static SoLinger createSoLinger(int timeout) {
		try {
			return SoLinger.parseInt(timeout);
		} catch (IllegalSoLingerException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a SoLinger with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static SendBufferSize createSendBufferSize(int size) {
		try {
			return SendBufferSize.parseInt(size);
		} catch (IllegalSendBufferSizeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a SendBufferSize with value '" + size + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ReceiveBufferSize createReceiveBufferSize(int size) {
		try {
			return ReceiveBufferSize.parseInt(size);
		} catch (IllegalReceiveBufferSizeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ReceiveBufferSize with value '" + size + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ConnectionTimeout DEFAULT_CONNECTION_TIMEOUT = createConnectionTimeout(60000);
	private static ConnectionRetry DEFAULT_CONNECTION_RETRY = createConnectionRetry(3);
	private static ReadTimeout DEFAULT_READ_TIMEOUT = createReadTimeout(60000);
	private static SoLinger DEFAULT_SO_LINGER = createSoLinger(-1);
	private static boolean DEFAULT_TCP_NODELAY = false;
	private static SendBufferSize DEFAULT_SEND_BUFFER_SIZE = createSendBufferSize(0);
	private static ReceiveBufferSize DEFAULT_RECEIVE_BUFFER_SIZE = createReceiveBufferSize(0);

	private ConnectionTimeout _connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private ConnectionRetry _connectionRetry = DEFAULT_CONNECTION_RETRY;
	private ReadTimeout _readTimeout = DEFAULT_READ_TIMEOUT;
	private SoLinger _soLinger = DEFAULT_SO_LINGER;
	private boolean _tcpNoDelay = DEFAULT_TCP_NODELAY;
	private SendBufferSize _sendBufferSize = DEFAULT_SEND_BUFFER_SIZE;
	private ReceiveBufferSize _receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;

	public TelnetSessionConfiguration() {
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("connection-timeout:");
		str.append(getConnectionTimeout());
		str.append(", connection-retry:");
		str.append(getConnectionRetry());
		str.append(", read-timeout:");
		str.append(getReadTimeout());
		str.append(", so-linger:");
		str.append(getSoLinger());
		str.append(", tcp-no-delay:");
		str.append(getTcpNoDelay());
		str.append(", send-buffer-size:");
		str.append(getSendBufferSize());
		str.append(", receive-buffer-size:");
		str.append(getReceiveBufferSize());
		str.append(" }");
		return str.toString();
	}

	public ConnectionTimeout getConnectionTimeout() {
		return _connectionTimeout;
	}

	public ConnectionTimeout setConnectionTimeout(ConnectionTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_CONNECTION_TIMEOUT;
		}
		ConnectionTimeout previous = getConnectionTimeout();
		_connectionTimeout = timeout;
		return previous;
	}

	@Override
	public ConnectionRetry getConnectionRetry() {
		return _connectionRetry;
	}

	@Override
	public ConnectionRetry setConnectionRetry(ConnectionRetry connectionRetry) {
		if (connectionRetry == null) {
			connectionRetry = DEFAULT_CONNECTION_RETRY;
		}
		ConnectionRetry previous = getConnectionRetry();
		_connectionRetry = connectionRetry;
		return previous;
	}

	public ReadTimeout getReadTimeout() {
		return _readTimeout;
	}

	public ReadTimeout setReadTimeout(ReadTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_READ_TIMEOUT;
		}
		ReadTimeout previous = getReadTimeout();
		_readTimeout = timeout;
		return previous;
	}

	public SoLinger getSoLinger() {
		return _soLinger;
	}

	public SoLinger setSoLinger(SoLinger soLinger) {
		if (soLinger == null) {
			soLinger = DEFAULT_SO_LINGER;
		}
		SoLinger previous = getSoLinger();
		_soLinger = soLinger;
		return previous;
	}

	public boolean getTcpNoDelay() {
		return _tcpNoDelay;
	}

	public boolean setTcpNoDelay(boolean tcpNoDelay) {
		boolean previous = getTcpNoDelay();
		_tcpNoDelay = tcpNoDelay;
		return previous;
	}

	public SendBufferSize getSendBufferSize() {
		return _sendBufferSize;
	}

	public SendBufferSize setSendBufferSize(SendBufferSize size) {
		if (size == null) {
			size = DEFAULT_SEND_BUFFER_SIZE;
		}
		SendBufferSize previous = getSendBufferSize();
		_sendBufferSize = size;
		return previous;
	}

	public ReceiveBufferSize getReceiveBufferSize() {
		return _receiveBufferSize;
	}

	public ReceiveBufferSize setReceiveBufferSize(ReceiveBufferSize size) {
		if (size == null) {
			size = DEFAULT_RECEIVE_BUFFER_SIZE;
		}
		ReceiveBufferSize previous = getReceiveBufferSize();
		_receiveBufferSize = size;
		return previous;
	}

}