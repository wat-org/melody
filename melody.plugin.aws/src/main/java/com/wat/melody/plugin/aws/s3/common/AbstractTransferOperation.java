package com.wat.melody.plugin.aws.s3.common;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractTransferOperation extends AbstractOperation {

	/**
	 * Defines the keypair's passphrase.
	 */
	public static final String PASS_ATTR = "passphrase";

	/**
	 * Defines the path of the keypair repository which contains the keypair
	 * used to perform client-side encryption.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * Defines the name of the keypair - relative to the keypair-repository - to
	 * use to perform client-side encryption. If the keypair-repository doesn't
	 * contains such keypair, it will be automatically created. If a passphrase
	 * was provided, the keypair will be encrypted with it.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	private KeyPairRepositoryPath _kprp = null;
	private KeyPairName _kpn = null;
	private String _passphrase = null;

	public AbstractTransferOperation() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInS3Exception {
		// TODO : should get an encrypted S3 connection
		setS3Connection(getAwsPlugInConfiguration().getAwsS3Connection());
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

}