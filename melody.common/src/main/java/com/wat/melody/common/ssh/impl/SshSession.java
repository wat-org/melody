package com.wat.melody.common.ssh.impl;

import java.io.OutputStream;
import java.util.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.LocalIdentityRepository;
import com.jcraft.jsch.Session;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.InvalidCredentialException;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.types.SimpleResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshSession implements ISshSession {

	private static JSch JSCH = new JSch();

	private Session _session = null;

	private ISshSessionConfiguration moSshSessionConfiguration = null;
	private ISshUserDatas moSshUserDatas = null;
	private ISshConnectionDatas moSshConnectionDatas = null;

	public SshSession() {
	}

	@Override
	public ISshSessionConfiguration getSessionConfiguration() {
		return moSshSessionConfiguration;
	}

	@Override
	public ISshSessionConfiguration setSessionConfiguration(
			ISshSessionConfiguration sc) {
		ISshSessionConfiguration previous = getSessionConfiguration();
		moSshSessionConfiguration = sc;
		return previous;
	}

	@Override
	public ISshUserDatas getUserDatas() {
		return moSshUserDatas;
	}

	@Override
	public ISshUserDatas setUserDatas(ISshUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshUserDatas.class.getCanonicalName() + ".");
		}
		ISshUserDatas previous = getUserDatas();
		moSshUserDatas = ud;
		return previous;
	}

	@Override
	public ISshConnectionDatas getConnectionDatas() {
		return moSshConnectionDatas;
	}

	@Override
	public ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd) {
		if (cd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshConnectionDatas.class.getCanonicalName() + ".");
		}
		ISshConnectionDatas previous = getConnectionDatas();
		moSshConnectionDatas = cd;
		return previous;
	}

	/**
	 * 
	 * @throws InvalidCredentialException
	 *             on credentials error.
	 * @throws SshSessionException
	 *             if the Ssh Management Feature failed to operate properly (ex
	 *             : no user key have been provided, or the given user key is
	 *             not valid, or the given master user key is not valid, or the
	 *             given master user credentials are invalid, or the remote host
	 *             is not reachable - no route to host, dns failure, ... -, or
	 *             ... ).
	 */
	@Override
	public synchronized void connect() throws SshSessionException,
			InvalidCredentialException {
		if (isConnected()) {
			return;
		}
		applyDatas();
		applySessionConfiguration();
		_connect();
		// Change the name of the thread so that the log is more clear
		_session.getConnectThread().setName(Thread.currentThread().getName());
	}

	@Override
	public synchronized void disconnect() {
		if (_session != null && _session.isConnected()) {
			_session.disconnect();
			_session = null;
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return _session != null && _session.isConnected();
	}

	@Override
	public int execRemoteCommand(String sCommand, boolean requiretty,
			OutputStream outStream, OutputStream errStream)
			throws SshSessionException, InterruptedException {
		return new RemoteExec(this, sCommand, requiretty, outStream, errStream)
				.exec();
	}

	@Override
	public void upload(List<SimpleResource> r, int maxPar, TemplatingHandler th)
			throws SshSessionException, InterruptedException {
		new UploaderMultiThread(this, r, maxPar, th).upload();
	}

	@Override
	public String toString() {
		return "{ host:" + getHost() + ", port:" + getPort() + ", login:"
				+ getLogin() + " }";
	}

	@Override
	public IHostKey getHostKey() {
		return _session != null ? new HostKeyAdapter(_session.getHostKey())
				: null;
	}

	protected String getHost() {
		return getConnectionDatas().getHost().getAddress();
	}

	protected int getPort() {
		return getConnectionDatas().getPort().getValue();
	}

	protected String getLogin() {
		return getUserDatas().getLogin();
	}

	protected String getPassword() {
		return getUserDatas().getPassword();
	}

	protected KeyPairRepository getKeyPairRepository() {
		return getUserDatas().getKeyPairRepository();
	}

	protected KeyPairName getKeyPairName() {
		return getUserDatas().getKeyPairName();
	}

	protected String getKeyPairPath() {
		return getKeyPairRepository().getPrivateKeyFile(getKeyPairName())
				.toString();
	}

	private void applyDatas() {
		if (getUserDatas() == null) {
			throw new IllegalStateException("No user datas defined.");
		}
		if (getConnectionDatas() == null) {
			throw new IllegalStateException("No connection datas defined.");
		}
		try {
			_session = JSCH.getSession(getLogin(), getHost(), getPort());
		} catch (JSchException Ex) {
			throw new RuntimeException("Improbable, tous les param ont été "
					+ "validés.", Ex);
		}
		_session.setUserInfo(new JSchUserInfoAdapter(getUserDatas(),
				getConnectionDatas()));

		if (getKeyPairName() == null) {
			return;
		}
		LocalIdentityRepository ir = new LocalIdentityRepository(JSCH);
		try {
			ir.add(getKeyPairPath(), getPassword());
		} catch (JSchException Ex) {
			throw new RuntimeException(getKeyPairPath()
					+ ": Invalid Private Key file.", Ex);
		}
		_session.setIdentityRepository(ir);
	}

	private void applySessionConfiguration() {
		ISshSessionConfiguration conf = getSessionConfiguration();
		if (getSessionConfiguration() == null) {
			// no session configuration defined, will use defaults
			return;
		}
		_session.setServerAliveCountMax(conf.getServerAliveCountMax()
				.getValue());

		try {
			_session.setServerAliveInterval((int) conf.getServerAliveInterval()
					.getTimeoutInMillis());
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to set the serverAliveInterval "
					+ "to '" + conf.getServerAliveInterval() + "'. "
					+ "Because this value have been retreives from the "
					+ "configuration, such error cannot happened.", Ex);
		}

		try {
			_session.setTimeout((int) conf.getReadTimeout()
					.getTimeoutInMillis());
		} catch (JSchException Ex) {
			throw new RuntimeException("Failed to set the timeout " + "to '"
					+ conf.getReadTimeout() + "'. "
					+ "Because this value have been retreives from the "
					+ "configuration, such error cannot happened.", Ex);
		}

		_session.setConfig("compression.s2c", conf.getCompressionType()
				.getValue());
		_session.setConfig("compression.c2s", conf.getCompressionType()
				.getValue());
		_session.setConfig("compression_level", conf.getCompressionLevel()
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
		_session.setConfig("PreferredAuthentications", "publickey,password");

		_session.setHostKeyRepository(new KnownHostsAdapter(
				getSessionConfiguration().getKnownHosts()));

		if (conf.getProxyType() != null) {
			/*
			 * TODO : hande proxy parameters
			 */
		}
	}

	private void _connect() throws SshSessionException,
			InvalidCredentialException {
		int cnxTimeout = 0;
		if (getSessionConfiguration() != null) {
			cnxTimeout = (int) getSessionConfiguration().getConnectionTimeout()
					.getTimeoutInMillis();
		}
		try {
			_session.connect(cnxTimeout);
		} catch (JSchException Ex) {
			String msg;
			msg = Messages.bind(Messages.SessionEx_FAILED_TO_CONNECT, this);
			if (Ex.getMessage() != null && Ex.getMessage().indexOf("Auth") == 0) {
				// will match message 'Auth cancel' and 'Auth fail'
				// => dedicated exception on credentials errors
				throw new InvalidCredentialException(msg, Ex);
			}
			throw new SshSessionException(msg, Ex);
		}
	}

	protected ChannelExec openExecChannel() {
		if (!isConnected()) {
			throw new IllegalStateException("session: Not accepted. "
					+ "Session must be connected.");
		}
		ChannelExec channel = null;
		try {
			channel = (ChannelExec) _session.openChannel("exec");
		} catch (JSchException Ex) {
			throw new RuntimeException(
					"Failed to connect a JSch 'exec' Channel.", Ex);
		}
		return channel;
	}

	protected ChannelSftp openSftpChannel() {
		if (!isConnected()) {
			throw new IllegalStateException("session: Not accepted. "
					+ "Session must be connected.");
		}
		ChannelSftp channel = null;
		try {
			channel = (ChannelSftp) _session.openChannel("sftp");
			channel.connect();
		} catch (JSchException Ex) {
			if (channel != null) {
				channel.disconnect();
			}
			throw new RuntimeException(
					"Failed to connect a JSch 'sftp' Channel.", Ex);
		}
		return channel;
	}

}