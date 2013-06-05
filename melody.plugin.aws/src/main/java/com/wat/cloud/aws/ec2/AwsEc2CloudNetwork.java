package com.wat.cloud.aws.ec2;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.exception.IllegalNetworkDeviceNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsEc2CloudNetwork {

	private static Log log = LogFactory.getLog(AwsEc2CloudNetwork.class);

	public static NetworkDeviceNameList getNetworkDevices(AmazonEC2 ec2,
			Instance i) {
		/*
		 * always reply [eth0], because, using Aws Ec2, only 1 network device
		 * can be allocated.
		 */
		NetworkDeviceNameList netdevs = new NetworkDeviceNameList();
		NetworkDeviceName eth0 = null;
		try {
			eth0 = NetworkDeviceName.parseString("eth0");
			netdevs.addNetworkDevice(eth0);
		} catch (IllegalNetworkDeviceNameException
				| IllegalNetworkDeviceNameListException Ex) {
			throw new RuntimeException(Ex);
		}
		return netdevs;
	}

	public static NetworkDeviceDatas getNetworkDeviceDatas(AmazonEC2 ec2,
			Instance i, NetworkDeviceName netdev) {
		/*
		 * always get datas of eth0, because, using Aws Ec2, only eth0 is
		 * available.
		 */
		return new NetworkDeviceDatas(null, i.getPrivateIpAddress(),
				i.getPrivateDnsName(), i.getPublicIpAddress(),
				i.getPublicDnsName());
	}

	public static void detachNetworkDevices(AmazonEC2 ec2, Instance i,
			NetworkDeviceNameList toRemove, long detachTimeout)
			throws InterruptedException {
		for (NetworkDeviceName netdev : toRemove) {
			log.info(Messages.bind(
					Messages.CommonMsg_DETACH_NOTWORK_DEVICE_NOT_SUPPORTED,
					i.getImageId(), netdev));
		}
	}

	public static void attachNetworkDevices(AmazonEC2 ec2, Instance i,
			NetworkDeviceNameList toAdd, long attachTimeout)
			throws InterruptedException {
		for (NetworkDeviceName netdev : toAdd) {
			log.info(Messages.bind(
					Messages.CommonMsg_ATTACH_NOTWORK_DEVICE_NOT_SUPPORTED,
					i.getImageId(), netdev));
		}
	}

	public static String getSecurityGroup(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev) {
		/*
		 * always retrieve the security group associated to eth0.
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
	 * Create an AWS Security Group with the given name and description.
	 * </p>
	 * 
	 * <p>
	 * <i> * The newly created AWS Security Group is empty (e.g. contains no
	 * ingress permissions).<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sSGName
	 *            is the name of the AWS Security Group to create.
	 * @param sSGDesc
	 *            is the associated description.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed (ex : because the sgname is invalid -
	 *             Character sets beyond ASCII are not supported).
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sSGName is <code>null</code>.
	 */
	protected static void createSecurityGroup(AmazonEC2 ec2, String sSGName,
			String sSGDesc) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sSGName == null || sSGName.trim().length() == 0) {
			throw new IllegalArgumentException(sSGName + ": Not accepted. "
					+ "Must be a String (an AWS Security Group name).");
		}

		CreateSecurityGroupRequest csgreq = null;
		csgreq = new CreateSecurityGroupRequest(sSGName, sSGDesc);

		try {
			log.trace("Creating Security Group '" + sSGName + "' ...");
			ec2.createSecurityGroup(csgreq);
			log.debug("Security Group '" + sSGName + "' created.");
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
	 * <p>
	 * <i> * If the given name doesn't match any AWS Security Group Name, does
	 * nothing.<BR/>
	 * <i> * The AWS Security must not be 'in use', otherwise, an
	 * {@link AmazonServiceException} will be generated.<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sSGName
	 *            is the name of the AWS Security Group to delete.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sSGName is <code>null</code> or an empty
	 *             <code>String</code>.
	 */
	protected static void deleteSecurityGroup(AmazonEC2 ec2, String sSGName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sSGName == null || sSGName.length() == 0) {
			return;
		}

		DeleteSecurityGroupRequest dsgreq = new DeleteSecurityGroupRequest();
		dsgreq.withGroupName(sSGName);

		try {
			log.trace("Deleting Security Group '" + sSGName + "' ...");
			ec2.deleteSecurityGroup(dsgreq);
			log.debug("Security Group '" + sSGName + "' deleted.");
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return;
			} else {
				throw Ex;
			}
		}
	}

}