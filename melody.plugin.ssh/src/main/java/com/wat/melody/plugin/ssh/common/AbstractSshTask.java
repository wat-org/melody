package com.wat.melody.plugin.ssh.common;

import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractSshTask implements ITask {

	/**
	 * The 'keyPairRepository' XML attribute
	 */
	public static final String KEYPAIR_REPO_ATTR = "keyPairRepository";

	/**
	 * The 'keyPairName' XML attribute
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";

	/**
	 * The 'passphrase' XML attribute
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	private ITaskContext moContext;
	private SshPlugInConfiguration moPluginConf;
	private KeyPairRepository moKeyPairRepository;
	private KeyPairName moKeyPairName;
	private String msPassphrase;

	public AbstractSshTask() {
		initContext();
		initPluginConf();
		initPassphrase();
		initKeyPairName();
		initKeyPairRepository();
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private void initPassphrase() {
		msPassphrase = null;
	}

	private void initKeyPairName() {
		moKeyPairName = null;
	}

	private void initKeyPairRepository() {
		moKeyPairRepository = null;
	}

	@Override
	public void validate() throws SshException {
		// Validate task attribute
		if (getKeyPairName() == null) {
			return;
		}
		if (getKeyPairRepository() == null) {
			setKeyPairRepository(getPluginConf().getKeyPairRepo());
		}
		KeyPairRepository kpr = getKeyPairRepository();
		try {
			if (!kpr.containsKeyPair(getKeyPairName())) {
				kpr.createKeyPair(getKeyPairName(), getPluginConf()
						.getKeyPairSize(), getPassphrase());
			}
		} catch (IOException Ex) {
			throw new RuntimeException(Ex);
		}
		try {
			getPluginConf()
					.addIdentity(kpr.getPrivateKeyFile(getKeyPairName()));
		} catch (JSchException Ex) {
			throw new SshException(Ex);
		}
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	/**
	 * <p>
	 * Set the {@link ITaskContext} of this object with the given
	 * {@link ITaskContext} and retrieve the Ssh Plug-In
	 * {@link SshPlugInConfiguration}.
	 * </p>
	 * 
	 * @param p
	 *            is the {@link ITaskContext} to set.
	 * 
	 * @throws SshException
	 *             if an error occurred while retrieving the Ssh Plug-In
	 *             {@link SshPlugInConfiguration}.
	 * @throws IllegalArgumentException
	 *             if the given {@link ITaskContext} is <tt>null</tt>.
	 */
	@Override
	public void setContext(ITaskContext p) throws SshException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(SshPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new SshException(Ex);
		}

	}

	protected SshPlugInConfiguration getPluginConf() {
		return moPluginConf;
	}

	public SshPlugInConfiguration setPluginConf(SshPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		SshPlugInConfiguration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	public KeyPairRepository getKeyPairRepository() {
		return moKeyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepository setKeyPairRepository(
			KeyPairRepository keyPairRepository) {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		KeyPairRepository previous = getKeyPairRepository();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return moKeyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		KeyPairName previous = getKeyPairName();
		moKeyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return msPassphrase;
	}

	@Attribute(name = PASSPHRASE_ATTR)
	public String setPassphrase(String sPassphrase) {
		if (sPassphrase == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassphrase();
		msPassphrase = sPassphrase;
		return previous;
	}

}
