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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.IFileBased;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;

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

	private static Logger log = LoggerFactory
			.getLogger(KeyPairRepository.class);

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
	 * @param keyPairName
	 *            is the name of the KeyPair to test.
	 * 
	 * @return <tt>true</tt> if the given KeyPair exists in the Repository,
	 *         <tt>false</tt> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given KeyPair name is <tt>null</tt>.
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
	 * @param keyPairName
	 *            is the name of the key to create.
	 * @param size
	 *            is the size of the key to create.
	 * @param passphrase
	 *            is a password, which will be used to encrypt the key. Can be
	 *            <tt>null</tt>, if the key shouldn't be crypted.
	 * 
	 * @return a new RSA {@link KeyPair}, created in this repository, or the
	 *         {@link KeyPair} which match the given name if it already exists
	 *         in the repository.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair size is <tt>null</tt> ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while storing the {@link KeyPair} in
	 *             this Repository.
	 * @throws IllegalPassphraseException
	 *             if the key already exists but the given pass-phrase is not
	 *             correct (the key can't be decrypted).
	 */
	public synchronized KeyPair createKeyPair(KeyPairName keyPairName,
			KeyPairSize size, String passphrase) throws IOException,
			IllegalPassphraseException {
		if (containsKeyPair(keyPairName)) {
			return getKeyPair(keyPairName, passphrase);
		}
		if (size == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + KeyPairSize.class.getCanonicalName()
					+ ".");
		}
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("RSA algorithm doesn't exists ! "
					+ "Source code have been modified and a bug "
					+ "introduced.", Ex);
		}
		log.trace(Msg.bind(Messages.KeyPairRepoMsg_GENKEY_BEGIN, keyPairName,
				getKeyPairRepositoryPath()));
		keyGen.initialize(size.getValue(), new SecureRandom());
		KeyPair kp = keyGen.generateKeyPair();
		KeyPairHelper.writeOpenSslPEMPrivateKey(getPrivateKeyPath(keyPairName),
				kp, passphrase);
		log.debug(Msg.bind(Messages.KeyPairRepoMsg_GENKEY_END, keyPairName,
				getKeyPairRepositoryPath()));
		return kp;
	}

	/**
	 * @param keyPairName
	 *            is the name of the key to destroy.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given KeyPair name is <tt>null</tt>.
	 */
	public synchronized void destroyKeyPair(KeyPairName keyPairName) {
		if (containsKeyPair(keyPairName)) {
			log.trace(Msg.bind(Messages.KeyPairRepoMsg_DELKEY_BEGIN,
					keyPairName, getKeyPairRepositoryPath()));
			getPrivateKeyFile(keyPairName).delete();
			log.debug(Msg.bind(Messages.KeyPairRepoMsg_DELKEY_END, keyPairName,
					getKeyPairRepositoryPath()));
		}
	}

	/**
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * 
	 * @return the private key of the KeyPair which match the given Name in
	 *         OpenSshFormat. If the given key was stored with encryption, the
	 *         returned data will not be decrypted.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair doesn't exists in this
	 *             KeyPairRepository ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
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
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key. Can be
	 *            <tt>null</tt>, if the key is not crypted. If the given key was
	 *            stored with encryption, the returned {@link KeyPair} will be
	 *            decrypted.
	 * 
	 * @return the {@link KeyPair} which match the given Name.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair doesn't exists in this
	 *             KeyPairRepository ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given pass-phrase is not correct (the key can't be
	 *             decrypted).
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
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key. Can be
	 *            <tt>null</tt>, if the key is not crypted.
	 * 
	 * @return the public key of the {@link KeyPair} which match the given Name,
	 *         in OpenSshFormat.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair doesn't exists in this
	 *             KeyPairRepository ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given pass-phrase is not correct (the key can't be
	 *             decrypted).
	 */
	public synchronized String getPublicKeyInOpenSshFormat(
			KeyPairName keyPairName, String passphrase, String sComment)
			throws IOException, IllegalPassphraseException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return getPublicKeyInOpenSshFormat(kp, sComment);
	}

	/**
	 * @param keyPairName
	 *            is Name of the desired {@link KeyPair}.
	 * @param passphrase
	 *            is the password that was used to encrypt the key. Can be
	 *            <tt>null</tt>, if the key is not crypted.
	 * 
	 * @return the finger-print of the {@link KeyPair} which match the given
	 *         Name. The finger-print is generated from raw keypair datas, which
	 *         is not equal to a finger-print generated from OpenSshFormat
	 *         datas.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given KeyPair name is <tt>null</tt> ;</li>
	 *             <li>if the given KeyPair doesn't exists in this
	 *             KeyPairRepository ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the given KeyPair's
	 *             PrivateKey file.
	 * @throws IllegalPassphraseException
	 *             if the given pass-phrase is not correct (the key can't be
	 *             decrypted).
	 */
	public synchronized String getFingerprint(KeyPairName keyPairName,
			String passphrase) throws IOException, IllegalPassphraseException {
		KeyPair kp = getKeyPair(keyPairName, passphrase);
		return getFingerprint(kp);
	}

}