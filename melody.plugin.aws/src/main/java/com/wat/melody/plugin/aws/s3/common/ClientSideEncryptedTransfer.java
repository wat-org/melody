package com.wat.melody.plugin.aws.s3.common;

import java.io.IOException;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ClientSideEncryptedTransfer extends AbstractOperation {

	/**
	 * Defines the path of the keypair repository which contains the keypair
	 * used to perform client-side encryption.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * Defines the name of the keypair - relative to the keypair-repository - to
	 * use to perform client-side encryption. If the keypair-repository doesn't
	 * contains such keypair, it will be automatically created.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * Defines the passphrase of the keypair. If the keypair need to be created,
	 * it will be created with this size.
	 */
	public static final String KEYPAIR_SIZE_ATTR = "keypair-size";

	/**
	 * Defines the passphrase of the keypair. If the keypair need to be created,
	 * it will be created with this passphrase.
	 */
	public static final String PASS_ATTR = "passphrase";

	private KeyPairRepositoryPath _kprp = null;
	private KeyPairName _kpn = null;
	private KeyPairSize _kps = null;
	private String _passphrase = null;

	public ClientSideEncryptedTransfer() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInS3Exception {
		// if no keypair have been specified, return
		if (getKeyPairName() == null) {
			super.validate();// get a standard S3 connection
			return;
		}
		// if a keypair have been specified, define repo and size
		// then, build an encrypted connection to S3

		// if no repo have been specified, get the default one
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(getSshPlugInConfiguration()
					.getKeyPairRepositoryPath());
		}
		// if no size have been specified, get the default one
		if (getKeyPairSize() == null) {
			setKeyPairSize(getSshPlugInConfiguration().getKeyPairSize());
		}
		// get an encrypted S3 connection (create the keypair if necessary)
		try {
			setS3Connection(getAwsPlugInConfiguration().getAwsS3Connection(
					getKeyPairRepositoryPath(), getKeyPairName(),
					getKeyPairSize(), getPassphrase()));
		} catch (IllegalPassphraseException Ex) {
			if (getPassphrase() == null) {
				throw new AwsPlugInS3Exception(Msg.bind(
						Messages.TransferEx_MISSING_PASSPHRASE_ATTR, PASS_ATTR,
						getKeyPairName()));
			} else {
				throw new AwsPlugInS3Exception(Msg.bind(
						Messages.TransferEx_INVALID_PASSPHRASE_ATTR, PASS_ATTR,
						getKeyPairName()));
			}
		} catch (IOException Ex) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.TransferEx_KEYPAIR_IO_ERROR, getKeyPairName(),
					getKeyPairRepositoryPath()), Ex);
		}
	}

	protected SshPlugInConfiguration getSshPlugInConfiguration()
			throws AwsPlugInS3Exception {
		try {
			return SshPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new AwsPlugInS3Exception(Ex);
		}
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _kprp;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		// can be null
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_kprp = keyPairRepository;
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return _kpn;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		// can be null
		KeyPairName previous = getKeyPairName();
		_kpn = keyPairName;
		return previous;
	}

	public KeyPairSize getKeyPairSize() {
		return _kps;
	}

	@Attribute(name = KEYPAIR_SIZE_ATTR)
	public KeyPairSize setKeyPairSize(KeyPairSize keyPairSize) {
		// can be null
		KeyPairSize previous = getKeyPairSize();
		_kps = keyPairSize;
		return previous;
	}

	public String getPassphrase() {
		return _passphrase;
	}

	@Attribute(name = PASS_ATTR)
	public String setPassword(String passphrase) {
		// can be null
		String previous = getPassphrase();
		_passphrase = passphrase;
		return previous;
	}

}