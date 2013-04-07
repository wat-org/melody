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

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalFileException;

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
		return KeyPairHelper.generateFingerprint(kp);
	}

	private KeyPairRepositoryPath _f;

	private KeyPairRepository(KeyPairRepositoryPath keyPairRepositoryPath) {
		_f = keyPairRepositoryPath;
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
	 * Create a RSA {@link KeyPair}, and store it in this Repository.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is the name of the key to create.
	 * @param size
	 *            is the size of the key to create.
	 * @param sPassphrase
	 *            is the passphrase of the key to create.
	 * 
	 * @return a new RSA {@link KeyPair}, which is now stored in this
	 *         Repository.
	 * 
	 * @throws IOException
	 *             if an IO error occurred while storing the {@link KeyPair} in
	 *             this Repository.
	 * @throws IllegalArgumentException
	 *             if a {@link KeyPair} with the same Name already exists in
	 *             this Repository.
	 */
	public synchronized KeyPair createKeyPair(KeyPairName keyPairName,
			int size, String sPassphrase) throws IOException {
		if (containsKeyPair(keyPairName)) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair "
					+ "Name already exists. "
					+ "Cannot create this KeyPair into the KeyPair "
					+ "Repository '" + _f + "'.");
		}
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("RSA algorithm doesn't exists ! "
					+ "Source code have been modified and a bug "
					+ "introduced.", Ex);
		}
		keyGen.initialize(size, new SecureRandom());
		KeyPair kp = keyGen.generateKeyPair();
		KeyPairHelper.writeOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName),
				kp, sPassphrase);
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
			getPrivateKeyFile(keyPairName).delete();
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
	public String getPrivateKey(KeyPairName keyPairName) throws IOException {
		try {
			return new String(
					Files.readAllBytes(getPrivateKeyPath(keyPairName)));
		} catch (FileNotFoundException Ex) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "doesn't exists. Cannot retreive this KeyPair into the "
					+ "KeyPair Repository '" + _f + "'.");
		}
	}

	public Path getPrivateKeyPath(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return Paths.get(_f.getPath(), keyPairName.getValue());
	}

	public File getPrivateKeyFile(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return new File(_f.getPath(), keyPairName.getValue());
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
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public KeyPair getKeyPair(KeyPairName keyPairName, String passphrase)
			throws IOException {
		try {
			return KeyPairHelper.readOpenSslPEMPrivateKey(
					getPrivateKeyPath(keyPairName), passphrase);
		} catch (FileNotFoundException Ex) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "doesn't exists. Cannot retreive such KeyPair into the "
					+ "KeyPair Repository '" + _f + "'.");
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
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public String getPublicKeyInOpenSshFormat(KeyPairName keyPairName,
			String passphrase, String sComment) throws IOException {
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
	 * @throws IllegalArgumentException
	 *             if the given KeyPair's doesn't exists in this
	 *             KeyPairRepository.
	 */
	public String getFingerprint(KeyPairName keyPairName, String passphrase)
			throws IOException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return getFingerprint(kp);
	}

}