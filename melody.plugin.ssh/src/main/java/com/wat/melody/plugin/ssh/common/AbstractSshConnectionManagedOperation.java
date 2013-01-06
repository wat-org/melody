package com.wat.melody.plugin.ssh.common;

import com.jcraft.jsch.Session;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.jsch.IncorrectCredentialsException;
import com.wat.melody.plugin.ssh.common.mgmt.SshConnectionManager;
import com.wat.melody.plugin.ssh.common.mgmt.SshManagementConnectionDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractSshConnectionManagedOperation extends
		AbstractSshOperation implements SshManagementConnectionDatas {

	/**
	 * XML attribute in the SD which define the management master user to use to
	 * connect to remote system for keypair deployment.
	 */
	public static final String MGMT_MASTER_USER_ATTR = "mgmt-master-login";

	/**
	 * XML attribute in the SD which define the management master keypairname to
	 * use to connect to remote system for keypair deployment.
	 */
	public static final String MGMT_MASTER_KEY_ATTR = "mgmt-master-key";

	/**
	 * XML attribute in the SD which define the management master password to
	 * use to connect to remote system for keypair deployment. It can be either
	 * the password of the management master user, or the password of the
	 * keypair of the management master user.
	 */
	public static final String MGMT_MASTER_PASS_ATTR = "mgmt-master-pass";

	private String moMgmtMasterUser;
	private KeyPairName moMgmtMasterKey;
	private String moMgmtMasterPass;

	public AbstractSshConnectionManagedOperation() {
		super();
		initManagementMasterUser();
		initManagementMasterKey();
		initManagementMasterPass();
	}

	private void initManagementMasterUser() {
		moMgmtMasterUser = null;
	}

	private void initManagementMasterKey() {
		moMgmtMasterKey = null;
	}

	private void initManagementMasterPass() {
		moMgmtMasterPass = null;
	}

	@Override
	public void validate() throws SshException {
		super.validate();
		try {
			if (getManagementMasterUser() == null) {
				setManagementMasterUser(getPluginConf().getMgmtMasterUser());
			}
		} catch (SshException Ex) {
			throw new RuntimeException("Failed to assign ssh mgmt master user "
					+ "with value taken in the configuration. "
					+ "Source code have certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
		if (getManagementMasterKey() == null) {
			setManagementMasterKey(getPluginConf().getMgmtMasterKey());
		}
		if (getManagementMasterPass() == null) {
			setManagementMasterPass(getPluginConf().getMgmtMasterPass());
		}
		if (getManagementMasterKey() == null
				&& getManagementMasterPass() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_PASSWORD_OR_PK_ATTR,
					MGMT_MASTER_PASS_ATTR, MGMT_MASTER_KEY_ATTR));
		}
	}

	/**
	 * <p>
	 * Open a ssh session.
	 * </p>
	 * 
	 * @return the opened session.
	 * 
	 * @throws SshException
	 * @throws InterruptedException
	 */
	public Session openSession() throws SshException, InterruptedException {
		try {
			return super.openSession();
		} catch (IncorrectCredentialsException Ex) {
			if (!getPluginConf().getMgmtEnable()) {
				throw Ex;
			}
			return SshConnectionManager
					.enableSshConnectionManagementOnRemoteSystem(this, this,
							getPluginConf());
		}
	}

	@Override
	public String toString() {
		return "{ user:" + getManagementMasterUser() + ", password:"
				+ getManagementMasterPass() + ", keypairname:"
				+ getManagementMasterKey() + " }";
	}

	@Override
	public KeyPairRepository getManagementKeyPairRepository() {
		return getPluginConf().getKeyPairRepo();
	}

	@Override
	public String getManagementMasterUser() {
		return moMgmtMasterUser;
	}

	@Attribute(name = MGMT_MASTER_USER_ATTR)
	public String setManagementMasterUser(String mgmtMasterUser)
			throws SshException {
		if (mgmtMasterUser == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a user.");
		}
		if (mgmtMasterUser.trim().length() == 0) {
			throw new SshException(Messages.bind(
					Messages.SshEx_EMPTY_LOGIN_ATTR, mgmtMasterUser));
		}
		String previous = getManagementMasterUser();
		moMgmtMasterUser = mgmtMasterUser;
		return previous;
	}

	@Override
	public KeyPairName getManagementMasterKey() {
		return moMgmtMasterKey;
	}

	@Attribute(name = MGMT_MASTER_KEY_ATTR)
	public KeyPairName setManagementMasterKey(KeyPairName keyPairName) {
		/*
		 * Can be null, when the connection as ssh management master user should
		 * be done without keypair.
		 */
		KeyPairName previous = getManagementMasterKey();
		moMgmtMasterKey = keyPairName;
		return previous;
	}

	@Override
	public String getManagementMasterPass() {
		return moMgmtMasterPass;
	}

	@Attribute(name = MGMT_MASTER_PASS_ATTR)
	public String setManagementMasterPass(String mgmtMasterPass) {
		/*
		 * Can be null, when the connection as ssh management master user should
		 * be done with a keypair which have no passphrase.
		 */
		String previous = getManagementMasterUser();
		moMgmtMasterPass = mgmtMasterPass;
		return previous;
	}

}