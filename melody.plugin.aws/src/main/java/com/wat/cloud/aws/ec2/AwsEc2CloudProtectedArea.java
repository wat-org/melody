package com.wat.cloud.aws.ec2;

import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.common.firewall.NetworkDeviceName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsEc2CloudProtectedArea {

	/**
	 * When an instance is created, it is automatically associated a network
	 * device eth0 (AWS does this for you). In order to protect this network
	 * device, we can rely on Instance's Security Group. But we consider that
	 * Instance's Security Groups are limited because there's no way to
	 * allow/deny network data to send/receive by this network device (you can
	 * change the security group's rules, which will impact all the other
	 * instances of the security group)... For these reason, we will
	 * automatically create a security group and associate it to the network
	 * device. Such security group is call a Self Protected Area. Via the Self
	 * Protected Area, we can allow/deny network data to send/receive by this
	 * network device without impact on other instance.
	 * 
	 * The Self Protected Area is created during the instance creation. Its
	 * Identifier is the stored into a tag, so it can be retrieve easily.
	 */

	/**
	 * Key of the Instance's tag which store the Self Protected Area Identifier.
	 */
	private static final String MELODY_SELF_PROTECTED_AREA_ID = "melody-self-protected-area-id";

	/**
	 * Given an Identifier, build the Instance's tag which store the Self
	 * Protected Area Identifier.
	 */
	protected static Tag createSelfProtectedAreaIdTag(ProtectedAreaId id) {
		if (id == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		return new Tag(MELODY_SELF_PROTECTED_AREA_ID, id.getValue());
	}

	/**
	 * Given an Instance, retrieve the Instance's tag which store the Self
	 * Protected Area Identifier.
	 * 
	 * @return cannot return <tt>null</tt>.
	 */
	protected static ProtectedAreaId getSelfProtectedAreaIdTag(Instance i) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		for (Tag tag : i.getTags()) {
			if (!tag.getKey().equals(MELODY_SELF_PROTECTED_AREA_ID)) {
				continue;
			}
			String v = tag.getValue();
			if (v == null) {
				throw new RuntimeException("The instance '" + i.getInstanceId()
						+ "' has a tag called '"
						+ MELODY_SELF_PROTECTED_AREA_ID
						+ "' which contains a null value. "
						+ "Because AWS tag's value cannot be null, such error "
						+ "couldn't happened. "
						+ "The source code have changed and a bug have been "
						+ "introduced.");
			}
			try {
				return ProtectedAreaId.parseString(v);
			} catch (IllegalProtectedAreaIdException Ex) {
				throw new RuntimeException("The instance '" + i.getInstanceId()
						+ "' has a tag called '"
						+ MELODY_SELF_PROTECTED_AREA_ID
						+ "' which contains the illegal value '" + v + "'. "
						+ "Because this tag have been automatically created "
						+ "during this instance creation, such error couldn't "
						+ "happened. "
						+ "The source code have changed and a bug have been "
						+ "introduced.");
			}
		}
		throw new RuntimeException("The instance '" + i.getInstanceId()
				+ "' has no tag called '" + MELODY_SELF_PROTECTED_AREA_ID
				+ "'. " + "Because this tag have been automatically created "
				+ "during this instance creation, such error couldn't "
				+ "happened. "
				+ "Is it possible that this instance doesn't exists anymore. "
				+ "It is aslo possible that this tag have been changed "
				+ "manually or a bug have been introduced.");
	}

	/**
	 * @param ec2
	 *            unsed. Cause the info is stored in the instance.
	 * @param i
	 *            an instance.
	 * @param netdev
	 *            unsed. Cause the instance has only one network device (VPC
	 *            excluded).
	 * 
	 * @return the {@link ProtectedAreaId} of the Security Group associated to
	 *         the given network device of the given instance.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Instance} is <tt>null</tt>.
	 */
	protected static ProtectedAreaId getProtectedAreaId(AmazonEC2 ec2,
			Instance i, NetworkDeviceName netdev) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		/*
		 * Simplification: we suppose (VPC excluded) that all network interface
		 * of an instance will share the same security groups specified when the
		 * instance was created.
		 * 
		 * Retrieve the self protected area identifier - which was automatically
		 * created during the instance creation - from the instance's tags
		 * called MELODY_SELF_PROTECTED_AREA_ID.
		 */
		return getSelfProtectedAreaIdTag(i);
	}

	/**
	 * Self Protected Area are automatically created during network
	 * interface/instance creation. Such creation is fully automated. This
	 * method allow to generate a description, which will be used during the
	 * Self Protected Area creation.
	 */
	private static String generateSelfProtectedAreaDescription() {
		return "Protected area dedicated to an instance, generated by Melody.";
	}

	/**
	 * Self Protected Area are automatically created during network
	 * interface/instance creation. Such creation is fully automated. This
	 * method allow to generate a name, based on the given network device name,
	 * which will be used during the Self Protected Area creation.
	 */
	private static ProtectedAreaName generateSelfProtectedAreaName(
			NetworkDeviceName netdev) {
		if (netdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceName.class.getCanonicalName() + ".");
		}
		// This formula should produce a unique name
		String name = "melody-self-protected-area:" + netdev + ":"
				+ UUID.randomUUID();
		try {
			return ProtectedAreaName.parseString(name);
		} catch (IllegalProtectedAreaNameException Ex) {
			throw new RuntimeException("Fail to convert '" + name + "' into '"
					+ ProtectedAreaName.class.getCanonicalName() + "'. "
					+ "If this error happened, you should modify the "
					+ "conversion rule.", Ex);
		}
	}

	/**
	 * <p>
	 * Create an empty self Protected Area (e.g. an empty Security Group),
	 * dedicated for the given network interface of an instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param netdev
	 *            is the network interface to associate to the Protected Area to
	 *            create.
	 * 
	 * @return the newly created Protected Area Identifier.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed (ex : because the sgname is invalid -
	 *             Character sets beyond ASCII are not supported).
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given network device name is <tt>null</tt>.
	 */
	protected static ProtectedAreaId createSelfProtectedArea(AmazonEC2 ec2,
			NetworkDeviceName netdev) {
		// TODO : add rules to accept all incoming traffic from itself
		ProtectedAreaName name = generateSelfProtectedAreaName(netdev);
		String desc = generateSelfProtectedAreaDescription();
		String sgid = AwsEc2CloudNetwork.createSecurityGroup(ec2,
				name.getValue(), desc);
		try {
			return ProtectedAreaId.parseString(sgid);
		} catch (IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException("Fail to convert '" + sgid + "' into '"
					+ ProtectedAreaId.class.getCanonicalName() + "'. "
					+ "If this error happened, you should modify the "
					+ "conversion rule.", Ex);
		}
	}

	/**
	 * <p>
	 * Create an empty self Protected Area (e.g. an empty Security Group),
	 * dedicated for the first network interface of an instance.
	 * </p>
	 * 
	 * @param ec2
	 * 
	 * @return the newly created Protected Area Identifier.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed (ex : because the securty group name
	 *             is invalid - Character sets beyond ASCII are not supported).
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 */
	protected static ProtectedAreaId createSelfProtectedArea(AmazonEC2 ec2) {
		return createSelfProtectedArea(ec2, AwsEc2CloudNetwork.eth0);
	}

}