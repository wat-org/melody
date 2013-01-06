package com.wat.melody.plugin.ssh.common.mgmt;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.utils.LogThreshold;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.jsch.IncorrectCredentialsException;
import com.wat.melody.plugin.ssh.common.jsch.JSchHelper;
import com.wat.melody.plugin.ssh.common.jsch.LoggerOutputStream;
import com.wat.melody.plugin.ssh.common.jsch.SshConnectionDatas;

public class SshConnectionManager {

	private static Log log = LogFactory.getLog(SshConnectionManager.class);

	/**
	 * <p>
	 * Deploy the given key on the given remote system for the specified user,
	 * using the given master user credentials.
	 * </p>
	 * 
	 * @param cnxMgmtDatas
	 *            contains the master user credentials (e.g. credentials to
	 *            connect to remote system to deploy the key on).
	 * @param cnxDatas
	 *            contains the remote system datas, the specified user and the
	 *            key to deploy.
	 * @param conf
	 *            contains all datas to open a ssh session (proxy, timeouts,
	 *            packets size, etc).
	 * 
	 * @return the opened session (which was opened using the specified user
	 *         credentials).
	 * 
	 * @throws SshException
	 *             if the Ssh Management Feature failed to operate properly (ex
	 *             : no user key have been provided, or the given user key is
	 *             not valid, or the given master user key is not valid, or the
	 *             given master user credentials are invalid, or the remote host
	 *             is not reachable - no route to host, dns failure, ... -, or
	 *             ... ).
	 * @throws InterruptedException
	 *             if the key deployment was interrupted. Note that when this
	 *             exception is raised, the command have been completely
	 *             executed.
	 */
	public static Session enableSshConnectionManagementOnRemoteSystem(
			SshManagementConnectionDatas cnxMgmtDatas,
			SshConnectionDatas cnxDatas, SshPlugInConfiguration conf)
			throws SshException, InterruptedException {
		if (cnxMgmtDatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshManagementConnectionDatas.class.getCanonicalName()
					+ ".");
		}
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
		try {
			if (cnxDatas.getKeyPairName() == null) {
				throw new SshException(Messages.SshMgmtCnxEx_NO_KEY);
			}
			SshConnectionDatas cnxSuperDatas = createSuperConnectionDatas(
					cnxMgmtDatas, cnxDatas);
			addIdentityFile(cnxMgmtDatas, conf);
			Session session = openMasterSession(cnxSuperDatas, conf);
			String key = getKey(cnxDatas);
			String suCommand = createCommandToDeployKey(cnxDatas, key);
			int res = executeCommandToDeployKey(session, suCommand);
			analyseCommandToDeployKeyResult(res, cnxDatas, key);
			return openUserSession(cnxDatas, conf);
		} catch (SshException Ex) {
			throw new SshException(Messages.SshMgmtCnxEx_GENERIC_FAIL, Ex);
		}
	}

	private static SshConnectionDatas createSuperConnectionDatas(
			final SshManagementConnectionDatas cnxMgmtDatas,
			final SshConnectionDatas cnxDatas) {
		return new SshConnectionDatas() {

			@Override
			public void showMessage(String arg0) {
			}

			@Override
			public boolean promptYesNo(String arg0) {
				return cnxDatas.promptYesNo(arg0);
			}

			@Override
			public boolean promptPassword(String arg0) {
				return true;
			}

			@Override
			public boolean promptPassphrase(String arg0) {
				return true;
			}

			@Override
			public KeyPairName getKeyPairName() {
				return cnxMgmtDatas.getManagementMasterKey();
			}

			@Override
			public KeyPairRepository getKeyPairRepository() {
				return cnxDatas.getKeyPairRepository();
			}

			@Override
			public String getPassword() {
				return cnxMgmtDatas.getManagementMasterPass();
			}

			@Override
			public String getPassphrase() {
				return cnxMgmtDatas.getManagementMasterPass();
			}

			@Override
			public String getLogin() {
				return cnxMgmtDatas.getManagementMasterUser();
			}

			@Override
			public Port getPort() {
				return cnxDatas.getPort();
			}

			@Override
			public Host getHost() {
				return cnxDatas.getHost();
			}

			@Override
			public String toString() {
				return "{ host:" + getHost() + ", port:" + getPort()
						+ ", user:" + getLogin() + ", password:"
						+ getPassword() + ", keypairname:" + getKeyPairName()
						+ " }";
			}
		};
	}

	private static void addIdentityFile(
			SshManagementConnectionDatas cnxMgmtDatas,
			SshPlugInConfiguration conf) throws SshException {
		if (cnxMgmtDatas.getManagementMasterKey() != null) {
			KeyPairRepository kpr = cnxMgmtDatas
					.getManagementKeyPairRepository();
			KeyPairName kpn = cnxMgmtDatas.getManagementMasterKey();
			File kpf = cnxMgmtDatas.getManagementKeyPairRepository()
					.getPrivateKeyFile(kpn);
			try {
				if (!kpr.containsKeyPair(kpn)) {
					kpr.createKeyPair(kpn, conf.getKeyPairSize(),
							cnxMgmtDatas.getManagementMasterPass());
				}
			} catch (IOException Ex) {
				throw new SshException(Messages.bind(
						Messages.SshMgmtCnxEx_INVALID_MASTER_KEY, kpf), Ex);
			}
			try {
				/*
				 * TODO : don't attach identity to JSch. Attach identity 'on the
				 * fly' to the session with
				 * session.setIdentityRepository(identityRepository).
				 */
				conf.addIdentity(kpf);
			} catch (JSchException Ex) {
				throw new RuntimeException("Unexpected error while adding a "
						+ "keypair '" + kpf + "' to the ssh session. "
						+ "Because this key have been previously validated, "
						+ "such error cannot happened. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.", Ex);
			}
		}
	}

	private static Session openMasterSession(SshConnectionDatas cnxSuperDatas,
			SshPlugInConfiguration conf) throws SshException {
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_OPENING, cnxSuperDatas));
		try {
			Session session = JSchHelper.openSession(cnxSuperDatas, conf);
			log.trace(Messages.SshMgmtCnxMsg_OPENED);
			return session;
		} catch (IncorrectCredentialsException Ex) {
			throw new SshException(
					Messages.SshMgmtCnxEx_INVALID_MASTER_CREDENTIALS, Ex);
		}
	}

	private static Session openUserSession(SshConnectionDatas cnxDatas,
			SshPlugInConfiguration conf) throws SshException {
		try {
			return JSchHelper.openSession(cnxDatas, conf);
		} catch (IncorrectCredentialsException Ex) {
			throw new RuntimeException("Failed to connect to remote system. "
					+ "Ssh Management Feature must have fail to do its job. "
					+ "Please send feedback to the development team so that "
					+ "they can provide a fix.", Ex);
		}
	}

	private static String getKey(SshConnectionDatas cnxDatas)
			throws SshException {
		KeyPairRepository kpr = cnxDatas.getKeyPairRepository();
		KeyPairName kpn = cnxDatas.getKeyPairName();
		String key = null;
		try {
			key = kpr.getPublicKeyInOpenSshFormat(kpn, null);
		} catch (IOException Ex) {
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_INVALID_KEY,
					kpr.getPrivateKeyFile(kpn)), Ex);
		}
		return key;
	}

	private static String createCommandToDeployKey(SshConnectionDatas cnxDatas,
			String key) throws SshException {
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
	private static int executeCommandToDeployKey(Session session,
			String suCommand) throws SshException, InterruptedException {
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_DEPLOYING, suCommand));
		OutputStream outStream = new LoggerOutputStream("[ssh_cnx_mgmt:"
				+ session.getHost() + "]", LogThreshold.DEBUG);
		try {
			return JSchHelper.execSshCommand(session, suCommand, outStream,
					outStream);
		} catch (InterruptedException Ex) {
			InterruptedException iex = new InterruptedException(
					Messages.SshMgmtCnxEx_DEPLOY_INTERRUPTED);
			iex.initCause(Ex);
			throw iex;
		}
	}

	private static void analyseCommandToDeployKeyResult(int res,
			SshConnectionDatas cnxDatas, String key) throws SshException {
		switch (res) {
		case 0:
			log.trace(Messages.SshMgmtCnxMsg_DEPLOYED);
			return;
		case 100:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_USERADD_FAIL, cnxDatas.getLogin()));
		case 101:
			throw new SshException(Messages.SshMgmtCnxEx_UMASK_FAIL);
		case 102:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_MKDIR_FAIL, cnxDatas.getLogin()));
		case 103:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_CHOWN_SSH_FAIL, cnxDatas.getLogin()));
		case 104:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_TOUCH_AUTH_FAIL, cnxDatas.getLogin()));
		case 105:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_CHOWN_AUTH_FAIL, cnxDatas.getLogin()));
		case 106:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_ADD_KEY_FAIL, cnxDatas.getLogin(),
					key));
		case 107:
			throw new SshException(Messages.bind(
					Messages.SshMgmtCnxEx_SELIUNX_FAIL, cnxDatas.getLogin()));
		default:
			throw new SshException(Messages.bind(
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
			+ "selrest() { c=$(readlink -f \"$1\"); [ \"$c\" != \"/\" ] && { /sbin/restorecon -v \"$c\" || exit 107 ; selrest \"$(dirname \"$c\")\"; } ; } ;"
			+ "selrest ~{{LOGIN}}/.ssh/authorized_keys ;" // selinux_support
			+ "exit 0";

}