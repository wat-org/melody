package com.wat.melody.common.ssh.impl;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class RemoteExec {

	private static Logger log = LoggerFactory.getLogger(RemoteExec.class);

	private static Logger ex = LoggerFactory.getLogger("exception."
			+ RemoteExec.class.getName());

	private static GenericTimeout createGenericTimeout(int timeout) {
		try {
			return new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenericTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static final GenericTimeout DEFAULT_KILL_TIMEOUT = createGenericTimeout(30000);

	private SshSession _session;
	private String _cmd;
	private boolean _requiretty;
	private OutputStream _out;
	private OutputStream _err;
	private GenericTimeout _killTimeout = DEFAULT_KILL_TIMEOUT;

	protected RemoteExec(SshSession session, String command,
			boolean requiretty, OutputStream outStream, OutputStream errStream) {
		setSession(session);
		setCommand(command);
		setRequiretty(requiretty);
		setOutputStream(outStream);
		setErrorStream(errStream);
	}

	protected RemoteExec(SshSession session, String command,
			boolean requiretty, OutputStream outStream, OutputStream errStream,
			GenericTimeout killTimeout) {
		this(session, command, requiretty, outStream, errStream);
		setKillTimeout(killTimeout);
	}

	public int exec() throws InterruptedException {
		ChannelExec channel = null;
		InterruptedException iex = null;
		long start = 0;
		long timeout = getKillTimeout().getTimeoutInMillis();

		int retry = 3;
		while (true) {
			try {
				channel = getSession().openExecChannel();
				channel.setCommand(getCommand());
				channel.setPty(getRequiretty());
				channel.setInputStream(null);
				channel.setOutputStream(getOutputStream());
				channel.setErrStream(getErrorStream());

				channel.connect();
				while (true) {
					/*
					 * Can't find a way to 'join' the session or the channel ...
					 * So we're pooling the isClosed ....
					 */
					if (channel.isClosed()) {
						break;
					}
					// were we interrupted ?
					if (iex != null
							&& System.currentTimeMillis() - start > timeout) {
						log.warn(Msg.bind(Messages.ExecMsg_FORCE_STOP, timeout));
						throw iex;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException Ex) {
						if (iex != null) {
							continue;
						}
						start = System.currentTimeMillis();
						iex = new WrapperInterruptedException(
								Messages.ExecEx_INTERRUPTED, Ex);
						log.info(Msg.bind(Messages.ExecMsg_GRACEFULL_STOP,
								timeout));
					}
					if (Thread.interrupted()) {
						if (iex != null) {
							continue;
						}
						start = System.currentTimeMillis();
						iex = new InterruptedException(
								Messages.ExecEx_INTERRUPTED);
						log.info(Msg.bind(Messages.ExecMsg_GRACEFULL_STOP,
								timeout));
					}
				}
				break;
			} catch (JSchException Ex) {
				String msg = Ex.getMessage();
				msg = msg != null ? msg : "";
				/*
				 * Sometimes, don't know why, connect operation fail with
				 * message 'channel is not opened'... When such error happened,
				 * we observe that if we close the channel (see finally
				 * statement), open the channel again, and connect, the command
				 * is successfully executed. That's why we introduce a retry
				 * mechanism.
				 */
				if (msg.indexOf("channel is not opened") != -1) {
					if (--retry > 0) {
						MelodyException mex = new MelodyException(Ex);
						log.warn(Msg.bind(Messages.ExecMsg_CHANNEL_CLOSED,
								retry, mex.getUserFriendlyStackTrace()));
						ex.warn(Msg.bind(Messages.ExecMsg_CHANNEL_CLOSED,
								retry, mex.getFullStackTrace()));
						continue;
					}
				}
				throw new RuntimeException("Failed to exec an ssh command "
						+ "through a JSch 'exec' Channel.", Ex);
			} catch (InterruptedException Ex) {
				throw new WrapperInterruptedException(Msg.bind(
						Messages.ExecMsg_FORCE_STOP_DONE, getSession()
								.getConnectionDatas()), Ex);
			} finally {
				if (channel != null) {
					channel.disconnect(); // This closes stream
				}
			}
		}
		if (iex != null) {
			log.info(Messages.ExecMsg_FORCE_STOP_AVOID);
		}

		return channel.getExitStatus();
	}

	protected SshSession getSession() {
		return _session;
	}

	private SshSession setSession(SshSession session) {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SshSession.class.getCanonicalName()
					+ ".");
		}
		SshSession previous = getSession();
		_session = session;
		return previous;
	}

	protected String getCommand() {
		return _cmd;
	}

	private String setCommand(String command) {
		if (command == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an ssh command to execute).");
		}
		String previous = getCommand();
		_cmd = command;
		return previous;
	}

	protected boolean getRequiretty() {
		return _requiretty;
	}

	/**
	 * <p>
	 * Note that :
	 * <ul>
	 * <li>Set requiretty to <tt>true</tt> is necessary when remote system
	 * sudo's configuration requires tty ;</li>
	 * <li>When requiretty is set to <tt>true</tt>, all stderr will be received
	 * into stdout ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param requiretty
	 * 
	 * @return
	 */
	private boolean setRequiretty(boolean requiretty) {
		boolean previous = getRequiretty();
		_requiretty = requiretty;
		return previous;
	}

	protected OutputStream getOutputStream() {
		return _out;
	}

	private OutputStream setOutputStream(OutputStream outputStream) {
		OutputStream previous = getOutputStream();
		_out = outputStream;
		if (_out == null) { // provide a default impl
			_out = new LoggerOutputStream("[STDOUT]", LogThreshold.DEBUG);
		}
		return previous;
	}

	protected OutputStream getErrorStream() {
		return _err;
	}

	private OutputStream setErrorStream(OutputStream errorStream) {
		OutputStream previous = getErrorStream();
		_err = errorStream;
		if (_err == null) { // provide a default impl
			_err = new LoggerOutputStream("[STDERR]", LogThreshold.ERROR);
		}
		return previous;
	}

	protected GenericTimeout getKillTimeout() {
		return _killTimeout;
	}

	private GenericTimeout setKillTimeout(GenericTimeout killTimeout) {
		GenericTimeout previous = getKillTimeout();
		_killTimeout = killTimeout;
		if (_killTimeout == null) { // provide a default value
			_killTimeout = DEFAULT_KILL_TIMEOUT;
		}
		return previous;
	}

}