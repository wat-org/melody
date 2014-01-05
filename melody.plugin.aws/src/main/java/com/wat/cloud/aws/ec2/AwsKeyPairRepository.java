package com.wat.cloud.aws.ec2;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.wat.cloud.aws.ec2.exception.AwsKeyPairRepositoryException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;

/**
 * <p>
 * A {@link AwsKeyPairRepository} helps to create, store and destroy
 * {@link KeyPair} on the local File System and in Aws EC2.
 * </p>
 * 
 * <p>
 * A {@link AwsKeyPairRepository} enhance the {@link KeyPairRepostory} by
 * providing Local KeyPair synchronization with Aws KeyPairs.
 * </p>
 * 
 * <p>
 * A {@link AwsKeyPairRepository} is thread safe.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsKeyPairRepository {

	private static Map<KeyPairRepositoryPath, AwsKeyPairRepository> REGISTERED_REPOS = new HashMap<KeyPairRepositoryPath, AwsKeyPairRepository>();

	public synchronized static AwsKeyPairRepository getAwsKeyPairRepository(
			AmazonEC2 ec2, KeyPairRepositoryPath keyPairRepositoryPath) {
		if (keyPairRepositoryPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepositoryPath.class.getCanonicalName() + ".");
		}
		if (REGISTERED_REPOS.containsKey(keyPairRepositoryPath)) {
			return REGISTERED_REPOS.get(keyPairRepositoryPath);
		}
		AwsKeyPairRepository kpr = new AwsKeyPairRepository(ec2,
				keyPairRepositoryPath);
		REGISTERED_REPOS.put(keyPairRepositoryPath, kpr);
		return kpr;
	}

	private KeyPairRepository _kpr;
	private AmazonEC2 _cnx;

	protected AwsKeyPairRepository(AmazonEC2 ec2, KeyPairRepositoryPath kppr) {
		setConnection(ec2);
		setKeyPairRepository(KeyPairRepository.getKeyPairRepository(kppr));
	}

	public AmazonEC2 getConnection() {
		return _cnx;
	}

	private AmazonEC2 setConnection(AmazonEC2 connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		AmazonEC2 previous = getConnection();
		_cnx = connection;
		return previous;
	}

	public KeyPairRepository getKeyPairRepository() {
		return _kpr;
	}

	private KeyPairRepository setKeyPairRepository(KeyPairRepository kpr) {
		if (kpr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepository.class.getCanonicalName() + ".");
		}
		KeyPairRepository previous = getKeyPairRepository();
		_kpr = kpr;
		return previous;
	}

	public synchronized boolean containsKeyPair(KeyPairName keyPairName) {
		return getKeyPairRepository().containsKeyPair(keyPairName);
	}

	/**
	 * <p>
	 * Ensure the given keypair exists in the Local Keypair Repository and in
	 * the AWS EC2 Keypair Repository. If no keypair with the provided name
	 * exists in the Local Keypair Repository, it will be created with the given
	 * materials. If no keypair with the provided name exists in AWS EC2 Keypair
	 * Repository, it will be created with the given materials.
	 * </p>
	 * 
	 * @param keyPairName
	 *            the name of the key to create/validate.
	 * @param size
	 *            the desired size of the keypair.
	 * @param passphrase
	 *            the desired passphrase of the keypair.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair size is <tt>null</tt> ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading/creating the
	 *             {@link KeyPair} in this Repository.
	 * @throws IllegalPassphraseException
	 *             if the key already exists in the Local Keypair Repository but
	 *             the given pass-phrase is not correct (the key can't be
	 *             decrypted).
	 * @throws AwsKeyPairRepositoryException
	 *             if the key already exists in the AWS EC2 Keypair Repository
	 *             but has a different fingerprint than the provided materials
	 *             (meaning that the Local and the AWS EC2 Keypair Repository
	 *             diverge).
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 */
	public synchronized KeyPair createKeyPair(KeyPairName keyPairName,
			KeyPairSize size, String passphrase)
			throws AwsKeyPairRepositoryException, IOException,
			IllegalPassphraseException {
		// Get/Create KeyPair in the underlying repository
		KeyPair kp = getKeyPairRepository().createKeyPair(keyPairName, size,
				passphrase);
		// Create/test KeyPair in Aws
		ensureKeyPairInAws(keyPairName, kp);
		return kp;
	}

	public synchronized void destroyKeyPair(KeyPairName keyPairName) {
		// Delete KeyPair in Aws
		AwsEc2CloudKeyPair.deleteKeyPair(getConnection(), keyPairName);
		// Delete KeyPair in the underlying repository
		getKeyPairRepository().destroyKeyPair(keyPairName);
	}

	/**
	 * <p>
	 * Ensure the given keypair exists in the AWS EC2 Keypair Repository. If no
	 * keypair with the provided name exists in the AWS E2 Keypair Repository,
	 * it will be created with the given materials.
	 * </p>
	 * 
	 * @param kpn
	 *            the name of the key to create/validate.
	 * @param kp
	 *            the keypair materials.
	 * 
	 * @throws IllegalArgumentException
	 *             if <tt>kp</tt> is <tt>null</tt>.
	 * @throws AwsKeyPairRepositoryException
	 *             if the key already exists in the AWS EC2 Keypair Repository
	 *             but has a different fingerprint than the provided materials
	 *             (meaning that the Local and the AWS EC2 Keypair Repository
	 *             diverge).
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 */
	private synchronized void ensureKeyPairInAws(KeyPairName kpn, KeyPair kp)
			throws AwsKeyPairRepositoryException {
		if (AwsEc2CloudKeyPair.keyPairExists(getConnection(), kpn)) {
			// when KeyPair is already in AWS, verify the fingerprint
			String fprint = KeyPairRepository.getFingerprint(kp);
			if (AwsEc2CloudKeyPair.compareKeyPair(getConnection(), kpn, fprint) == false) {
				throw new AwsKeyPairRepositoryException(Msg.bind(
						Messages.KeyPairEx_DIFFERENT, kpn,
						getKeyPairRepository().getKeyPairRepositoryPath()));
			}
		} else {
			// when KeyPair is not in AWS, import the public key
			String pubkey = KeyPairRepository.getPublicKeyInOpenSshFormat(kp,
					"Generated by Melody");
			AwsEc2CloudKeyPair.importKeyPair(getConnection(), kpn, pubkey);
		}
	}

}