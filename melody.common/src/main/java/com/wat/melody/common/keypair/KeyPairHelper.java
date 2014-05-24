package com.wat.melody.common.keypair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.EncryptionException;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;

import com.jcraft.jsch.Buffer;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class KeyPairHelper {

	private static final Provider BCProvider;
	private static final JcaPEMKeyConverter PemKeyConverter;
	private static final JcePEMDecryptorProviderBuilder PemDecryptorProviderBuilder;
	private static final JcePEMEncryptorBuilder PemEncryptorBuilder;

	static {
		BCProvider = new BouncyCastleProvider();
		Security.addProvider(BCProvider);
		PemKeyConverter = new JcaPEMKeyConverter().setProvider(BCProvider);
		PemDecryptorProviderBuilder = new JcePEMDecryptorProviderBuilder();
		PemEncryptorBuilder = new JcePEMEncryptorBuilder("DES-EDE3-CBC");
		// when using AES-256-CFB, JSch doesn't managed to deal with it
		// (=> authentication failure)
	}

	public static KeyPair readOpenSslPEMPrivateKey(Path privateKey,
			final String passphrase) throws IOException, FileNotFoundException,
			IllegalPassphraseException {
		File fin = privateKey.toFile();
		FileReader fr = null;
		PEMParser pemr = null;
		try {
			fr = new FileReader(fin);
			pemr = new PEMParser(fr);
			Object o = pemr.readObject();
			PEMKeyPair pemkp = null;
			if (o instanceof PEMEncryptedKeyPair) {
				if (passphrase == null) {
					throw new IllegalPassphraseException("Key is encrypted. "
							+ "A passphrase is necessary to decode it.");
				}
				PEMDecryptorProvider pdec = null;
				pdec = PemDecryptorProviderBuilder.build(passphrase
						.toCharArray());
				pemkp = ((PEMEncryptedKeyPair) o).decryptKeyPair(pdec);
			} else {
				pemkp = (PEMKeyPair) o;
			}
			return PemKeyConverter.getKeyPair(pemkp);
		} catch (EncryptionException Ex) {
			throw new IllegalPassphraseException("Incorrect passphrase.", Ex);
		} finally {
			try {
				if (pemr != null) {
					pemr.close();
				}
			} finally {
				if (fr != null) {
					fr.close();
				}
			}
		}
	}

	public static void writeOpenSshPublicKey(Path filePath, KeyPair kp,
			String sComment) throws IOException {
		Files.write(filePath, generateOpenSshRSAPublicKey(kp, sComment)
				.getBytes());
	}

	public static void writeOpenSslPEMFingerprint(Path filePath, KeyPair kp)
			throws IOException {
		Files.write(filePath, generateFingerprint(kp.getPublic()).getBytes());
	}

	public static void writeOpenSslPEMPrivateKey(Path filePath, KeyPair kp,
			String passphrase) throws IOException {
		File fprivout = new File(filePath.toString());
		OutputStream privos = null;
		PEMWriter privpemw = null;
		try {
			privos = new FileOutputStream(fprivout);
			privpemw = new PEMWriter(new OutputStreamWriter(privos));
			if (passphrase == null || passphrase.length() == 0) {
				privpemw.writeObject(kp.getPrivate());
			} else {
				PEMEncryptor penc = null;
				penc = PemEncryptorBuilder.build(passphrase.toCharArray());
				privpemw.writeObject(kp.getPrivate(), penc);
			}
			privpemw.flush();
			privos.flush();
		} finally {
			try {
				if (privpemw != null) {
					privpemw.close();
				}
			} finally {
				if (privos != null) {
					privos.close();
				}
			}
		}
	}

	/**
	 * @param key
	 * 
	 * @return the MD5 fingerprint of the given materials.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given key is <tt>null</tt>.
	 */
	public static String generateFingerprint(Key key) {
		if (key == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Key.class.getCanonicalName() + ".");
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("MD5 algorithm doesn't exists ! "
					+ "Source code have been modified and a bug introduced.",
					Ex);
		}
		md.update(key.getEncoded());
		return bytesToHex(md.digest());
	}

	final private static char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static String bytesToHex(byte[] b) {
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
			if (j + 1 < b.length) {
				buf.append(":");
			}
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * Converts a OpenSsh RSA public key in a {@link PublicKey}.
	 * </p>
	 * 
	 * <p>
	 * OpenSsh RSA public key looks like :
	 * 
	 * <pre>
	 * ssh-rsa AAAAB3NzaC1yc2EAAAADAQ .... I2ofbbqeP6Ljq45Vtfat5 comment
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param filePath
	 *            is the path a file file, which contains one line and this line
	 *            should be an OpenSsh Rsa public key.
	 * 
	 * @return a {@link PublicKey}, which is equal to the given file content.
	 * 
	 * @throws IOException
	 *             if the given path is not valid.
	 * @throws InvalidKeySpecException
	 *             if the content of the given file is not a valid RSA public
	 *             key.
	 */
	public static PublicKey readOpenSshRSAPublicKey(Path filePath)
			throws IOException, InvalidKeySpecException {
		String content = new String(Files.readAllBytes(filePath));
		String opensshpubkey = content.split(" ")[1];
		byte[] opensshpubblob = Base64.decodeBase64(opensshpubkey.getBytes());

		Buffer buf = new Buffer(opensshpubblob);
		String keyalg = new String(buf.getString());
		BigInteger pubexponent = new BigInteger(buf.getString());
		BigInteger modulus = new BigInteger(buf.getString());

		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, pubexponent);
		KeyFactory factory = null;
		if (!keyalg.equals("ssh-rsa")) {
			throw new RuntimeException(keyalg + ": Not accepted. "
					+ "Only accepts RSA keys.");
		}
		try {
			factory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("RSA algorithm doesn't exists ! "
					+ "Source code have been modified and a bug introduced.",
					Ex);
		}
		return factory.generatePublic(spec);
	}

	public static String generateOpenSshRSAPublicKey(KeyPair kp, String comment) {
		byte[] pubblob = getPublicKeyBlob(kp);
		String res = null;
		byte[] pub = Base64.encodeBase64(pubblob);
		try {
			res = new String(sshrsa);
			res += new String(space);
			res += new String(pub);
			if (comment != null && comment.trim().length() != 0) {
				res += new String(space);
				res += new String(str2byte(comment));
			}
		} catch (Exception e) {
			return null;
		}
		return res;
	}

	private static byte[] getPublicKeyBlob(KeyPair kp) {
		byte[] pub_array = ((RSAPublicKey) kp.getPublic()).getPublicExponent()
				.toByteArray();
		byte[] n_array = ((RSAPrivateKey) kp.getPrivate()).getModulus()
				.toByteArray();
		Buffer buf = new Buffer(sshrsa.length + 4 + pub_array.length + 4
				+ n_array.length + 4);
		buf.putString(sshrsa);
		buf.putString(pub_array);
		buf.putString(n_array);
		return buf.getBytes();
	}

	private static byte[] str2byte(String str, String encoding) {
		if (str == null)
			return null;
		try {
			return str.getBytes(encoding);
		} catch (java.io.UnsupportedEncodingException e) {
			return str.getBytes();
		}
	}

	private static byte[] str2byte(String str) {
		return str2byte(str, "UTF-8");
	}

	private static byte[] space = str2byte(" ");
	private static final byte[] sshrsa = str2byte("ssh-rsa");

}