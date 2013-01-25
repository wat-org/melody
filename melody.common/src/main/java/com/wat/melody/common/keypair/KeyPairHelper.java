package com.wat.melody.common.keypair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import com.jcraft.jsch.Buffer;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class KeyPairHelper {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static KeyPair readOpenSslPEMPrivateKey(Path privateKey, String passphrase)
			throws IOException {
		File fin = privateKey.toFile();
		FileReader fr = null;
		try {
			/*
			 * TODO : deal with pass-phrase when reading the private key
			 */
			fr = new FileReader(fin);
			PEMReader pemr = new PEMReader(fr);
			return (KeyPair) pemr.readObject();
		} finally {
			if (fr != null) {
				fr.close();
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
		Files.write(filePath, generateFingerprint(kp).getBytes());
	}

	public static void writeOpenSslPEMPrivateKey(Path filePath, KeyPair kp)
			throws IOException {
		writeOpenSslPEMDatas(filePath.toString(), kp.getPrivate());
	}

	public static void writeOpenSslPEMPublicKey(Path filePath, KeyPair kp)
			throws IOException {
		writeOpenSslPEMDatas(filePath.toString(), kp.getPublic());
	}

	private static void writeOpenSslPEMDatas(String fileName, Key datas)
			throws IOException {
		File fprivout = new File(fileName);
		OutputStream privos = null;
		PEMWriter privpemw = null;
		try {
			privos = new FileOutputStream(fprivout);
			privpemw = new PEMWriter(new OutputStreamWriter(privos));
			privpemw.writeObject(datas);
			privpemw.flush();
			privos.flush();
		} finally {
			if (privpemw != null) {
				privpemw.close();
			}
			if (privos != null) {
				privos.close();
			}
		}
	}

	public static String generateFingerprint(KeyPair kp) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("MD5 algorithm doesn't exists ! "
					+ "Source code have been modified and a bug introduced.",
					Ex);
		}
		md.update(kp.getPublic().getEncoded());
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

	public static String generateOpenSshRSAPublicKey(KeyPair kp, String comment) {
		byte[] pubblob = getPublicKeyBlob(kp);
		String res = null;
		byte[] pub = Base64.encodeBase64(pubblob);
		try {
			res = new String(getKeyTypeName());
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

	private static byte[] getKeyTypeName() {
		return sshrsa;
	}

}
