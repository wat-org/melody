package com.wat.melody.plugin.ssh.common.jsch;

import java.io.OutputStream;

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

	/**
	 * <p>
	 * Open a Jssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * 
	 * @throws SshException
	 *             on error.
	 * @throws SshException
	 *             which contains a {@link JSchException}, which error message
	 *             is "Incorrect credentials" on credential error.
	 * 
	 */
	public static Session openSession(JSchConnectionDatas base,
			SshPlugInConfiguration conf) throws SshException {
		if (base == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ JSchConnectionDatas.class.getCanonicalName() + ".");
		}
		if (conf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshPlugInConfiguration.class.getCanonicalName() + ".");
		}
		Session session = null;
		try {
			session = conf.getJSch().getSession(base.getLogin(),
					base.getHost().getValue().getHostAddress(),
					base.getPort().getValue());
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Ssh Session. "
					+ "Because all parameters have already been validated, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		session.setUserInfo(base);

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
		 * TODO : when the target sshd is Kerberized, and when the client has no
		 * Kerberos ticket yet, the auth method 'gssapi-with-mic' will wait for
		 * the user to prompt a password.
		 * 
		 * This is a bug in the JSch, class UserAuthGSSAPIWithMIC.java. As long
		 * as the bug is not solved, we must exclude kerberos/GSS auth method.
		 */
		session.setConfig("PreferredAuthentications",
				"publickey,keyboard-interactive,password");

		if (conf.getProxyType() != null) {
			/*
			 * TODO : hande proxy parameters
			 */
		}

		try {
			session.connect(conf.getConnectionTimeout());
		} catch (JSchException Ex) {
			if (Ex.getMessage() != null && Ex.getMessage().indexOf("Auth") == 0) {
				// will match message 'Auth cancel' and 'Auth fail'
				// provide an unified error message on credentials errors
				Ex = new JSchException("Incorrect credentials.", Ex);
			}
			throw new SshException(Messages.bind(
					Messages.SshEx_FAILED_TO_CONNECT, new Object[] {
							base.getHost().getValue().getHostAddress(),
							base.getPort().getValue(), base.getLogin() }), Ex);
		}

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
		if (outStream == null) {
			outStream = new LoggerOutputStream("[STDOUT]", LogThreshold.DEBUG);
		}
		if (errStream == null) {
			errStream = new LoggerOutputStream("[STDERR]", LogThreshold.ERROR);
		}
		if (!session.isConnected()) {
			throw new IllegalArgumentException("session: Not accepted. "
					+ "Given session must be connected.");
		}
		ChannelExec c = null;
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
				if (c.isClosed()) {
					break;
				}
				/*
				 * can't find a way to 'join' the session or the channel... So
				 * we're pooling the isClosed ....
				 */
				Thread.sleep(500);
			}
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to exec an ssh command through "
					+ "a JSch 'exec' Channel.", Ex);
		} finally {
			if (c != null) {
				c.disconnect();
			}
		}

		return c.getExitStatus();
	}

}
