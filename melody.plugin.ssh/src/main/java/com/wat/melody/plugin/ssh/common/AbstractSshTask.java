package com.wat.melody.plugin.ssh.common;

import java.io.File;
import java.io.IOException;

import com.jcraft.jsch.JSchException;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.plugin.ssh.common.exception.ConfigurationException;
import com.wat.melody.plugin.ssh.common.exception.SshException;

public abstract class AbstractSshTask implements ITask {

	/**
	 * The 'keyPairRepository' XML attribute
	 */
	public static final String KEYPAIR_REPO_ATTR = "keyPairRepository";

	/**
	 * The 'keyPairName' XML attribute
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";
	public static final String KEYPAIR_NAME_PATTERN = "[.\\d\\w-_\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	/**
	 * The 'passphrase' XML attribute
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	private ITaskContext moContext;
	private Configuration moPluginConf;
	private File moKeyPairRepository;
	private String moKeyPairName;
	private String msPassphrase;

	public AbstractSshTask() {
		initContext();
		initPluginConf();
		initKeyPairRepository();
		initPrivateKey();
		initPassphrase();
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private void initKeyPairRepository() {
		moKeyPairRepository = null;
	}

	private void initPrivateKey() {
		moKeyPairName = null;
	}

	private void initPassphrase() {
		msPassphrase = null;
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
		KeyPairRepository kpr = null;
		try {
			kpr = new KeyPairRepository(getKeyPairRepository());
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the KeyPair Repository " + getKeyPairRepository() + ". "
					+ "Because this KeyPair Repository have been previously "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced, or an external process made this "
					+ "KeyPair Repository is no more available.", Ex);
		}
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

	@Override
	public void setContext(ITaskContext p) throws SshException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(Configuration.get(getContext().getProcessorManager()));
		} catch (ConfigurationException Ex) {
			throw new SshException(Ex);
		}

	}

	protected Configuration getPluginConf() {
		return moPluginConf;
	}

	public Configuration setPluginConf(Configuration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		Configuration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	public File getKeyPairRepository() {
		return moKeyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public File setKeyPairRepository(File keyPairRepository)
			throws SshException {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		try {
			Tools.validateDirExists(keyPairRepository.getAbsolutePath());
		} catch (IllegalDirectoryException Ex) {
			throw new SshException(
					Messages.bind(Messages.SshEx_INVALID_KEYPAIR_REPO_ATTR,
							keyPairRepository), Ex);
		}
		File previous = getKeyPairRepository();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	public String getKeyPairName() {
		return moKeyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public String setKeyPairName(String keyPairName) throws SshException {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (keyPairName.trim().length() == 0) {
			throw new SshException(Messages.bind(
					Messages.SshEx_EMPTY_KEYPAIR_NAME_ATTR, keyPairName));
		} else if (!keyPairName.matches("^" + KEYPAIR_NAME_PATTERN + "$")) {
			throw new SshException(Messages.bind(
					Messages.SshEx_INVALID_KEYPAIR_NAME_ATTR, keyPairName,
					KEYPAIR_NAME_PATTERN));
		}
		String previous = getKeyPairName();
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
