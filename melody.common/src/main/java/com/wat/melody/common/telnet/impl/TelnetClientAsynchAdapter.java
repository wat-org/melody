package com.wat.melody.common.telnet.impl;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * <P>
 * Will read the data received by the given {@link TelnetClient} in a dedicated
 * {@link Thread}. Unlike the method {@link TelnetClientSynchAdapter#read()},
 * this class provide a method {@link #read()} which can be interrupted with a
 * delay (see {@link #setKillTimeout(Timeout)}).
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetClientAsynchAdapter extends TelnetClientSynchAdapter
		implements Runnable {

	private static Logger log = LoggerFactory.getLogger(TelnetSession.class);

	private static GenericTimeout createKillTimeout(int timeout) {
		try {
			return GenericTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenericTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private Thread _thread = null;
	private IOException _ioex = null;

	private InterruptedException _iex = null;
	private long _iexTime = 0;
	private Timeout<Long> _killTimeout = null;

	public TelnetClientAsynchAdapter(TelnetClient tc, TelnetSession ts) {
		super(tc, ts);
	}

	@Override
	public void run() {
		try {
			while (true) {
				int cInt = 0;
				try {
					// read (will wait until new data is available)
					cInt = _tc.getInputStream().read();
					if (cInt == -1) {
						throw new IOException("End of stream reached");
					}
					// store data
					_tra.append((char) cInt);
					// notify reader
					synchronized (this) {
						notify();
					}
				} catch (InterruptedIOException ignored) {
					/*
					 * when interrupted, read() will throw an
					 * InterruptedIOException. We ignore it because we want this
					 * thread to continue reading even if interrupted.
					 */
				}
			}
		} catch (IOException Ex) {
			// store the exception. Will be thrown by {@link #read()}
			synchronized (this) {
				_ioex = Ex;
			}
		}
	}

	@Override
	public void setKillTimeout(Timeout<Long> killTimeout) {
		if (killTimeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Timeout.class.getCanonicalName()
					+ ".");
		}
		_killTimeout = killTimeout;
	}

	@Override
	public void connect() throws IOException, InterruptedException {
		super.connect();
		_killTimeout = createKillTimeout(0);
		_thread = new Thread(this, "Telnet Reader");
		_thread.setDaemon(true);
		_thread.start();
	}

	/**
	 * <p>
	 * Wait until a new char was received.
	 * </p>
	 * <p>
	 * If this method is interrupted, it will continue to read data until the
	 * kill timeout elapsed. If the kill timeout elapsed before a new char is
	 * received, an InterruptedException is thrown.
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
		while (true) {
			if (Thread.interrupted()) {
				// if interrupted, mark it and continue to receive data
				// until kill timeout elapsed
				if (_iex == null) {
					_iexTime = System.currentTimeMillis();
					_iex = new WrapperInterruptedException(
							Messages.ExecEx_INTERRUPTED);
					log.info(Msg.bind(Messages.ExecMsg_GRACEFULL_STOP,
							_killTimeout.getTimeoutInMillis()));
				}
			}
			// if the thread was interrupted
			// and the kill timeout elapsed, raise interruption
			if (_iex != null
					&& System.currentTimeMillis() - _iexTime > _killTimeout
							.getTimeoutInMillis()) {
				log.warn(Msg.bind(Messages.ExecMsg_FORCE_STOP,
						_killTimeout.getTimeoutInMillis()));
				throw new WrapperInterruptedException(Msg.bind(
						Messages.ExecMsg_FORCE_STOP_DONE,
						_ts.getConnectionDatas()), _iex);
			}
			// if new data was received, return it
			Character next = _tra.readNext();
			if (next != null) {
				return next;
			}
			// if an error raised during data reception, raise it
			synchronized (this) {
				if (_ioex != null) {
					if (Thread.interrupted()) {
						throw new WrapperInterruptedException(Msg.bind(
								Messages.ExecMsg_FORCE_STOP_DONE,
								_ts.getConnectionDatas()));
					}
					throw _ioex;
				}
			}
			// Wait max 2 sec for new data
			synchronized (this) {
				try {
					wait(2000);
				} catch (InterruptedException Ex) {
					// if interrupted, mark it and continue to receive data
					// until kill timeout elapsed
					if (_iex != null) {
						continue;
					}
					_iexTime = System.currentTimeMillis();
					_iex = new WrapperInterruptedException(
							Messages.ExecEx_INTERRUPTED, Ex);
					log.info(Msg.bind(Messages.ExecMsg_GRACEFULL_STOP,
							_killTimeout.getTimeoutInMillis()));
				}
			}
		}
	}

	@Override
	public boolean wasInterrupted() {
		return _iex != null;
	}

}