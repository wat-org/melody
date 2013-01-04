package com.wat.melody.plugin.ssh.common;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractManagedSshOperation extends AbstractSshOperation {

	/**
	 * XML attribute in the SD which define the name of the management keypair
	 * to connect-with/deploy on remote system.
	 */
	public static final String MGMT_KEY_ATTR = "mgmt-key";

	/**
	 * XML attribute in the SD which define the password of the management
	 * keypair.
	 */
	public static final String MGMT_PASS_ATTR = "mgmt-pass";

	/**
	 * XML attribute in the SD which define the management master user to use to
	 * connect to remote system for management keypair deployment.
	 */
	public static final String MGMT_MASTER_USER_ATTR = "mgmt-master-login";

	/**
	 * XML attribute in the SD which define the management master key to use to
	 * connect to remote system for management keypair deployment.
	 */
	public static final String MGMT_MASTER_KEY_ATTR = "mgmt-master-login";

	/**
	 * XML attribute in the SD which define the management master pass to use to
	 * connect to remote system for management keypair deployment. It can be
	 * either the password of the management master user, or the password of
	 * keypair of the management master user.
	 */
	public static final String MGMT_MASTER_PASS_ATTR = "mgmt-master-login";

	private KeyPairName moMgmtKey;
	private String moMgmtPass;
	private String moMgmtMasterUser;
	private KeyPairName moMgmtMasterKey;
	private String moMgmtMasterPass;

	public AbstractManagedSshOperation() {
		super();
		setMgmtKey(getPluginConf().getMgmtKey());
		setMgmtPass(getPluginConf().getMgmtPass());
		try {
			setMgmtMasterUser(getPluginConf().getMgmtMasterUser());
		} catch (SshException Ex) {
			throw new RuntimeException("Failed to assign ssh mgmt master user "
					+ "with value taken in the configuration. "
					+ "Source code have certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
		setMgmtMasterKey(getPluginConf().getMgmtMasterKey());
		setMgmtMasterPass(getPluginConf().getMgmtMasterPass());
	}

	@Override
	public void validate() throws SshException {
		super.validate();
	}

	public KeyPairName getMgmtKey() {
		return moMgmtKey;
	}

	@Attribute(name = MGMT_KEY_ATTR)
	public KeyPairName setMgmtKey(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		KeyPairName previous = getMgmtKey();
		moMgmtKey = keyPairName;
		return previous;
	}

	public String getMgmtPass() {
		return moMgmtPass;
	}

	@Attribute(name = MGMT_PASS_ATTR)
	public String setMgmtPass(String mgmtPass) {
		if (mgmtPass == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a password.");
		}
		String previous = getMgmtPass();
		moMgmtPass = mgmtPass;
		return previous;
	}

	public String getMgmtMasterUser() {
		return moMgmtMasterUser;
	}

	@Attribute(name = MGMT_MASTER_USER_ATTR)
	public String setMgmtMasterUser(String mgmtMasterUser) throws SshException {
		if (mgmtMasterUser == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a user.");
		}
		if (mgmtMasterUser.trim().length() == 0) {
			throw new SshException(Messages.bind(
					Messages.SshEx_EMPTY_LOGIN_ATTR, mgmtMasterUser));
		}
		String previous = getMgmtMasterUser();
		moMgmtMasterUser = mgmtMasterUser;
		return previous;
	}

	public KeyPairName getMgmtMasterKey() {
		return moMgmtMasterKey;
	}

	@Attribute(name = MGMT_MASTER_KEY_ATTR)
	public KeyPairName setMgmtMasterKey(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		KeyPairName previous = getMgmtMasterKey();
		moMgmtMasterKey = keyPairName;
		return previous;
	}

	public String getMgmtMasterPass() {
		return moMgmtMasterPass;
	}

	@Attribute(name = MGMT_MASTER_PASS_ATTR)
	public String setMgmtMasterPass(String mgmtMasterPass) {
		if (mgmtMasterPass == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String which represents a password.");
		}
		String previous = getMgmtMasterUser();
		moMgmtMasterPass = mgmtMasterPass;
		return previous;
	}

}