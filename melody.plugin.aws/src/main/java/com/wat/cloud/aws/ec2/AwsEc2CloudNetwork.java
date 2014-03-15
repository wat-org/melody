package com.wat.cloud.aws.ec2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Tag;
import com.wat.cloud.aws.ec2.exception.SecurityGroupInUseException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsEc2CloudNetwork {

	private static Logger log = LoggerFactory
			.getLogger(AwsEc2CloudNetwork.class);

	private static NetworkDeviceName createNetworkDeviceName(String n) {
		try {
			return NetworkDeviceName.parseString(n);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	private static NetworkDeviceName eth0 = createNetworkDeviceName("eth0");

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
		ProtectedAreaName name = generateSelfProtectedAreaName(netdev);
		String desc = generateSelfProtectedAreaDescription();
		String sgid = createSecurityGroup(ec2, name.getValue(), desc);
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
		return createSelfProtectedArea(ec2, eth0);
	}

	/**
	 * <p>
	 * A fake list, as long as this framework will only support one eth per AWS
	 * Instance.
	 * </p>
	 * 
	 * @param i
	 *            is an {@link Instance}.
	 * 
	 * @return a {@link NetworkDeviceList}, which contains the
	 *         {@link NetworkDevice}s of the given {@link Instance}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Instance} is <tt>null</tt>.
	 */
	public static NetworkDeviceList getNetworkDevices(Instance i) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		/*
		 * always return [eth0] : with Aws Ec2, only 1 network device (e.g.
		 * eth0) can be allocated (VPC excluded).
		 */
		NetworkDeviceList netdevs = new NetworkDeviceList();
		try {
			netdevs.addNetworkDevice(new NetworkDevice(eth0, null, i
					.getPrivateIpAddress(), i.getPrivateDnsName(), i
					.getPublicIpAddress(), i.getPublicDnsName(), null, null));
		} catch (IllegalNetworkDeviceListException Ex) {
			throw new RuntimeException(Ex);
		}
		return netdevs;
	}

	/**
	 * <p>
	 * Do nothing. Will just log that this feature is not implemented.
	 * </p>
	 */
	public static void detachNetworkDevices(AmazonEC2 ec2, Instance i,
			NetworkDeviceList toRemove) throws InterruptedException {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		if (toRemove == null) {
			return;
		}
		for (NetworkDevice netdev : toRemove) {
			log.info(Msg.bind(
					Messages.CommonMsg_DETACH_NOTWORK_DEVICE_NOT_SUPPORTED,
					i.getImageId(), netdev));
		}
	}

	/**
	 * <p>
	 * Do nothing. Will just log that this feature is not implemented.
	 * </p>
	 */
	public static void attachNetworkDevices(AmazonEC2 ec2, Instance i,
			NetworkDeviceList toAdd) throws InterruptedException {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		if (toAdd == null) {
			return;
		}
		for (NetworkDevice netdev : toAdd) {
			log.info(Msg.bind(
					Messages.CommonMsg_ATTACH_NOTWORK_DEVICE_NOT_SUPPORTED,
					i.getImageId(), netdev));
		}
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
	public static ProtectedAreaId getProtectedAreaId(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev) {
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
	 * <p>
	 * Get the AWS Security Group which match the given identifier.
	 * </p>
	 * 
	 * @param ec2
	 * @param sgid
	 *            is the identifier of the AWS Security Group to request.
	 * 
	 * @return <tt>null</tt>, if no security group with the given id was found.
	 * 
	 * @throws AmazonServiceException
	 *             if the description failed (ex : because the identifier is
	 *             invalid - Character sets beyond ASCII are not supported).
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 */
	public static SecurityGroup getSecurityGroupById(AmazonEC2 ec2, String sgid) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sgid == null) {
			return null;
		}
		List<String> ids = new ArrayList<String>();
		ids.add(sgid);
		DescribeSecurityGroupsRequest csgreq = null;
		DescribeSecurityGroupsResult csgres = null;
		try {
			csgreq = new DescribeSecurityGroupsRequest();
			csgreq = csgreq.withGroupIds(ids);
			csgres = ec2.describeSecurityGroups(csgreq);
			return csgres.getSecurityGroups().get(0);
		} catch (NullPointerException Ex) {
			return null;
		} catch (IndexOutOfBoundsException Ex) {
			return null;
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return null;
			} else if (Ex.getErrorCode().indexOf("InvalidGroupId.Malformed") != -1) {
				return null;
			} else {
				throw Ex;
			}
		}
	}

	/**
	 * <p>
	 * Create an empty AWS Security Group with the given name and description.
	 * </p>
	 * 
	 * @param ec2
	 * @param sgname
	 *            is the name of the AWS Security Group to create.
	 * @param sgdesc
	 *            is the associated description.
	 * 
	 * @return the newly created Aws Security Group Identifier.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed (ex : because the sgname is invalid -
	 *             Character sets beyond ASCII are not supported).
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given Security Group name is <tt>null</tt> or empty.
	 */
	protected static String createSecurityGroup(AmazonEC2 ec2, String sgname,
			String sgdesc) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sgname == null || sgname.trim().length() == 0) {
			throw new IllegalArgumentException(sgname + ": Not accepted. "
					+ "Must be a String (an AWS Security Group Name).");
		}

		CreateSecurityGroupRequest csgreq = null;
		CreateSecurityGroupResult csgres = null;
		csgreq = new CreateSecurityGroupRequest(sgname, sgdesc);

		try {
			log.trace(Msg.bind(Messages.CommonMsg_SECURITY_GROUP_CREATING,
					sgname));
			csgres = ec2.createSecurityGroup(csgreq);
			log.trace(Msg.bind(Messages.CommonMsg_SECURITY_GROUP_CREATED,
					sgname));
			return csgres.getGroupId();
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
				throw new RuntimeException("Unexpected error while creating "
						+ "an empty AWS Security Group. "
						+ "Because the SGName and the SGDescription have "
						+ "been automatically generated, such error cannot "
						+ "happened. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced.", Ex);
			} else {
				throw Ex;
			}
		}
	}

	/**
	 * <p>
	 * Delete the AWS Security Group which match the given identifier.
	 * </p>
	 * 
	 * <ul>
	 * <li>If the given identifier doesn't match any AWS Security Group
	 * Identifier, does nothing ;</li>
	 * <li>The AWS Security must not be 'in use', otherwise, an
	 * {@link AmazonServiceException} will be generated ;</li>
	 * </ul>
	 * 
	 * @param ec2
	 * @param sgid
	 *            is the identifier of the AWS Security Group to delete.
	 * 
	 * @throws SecurityGroupInUseException
	 *             if the security group can not be deleted because it is still
	 *             in use.
	 * @throws AmazonServiceException
	 *             if the creation failed.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 */
	protected static void deleteSecurityGroup(AmazonEC2 ec2, String sgid)
			throws SecurityGroupInUseException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sgid == null || sgid.length() == 0) {
			return;
		}

		DeleteSecurityGroupRequest dsgreq = new DeleteSecurityGroupRequest();
		dsgreq.withGroupId(sgid);

		try {
			log.trace(Msg
					.bind(Messages.CommonMsg_SECURITY_GROUP_DELETING, sgid));
			ec2.deleteSecurityGroup(dsgreq);
			log.trace(Msg.bind(Messages.CommonMsg_SECURITY_GROUP_DELETED, sgid));
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.InUse") != -1) {
				throw new SecurityGroupInUseException(Ex);
			} else {
				throw Ex;
			}
		}
	}

}