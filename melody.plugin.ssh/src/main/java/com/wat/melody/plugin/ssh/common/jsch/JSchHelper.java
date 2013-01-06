package com.wat.melody.plugin.ssh.common.jsch;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wat.melody.common.utils.LogThreshold;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class JSchHelper {

	/*
	 * TODO : extract a JSchConfiguration from SshPlugInConfiguration, so that
	 * all links to the Ssh Plug-In disappear. This would be difficult due to
	 * SshPlugInConfigurationException.
	 */

	private static Log log = LogFactory.getLog(JSchHelper.class);

	/**
	 * <p>
	 * Open a Jssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * 
	 * @throws IncorrectCredentialsException
	 *             on credentials error.
	 * @throws SshException
	 *             on most error.
	 * 
	 */
	public static Session openSession(SshConnectionDatas cnxDatas,
			SshPlugInConfiguration conf) throws SshException,
			IncorrectCredentialsException {
		if (cnxDatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshConnectionDatas.class.getCanonicalName() + ".");
		}
		if (conf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshPlugInConfiguration.class.getCanonicalName() + ".");
		}
		Session session = null;
		try {
			session = conf.getJSch().getSession(cnxDatas.getLogin(),
					cnxDatas.getHost().getValue().getHostAddress(),
					cnxDatas.getPort().getValue());
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Ssh Session. "
					+ "Because all parameters have already been validated, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		session.setUserInfo(cnxDatas);

		session.setServerAliveCountMax(conf.getServerAliveCountMax());

		try {
			session.setServerAliveInterval(conf.getServerAliveInterval());
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to set the serverAliveInterval "
					+ "to '" + conf.getServerAliveInterval() + "'. "
					+ "Because this value have been retreives from the "
					+ "configuration, such error cannot happened.", Ex);
		}

		try {
			session.setTimeout(conf.getReadTimeout());
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to set the timeout " + "to '"
					+ conf.getReadTimeout() + "'. "
					+ "Because this value have been retreives from the "
					+ "configuration, such error cannot happened.", Ex);
		}

		session.setConfig("compression.s2c", conf.getCompressionType()
				.getValue());
		session.setConfig("compression.c2s", conf.getCompressionType()
				.getValue());
		session.setConfig("compression_level", conf.getCompressionLevel()
				.getValue());
		/*
		 * TODO : We remove 'gssapi-with-mic' authentication method because,
		 * when the target sshd is Kerberized, and when the client has no
		 * Kerberos ticket yet, the auth method 'gssapi-with-mic' will wait for
		 * the user to prompt a password.
		 * 
		 * This is a bug in the JSch, class UserAuthGSSAPIWithMIC.java. As long
		 * as the bug is not solved, we must exclude kerberos/GSS auth method.
		 */
		/*
		 * We remove 'keyboard-interactive' authentication method because, we
		 * don't want the user to be prompt for anything.
		 */
		session.setConfig("PreferredAuthentications", "publickey,password");

		if (conf.getProxyType() != null) {
			/*
			 * TODO : hande proxy parameters
			 */
		}

		try {
			session.connect(conf.getConnectionTimeout());
		} catch (JSchException Ex) {
			String msg = Messages.bind(Messages.SshEx_FAILED_TO_CONNECT,
					new Object[] { session.getHost(), session.getPort(),
							session.getUserName() });
			if (Ex.getMessage() != null && Ex.getMessage().indexOf("Auth") == 0) {
				// will match message 'Auth cancel' and 'Auth fail'
				// => dedicated exception on credentials errors
				throw new IncorrectCredentialsException(msg, Ex);
			}
			throw new SshException(msg, Ex);
		}

		// Change the name of the thread so that the log is more clear
		session.getConnectThread().setName(Thread.currentThread().getName());
		return session;
	}

	/**
	 * <p>
	 * Open a sftp channel.
	 * </p>
	 * 
	 * @return the opened sftp channel.
	 * 
	 * @throws SshException
	 */
	public static ChannelSftp openSftpChannel(Session session,
			SshPlugInConfiguration conf) throws SshException {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Session.class.getCanonicalName()
					+ ".");
		}
		if (conf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshPlugInConfiguration.class.getCanonicalName() + ".");
		}
		if (!session.isConnected()) {
			throw new IllegalArgumentException("session: Not accepted. "
					+ "Given session must be connected.");
		}
		ChannelSftp channel = null;
		try {
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect(conf.getConnectionTimeout());
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to open a JSch 'sftp' Channel.",
					Ex);
		}
		return channel;
	}

	/**
	 * <p>
	 * Synchronously execute a command on a remote system, write standard output
	 * and error into streams.
	 * </p>
	 * 
	 * @param session
	 *            is a connected {@link Session} through which the command will
	 *            be executed.
	 * @param sCommand
	 *            is the command to execute.
	 * @param outStream
	 *            is the {@link OutputStream} which will receive the standard
	 *            output.
	 * @param errStream
	 *            is the {@link OutputStream} which will receive the standard
	 *            error.
	 * 
	 * @return the return value of the command which have been executed.
	 * 
	 * @throws SshException
	 *             if the given {@link Session} is down, ...
	 * @throws InterruptedException
	 *             if this remote command execution was interrupted. Note that
	 *             when this exception is raised, the command have been
	 *             completely executed .
	 */
	public static int execSshCommand(Session session, String sCommand,
			OutputStream outStream, OutputStream errStream)
			throws SshException, InterruptedException {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Session.class.getCanonicalName()
					+ ".");
		}
		if (sCommand == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (a ssh command).");
		}
		if (outStream == null) { // default impl
			outStream = new LoggerOutputStream("[STDOUT]", LogThreshold.DEBUG);
		}
		if (errStream == null) { // default impl
			errStream = new LoggerOutputStream("[STDERR]", LogThreshold.ERROR);
		}
		if (!session.isConnected()) {
			throw new IllegalArgumentException("session: Not accepted. "
					+ "Given session must be connected.");
		}
		ChannelExec c = null;
		InterruptedException iex = null;
		try {
			c = (ChannelExec) session.openChannel("exec");

			// force the tty allocation
			c.setPty(true);

			c.setCommand(sCommand);

			c.setInputStream(null);
			c.setOutputStream(outStream);
			c.setErrStream(errStream);

			c.connect();
			while (true) {
				/*
				 * Can't find a way to 'join' the session or the channel ... So
				 * we're pooling the isClosed ....
				 */
				if (c.isClosed()) {
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException Ex) {
					log.info(Messages.SshMsg_GRACEFULL_SHUTDOWN);
					iex = new InterruptedException(
							Messages.SshEx_EXEC_INTERRUPTED);
					iex.initCause(Ex);
				}
			}
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to exec an ssh command "
					+ "through a JSch 'exec' Channel.", Ex);
		} finally {
			if (c != null) {
				c.disconnect();
			}
		}
		if (iex != null) {
			throw iex;
		}

		return c.getExitStatus();
	}

}
