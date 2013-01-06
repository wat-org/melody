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

	public static Session enableSshConnectionManagementOnRemoteSystem(
			final SshManagementConnectionDatas cnxMgmtDatas,
			final SshConnectionDatas cnxDatas, final SshPlugInConfiguration conf)
			throws SshException {
		SshConnectionDatas cnxSuperDatas = createSuperConnectionDatas(
				cnxMgmtDatas, cnxDatas);
		addIdentityFile(cnxMgmtDatas, conf);
		Session session = openMasterSession(cnxSuperDatas, conf);
		String suCommand = createCommandToDeployKey(cnxDatas);
		executeCommandToDeployKey(session, suCommand);
		return openUserSession(cnxDatas, conf);
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
			File keypairfile = cnxMgmtDatas.getManagementKeyPairRepository()
					.getPrivateKeyFile(cnxMgmtDatas.getManagementMasterKey());
			try {
				if (!cnxMgmtDatas.getManagementKeyPairRepository()
						.containsKeyPair(cnxMgmtDatas.getManagementMasterKey())) {
					/*
					 * TODO : externalize error message
					 */
					throw new SshException(keypairfile
							+ ": Not accepted. Identity file doesn't exists.");
				}
			} catch (IOException Ex) {
				throw new SshException(keypairfile
						+ ": Not accepted. Identity file is not valid.");
			}
			try {
				/*
				 * TODO : don't attach identity to JSch. Attach identity 'on the
				 * fly' to the session with
				 * session.setIdentityRepository(identityRepository).
				 */
				conf.addIdentity(keypairfile);
			} catch (JSchException Ex) {
				throw new RuntimeException(
						keypairfile
								+ ": Not accepted. Identity file is not valid. Shouldn't happened because key have been validated previously.",
						Ex);
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
			/*
			 * TODO : externalize error message
			 */
			throw new SshException("Connection failed. "
					+ "Master user credentials should be incorrect.", Ex);
		}
	}

	private static Session openUserSession(SshConnectionDatas cnxDatas,
			SshPlugInConfiguration conf) throws SshException {
		try {
			return JSchHelper.openSession(cnxDatas, conf);
		} catch (IncorrectCredentialsException Ex) {
			throw new RuntimeException("Failed to connect to remote system. "
					+ "Ssh Management feature must have fail to do its job.",
					Ex);
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
			+ "selrest() { c=$(readlink -f \"$1\"); [ \"$c\" != \"/\" ] && { /sbin/restorecon -v \"$c\"; selrest \"$(dirname \"$c\")\"; } ; } ;"
			+ "selrest ~{{LOGIN}}/.ssh/authorized_keys || exit 108 ;" // selinux_support
			+ "exit 0";

	private static String createCommandToDeployKey(SshConnectionDatas cnxDatas)
			throws SshException {
		if (cnxDatas.getKeyPairName() == null) {
			throw new SshException(
					"No user's keypairname provided. Ssh management feature require a such info. Please provide a user's keypairname.");
		}
		String key = null;
		try {
			key = cnxDatas.getKeyPairRepository().getPublicKeyInOpenSshFormat(
					cnxDatas.getKeyPairName(), "");
		} catch (IOException Ex) {
			throw new SshException(cnxDatas.getKeyPairRepository()
					.getPrivateKeyFile(cnxDatas.getKeyPairName())
					+ ": Not accepted. Identity file is not valid.");
		}
		if (key.charAt(key.length() - 1) == '\n') {
			key = key.substring(0, key.length() - 2);
		}
		String sCommand = COMMAND_TO_DEPLOY_KEY.replaceAll("[{][{]LOGIN[}][}]",
				cnxDatas.getLogin());
		return "KEY=\"" + key + "\" ; [ $(id -u) = 0 ] && { " + sCommand
				+ "; } || " + "{ sudo su - <<EOF \n" + sCommand + "\nEOF\n }";
	}

	private static void executeCommandToDeployKey(Session session,
			String suCommand) throws SshException {
		log.trace(Messages.bind(Messages.SshMgmtCnxMsg_DEPLOYING, suCommand));
		OutputStream outStream = new LoggerOutputStream("[ssh_cnx_mgmt:"
				+ session.getHost() + "]", LogThreshold.DEBUG);
		int res = -1;
		try {
			res = JSchHelper.execSshCommand(session, suCommand, outStream,
					outStream);
		} catch (InterruptedException Ex) {
			/*
			 * TODO : deal with interrupted exception into execSshCommand
			 */
		}
		switch (res) {
		case 0:
			log.trace(Messages.SshMgmtCnxMsg_DEPLOYED);
			return;
		case 101:
			/*
			 * TODO : throw error
			 */
			break;
		case 102:
			break;
		case 103:
			break;
		case 104:
			break;
		case 105:
			break;
		case 106:
			break;
		case 107:
			break;
		case 108:
			break;
		default:
			break;
		}
	}
}
