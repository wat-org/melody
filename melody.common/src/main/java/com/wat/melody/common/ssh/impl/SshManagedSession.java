package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.HostKey;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.IncorrectCredentialsException;
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

	public ISshUserDatas setManagementUserDatas(ISshUserDatas nd) {
		ISshUserDatas previous = getManagementUserDatas();
		_sshManagementUserDatas = nd;
		return previous;
	}

	@Override
	public synchronized void connect() throws SshSessionException,
			IncorrectCredentialsException, InterruptedException {
		try {
			openSession(_sshUserDatas);
		} catch (IncorrectCredentialsException Ex) {
			/*
			 * On auth error, connect with ManagementMaster User and deploy
			 * user's key. Then open session.
			 */
			connectAsMasterUserAndDeployKey();
			openUserSession();
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

	public HostKey getHostKey() {
		return _session.getHostKey();
	}

	private void openSession(ISshUserDatas sshUserDatas)
			throws SshSessionException, InterruptedException {
		_session.setUserDatas(sshUserDatas);
		_session.connect();
	}

	private void openUserSession() throws SshSessionException,
			InterruptedException {
		try {
			openSession(_sshUserDatas);
		} catch (IncorrectCredentialsException Ex) {
			throw new RuntimeException("Failed to connect to remote system. "
					+ "Ssh Management Feature must have fail to do its job. "
					+ "Please send feedback to the development team so that "
					+ "they can provide a fix.", Ex);
		}
	}

	private void connectAsMasterUserAndDeployKey() throws SshSessionException,
			IncorrectCredentialsException, InterruptedException {
		if (getManagementUserDatas() == null) {
			throw new IllegalStateException("No user datas defined.");
		}
		try {
			if (getUserDatas().getKeyPairName() == null) {
				throw new SshSessionException(Messages.SshMgmtCnxEx_NO_KEY);
			}
			openMasterSession();
			String key = getKey();
			String suCommand = createCommandToDeployKey(key);
			int res = executeCommandToDeployKey(suCommand);
			analyseCommandToDeployKeyResult(res, key);
		} catch (SshSessionException Ex) {
			throw new SshSessionException(Messages.SshMgmtCnxEx_GENERIC_FAIL,
					Ex);
		} finally {
			disconnect();
		}
	}

	private void openMasterSession() throws SshSessionException,
			IncorrectCredentialsException, InterruptedException {
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_OPENING,
				getManagementUserDatas()));
		try {
			openSession(_sshManagementUserDatas);
			log.trace(Messages.SshMgmtCnxMsg_OPENED);
		} catch (IncorrectCredentialsException Ex) {
			throw new IncorrectCredentialsException(
					Messages.SshMgmtCnxEx_INVALID_MASTER_CREDENTIALS, Ex);
		}
	}

	private String getKey() throws SshSessionException {
		ISshUserDatas cnxDatas = getUserDatas();
		KeyPairRepository kpr = cnxDatas.getKeyPairRepository();
		KeyPairName kpn = cnxDatas.getKeyPairName();
		String key = null;
		try {
			key = kpr.getPublicKeyInOpenSshFormat(kpn, null);
		} catch (IOException Ex) {
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_INVALID_KEY,
					kpr.getPrivateKeyFile(kpn)), Ex);
		}
		return key;
	}

	private String createCommandToDeployKey(String key)
			throws SshSessionException {
		ISshUserDatas cnxDatas = getUserDatas();
		String sCommand = COMMAND_TO_DEPLOY_KEY.replaceAll("[{][{]LOGIN[}][}]",
				cnxDatas.getLogin());
		return "KEY=\"" + key + "\" ; [ $(id -g) = 0 ] && { " + sCommand
				+ "; } || " + "{ sudo su - <<EOF \n" + sCommand + "\nEOF\n }";
		/*
		 * TODO : find a way to test remote sudo configuration (ex : require
		 * tty, password needed, ...) ...
		 */
	}

	/**
	 * 
	 * @param session
	 * @param suCommand
	 * 
	 * @return the return value of the command.
	 * 
	 * @throws SshException
	 * @throws InterruptedException
	 *             if the key deployment was interrupted. Note that when this
	 *             exception is raised, the command have been completely
	 *             executed.
	 */
	private int executeCommandToDeployKey(String suCommand)
			throws SshSessionException, InterruptedException {
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_DEPLOYING, suCommand));
		OutputStream outStream = new LoggerOutputStream("[ssh_cnx_mgmt:"
				+ getConnectionDatas().getHost() + "]", LogThreshold.DEBUG);
		try {
			return execRemoteCommand(suCommand, outStream, outStream);
		} catch (InterruptedException Ex) {
			InterruptedException iex = new InterruptedException(
					Messages.SshMgmtCnxEx_DEPLOY_INTERRUPTED);
			iex.initCause(Ex);
			throw iex;
		}
	}

	private void analyseCommandToDeployKeyResult(int res, String key)
			throws SshSessionException {
		ISshUserDatas cnxDatas = getUserDatas();
		switch (res) {
		case 0:
			log.trace(Messages.SshMgmtCnxMsg_DEPLOYED);
			return;
		case 100:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_USERADD_FAIL, cnxDatas.getLogin()));
		case 101:
			throw new SshSessionException(Messages.SshMgmtCnxEx_UMASK_FAIL);
		case 102:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_MKDIR_FAIL, cnxDatas.getLogin()));
		case 103:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_CHOWN_SSH_FAIL, cnxDatas.getLogin()));
		case 104:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_TOUCH_AUTH_FAIL, cnxDatas.getLogin()));
		case 105:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_CHOWN_AUTH_FAIL, cnxDatas.getLogin()));
		case 106:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_ADD_KEY_FAIL, cnxDatas.getLogin(),
					key));
		case 107:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_SELIUNX_FAIL, cnxDatas.getLogin()));
		default:
			throw new SshSessionException(Messages.bind(
					Messages.SshMgmtCnxEx_DEPLOY_GENERIC_FAIL,
					cnxDatas.getLogin(), key));
		}
	}

	private static final String COMMAND_TO_DEPLOY_KEY = "id {{LOGIN}} 1>/dev/null 2>&1 || useradd {{LOGIN}} || exit 100 ;"
			+ "umask 077 || exit 101 ;"
			+ "mkdir -p ~{{LOGIN}}/.ssh || exit 102 ;"
			+ "chown {{LOGIN}}:{{LOGIN}} ~{{LOGIN}}/.ssh || exit 103 ;"
			+ "touch ~{{LOGIN}}/.ssh/authorized_keys || exit 104 ;"
			+ "chown {{LOGIN}}:{{LOGIN}} ~{{LOGIN}}/.ssh/authorized_keys || exit 105 ;"
			+ "grep \"${KEY}\" ~{{LOGIN}}/.ssh/authorized_keys 1>/dev/null || echo \"${KEY} {{LOGIN}}@melody\" >> ~{{LOGIN}}/.ssh/authorized_keys || exit 106 ;"
			+ "test -x /sbin/restorecon || exit 0 ;"
			+ "selrest ~{{LOGIN}}/.ssh/authorized_keys ;" // selinux_support
			+ "selrest ~{{LOGIN}}/.ssh/ ;" // selinux_support
			+ "exit 0";

}