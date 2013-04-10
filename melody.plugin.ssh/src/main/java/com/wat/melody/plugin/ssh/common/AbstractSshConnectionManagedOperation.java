package com.wat.melody.plugin.ssh.common;

import java.io.IOException;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshUserDatas;
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
	 * Defines the management master user to use to connect to remote system for
	 * keypair deployment.
	 */
	public static final String MGMT_MASTER_USER_ATTR = "mgmt-master-login";

	/**
	 * Defines the management master password to use to connect to remote system
	 * for keypair deployment. It can be either the password of the management
	 * master user, or the password of the management keypair.
	 */
	public static final String MGMT_MASTER_PASS_ATTR = "mgmt-master-pass";

	/**
	 * Defines the path of the keypair repository which contains the management
	 * keypair used to connect to the remote system for keypair deployment.
	 */
	public static final String MGMT_MASTER_REPO_ATTR = "mgmt-master-repo";

	/**
	 * Defines the management master keypair - relative to the
	 * keypair-repository - to use to connect to remote system for keypair
	 * deployment. If the keypair-repository doesn't contains such keypair, it
	 * will not be automatically created.
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

		// if Ssh Management Feature is disable => exit
		if (getSshPlugInConf().getMgmtEnable() == false) {
			return;
		}

		// Ensure a user keypairname is defined
		if (getKeyPairName() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_USER_KEYPAIRNAME_ATTR, new Object[] {
							KEYPAIR_NAME_ATTR,
							SshPlugInConfiguration.MGMT_ENABLE,
							getSshPlugInConf().getFilePath() }));
		}

		// Verify that the Management User Login is defined
		if (getManagementLogin() == null
				&& getSshPlugInConf().getManagementLogin() != null) {
			setManagementLogin(getSshPlugInConf().getManagementLogin());
		}
		if (getManagementLogin() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_MGMT_LOGIN_ATTR, new Object[] {
							MGMT_MASTER_USER_ATTR,
							SshPlugInConfiguration.MGMT_LOGIN,
							SshPlugInConfiguration.MGMT_ENABLE,
							getSshPlugInConf().getFilePath() }));
		}

		// Verify that the Management User Credentials are defined
		if (getManagementKeyPairRepositoryPath() == null) {
			setManagementKeyPairRepositoryPath(getSshPlugInConf()
					.getKeyPairRepositoryPath());
		}
		if (getManagementKeyPairName() == null) {
			setManagementKeyPairName(getSshPlugInConf()
					.getManagementKeyPairName());
		}
		if (getManagementPassword() == null) {
			setManagementPassword(getSshPlugInConf().getManagementPassword());
		}
		if (getManagementKeyPairName() == null
				&& getManagementPassword() == null) {
			throw new SshException(Messages.bind(
					Messages.SshEx_MISSING_MGMT_PASSWORD_OR_PK_ATTR,
					new Object[] { MGMT_MASTER_PASS_ATTR, MGMT_MASTER_KEY_ATTR,
							SshPlugInConfiguration.MGMT_PASSWORD,
							SshPlugInConfiguration.MGMT_KEYPAIRNAME,
							SshPlugInConfiguration.MGMT_ENABLE,
							getSshPlugInConf().getFilePath() }));
		}

		// Create the Management User KeyPair if it doesn't exists
		if (getManagementKeyPairName() == null) {
			return;
		}
		KeyPairRepositoryPath kprp = getManagementKeyPairRepositoryPath();
		KeyPairRepository kpr = KeyPairRepository.getKeyPairRepository(kprp);
		try {
			kpr.createKeyPair(getManagementKeyPairName(), getSshPlugInConf()
					.getKeyPairSize(), getManagementPassword());
		} catch (IOException Ex) {
			throw new SshException(Ex);
		}
	}

	/**
	 * @return an {@link ISshSession} which provides additional Ssh Management
	 *         features (if this feature is enabled).
	 */
	@Override
	protected ISshSession createSession() throws SshException {
		if (getSshPlugInConf().getMgmtEnable() == false) {
			return super.createSession();
		}
		SshManagedSession session = new SshManagedSession(super.createSession());
		session.setManagementUserDatas(getMgmtUserDatas());
		return session;
	}

	public String getManagementLogin() {
		return getMgmtUserDatas().getLogin();
	}

	@Attribute(name = MGMT_MASTER_USER_ATTR)
	public String setManagementLogin(String mgmtLogin) {
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

	public KeyPairRepositoryPath getManagementKeyPairRepositoryPath() {
		return getMgmtUserDatas().getKeyPairRepositoryPath();
	}

	@Attribute(name = MGMT_MASTER_REPO_ATTR)
	public KeyPairRepositoryPath setManagementKeyPairRepositoryPath(
			KeyPairRepositoryPath mgmtKeyPairRepository) {
		return getMgmtUserDatas().setKeyPairRepositoryPath(
				mgmtKeyPairRepository);
	}

	public KeyPairName getManagementKeyPairName() {
		return getMgmtUserDatas().getKeyPairName();
	}

	@Attribute(name = MGMT_MASTER_KEY_ATTR)
	public KeyPairName setManagementKeyPairName(KeyPairName mgmtKeyPairName) {
		return getMgmtUserDatas().setKeyPairName(mgmtKeyPairName);
	}

}