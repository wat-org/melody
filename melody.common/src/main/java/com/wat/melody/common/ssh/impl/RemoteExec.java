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
	private OutputStream _out;
	private OutputStream _err;

	protected RemoteExec(SshSession session, String command,
			OutputStream outStream, OutputStream errStream) {
		setSession(session);
		setCommand(command);
		setOutputStream(outStream);
		setErrorStream(errStream);
	}

	public int exec() throws InterruptedException {
		if (!getSession().isConnected()) {
			throw new IllegalStateException("session: Not accepted. "
					+ "Session must be connected.");
		}
		ChannelExec channel = null;
		InterruptedException iex = null;
		try {
			channel = getSession().openExecChannel();
			channel.setCommand(_cmd);
			channel.setPty(true); // force the tty allocation
			channel.setInputStream(null);
			channel.setOutputStream(_out);
			/*
			 * FIXME : nothing is never writen in stderr. Everything goes into
			 * stdout... Jsh bug ?
			 */
			channel.setErrStream(_err);

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
				channel.disconnect();// This closes stream
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

	protected OutputStream getOutputStream() {
		return _out;
	}

	private OutputStream setOutputStream(OutputStream outputStream) {
		OutputStream previous = getOutputStream();
		_out = outputStream;
		if (_out == null) { // default impl
			_out = new LoggerOutputStream("[STDOUT]", LogThreshold.DEBUG);
		}
		return previous;
	}

	protected OutputStream getErrorStream() {
		return _out;
	}

	private OutputStream setErrorStream(OutputStream errorStream) {
		OutputStream previous = getErrorStream();
		_err = errorStream;
		if (_err == null) { // default impl
			_err = new LoggerOutputStream("[STDERR]", LogThreshold.ERROR);
		}
		return previous;
	}

}
