package com.wat.cloud.aws.ec2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DeleteKeyPairRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsEc2CloudKeyPair {

	private static Log log = LogFactory.getLog(AwsEc2CloudKeyPair.class);

	/**
	 * <p>
	 * Get the Aws {@link KeyPairInfo} whose match the given name.
	 * </p>
	 * 
	 * @param ec2
	 * @param keyPairName
	 *            is the name of the key pair to retrieve.
	 * 
	 * @return an Aws {@link KeyPairInfo} if the given key pair exists,
	 *         <code>null</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static KeyPairInfo getKeyPair(AmazonEC2 ec2, KeyPairName keyPairName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (keyPairName == null) {
			return null;
		}
		DescribeKeyPairsRequest dkpreq = new DescribeKeyPairsRequest();
		dkpreq.withKeyNames(keyPairName.getValue());

		try {
			return ec2.describeKeyPairs(dkpreq).getKeyPairs().get(0);
		} catch (AmazonServiceException Ex) {
			// Means that the given Key Pair Name is not valid
			if (Ex.getErrorCode().indexOf("InvalidKeyPair") != -1) {
				return null;
			} else {
				throw Ex;
			}
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return null;
		}
	}

	/**
	 * <p>
	 * Tests if the given key pair exists.
	 * </p>
	 * 
	 * @param ec2
	 * @param keyPairName
	 *            is the name of the key pair to validate existence.
	 * 
	 * @return <code>true</code> if the given key pair exists,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean keyPairExists(AmazonEC2 ec2, KeyPairName keyPairName) {
		return getKeyPair(ec2, keyPairName) != null;
	}

	/**
	 * <p>
	 * Compare the given fingerprint with the given Aws KeyPair's fingerprint.
	 * </p>
	 * 
	 * @param ec2
	 * @param keyPairName
	 *            is the name of the remote key pair to compare the given
	 *            fingerprint to.
	 * @param sFingerprint
	 *            is the fingerprint of the local key pair.
	 * 
	 * @return <code>true</code> if the given fingerprint and the given Aws
	 *         KeyPair's fingerprint are equals, <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean compareKeyPair(AmazonEC2 ec2,
			KeyPairName keyPairName, String sFingerprint) {
		KeyPairInfo kpi = getKeyPair(ec2, keyPairName);
		if (kpi == null) {
			return false;
		}
		return kpi.getKeyFingerprint().equals(sFingerprint);
	}

	/**
	 * <p>
	 * Import a RSA KeyPair into Aws.
	 * </p>
	 * 
	 * @param ec2
	 * @param keyPairName
	 *            is the name of the key pair which will be imported.
	 * @param sPublicKey
	 *            is the public key material, in the openssh format.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static void importKeyPair(AmazonEC2 ec2, KeyPairName keyPairName,
			String sPublicKey) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws KeyPair Name).");
		}
		if (sPublicKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Public Key File).");
		}

		log.trace(Msg.bind(Messages.CommonMsg_GENKEY_BEGIN, keyPairName));
		ImportKeyPairRequest ikpreq = new ImportKeyPairRequest();
		ikpreq.withKeyName(keyPairName.getValue());
		ikpreq.withPublicKeyMaterial(sPublicKey);

		try {
			ec2.importKeyPair(ikpreq);
		} catch (AmazonServiceException Ex) {
			// Means that the given Key Pair Name is not valid
			if (Ex.getErrorCode().indexOf("InvalidKeyPair.Duplicate") != -1) {
				log.debug(Msg.bind(Messages.CommonMsg_GENKEY_DUP, keyPairName));
				return;
			} else {
				throw Ex;
			}
		}
		log.debug(Msg.bind(Messages.CommonMsg_GENKEY_END, keyPairName));
	}

	/**
	 * <p>
	 * Delete a RSA KeyPair into Aws. Will not fail if the given KeyPair doesn't
	 * exists.
	 * </p>
	 * 
	 * @param ec2
	 * @param keyPairName
	 *            is the name of the key pair which will be imported.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static void deleteKeyPair(AmazonEC2 ec2, KeyPairName keyPairName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws KeyPair Name).");
		}

		log.trace(Msg.bind(Messages.CommonMsg_DELKEY_BEGIN, keyPairName));
		DeleteKeyPairRequest dkpreq = new DeleteKeyPairRequest();
		dkpreq.withKeyName(keyPairName.getValue());

		// will not fail if the given doesn't exists
		ec2.deleteKeyPair(dkpreq);
		log.debug(Msg.bind(Messages.CommonMsg_DELKEY_END, keyPairName));
	}

}