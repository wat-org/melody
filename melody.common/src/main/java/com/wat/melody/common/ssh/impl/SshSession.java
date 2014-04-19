package com.wat.melody.common.ssh.impl;

import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSchExceptionInterrupted;
import com.jcraft.jsch.LocalIdentityRepository;
import com.jcraft.jsch.Session;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.IHostKey;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.exception.HostKeyChangedException;
import com.wat.melody.common.ssh.exception.HostKeyNotFoundException;
import com.wat.melody.common.ssh.exception.InvalidCredentialException;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.impl.transfer.SftpDownloaderMultiThread;
import com.wat.melody.common.ssh.impl.transfer.SftpUploaderMultiThread;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshSession implements ISshSession {

	private static Logger log = LoggerFactory.getLogger(SshSession.class);

	private static Logger ex = LoggerFactory.getLogger("exception."
			+ SshSession.class.getName());

	private static JSch JSCH = new JSch();

	private Session _session = null;

	private ISshSessionConfiguration _sshSessionConfiguration = null;
	private ISshUserDatas _sshUserDatas = null;
	private ISshConnectionDatas _sshConnectionDatas = null;

	public SshSession(ISshUserDatas ud, ISshConnectionDatas cd) {
		setUserDatas(ud);
		setConnectionDatas(cd);
	}

	@Override
	public ISshSessionConfiguration getSessionConfiguration() {
		return _sshSessionConfiguration;
	}

	@Override
	public ISshSessionConfiguration setSessionConfiguration(
			ISshSessionConfiguration sc) {
		// can be null
		ISshSessionConfiguration previous = getSessionConfiguration();
		_sshSessionConfiguration = sc;
		return previous;
	}

	@Override
	public ISshUserDatas getUserDatas() {
		return _sshUserDatas;
	}

	@Override
	public ISshUserDatas setUserDatas(ISshUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshUserDatas.class.getCanonicalName() + ".");
		}
		// the user must be defined
		if (ud.getLogin() == null) {
			throw new IllegalArgumentException("No login defined ! "
					+ "The caller should define a login.");
		}
		ISshUserDatas previous = getUserDatas();
		_sshUserDatas = ud;
		return previous;
	}

	@Override
	public ISshConnectionDatas getConnectionDatas() {
		return _sshConnectionDatas;
	}

	@Override
	public ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd) {
		if (cd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshConnectionDatas.class.getCanonicalName() + ".");
		}
		ISshConnectionDatas previous = getConnectionDatas();
		_sshConnectionDatas = cd;
		return previous;
	}

	/**
	 * @throws InvalidCredentialException
	 *             on authentication failure.
	 * @throws HostKeyChangedException
	 *             when the remote system was not trusted, and the host key
	 *             presented by the remote system does not match the host key
	 *             stored in the given known host file.
	 * @throws HostKeyNotFoundException
	 *             when the remote system was not trusted, and the host key
	 *             presented by the remote system was not stored in the given
	 *             known host file.
	 * @throws SshSessionException
	 *             if the connection fail for any other reason (no route to
	 *             host, dns failure, network unreachable, ...).
	 */
	@Override
	public synchronized void connect() throws SshSessionException,
			InvalidCredentialException, HostKeyChangedException,
			HostKeyNotFoundException, InterruptedException {
		if (isConnected()) {
			return;
		}
		log.trace(Msg.bind(Messages.SessionMsg_CNX, getConnectionDatas(),
				getUserDatas()));
		applyDatas();
		applySessionConfiguration();
		_connect();
		// Change the name of the thread so that the log is more clear
		_session.getConnectThread().setName(Thread.currentThread().getName());
		log.trace(Messages.SessionMsg_CNX_OK);
	}

	@Override
	public synchronized void disconnect() {
		if (_session != null) {
			if (_session.isConnected()) {
				_session.disconnect();
			}
			_session = null;
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return _session != null && _session.isConnected();
	}

	@Override
	public int execRemoteCommand(String command, boolean requiretty,
			OutputStream outStream, OutputStream errStream)
			throws SshSessionException, InterruptedException {
		return new RemoteExec(this, command, requiretty, outStream, errStream)
				.exec();
	}

	@Override
	public int execRemoteCommand(String command, boolean requiretty,
			OutputStream outStream, OutputStream errStream,
			GenericTimeout killTimeout) throws SshSessionException,
			InterruptedException {
		return new RemoteExec(this, command, requiretty, outStream, errStream,
				killTimeout).exec();
	}

	@Override
	public void upload(List<ResourcesSpecification> rss, int maxPar,
			TemplatingHandler th) throws SshSessionException,
			InterruptedException {
		try {
			new SftpUploaderMultiThread(this, rss, maxPar, th).doTransfer();
		} catch (TransferException Ex) {
			throw new SshSessionException(Ex);
		}
	}

	@Override
	public void download(List<ResourcesSpecification> rrss, int maxPar,
			TemplatingHandler th) throws SshSessionException,
			InterruptedException {
		try {
			new SftpDownloaderMultiThread(this, rrss, maxPar, th).doTransfer();
		} catch (TransferException Ex) {
			throw new SshSessionException(Ex);
		}
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

	protected KeyPairRepositoryPath getKeyPairRepository() {
		return getUserDatas().getKeyPairRepositoryPath();
	}

	protected KeyPairName getKeyPairName() {
		return getUserDatas().getKeyPairName();
	}

	private String getKeyPairPath() {
		if (getKeyPairName() == null || getKeyPairRepository() == null) {
			return null;
		}
		return KeyPairRepository.getKeyPairRepository(getKeyPairRepository())
				.getPrivateKeyFile(getKeyPairName()).getPath();
	}

	private void applyDatas() {
		try {
			_session = JSCH.getSession(getLogin(), getHost(), getPort());
		} catch (JSchException Ex) {
			throw new RuntimeException("Improbable, tous les param ont été "
					+ "validés.", Ex);
		}
		_session.setUserInfo(new JSchUserInfoAdapter(getUserDatas(),
				getConnectionDatas()));

		if (getKeyPairPath() == null) {
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
		/*
		 * We remove 'gssapi-with-mic' authentication method because, when the
		 * target sshd is Kerberized, and when the client has no Kerberos ticket
		 * yet, the auth method 'gssapi-with-mic' will wait for the user to
		 * prompt a password.
		 * 
		 * This is a bug in the JSch, class UserAuthGSSAPIWithMIC.java. As long
		 * as the bug is not solved, we must exclude kerberos/GSS auth method.
		 */
		/*
		 * We remove 'keyboard-interactive' authentication method because, we
		 * don't want the user to be prompt for anything.
		 */
		_session.setConfig("PreferredAuthentications", "publickey,password");

		ISshSessionConfiguration conf = getSessionConfiguration();
		if (getSessionConfiguration() == null) {
			// no session configuration defined, will use defaults
			return;
		}
		_session.setServerAliveCountMax(conf.getServerAliveMaxCount()
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

		if (conf.getKnownHosts() != null) {
			_session.setHostKeyRepository(new KnownHostsAdapter(conf
					.getKnownHosts()));
		}

		if (conf.getProxyType() != null) {
			/*
			 * TODO : hande proxy parameters
			 */
		}
	}

	private void _connect() throws SshSessionException,
			InvalidCredentialException, HostKeyChangedException,
			HostKeyNotFoundException, InterruptedException {
		long cnxTimeout = 0;
		int cnxRetry = 0;
		int cnxDelay = 3;
		ConsolidatedException cex = new ConsolidatedException(Msg.bind(
				Messages.SessionEx_FAILED_TO_CONNECT, getConnectionDatas(),
				getUserDatas()));
		if (getSessionConfiguration() != null) {
			cnxTimeout = getSessionConfiguration().getConnectionTimeout()
					.getTimeoutInMillis();
			cnxRetry = getSessionConfiguration().getConnectionRetry()
					.getValue();
		}
		while (true) {
			try {
				_session.connect((int) cnxTimeout);
				// success => exit
				return;
			} catch (JSchExceptionInterrupted Ex) {
				throw new WrapperInterruptedException(Ex);
			} catch (JSchException Ex) {
				String msg = Ex.getMessage();
				msg = msg != null ? msg : "";
				// authentication error => fast fail
				if (msg.indexOf("Too many authentication failures") != -1
						|| msg.indexOf("Auth") == 0) {
					// will match message 'Auth cancel', 'Auth fail'
					// and 'SSH_MSG_DISCONNECT: 2 Too many authentication
					// failures for <user>'
					// => throw dedicated exception if credentials error
					throw new InvalidCredentialException(Msg.bind(
							Messages.SessionEx_FAILED_TO_CONNECT,
							getConnectionDatas(), getUserDatas()), Ex);
				}
				// HostKey has been changed => fast fail
				if (msg.indexOf("HostKey has been changed") == 0) {
					throw new HostKeyChangedException(Msg.bind(
							Messages.SessionEx_FAILED_TO_CONNECT,
							getConnectionDatas(), getUserDatas()),
							new HostKeyChangedException(Msg.bind(
									Messages.SessionEx_HOSTKEY_CHANGED,
									getSessionConfiguration().getKnownHosts(),
									_session.getHostKey().getType()), Ex));
				}
				// HostKey rejected => fast fail
				if (msg.indexOf("reject HostKey") == 0) {
					throw new HostKeyNotFoundException(Msg.bind(
							Messages.SessionEx_FAILED_TO_CONNECT,
							getConnectionDatas(), getUserDatas()),
							new HostKeyNotFoundException(Msg.bind(
									Messages.SessionEx_HOSTKEY_UNDEFINED,
									getSessionConfiguration().getKnownHosts()),
									Ex));
				}
				// no retry left => fail
				if (cnxRetry <= 0) {
					cex.addCause(Ex);
					throw new SshSessionException(cex);
				}
				/*
				 * Sometimes (don't know why), we receive an exception with
				 * message 'connection is closed by foreign host'. Then, at
				 * first retry, we receive an exception with message
				 * 'SSH_MSG_DISCONNECT: 2 Packet corrupt'. Then, a second retry,
				 * we receive an exception with message 'Packet corrupt'. In
				 * order to resolve this issue, when we receive an exception
				 * with message 'connection is closed by foreign host', we
				 * disconnect and we rebuild a new session. After that, we
				 * retry.
				 */
				// connection closed by foreign host => create a new session
				// packet corrupt => create a new session and retry
				if (msg.indexOf("connection is closed by foreign host") == 0
						|| msg.indexOf("Packet corrupt") != -1) {
					disconnect();
					applyDatas();
					applySessionConfiguration();
					// => retry
				}
				// retrying
				SshSessionException pex = new SshSessionException(Msg.bind(
						Messages.SessionMsg_RETRY_TO_CONNECT,
						getConnectionDatas(), getUserDatas(), cnxRetry), Ex);
				log.info(pex.getUserFriendlyStackTrace());
				ex.info(pex.getFullStackTrace());
				cex.addCause(pex);
				cnxRetry -= 1;
				cnxDelay += 3;
				try {
					Thread.sleep(cnxDelay * 1000);
				} catch (InterruptedException e) {
					throw new WrapperInterruptedException(
							Messages.SessionEx_CNX_INTERRUPTED, e);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException(
							Messages.SessionEx_CNX_INTERRUPTED);
				}
			}
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

	public ChannelSftp openSftpChannel() throws InterruptedException {
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
			if (Ex.getCause() instanceof InterruptedIOException) {
				throw new WrapperInterruptedException(
						"open sftp channel interrupted", Ex.getCause());
			}
			throw new RuntimeException(
					"Failed to connect a JSch 'sftp' Channel.", Ex);
		}
		return channel;
	}

}