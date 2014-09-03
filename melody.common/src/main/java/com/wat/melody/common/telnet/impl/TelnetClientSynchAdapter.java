package com.wat.melody.common.telnet.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;

import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.impl.exception.UnexpectedResultReceived;
import com.wat.melody.common.timeout.Timeout;

/**
 * <P>
 * Will read the data received by the given {@link TelnetClient} in the same
 * {@link Thread}. This class provide a method {@link #read()} which can be
 * interrupted with no delay.
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetClientSynchAdapter implements ITelnetClient {

	protected TelnetClient _tc = null;
	protected TelnetSession _ts = null;
	protected TelnetResponseAnalyzer _tra = null;
	private boolean _wasInterrupted = false;

	public TelnetClientSynchAdapter(TelnetClient tc, TelnetSession ts) {
		setTelnetClient(tc);
		setTelnetSession(ts);
	}

	private void setTelnetSession(TelnetSession ts) {
		if (ts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TelnetSession.class.getCanonicalName() + ".");
		}
		_ts = ts;
	}

	private void setTelnetClient(TelnetClient tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TelnetClient.class.getCanonicalName() + ".");
		}
		_tc = tc;
	}

	@Override
	public void setConnectTimeout(int connectTimeout) {
		_tc.setConnectTimeout(connectTimeout);
	}

	@Override
	public void setSoTimeout(int timeout) throws SocketException {
		_tc.setSoTimeout(timeout);
	}

	@Override
	public void setSoLinger(boolean on, int val) throws SocketException {
		_tc.setSoLinger(on, val);
	}

	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		_tc.setTcpNoDelay(on);
	}

	@Override
	public void setSendBufferSize(int size) throws SocketException {
		_tc.setSendBufferSize(size);
	}

	@Override
	public void setReceiveBufferSize(int size) throws SocketException {
		_tc.setReceiveBufferSize(size);
	}

	public void setKillTimeout(Timeout<Long> killTimeout) {
		// unmanaged attribute
	}

	@Override
	public void connect() throws IOException, InterruptedException {
		_tra = new TelnetResponseAnalyzer();
		_tc.connect(_ts.getHost(), _ts.getPort());
	}

	@Override
	public boolean isConnected() {
		return _tc.isConnected();
	}

	@Override
	public void disconnect() throws IOException {
		if (_tc.isConnected()) {
			_tc.disconnect();
		}
	}

	/**
	 * @param command
	 *            the command to execute on the remote host through this telnet
	 *            session.
	 * 
	 * @throws IOException
	 *             if on I/O error occurred (ex : socket error).
	 */
	@Override
	public void send(String line) throws IOException {
		OutputStream stdin = _tc.getOutputStream();
		stdin.write((line + "\r\n").getBytes());
		stdin.flush();
	}

	/**
	 * <p>
	 * Wait until a new char was received.
	 * </p>
	 * <p>
	 * If this method is interrupted, an InterruptedException is thrown
	 * immediately.
	 * </p>
	 * 
	 * @return the <tt>char</tt> that was received.
	 * 
	 * @throws IOException
	 *             if on I/O error occurred (ex : socket error).
	 * @throws InterruptedException
	 */
	@Override
	public char read() throws IOException, InterruptedException {
		if (Thread.interrupted()) {
			_wasInterrupted = true;
			throw new WrapperInterruptedException(
					Msg.bind(Messages.ExecMsg_FORCE_STOP_DONE,
							_ts.getConnectionDatas()),
					new WrapperInterruptedException(Messages.ExecEx_INTERRUPTED));
		}
		int cInt = 0;
		try {
			cInt = _tc.getInputStream().read();
		} catch (InterruptedIOException Ex) {
			_wasInterrupted = true;
			throw new WrapperInterruptedException(
					Msg.bind(Messages.ExecMsg_FORCE_STOP_DONE,
							_ts.getConnectionDatas()),
					new WrapperInterruptedException(
							Messages.ExecEx_INTERRUPTED, Ex));
		}
		if (cInt == -1) {
			throw new IOException("End of stream reached");
		}
		char c = (char) cInt;
		_tra.append(c);
		return c;
	}

	/**
	 * @param startToTruncate
	 *            Can be <tt>null</tt>.
	 * @param expected
	 *            is a set of expression, which, if detected in the output
	 *            produced by the given command, indicates that the command was
	 *            successfully proceed.
	 * @param unexpected
	 *            is a set of expression, which, if detected in the output
	 *            produced by the given command, indicates that the command was
	 *            proceed with error. Can be <tt>null</tt>.
	 * 
	 * @return A <tt>String</tt>, which contains the output produced by the
	 *         given command. If given, <tt>startToTruncate</tt> will be removed
	 *         from the beginning of the resulting string.
	 * 
	 * @throws UnexpectedResultReceived
	 *             if one of the given unexpected sequence can be found in the
	 *             received data.
	 * @throws IOException
	 *             if on I/O error occurred (ex : socket error).
	 * @throws InterruptedException
	 * @throws IllegalArgumentException
	 *             if <tt>expected</tt> is <tt>null</tt>.
	 */
	@Override
	public void waitUntil(OutputStream out, String startToTruncate,
			TelnetResponsesMatcher expected, TelnetResponsesMatcher unexpected)
			throws UnexpectedResultReceived, IOException, InterruptedException {
		if (expected == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TelnetResponsesMatcher.class.getCanonicalName() + ".");
		}
		while (true) {
			// will block until new data is available (or until interrupted)
			read();
			// try to find the given (un)expected pattern in the data received
			boolean found = _tra.analyze(out, startToTruncate, expected,
					unexpected);
			// if found, return
			if (found) {
				return;
			}
			// if not found, loop
		}
	}

	public boolean wasInterrupted() {
		return _wasInterrupted;
	}

}