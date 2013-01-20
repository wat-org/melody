package com.wat.melody.common.ssh.impl;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.utils.LogThreshold;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class RemoteExec {

	private static Log log = LogFactory.getLog(RemoteExec.class);

	private SshSession _session;
	private String _cmd;
	private boolean _requiretty;
	private OutputStream _out;
	private OutputStream _err;

	protected RemoteExec(SshSession session, String command,
			boolean requiretty, OutputStream outStream, OutputStream errStream) {
		setSession(session);
		setCommand(command);
		setRequiretty(requiretty);
		setOutputStream(outStream);
		setErrorStream(errStream);
	}

	public int exec() throws InterruptedException {
		ChannelExec channel = null;
		InterruptedException iex = null;
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
				 * Can't find a way to 'join' the session or the channel ... So
				 * we're pooling the isClosed ....
				 */
				if (channel.isClosed()) {
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException Ex) {
					if (iex == null) {
						log.info(Messages.SessionExecMsg_GRACEFULL_SHUTDOWN);
						iex = new InterruptedException(
								Messages.SessionExecEx_EXEC_INTERRUPTED);
						iex.initCause(Ex);
					} else {
						log.warn(Messages.SessionExecMsg_FORCE_SHUTDOWN);
						iex = new InterruptedException(
								Messages.SessionExecEx_EXEC_INTERRUPTED);
						iex.initCause(Ex);
						throw iex;
					}
				}
			}
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to exec an ssh command "
					+ "through a JSch 'exec' Channel.", Ex);
		} finally {
			if (channel != null) {
				channel.disconnect(); // This closes stream
			}
		}
		if (iex != null) {
			throw iex;
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

}
