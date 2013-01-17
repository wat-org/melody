package com.wat.melody.plugin.ssh.common;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.exception.IllegalSshUserDatasException;
import com.wat.melody.common.ssh.impl.SshManagedSession;
import com.wat.melody.common.ssh.impl.SshUserDatas;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractSshConnectionManagedOperation extends
		AbstractSshOperation {

	/**
	 * XML attribute in the SD which define the management master user to use to
	 * connect to remote system for keypair deployment.
	 */
	public static final String MGMT_MASTER_USER_ATTR = "mgmt-master-login";

	/**
	 * XML attribute in the SD which define the management master password to
	 * use to connect to remote system for keypair deployment. It can be either
	 * the password of the management master user, or the password of the
	 * keypair of the management master user.
	 */
	public static final String MGMT_MASTER_PASS_ATTR = "mgmt-master-pass";

	/**
	 * XML attribute in the SD which define the path of the keypair repository
	 * which contains the keypair used to connect to the remote system for
	 * keypair deployment.
	 */
	public static final String MGMT_MASTER_REPO_ATTR = "mgmt-master-repo";

	/**
	 * XML attribute in the SD which define the management master keypairname to
	 * use to connect to remote system for keypair deployment.
	 */
	public static final String MGMT_MASTER_KEY_ATTR = "mgmt-master-key";

	private ISshUserDatas moMgmtUserDatas;

	public AbstractSshConnectionManagedOperation() {
		super();
		setMgmtUserDatas(new SshUserDatas());
	}

	private ISshUserDatas getMgmtUserDatas() {
		return moMgmtUserDatas;
	}

	private ISshUserDatas setMgmtUserDatas(ISshUserDatas ud) {
		ISshUserDatas previous = getMgmtUserDatas();
		moMgmtUserDatas = ud;
		return previous;
	}

	@Override
	public void validate() throws SshException {
		super.validate();
		try {
			if (getManagementLogin() == null) {
				setManagementLogin(getPluginConf().getManagementLogin());
			}
		} catch (IllegalSshUserDatasException Ex) {
			throw new RuntimeException("Failed to assign ssh mgmt master user "
					+ "with value taken in the configuration. "
					+ "Source code have certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
		if (getManagementKeyPairRepository() == null) {
			setManagementKeyPairRepository(getPluginConf().getKeyPairRepo());
		}
		if (getManagementKeyPairName() == null) {
			setManagementKeyPairName(getPluginConf().getManagementKeyPairName());
		}
		if (getManagementPassword() == null) {
			setManagementPassword(getPluginConf().getManagementPassword());
		}
		if (getManagementKeyPairName() == null
				&& getManagementPassword() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_PASSWORD_OR_PK_ATTR,
					MGMT_MASTER_PASS_ATTR, MGMT_MASTER_KEY_ATTR));
		}
	}

	/**
	 * Create a Managed Session, which will deploy user's key if necessary.
	 */
	@Override
	protected ISshSession createSession() {
		SshManagedSession session = new SshManagedSession(super.createSession());
		session.setManagementUserDatas(getMgmtUserDatas());
		return session;
	}

	public String getManagementLogin() {
		return getMgmtUserDatas().getLogin();
	}

	@Attribute(name = MGMT_MASTER_USER_ATTR)
	public String setManagementLogin(String mgmtLogin)
			throws IllegalSshUserDatasException {
		if (mgmtLogin == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a login).");
		}
		return getMgmtUserDatas().setLogin(mgmtLogin);
	}

	public String getManagementPassword() {
		return getMgmtUserDatas().getPassword();
	}

	@Attribute(name = MGMT_MASTER_PASS_ATTR)
	public String setManagementPassword(String mgmtPass) {
		return getMgmtUserDatas().setPassword(mgmtPass);
	}

	public KeyPairRepository getManagementKeyPairRepository() {
		return getMgmtUserDatas().getKeyPairRepository();
	}

	@Attribute(name = MGMT_MASTER_REPO_ATTR)
	public KeyPairRepository setManagementKeyPairRepository(
			KeyPairRepository mgmtKeyPairRepository) {
		return getMgmtUserDatas().setKeyPairRepository(mgmtKeyPairRepository);
	}

	public KeyPairName getManagementKeyPairName() {
		return getMgmtUserDatas().getKeyPairName();
	}

	@Attribute(name = MGMT_MASTER_KEY_ATTR)
	public KeyPairName setManagementKeyPairName(KeyPairName mgmtKeyPairName) {
		return getMgmtUserDatas().setKeyPairName(mgmtKeyPairName);
	}

}