package com.wat.cloud.aws.ec2;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
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

	/**
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
		 * eth0) can be allocated.
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

	public static String getSecurityGroup(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		/*
		 * always return the security group associated to eth0 : with Aws Ec2,
		 * only 1 network device (e.g. eth0) can be allocated.
		 */
		return i.getSecurityGroups().get(0).getGroupName();
	}

	protected static String getSecurityGroupDescription() {
		return "Melody security group";
	}

	protected static String newSecurityGroupName() {
		// This formula should produce a unique name
		return "MelodySg" + "_" + System.currentTimeMillis() + "_"
				+ UUID.randomUUID().toString().substring(0, 8);
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
	protected static void createSecurityGroup(AmazonEC2 ec2, String sgname,
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
		csgreq = new CreateSecurityGroupRequest(sgname, sgdesc);

		try {
			log.trace("Creating Security Group '" + sgname + "' ...");
			ec2.createSecurityGroup(csgreq);
			log.debug("Security Group '" + sgname + "' created.");
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
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
	 * Delete the AWS Security Group which match the given name.
	 * </p>
	 * 
	 * <ul>
	 * <li>If the given name doesn't match any AWS Security Group Name, does
	 * nothing ;</li>
	 * <li>The AWS Security must not be 'in use', otherwise, an
	 * {@link AmazonServiceException} will be generated ;</li>
	 * </ul>
	 * 
	 * @param ec2
	 * @param sgname
	 *            is the name of the AWS Security Group to delete.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 */
	protected static void deleteSecurityGroup(AmazonEC2 ec2, String sgname) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sgname == null || sgname.length() == 0) {
			return;
		}

		DeleteSecurityGroupRequest dsgreq = new DeleteSecurityGroupRequest();
		dsgreq.withGroupName(sgname);

		try {
			log.trace("Deleting Security Group '" + sgname + "' ...");
			ec2.deleteSecurityGroup(dsgreq);
			log.debug("Security Group '" + sgname + "' deleted.");
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return;
			} else {
				throw Ex;
			}
		}
	}

	protected static NetworkDeviceName eth0 = createNetworkDeviceName("eth0");

	private static NetworkDeviceName createNetworkDeviceName(String n) {
		try {
			return NetworkDeviceName.parseString(n);
		} catch (IllegalNetworkDeviceNameException Ex) {
			throw new RuntimeException(Ex);
		}
	}

}