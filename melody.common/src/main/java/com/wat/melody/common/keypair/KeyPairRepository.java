package com.wat.melody.common.keypair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;

/**
 * <p>
 * A {@link KeyPairRepository} helps to create, store and destroy
 * {@link KeyPair}.
 * </p>
 * 
 * <p>
 * A {@link KeyPairRepository} is a directory where the private key of Ssh
 * KeyPairs are stored, with encryption if specified.
 * </p>
 * 
 * <p>
 * A KeyPair is identified by its name, more formally known as the KeyPair Name.
 * The keyPair Name is unique in the whole Repository. A
 * {@link KeyPairRepository} only holds the privateKey of each KeyPair. The
 * PublicKey and the FingerPrint of each KeyPair can be computed from the
 * PrivateKey using this object's methods.
 * </p>
 * 
 * <p>
 * A {@link KeyPairRepository} is thread safe.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepository implements IFileBased {

	private static Log log = LogFactory.getLog(KeyPairRepository.class);

	private static Map<KeyPairRepositoryPath, KeyPairRepository> REGISTERED_REPOS = new HashMap<KeyPairRepositoryPath, KeyPairRepository>();

	public synchronized static KeyPairRepository getKeyPairRepository(
			KeyPairRepositoryPath keyPairRepositoryPath) {
		if (keyPairRepositoryPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepositoryPath.class.getCanonicalName() + ".");
		}
		if (REGISTERED_REPOS.containsKey(keyPairRepositoryPath)) {
			return REGISTERED_REPOS.get(keyPairRepositoryPath);
		}
		KeyPairRepository kpr = new KeyPairRepository(keyPairRepositoryPath);
		REGISTERED_REPOS.put(keyPairRepositoryPath, kpr);
		return kpr;
	}

	public static String getPublicKeyInOpenSshFormat(KeyPair kp, String sComment) {
		return KeyPairHelper.generateOpenSshRSAPublicKey(kp, sComment);
	}

	public static String getFingerprint(KeyPair kp) {
		return KeyPairHelper.generateFingerprint(kp.getPublic());
	}

	private KeyPairRepositoryPath _kprp;

	private KeyPairRepository(KeyPairRepositoryPath kprp) {
		setKeyPairRepositoryPath(kprp);
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _kprp;
	}

	private KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath kprp) {
		if (kprp == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KeyPairRepositoryPath.class.getCanonicalName() + ".");
		}
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_kprp = kprp;
		return previous;
	}

	/**
	 * <p>
	 * Tests if the given KeyPair exists in the Repository.
	 * </p>
	 * 
	 * @param keyPairName
	 * 
	 * @return <code>true</code> if the given KeyPair exists in the Repository,
	 *         <code>false</code> otherwise.
	 */
	public synchronized boolean containsKeyPair(KeyPairName keyPairName) {
		try {
			FS.validateFileExists(getPrivateKeyFile(keyPairName).getPath());
		} catch (IllegalFileException Ex) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Create a RSA {@link KeyPair}, and store it in this Repository, or
	 * retrieve the {@link KeyPair} if it already exists in the repository.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is the name of the key to create.
	 * @param size
	 *            is the size of the key to create.
	 * @param passphrase
	 *            is the passphrase of the key to create.
	 * 
	 * @return a new RSA {@link KeyPair}, created in this repository, or the
	 *         existing {@link KeyPair} which match the given name if it already
	 *         exists in the repository.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while storing the {@link KeyPair} in
	 *             this Repository.
	 * @throws IllegalPassphraseException
	 *             if the key already exists but the given passphrase is not
	 *             correct (the key can't be decrypted).
	 */
	public synchronized KeyPair createKeyPair(KeyPairName keyPairName,
			KeyPairSize size, String passphrase) throws IOException,
			IllegalPassphraseException {
		if (containsKeyPair(keyPairName)) {
			return getKeyPair(keyPairName, passphrase);
		}
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("RSA algorithm doesn't exists ! "
					+ "Source code have been modified and a bug "
					+ "introduced.", Ex);
		}
		log.trace(Messages.bind(Messages.KeyPairRepoMsg_GENKEY_BEGIN,
				keyPairName, getKeyPairRepositoryPath()));
		keyGen.initialize(size.getValue(), new SecureRandom());
		KeyPair kp = keyGen.generateKeyPair();
		KeyPairHelper.writeOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName),
				kp, passphrase);
		log.debug(Messages.bind(Messages.KeyPairRepoMsg_GENKEY_END,
				keyPairName, getKeyPairRepositoryPath()));
		return kp;
	}

	/**
	 * <p>
	 * Destroy a RSA KeyPair in this Repository.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is the name of the key to create.
	 */
	public synchronized void destroyKeyPair(KeyPairName keyPairName) {
		if (containsKeyPair(keyPairName)) {
			log.trace(Messages.bind(Messages.KeyPairRepoMsg_DELKEY_BEGIN,
					keyPairName, getKeyPairRepositoryPath()));
			getPrivateKeyFile(keyPairName).delete();
			log.debug(Messages.bind(Messages.KeyPairRepoMsg_DELKEY_END,
					keyPairName, getKeyPairRepositoryPath()));
		}
	}

	/**
	 * <p>
	 * Returns the private key of the {@link KeyPair} which match the given Name
	 * in OpenSshFormat.
	 * </p>
	 * 
	 * <p>
	 * If the given key was stored with encryption, the returned data will not
	 * be decrypted.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * 
	 * @return the private key of the KeyPair which match the given Name in
	 *         OpenSshFormat.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public synchronized String getPrivateKey(KeyPairName keyPairName)
			throws IOException {
		try {
			return new String(
					Files.readAllBytes(getPrivateKeyPath(keyPairName)));
		} catch (FileNotFoundException Ex) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "doesn't exists. Cannot retreive this KeyPair into the "
					+ "KeyPair Repository '" + _kprp + "'.");
		}
	}

	public synchronized Path getPrivateKeyPath(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return Paths.get(_kprp.getPath(), keyPairName.getValue());
	}

	public synchronized File getPrivateKeyFile(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return new File(_kprp.getPath(), keyPairName.getValue());
	}

	/**
	 * <p>
	 * Returns the {@link KeyPair} which match the given Name.
	 * </p>
	 * 
	 * <p>
	 * If the given key was stored with encryption, the returned {@link KeyPair}
	 * will be decrypted.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key.
	 * 
	 * @return the {@link KeyPair} which match the given Name.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given passphrase is not correct (the key can't be
	 *             decrypted).
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public synchronized KeyPair getKeyPair(KeyPairName keyPairName,
			String passphrase) throws IOException, IllegalPassphraseException {
		try {
			return KeyPairHelper.readOpenSslPEMPrivateKey(
					getPrivateKeyPath(keyPairName), passphrase);
		} catch (FileNotFoundException Ex) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "doesn't exists. Cannot retreive such KeyPair into the "
					+ "KeyPair Repository '" + _kprp + "'.");
		}
	}

	/**
	 * <p>
	 * Returns the public key of the {@link KeyPair} which match the given Name,
	 * in OpenSshFormat.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key.
	 * 
	 * @return the public key of the {@link KeyPair} which match the given Name,
	 *         in OpenSshFormat.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given passphrase is not correct (the key can't be
	 *             decrypted).
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public synchronized String getPublicKeyInOpenSshFormat(
			KeyPairName keyPairName, String passphrase, String sComment)
			throws IOException, IllegalPassphraseException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return getPublicKeyInOpenSshFormat(kp, sComment);
	}

	/**
	 * <p>
	 * Returns the fingerprint of the {@link KeyPair} which match the given
	 * Name.
	 * </p>
	 * 
	 * <p>
	 * The fingerprint is generated from raw keypair datas, which is not equal
	 * to a fingerprint generated from OpenSshFormat datas.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key.
	 * 
	 * @return the public key of the {@link KeyPair} which match the given Name,
	 *         in OpenSshFormat.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given passphrase is not correct (the key can't be
	 *             decrypted).
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public synchronized String getFingerprint(KeyPairName keyPairName,
			String passphrase) throws IOException, IllegalPassphraseException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return getFingerprint(kp);
	}

}