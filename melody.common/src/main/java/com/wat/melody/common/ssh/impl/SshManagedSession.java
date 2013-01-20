package com.wat.melody.common.ssh.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.wat.melody.common.utils.LogThreshold;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshManagedSession implements ISshSession {

	private static Log log = LogFactory.getLog(SshManagedSession.class);

	private ISshSession _session = null;
	private ISshUserDatas _sshManagementUserDatas = null;
	private ISshUserDatas _sshUserDatas = null;

	public SshManagedSession(ISshSession session) {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ISshSession.class.getCanonicalName()
					+ ".");
		}
		_session = session;
		setUserDatas(session.getUserDatas());
	}

	@Override
	public ISshSessionConfiguration getSessionConfiguration() {
		return _session.getSessionConfiguration();
	}

	@Override
	public ISshSessionConfiguration setSessionConfiguration(
			ISshSessionConfiguration sc) {
		return _session.setSessionConfiguration(sc);
	}

	@Override
	public ISshUserDatas getUserDatas() {
		return _sshUserDatas;
	}

	@Override
	public ISshUserDatas setUserDatas(ISshUserDatas ud) {
		return _session.setUserDatas(_sshUserDatas = ud);
	}

	@Override
	public ISshConnectionDatas getConnectionDatas() {
		return _session.getConnectionDatas();
	}

	@Override
	public ISshConnectionDatas setConnectionDatas(ISshConnectionDatas cd) {
		return _session.setConnectionDatas(cd);
	}

	public ISshUserDatas getManagementUserDatas() {
		return _sshManagementUserDatas;
	}

	public ISshUserDatas setManagementUserDatas(ISshUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshUserDatas.class.getCanonicalName() + ".");
		}
		ISshUserDatas previous = getManagementUserDatas();
		_sshManagementUserDatas = ud;
		return previous;
	}

	@Override
	public synchronized void connect() throws SshSessionException,
			InvalidCredentialException, InterruptedException {
		try {
			openSession(getUserDatas());
		} catch (InvalidCredentialException Ex) {
			log.trace(Messages.bind(Messages.SshMgmtCnxMsg_CNX_USER_FAIL,
					_session, getUserDatas().getLogin()));
			connectAsMasterUserAndDeployKey();
			openUserSession();
			log.trace(Messages.bind(Messages.SshMgmtCnxMsg_CNX_USER_OK,
					_session));
		}
	}

	@Override
	public synchronized void disconnect() {
		_session.disconnect();
	}

	@Override
	public synchronized boolean isConnected() {
		return _session.isConnected();
	}

	@Override
	public int execRemoteCommand(String sCommand, OutputStream out,
			OutputStream err) throws SshSessionException, InterruptedException {
		return _session.execRemoteCommand(sCommand, out, err);
	}

	@Override
	public void upload(List<SimpleResource> r, int maxPar, TemplatingHandler th)
			throws SshSessionException, InterruptedException {
		_session.upload(r, maxPar, th);
	}

	@Override
	public IHostKey getHostKey() {
		return _session.getHostKey();
	}

	private void openSession(ISshUserDatas sshUserDatas)
			throws SshSessionException, InterruptedException {
		_session.setUserDatas(sshUserDatas);
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_CNX, _session));
		_session.connect();
		log.trace(Messages.SshMgmtCnxMsg_CNX_OK);
	}

	private void openUserSession() throws SshSessionException,
			InterruptedException {
		try {
			openSession(getUserDatas());
		} catch (InvalidCredentialException Ex) {
			throw new RuntimeException("Failed to connect to remote system. "
					+ "Ssh Management Feature must have fail to do its job. "
					+ "Please send feedback to the development team so that "
					+ "they can provide a fix.", Ex);
		}
	}

	private void connectAsMasterUserAndDeployKey() throws SshSessionException,
			InvalidCredentialException, InterruptedException {
		if (getManagementUserDatas() == null) {
			throw new IllegalStateException("BUG: No Management User Datas "
					+ "defined! "
					+ "Caller source code should set Management User Datas.");
		}
		if (getUserDatas().getKeyPairName() == null) {
			throw new IllegalStateException("BUG: No User keypairname "
					+ "defined! "
					+ "Caller source code should set a User keyPairName.");
		}
		try {
			openMasterSession();
			deployKey();
		} catch (SshSessionException Ex) {
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_GENERIC_FAIL, _session), Ex);
		} finally {
			disconnect();
		}
	}

	private void openMasterSession() throws SshSessionException,
			InvalidCredentialException, InterruptedException {
		try {
			openSession(getManagementUserDatas());
			log.trace(Messages.SshMgmtCnxMsg_OPENED);
		} catch (InvalidCredentialException Ex) {
			throw new InvalidCredentialException(Messages.bind(
					Messages.SshMgmtCnxEx_INVALID_MASTER_CREDENTIALS,
					getManagementUserDatas()), Ex);
		}
	}

	/**
	 * 
	 * @throws SshException
	 * @throws InterruptedException
	 *             if the key deployment was interrupted. Note that when this
	 *             exception is raised, the command have been completely
	 *             executed.
	 */
	private void deployKey() throws SshSessionException, InterruptedException {
		String k = getKey();
		String dkc = createDeployKeyCommand(k);
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_DEPLOYING, dkc,
				getUserDatas().getLogin()));
		OutputStream outStream = new LoggerOutputStream("[ssh_cnx_mgmt:"
				+ getConnectionDatas().getHost() + "]", LogThreshold.DEBUG);
		OutputStream errStream = new ByteArrayOutputStream();
		int res = -1;
		try {
			res = execRemoteCommand(dkc, outStream, errStream);
		} catch (InterruptedException Ex) {
			InterruptedException iex = new InterruptedException(
					Messages.SshMgmtCnxEx_DEPLOY_INTERRUPTED);
			iex.initCause(Ex);
			throw iex;
		}
		analyseDeployKeyCommandResult(res, k, errStream.toString());
	}

	private String getKey() throws SshSessionException {
		ISshUserDatas cnxDatas = getUserDatas();
		KeyPairRepository kpr = cnxDatas.getKeyPairRepository();
		KeyPairName kpn = cnxDatas.getKeyPairName();
		String key = null;
		try {
			key = kpr.getPublicKeyInOpenSshFormat(kpn, null);
		} catch (IOException Ex) {
			throw new RuntimeException("Unexpected error while reading "
					+ "the key '" + kpr.getPrivateKeyFile(kpn) + "' . "
					+ "Because this key have been validated previously, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		return key;
	}

	private String createDeployKeyCommand(String k) throws SshSessionException {
		String login = getUserDatas().getLogin();
		String c = DEPLOY_KEY_COMMAND.replaceAll("[{][{]LOGIN[}][}]", login);
		String f = "KEY=\"" + k + "\" || exit 99\n";
		f += "CMD=\"" + c + "\" || exit 98\n";
		f += "[ $(id -g) = 0 ] && eval \"$CMD\"\n";
		f += "test -x /usr/bin/sudo || exit 97\n";
		f += "sudo -l | grep \"(root) NOPASSWD: /bin/su -\" 1>/dev/null || exit 96\n";
		f += "sudo su - <<EOF\n$CMD\nEOF";
		return f;
	}

	private void analyseDeployKeyCommandResult(int res, String k, String errMsg)
			throws SshSessionException {
		String login = getUserDatas().getLogin();
		if (res == 0) {
			log.trace(Messages.bind(Messages.SshMgmtCnxMsg_DEPLOYED, login));
			return;
		}
		String msg = null;
		switch (res) {
		case 96:
			msg = Messages.bind(Messages.SshMgmtCnxEx_NO_AUTH_SUDO, login);
			break;
		case 97:
			msg = Messages.SshMgmtCnxEx_NO_SUDO;
			break;
		case 98:
			throw new RuntimeException("BUG during the construction of CMD."
					+ "Please report this.");
		case 99:
			throw new RuntimeException("BUG during the construction of KEY."
					+ "Please report this.");
		case 100:
			msg = Messages.bind(Messages.SshMgmtCnxEx_USERADD_FAIL, login);
			break;
		case 101:
			msg = Messages.SshMgmtCnxEx_UMASK_FAIL;
			break;
		case 102:
			msg = Messages.bind(Messages.SshMgmtCnxEx_MKDIR_FAIL, login);
		case 103:
			msg = Messages.bind(Messages.SshMgmtCnxEx_CHOWN_SSH_FAIL, login);
			break;
		case 104:
			msg = Messages.bind(Messages.SshMgmtCnxEx_TOUCH_AUTH_FAIL, login);
			break;
		case 105:
			msg = Messages.bind(Messages.SshMgmtCnxEx_CHOWN_AUTH_FAIL, login);
			break;
		case 106:
			msg = Messages.bind(Messages.SshMgmtCnxEx_ADD_KEY_FAIL, login, k);
			break;
		case 107:
			msg = Messages.bind(Messages.SshMgmtCnxEx_SELIUNX_FAIL, login);
		default:
			msg = Messages.bind(Messages.SshMgmtCnxEx_DEPLOY_FAIL, login, k);
			break;
		}
		SshSessionException cause = null;
		if (errMsg != null && errMsg.length() != 0) {
			cause = new SshSessionException(errMsg);
		}
		throw new SshSessionException(msg, cause);
	}

	private static final String DEPLOY_KEY_COMMAND = "id {{LOGIN}} 1>/dev/null 2>&1 || useradd {{LOGIN}} || exit 100 ;"
			+ "umask 077 || exit 101 ;"
			+ "mkdir -p ~{{LOGIN}}/.ssh || exit 102 ;"
			+ "chown {{LOGIN}}:{{LOGIN}} ~{{LOGIN}}/.ssh || exit 103 ;"
			+ "touch ~{{LOGIN}}/.ssh/authorized_keys || exit 104 ;"
			+ "chown {{LOGIN}}:{{LOGIN}} ~{{LOGIN}}/.ssh/authorized_keys || exit 105 ;"
			+ "grep \\\"${KEY}\\\" ~{{LOGIN}}/.ssh/authorized_keys 1>/dev/null || echo \\\"${KEY} {{LOGIN}}@melody\\\" >> ~{{LOGIN}}/.ssh/authorized_keys || exit 106 ;"
			+ "test -x /sbin/restorecon || exit 0 ;"
			+ "/sbin/restorecon -v ~{{LOGIN}}/.ssh/authorized_keys ;" // selinux_support
			+ "/sbin/restorecon -v ~{{LOGIN}}/.ssh/ ;" // selinux_support
			+ "exit 0";

}