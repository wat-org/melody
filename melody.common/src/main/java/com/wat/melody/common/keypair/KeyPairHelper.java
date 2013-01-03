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

	/*
	 * TODO : remove the reference to com.jcraft.jsch.Buffer, by using its own
	 * method.
	 */

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static KeyPair readOpenSslPEMPrivateKey(Path privateKey)
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

	public static void writeOpenSshPublicKey(Path filePath, KeyPair kp)
			throws IOException {
		Files.write(filePath, generateOpenSshRSAPublicKey(kp, "no-comment")
				.getBytes());
	}

	public static void writeOpenSslPEMFingerprint(Path filePath, KeyPair kp)
			throws IOException {
		Files.write(filePath, generateOpenSslPEMFingerprint(kp).getBytes());
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

	public static String generateOpenSslPEMFingerprint(KeyPair kp) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException Ex) {
			throw new RuntimeException("MD5 algorithm doesn't exists !", Ex);
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
		byte[] pub = toBase64(pubblob, 0, pubblob.length);
		try {
			res = new String(getKeyTypeName());
			res += new String(space);
			res += new String(pub);
			res += new String(space);
			res += new String(str2byte(comment));
			res += new String(cr);
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
		return buf.buffer;
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

	private static final byte[] cr = str2byte("\n");
	private static byte[] space = str2byte(" ");
	private static final byte[] sshrsa = str2byte("ssh-rsa");

	private static byte[] getKeyTypeName() {
		return sshrsa;
	}

	/*
	 * TODO : use common-codec.
	 */
	private static byte[] toBase64(byte[] buf, int start, int length) {

		byte[] tmp = new byte[length * 2];
		int i, j, k;

		int foo = (length / 3) * 3 + start;
		i = 0;
		for (j = start; j < foo; j += 3) {
			k = (buf[j] >>> 2) & 0x3f;
			tmp[i++] = b64[k];
			k = (buf[j] & 0x03) << 4 | (buf[j + 1] >>> 4) & 0x0f;
			tmp[i++] = b64[k];
			k = (buf[j + 1] & 0x0f) << 2 | (buf[j + 2] >>> 6) & 0x03;
			tmp[i++] = b64[k];
			k = buf[j + 2] & 0x3f;
			tmp[i++] = b64[k];
		}

		foo = (start + length) - foo;
		if (foo == 1) {
			k = (buf[j] >>> 2) & 0x3f;
			tmp[i++] = b64[k];
			k = ((buf[j] & 0x03) << 4) & 0x3f;
			tmp[i++] = b64[k];
			tmp[i++] = (byte) '=';
			tmp[i++] = (byte) '=';
		} else if (foo == 2) {
			k = (buf[j] >>> 2) & 0x3f;
			tmp[i++] = b64[k];
			k = (buf[j] & 0x03) << 4 | (buf[j + 1] >>> 4) & 0x0f;
			tmp[i++] = b64[k];
			k = ((buf[j + 1] & 0x0f) << 2) & 0x3f;
			tmp[i++] = b64[k];
			tmp[i++] = (byte) '=';
		}
		byte[] bar = new byte[i];
		System.arraycopy(tmp, 0, bar, 0, i);
		return bar;

		// return sun.misc.BASE64Encoder().encode(buf);
	}

	private static final byte[] b64 = str2byte("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");

}
