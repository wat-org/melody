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

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryException;

/**
 * <p>
 * A {@link KeyPairRepository} is a directory where Ssh KeyPairs are stored.
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
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepository implements IFileBased {

	private File _f;

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepository(String sPath) throws KeyPairRepositoryException {
		_f = new File(sPath);
		try {
			FS.validateDirExists(_f.getPath());
		} catch (IllegalDirectoryException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoEx_INVALID_REPO_PATH, _f.getPath()), Ex);
		}
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepository(File path) throws KeyPairRepositoryException {
		this(path.toString());
	}

	/**
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepository(Path path) throws KeyPairRepositoryException {
		this(path.toString());
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
	public boolean containsKeyPair(KeyPairName keyPairName) {
		try {
			FS.validateFileExists(getPrivateKeyFile(keyPairName).getPath());
		} catch (IllegalFileException Ex) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Create a RSA KeyPair in this Repository.
	 * </p>
	 * 
	 * @param keyPairName
	 *            is the name of the key to create.
	 * @param size
	 *            is the size of the key to create.
	 * @param sPassphrase
	 *            is the passphrase of the key to create.
	 * 
	 * @return the created
	 * 
	 * @throws IOException
	 *             if an IO error occurred while creating the KeyPair in the
	 *             repository.
	 * @throws IllegalArgumentException
	 *             if a KeyPair with the same name already exists in this
	 *             KeyPairRepository.
	 */
	public KeyPair createKeyPair(KeyPairName keyPairName, int size,
			String sPassphrase) throws IOException {
		if (containsKeyPair(keyPairName)) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "already exists. Cannot create this KeyPair into the "
					+ "KeyPair Repository '" + _f.getPath() + "'.");
		}
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("RSA algorithm doesn't exists ! "
					+ "Source code have been modified and a bug introduced.",
					Ex);
		}
		keyGen.initialize(size, new SecureRandom());
		KeyPair kp = keyGen.generateKeyPair();
		KeyPairHelper.writeOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName),
				kp, sPassphrase);
		return kp;
	}

	/**
	 * 
	 * @param keyPairName
	 * 
	 * @return
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
					+ "KeyPair Repository '" + _f.getPath() + "'.");
		}
	}

	public Path getPrivateKeyPath(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return Paths.get(_f.getPath().toString(), keyPairName.getValue());
	}

	public File getPrivateKeyFile(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return new File(_f.getPath(), keyPairName.getValue());
	}

	public KeyPair getKeyPair(KeyPairName keyPairName, String passphrase)
			throws IOException {
		try {
			return KeyPairHelper.readOpenSslPEMPrivateKey(
					getPrivateKeyPath(keyPairName), passphrase);
		} catch (FileNotFoundException Ex) {
			throw new IllegalArgumentException(keyPairName + ": KeyPair Name "
					+ "doesn't exists. Cannot retreive this KeyPair into the "
					+ "KeyPair Repository '" + _f.getPath() + "'.");
		}
	}

	public String getPublicKeyInOpenSshFormat(KeyPairName keyPairName,
			String passphrase, String sComment) throws IOException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return KeyPairHelper.generateOpenSshRSAPublicKey(kp, sComment);
	}

	public String getFingerprint(KeyPairName keyPairName, String passphrase)
			throws IOException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return KeyPairHelper.generateFingerprint(kp);
	}

	public static String getPublicKeyInOpenSshFormat(KeyPair kp, String sComment) {
		return KeyPairHelper.generateOpenSshRSAPublicKey(kp, sComment);
	}

	public static String getFingerprint(KeyPair kp) {
		return KeyPairHelper.generateFingerprint(kp);
	}

}