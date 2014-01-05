package com.wat.cloud.libvirt;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

import org.libvirt.Connect;

import com.wat.cloud.libvirt.exception.LibVirtKeyPairRepositoryException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;

/**
 * <p>
 * A {@link LibVirtKeyPairRepository} helps to create, store and destroy
 * {@link KeyPair} on the local File System and in LibVirtCloud.
 * </p>
 * 
 * <p>
 * A {@link LibVirtKeyPairRepository} enhance the {@link KeyPairRepostory} by
 * providing Local KeyPair synchronization with LibVirtCloud KeyPairs.
 * </p>
 * 
 * <p>
 * A {@link LibVirtKeyPairRepository} is thread safe.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtKeyPairRepository {

	private static Map<KeyPairRepositoryPath, LibVirtKeyPairRepository> REGISTERED_REPOS = new HashMap<KeyPairRepositoryPath, LibVirtKeyPairRepository>();

	public synchronized static LibVirtKeyPairRepository getLibVirtKeyPairRepository(
			Connect cnx, KeyPairRepositoryPath keyPairRepositoryPath) {
		if (keyPairRepositoryPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepositoryPath.class.getCanonicalName() + ".");
		}
		if (REGISTERED_REPOS.containsKey(keyPairRepositoryPath)) {
			return REGISTERED_REPOS.get(keyPairRepositoryPath);
		}
		LibVirtKeyPairRepository kpr = new LibVirtKeyPairRepository(cnx,
				keyPairRepositoryPath);
		REGISTERED_REPOS.put(keyPairRepositoryPath, kpr);
		return kpr;
	}

	private KeyPairRepository _kpr;
	private Connect _cnx;

	protected LibVirtKeyPairRepository(Connect cnx, KeyPairRepositoryPath kppr) {
		setConnection(cnx);
		setKeyPairRepository(KeyPairRepository.getKeyPairRepository(kppr));
	}

	public Connect getConnection() {
		return _cnx;
	}

	private Connect setConnection(Connect connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getConnection();
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
	 * the LibVirt Cloud Keypair Repository. If no keypair with the provided
	 * name exists in the Local Keypair Repository, it will be created with the
	 * given materials. If no keypair with the provided name exists in LibVirt
	 * Cloud Keypair Repository, it will be created with the given materials.
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
	 *             if the key already exists in the LibVirt Cloud Keypair
	 *             Repository but has a different fingerprint than the provided
	 *             materials (meaning that the Local and the LibVirt Cloud
	 *             Keypair Repository diverge).
	 */
	public synchronized KeyPair createKeyPair(KeyPairName keyPairName,
			KeyPairSize size, String passphrase)
			throws LibVirtKeyPairRepositoryException, IOException,
			IllegalPassphraseException {
		// Get/Create KeyPair in the underlying repository
		KeyPair kp = getKeyPairRepository().createKeyPair(keyPairName, size,
				passphrase);
		// Create/test KeyPair in LibVirtCloud
		ensureKeyPairInLibVirtCloud(keyPairName, kp);
		return kp;
	}

	public synchronized void destroyKeyPair(KeyPairName keyPairName) {
		// Delete KeyPair in LibVirtCloud
		LibVirtCloudKeyPair.deleteKeyPair(getConnection(), keyPairName);
		// Delete KeyPair in the underlying repository
		getKeyPairRepository().destroyKeyPair(keyPairName);
	}

	/**
	 * <p>
	 * Ensure the given keypair exists in the LibVirt Cloud Keypair Repository.
	 * If no keypair with the provided name exists in the LibVirt Cloud Keypair
	 * Repository, it will be created with the given materials.
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
	 *             if the key already exists in the LibVirt Cloud Keypair
	 *             Repository but has a different fingerprint than the provided
	 *             materials (meaning that the Local and the LibVirt Cloud
	 *             Keypair Repository diverge).
	 */
	private synchronized void ensureKeyPairInLibVirtCloud(KeyPairName kpn,
			KeyPair kp) throws LibVirtKeyPairRepositoryException {
		if (LibVirtCloudKeyPair.keyPairExists(getConnection(), kpn)) {
			// when KeyPair is already in LibVirtCloud, verify the fingerprint
			String fprint = KeyPairRepository.getFingerprint(kp);
			if (LibVirtCloudKeyPair
					.compareKeyPair(getConnection(), kpn, fprint) == false) {
				throw new LibVirtKeyPairRepositoryException(Msg.bind(
						Messages.KeyPairEx_DIFFERENT, kpn,
						getKeyPairRepository().getKeyPairRepositoryPath()));
			}
		} else {
			// when KeyPair is not in LibVirtCloud, import the public key
			String pubkey = KeyPairRepository.getPublicKeyInOpenSshFormat(kp,
					"Generated by Melody");
			LibVirtCloudKeyPair.importKeyPair(getConnection(), kpn, pubkey);
		}
	}

}