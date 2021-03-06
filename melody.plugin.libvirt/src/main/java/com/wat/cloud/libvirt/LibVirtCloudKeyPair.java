package com.wat.cloud.libvirt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.common.keypair.KeyPairHelper;
import com.wat.melody.common.keypair.KeyPairName;

/**
 * <p>
 * Quick and dirty class which provides libvirt keypair management features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LibVirtCloudKeyPair {

	private static Logger log = LoggerFactory
			.getLogger(LibVirtCloudKeyPair.class);

	/**
	 * <p>
	 * Delete a RSA KeyPair into LibVirtCloud. Will not fail if the given
	 * KeyPair doesn't exists.
	 * </p>
	 * 
	 * @param cnx
	 * @param keyPairName
	 *            is the name of the key pair which will be imported.
	 * 
	 * @throws IllegalArgumentException
	 *             if cnx is <code>null</code>.
	 */
	public static void deleteKeyPair(Connect cnx, KeyPairName keyPairName) {
		try {
			Element pkEntry = getPublicKeyEntry(keyPairName);
			if (pkEntry == null) {
				return;
			}
			log.trace("Deleting PublicKey '" + keyPairName
					+ "' in LibVirt Cloud ...");
			// remove the entry
			pkEntry.getParentNode().removeChild(pkEntry);
			LibVirtCloud.conf.store();
			// remove the file
			Files.delete(getPublicKeyPath(getPublicKeyRepoPath(), keyPairName));
			log.debug("PublicKey '" + keyPairName
					+ "' deleted in LibVirt Cloud.");
		} catch (IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	/**
	 * <p>
	 * Tests if the given key pair exists.
	 * </p>
	 * 
	 * @param cnx
	 * @param keyPairName
	 *            is the name of the key pair to validate existence.
	 * 
	 * @return <code>true</code> if the given key pair exists,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if cnx is <code>null</code>.
	 */
	public static boolean keyPairExists(Connect cnx, KeyPairName keyPairName) {
		Element pkentry = getPublicKeyEntry(keyPairName);
		if (pkentry == null) {
			return false;
		}
		if (Files.exists(getPublicKeyPath(getPublicKeyRepoPath(), keyPairName))) {
			return true;
		}
		/*
		 * here, the entry exists, but the underlying file doesn't exists... We
		 * need to remove the entry.
		 */
		pkentry.getParentNode().removeChild(pkentry);
		LibVirtCloud.conf.store();
		return false;
	}

	/**
	 * <p>
	 * Compare the given fingerprint with the given LibVirtCloud KeyPair's
	 * fingerprint.
	 * </p>
	 * 
	 * @param cnx
	 * @param keyPairName
	 *            is the name of the remote key pair to compare the given
	 *            fingerprint to.
	 * @param sFingerprint
	 *            is the fingerprint of the local key pair.
	 * 
	 * @return <code>true</code> if the given fingerprint and the given
	 *         LibVirtCLoud KeyPair's fingerprint are equals, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if cnx is <code>null</code>.
	 */
	public static boolean compareKeyPair(Connect cnx, KeyPairName keyPairName,
			String sFingerprint) {
		try {
			Path pubkeypath = getPublicKeyPath(getPublicKeyRepoPath(),
					keyPairName);
			PublicKey pubkey = KeyPairHelper
					.readOpenSshRSAPublicKey(pubkeypath);
			String storedFingerprint = KeyPairHelper
					.generateFingerprint(pubkey);
			return storedFingerprint.equals(sFingerprint);
		} catch (InvalidKeySpecException | IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	/**
	 * <p>
	 * Import a RSA KeyPair into LibVirtCloud.
	 * </p>
	 * 
	 * @param cnx
	 * @param keyPairName
	 *            is the name of the key pair which will be imported.
	 * @param sPublicKey
	 *            is the public key material, in the openssh format.
	 * 
	 * @throws IllegalArgumentException
	 *             if cnx is <code>null</code>.
	 */
	public static void importKeyPair(Connect cnx, KeyPairName keyPairName,
			String sPublicKey) {
		try {
			log.trace("Importing PublicKey '" + keyPairName
					+ "' in LibVirt Cloud ...");
			Element pkEntry = getPublicKeyEntry(keyPairName);
			if (pkEntry != null) {
				// already exists (We don't verify the content is the same)
				log.debug("PublicKey '" + keyPairName
						+ "' imported in LibVirt Cloud (already exists).");
				return;
			}
			// create a new entry for the key
			Element publicKeysNode = (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/public-keys");
			Element publicKeyNode = publicKeysNode.getOwnerDocument()
					.createElement("public-key");
			publicKeyNode.setAttribute("name", keyPairName.getValue());
			publicKeysNode.appendChild(publicKeyNode);
			LibVirtCloud.conf.store();
			// save the public key to disk
			Files.write(getPublicKeyPath(getPublicKeyRepoPath(), keyPairName),
					sPublicKey.getBytes());
			log.debug("PublicKey '" + keyPairName
					+ "' imported in LibVirt Cloud.");
		} catch (XPathExpressionException | IOException Ex) {
			throw new RuntimeException(Ex);
		}

	}

	private static String getPublicKeyRepoPath() {
		try {
			String pkRepoPath = LibVirtCloud.conf
					.evaluateAsString("/libvirtcloud/public-keys/@repository");
			if (pkRepoPath == null || pkRepoPath.trim().length() == 0) {
				throw new RuntimeException("The key pair repository path is "
						+ "not defined. "
						+ "An attribute /libvirtcloud/public-keys/@repository "
						+ "must exists in " + LibVirtCloud.conf.getSourceFile()
						+ ".");
			}
			return pkRepoPath;
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static Path getPublicKeyPath(String pkRepoPath,
			KeyPairName keyPairName) {
		return getPublicKeyPath(pkRepoPath, keyPairName.getValue());
	}

	private static Path getPublicKeyPath(String pkRepoPath, String keyPairName) {
		return Paths.get(pkRepoPath + "/" + keyPairName + ".pub");
	}

	private static Element getPublicKeyEntry(KeyPairName keyPairName) {
		try {
			return (Element) LibVirtCloud.conf
					.evaluateAsNode("/libvirtcloud/public-keys/public-key[@name='"
							+ keyPairName + "']");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static String getPublicKeyBytes(String keyPairName) {
		try {
			return new String(Files.readAllBytes(getPublicKeyPath(
					getPublicKeyRepoPath(), keyPairName)));
		} catch (IOException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	/**
	 * <p>
	 * Retrieve the public key which is associated the libvirt domain which have
	 * the given ip address.
	 * </p>
	 * 
	 * @param instanceIp
	 *            is the ip address of the requested libvrit domain.
	 * 
	 * @return the public key associated to the libvirt domain which have the
	 *         given ip, or <tt>null</tt> if the libvirt domain which have the
	 *         given ip address is not registered or has no publik-key
	 *         associated to.
	 */
	public static String getInstancePublicKey(String instanceIp) {
		try {
			String keypairname = LibVirtCloudNetwork.netconf
					.evaluateAsString("/network/ip/dhcp/host[ @ip='"
							+ instanceIp + "' ]/@keypair-name");
			if (keypairname == null || keypairname.trim().length() == 0) {
				return null;
			}
			return getPublicKeyBytes(keypairname);
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	protected static void associateKeyPairToInstance(Domain d, KeyPairName kpn) {
		String mac = LibVirtCloudNetwork.getDomainMacAddress(d,
				LibVirtCloudNetwork.eth0);
		String ip = LibVirtCloudNetwork.getDomainIpAddress(mac);
		String dName = null;
		Element n = null;
		try {
			dName = d.getName();
			n = (Element) LibVirtCloudNetwork.netconf
					.evaluateAsNode("/network/ip/dhcp/host[ @ip='" + ip + "' ]");
		} catch (LibvirtException | XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (n == null) {
			throw new RuntimeException("Cannot found Instance '" + dName
					+ "' network datas.");
		}
		log.trace("Associating public-key '" + kpn + "' to domain '" + dName
				+ "' ...");
		n.setAttribute("keypair-name", kpn.getValue());
		LibVirtCloudNetwork.netconf.store();
		log.debug("Public-key '" + kpn + "' associated to domain '" + dName
				+ "'.");
	}

	protected static void deassociateKeyPairToInstance(Domain d) {
		String mac = LibVirtCloudNetwork.getDomainMacAddress(d,
				LibVirtCloudNetwork.eth0);
		String ip = LibVirtCloudNetwork.getDomainIpAddress(mac);
		String dName = null;
		Element n = null;
		try {
			dName = d.getName();
			n = (Element) LibVirtCloudNetwork.netconf
					.evaluateAsNode("/network/ip/dhcp/host[ @ip='" + ip
							+ "' and exists(@keypair-name)]");
		} catch (LibvirtException | XPathExpressionException Ex) {
			throw new RuntimeException(Ex);
		}
		if (n == null) {
			return;
		}
		log.trace("De-associating KeyPair to domain '" + dName + "' ...");
		n.removeAttribute("keypair-name");
		LibVirtCloudNetwork.netconf.store();
		log.debug("KeyPair de-associated to domain '" + dName + "'.");
	}

}