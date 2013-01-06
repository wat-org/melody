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
import java.security.interfaces.RSAPrivateKey;

import com.wat.melody.common.keypair.exception.KeyPairRepositoryException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalFileException;

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
 * PrivateKey, using {@link KeyPairHelper}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepository extends File {

	private static final long serialVersionUID = 7188192370067564134L;

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
		super(sPath);
		try {
			Tools.validateDirExists(sPath);
		} catch (IllegalDirectoryException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoEx_INVALID_REPO_PATH, sPath), Ex);
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
	public KeyPairRepository(String parent, String child)
			throws KeyPairRepositoryException {
		super(parent, child);
		try {
			Tools.validateDirExists(this.getPath());
		} catch (IllegalDirectoryException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoEx_INVALID_REPO_PATH, this.getPath()),
					Ex);
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
	 * 
	 * @param path
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given path is not a valid KeyPairRepository path.
	 * @throws NullPointerException
	 *             if the given path is <code>null</code>.
	 */
	public KeyPairRepository(File parent, String child)
			throws KeyPairRepositoryException {
		this(parent.getPath(), child);
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
	 * 
	 * @throws IOException
	 *             if the KeyPair's data are corrupted (ex : the content of the
	 *             privateKey is not a valid private key in the Open Ssl PEM
	 *             format)
	 */
	public boolean containsKeyPair(KeyPairName keyPairName) throws IOException {
		try {
			Tools.validateFileExists(getPrivateKeyFile(keyPairName).getPath());
		} catch (IllegalFileException Ex) {
			return false;
		}
		KeyPair kp = null;
		kp = KeyPairHelper
				.readOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName));
		if (!(kp.getPrivate() instanceof RSAPrivateKey)) {
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
	 * @throws IllegalArgumentException
	 *             if a KeyPair with the same name already exists in this
	 *             KeyPairRepository.
	 * @throws IOException
	 *             if an IO error occurred while creating the KeyPair in the
	 *             repository.
	 */
	public KeyPair createKeyPair(KeyPairName keyPairName, int size,
			String sPassphrase) throws IOException {
		/*
		 * TODO : deal with pass-phrase when creating the private key
		 */
		if (containsKeyPair(keyPairName)) {
			throw new IllegalArgumentException(keyPairName
					+ ": KeyPair Name already exists. "
					+ "Cannot create this KeyPair KeyPair Repository '"
					+ getPath() + "'.");
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
				kp);
		return kp;
	}

	/**
	 * 
	 * @param keyPairName
	 * 
	 * @return
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given KeyPair's PrivateKey file is not a valid file.
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 */
	public String getPrivateKey(KeyPairName keyPairName)
			throws KeyPairRepositoryException, IOException {
		try {
			Path path = getPrivateKeyPath(keyPairName);
			return new String(Files.readAllBytes(path));
		} catch (FileNotFoundException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoEx_PRIVATE_KEY_NOT_FOUND, keyPairName,
					getPath()), Ex);
		}
	}

	public Path getPrivateKeyPath(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return Paths.get(getPath().toString(), keyPairName.getValue());
	}

	public File getPrivateKeyFile(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairName.class.getCanonicalName()
					+ ".");
		}
		return new File(getPath(), keyPairName.getValue());
	}

	public KeyPair getKeyPair(KeyPairName keyPairName) throws IOException {
		if (!containsKeyPair(keyPairName)) {
			throw new IllegalArgumentException(keyPairName
					+ ": KeyPair Name doens't exists. "
					+ "Cannot get this KeyPair in the KeyPair Repository '"
					+ getPath() + "'.");
		}
		return KeyPairHelper
				.readOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName));
	}

	public String getPublicKeyInOpenSshFormat(KeyPairName keyPairName,
			String sComment) throws IOException {
		KeyPair kp = getKeyPair(keyPairName);
		return KeyPairHelper.generateOpenSshRSAPublicKey(kp, sComment);
	}

	public String getFingerprint(KeyPairName keyPairName) throws IOException {
		KeyPair kp = getKeyPair(keyPairName);
		return KeyPairHelper.generateFingerprint(kp);
	}

	public static String getPublicKeyInOpenSshFormat(KeyPair kp, String sComment) {
		return KeyPairHelper.generateOpenSshRSAPublicKey(kp, sComment);
	}

	public static String getFingerprint(KeyPair kp) {
		return KeyPairHelper.generateFingerprint(kp);
	}

}