package com.wat.melody.plugin.ssh.common;

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

import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.plugin.ssh.common.exception.KeyPairRepositoryException;

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
public class KeyPairRepository {

	private File moPath;

	public KeyPairRepository(File path) throws IllegalDirectoryException {
		setPath(path);
	}

	public File getPath() {
		return moPath;
	}

	/**
	 * 
	 * @param path
	 * 
	 * @return
	 * 
	 * @throws IllegalDirectoryException
	 *             if the given path is not an existing directory.
	 * @throws IllegalDirectoryException
	 *             if the given path is <code>null</code>.
	 */
	public File setPath(File path) throws IllegalDirectoryException {
		Tools.validateDirExists(path.getAbsolutePath());
		File previous = getPath();
		moPath = path;
		return previous;
	}

	/**
	 * <p>
	 * Tests if the given KeyPair exists in the Repository.
	 * </p>
	 * 
	 * @param sKeyPairName
	 * 
	 * @return <code>true</code> if the given KeyPair exists in the Repository,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the KeyPair's data are corrupted(ex : the content of the
	 *             privateKey is not a valid private key in the Open Ssl PEM
	 *             format)
	 */
	public boolean containsKeyPair(String sKeyPairName)
			throws KeyPairRepositoryException {
		try {
			Tools.validateFileExists(getPrivateKeyFile(sKeyPairName).getPath());
		} catch (IllegalFileException Ex) {
			return false;
		}
		KeyPair kp = null;
		try {
			kp = KeyPairHelper
					.readOpenSslPEMPrivateKey(getPrivateKeyPath(sKeyPairName));
		} catch (IOException Ex) {
			throw new KeyPairRepositoryException("IO error.", Ex);
		}
		if (!(kp.getPrivate() instanceof RSAPrivateKey)) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Create a KeyPair in this Repository.
	 * </p>
	 * 
	 * @param sKeyPairName
	 *            is the name of the key to create.
	 * 
	 * @throws KeyPairRepositoryException
	 *             if an error occurred while creating the KeyPair (ex : a
	 *             KeyPair with the same name already exists, failed to write
	 *             privateKey file).
	 * @throws IOException
	 */
	public void createKeyPair(String sKeyPairName, int size, String sPassphrase)
			throws KeyPairRepositoryException, IOException {
		if (sKeyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a KeyPair Name)");
		}
		if (containsKeyPair(sKeyPairName)) {
			throw new KeyPairRepositoryException(
					Messages.bind(
							"''{0}'': KeyPair Name already exists. Cannot create a KeyPair with this Name in the KeyPair Repository ''{1}''.",
							sKeyPairName, getPath()));
		}
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("TODO developpement error.", Ex);
		}
		keyGen.initialize(size, new SecureRandom());
		KeyPair kp = keyGen.generateKeyPair();
		KeyPairHelper.writeOpenSslPEMPrivateKey(
				getPrivateKeyPath(sKeyPairName), kp);
	}

	/**
	 * 
	 * @param sKeyPairName
	 * 
	 * @return
	 * 
	 * @throws KeyPairRepositoryException
	 *             if the given KeyPair's PrivateKey file is not a valid file.
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 */
	public String getPrivateKey(String sKeyPairName)
			throws KeyPairRepositoryException, IOException {
		try {
			Path path = getPrivateKeyPath(sKeyPairName);
			return new String(Files.readAllBytes(path));
		} catch (FileNotFoundException Ex) {
			throw new KeyPairRepositoryException(Messages.bind(
					Messages.KeyPairRepoEx_PRIVATE_KEY_NOT_FOUND, sKeyPairName,
					getPath()), Ex);
		}
	}

	public Path getPrivateKeyPath(String sKeyPairName) {
		if (sKeyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a KeyPair Name)");
		}
		return Paths.get(getPath().toString(), sKeyPairName);
	}

	public File getPrivateKeyFile(String sKeyPairName) {
		if (sKeyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a KeyPair Name)");
		}
		return new File(getPath(), sKeyPairName);
	}

}
