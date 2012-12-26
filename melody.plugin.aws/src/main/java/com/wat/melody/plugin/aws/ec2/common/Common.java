package com.wat.melody.plugin.aws.ec2.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDeviceSpecification;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.ImportKeyPairRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMappingSpecification;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.wat.melody.cloud.disk.Disk;
import com.wat.melody.cloud.disk.DiskList;
import com.wat.melody.cloud.disk.exception.IllegalDiskException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;
import com.wat.melody.plugin.aws.ec2.common.exception.IllegalVolumeAttachmentStateException;
import com.wat.melody.plugin.aws.ec2.common.exception.IllegalVolumeStateException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeAttachmentStatusException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeStatusException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Common {

	private static Log log = LogFactory.getLog(Common.class);

	/**
	 * The 'AWS_instance_ID' XML attribute of the Aws instance Node
	 */
	public static final String AWS_INSTANCE_ID_ATTR = "AWS_instance_Id";

	/**
	 * The 'IP_priv' XML attribute of the Aws instance Node
	 */
	public static final String IP_PRIV_ATTR = "IP_priv";

	/**
	 * The 'FQDN_priv' XML attribute of the Aws Instance Node
	 */
	public static final String FQDN_PRIV_ATTR = "FQDN_priv";

	/**
	 * The 'IP_pub' XML attribute of the Aws instance Node
	 */
	public static final String IP_PUB_ATTR = "IP_pub";

	/**
	 * The 'FQDN_pub' XML attribute of the Aws Instance Node
	 */
	public static final String FQDN_PUB_ATTR = "FQDN_pub";

	/**
	 * The 'region' XML attribute of the Aws Instance Node
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * The 'instanceType' XML attribute of the Aws Instance Node
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	/**
	 * The 'imageId' XML attribute of the Aws Instance Node
	 */
	public static final String IMAGEID_ATTR = "imageId";

	/**
	 * The 'availabilityZone' XML attribute of the Aws Instance Node
	 */
	public static final String AVAILABILITYZONE_ATTR = "availabilityZone";

	/**
	 * The 'keyName' XML attribute of the Aws Instance Node
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";
	public static final String KEYPAIR_NAME_PATTERN = "[.\\d\\w-_\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	/**
	 * The 'passphrase' XML attribute of the Aws Instance Node
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * The 'fwrule' XML Nested element of the Aws Instance Node
	 */
	public static final String FWRULE_NE = "fwrule";

	/**
	 * <p>
	 * Validate AWSCredentials and ClientConfiguration of the given
	 * {@link AmazonEC2}.
	 * </p>
	 * 
	 * @param ec2
	 *            if the {@link AmazonEC2} to validate.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails (typically when AWS Credentials are
	 *             not valid).
	 * @throws AmazonClientException
	 *             if the operation fails (typically when network communication
	 *             encountered problems).
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static void validate(AmazonEC2 ec2) throws AmazonServiceException,
			AmazonClientException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}

		/*
		 * TODO : encapsulate most common errors ? so that resulting exception
		 * is more clear for the user
		 */
		ec2.describeRegions(new DescribeRegionsRequest());
	}

	/**
	 * <p>
	 * Get the Aws {@link Instance} designated by the given Aws Instance
	 * Identifier.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * 
	 * @return the Aws {@link Instance}, designated by the given Aws Instance
	 *         Identifier, or <code>null</code> if the given Aws Instance
	 *         Identifier doesn't match any Aws Instance.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static Instance getInstance(AmazonEC2 ec2, String sAwsInstanceId) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			return null;
		}

		DescribeInstancesRequest direq = new DescribeInstancesRequest();
		direq.withInstanceIds(sAwsInstanceId);

		try {
			return ec2.describeInstances(direq).getReservations().get(0)
					.getInstances().get(0);
		} catch (AmazonServiceException Ex) {
			// Means that the given AwsInstanceID is not valid
			if (Ex.getErrorCode().indexOf("InvalidInstanceID") != -1) {
				return null;
			} else {
				throw Ex;
			}
		} catch (NullPointerException | IndexOutOfBoundsException FEx) {
			return null;
		}
	}

	/**
	 * <p>
	 * Get the endpoint of the given region.
	 * </p>
	 * 
	 * @param ec2
	 * @param sRegion
	 *            is the given region.
	 * 
	 * @return a <code>String</code> which contains the endpoint of the given
	 *         region, or <code>null</code> if the region is not valid.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static String getEndpoint(AmazonEC2 ec2, String sRegion) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sRegion == null || sRegion.trim().length() == 0) {
			return null;
		}

		DescribeRegionsRequest drreq = new DescribeRegionsRequest();
		drreq.withRegionNames(sRegion);

		try {
			return ec2.describeRegions(drreq).getRegions().get(0).getEndpoint();
		} catch (AmazonServiceException Ex) {
			// Means that the given region is not valid
			if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
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
	 * Return the {@linkImage} which have the given AMI ID.
	 * </p>
	 * 
	 * @param ec2
	 * @param sImageId
	 *            is the AMI ID of the {@link Image] to retrieve.
	 * 
	 * @return he {@linkImage} which have the given AMI ID if the given AMI ID is valid,
	 *         <code>null</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static Image getImageId(AmazonEC2 ec2, String sImageId) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sImageId == null || sImageId.trim().length() == 0) {
			return null;
		}
		DescribeImagesRequest direq = new DescribeImagesRequest();
		direq.withImageIds(sImageId);

		try {
			return ec2.describeImages(direq).getImages().get(0);
		} catch (AmazonServiceException Ex) {
			// Means that the given AMI Id is not valid
			if (Ex.getErrorCode().indexOf("InvalidAMIID") != -1) {
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
	 * Tests whether or not the given AMi ID exists.
	 * </p>
	 * 
	 * @param ec2
	 * @param sImageId
	 *            is the AMI ID to test.
	 * 
	 * @return <code>true</code> if the given AMI ID is valid,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean imageIdExists(AmazonEC2 ec2, String sImageId) {
		return getImageId(ec2, sImageId) != null;
	}

	/**
	 * <p>
	 * Tests whether or not an Aws Instance exists.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * 
	 * @return <code>true</code> if an Aws instance exists with the given Aws
	 *         Instance Identifier, or <code>false</code> if the given Aws
	 *         Instance Identifier doesn't match any Aws Instance.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean instanceExists(AmazonEC2 ec2, String sAwsInstanceId) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		return getInstance(ec2, sAwsInstanceId) != null;
	}

	public static Tag getTag(List<Tag> tagList, String key) {
		for (Tag t : tagList) {
			if (t.getKey().equals(key)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Get the Aws {@link KeyPairInfo} whose match the given name.
	 * </p>
	 * 
	 * @param ec2
	 * @param sKeyPairName
	 *            is the key pair to retrieve.
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
	public static KeyPairInfo getKeyPair(AmazonEC2 ec2, String sKeyPairName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sKeyPairName == null || sKeyPairName.length() == 0) {
			return null;
		}
		DescribeKeyPairsRequest dkpreq = new DescribeKeyPairsRequest();
		dkpreq.withKeyNames(sKeyPairName);

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
	 * @param sKeyPairName
	 *            is the key pair to test.
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
	public static boolean keyPairExists(AmazonEC2 ec2, String sKeyPairName) {
		return getKeyPair(ec2, sKeyPairName) != null;
	}

	/**
	 * <p>
	 * Compare the given fingerprint with the given Aws KeyPair's fingerprint.
	 * </p>
	 * 
	 * @param ec2
	 * @param sKeyPairName
	 *            is the key pair to test.
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
	public static boolean keyPairCompare(AmazonEC2 ec2, String sKeyPairName,
			String sFingerprint) {
		KeyPairInfo kpi = getKeyPair(ec2, sKeyPairName);
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
	 * @param sKeyPairName
	 *            is the key pair to import.
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
	public static void importKeyPair(AmazonEC2 ec2, String sKeyPairName,
			String sPublicKey) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sKeyPairName == null || sKeyPairName.length() == 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws KeyPair Name).");
		}
		if (sPublicKey == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Public Key File).");
		}

		ImportKeyPairRequest ikpreq = new ImportKeyPairRequest();
		ikpreq.withKeyName(sKeyPairName);
		ikpreq.withPublicKeyMaterial(sPublicKey);

		try {
			ec2.importKeyPair(ikpreq);
		} catch (AmazonServiceException Ex) {
			// Means that the given Key Pair Name is not valid
			if (Ex.getErrorCode().indexOf("InvalidKeyPair.Duplicate") != -1) {
				return;
			} else {
				throw Ex;
			}
		}
	}

	/**
	 * <p>
	 * Tests whether or not the given availability zone exists.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAZ
	 *            is the availability zone to test.
	 * 
	 * @return <code>true</code> if the given availability zone is valid,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean availabilityZoneExists(AmazonEC2 ec2, String sAZ) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAZ == null || sAZ.trim().length() == 0) {
			return false;
		}

		DescribeAvailabilityZonesRequest dazreq = null;
		dazreq = new DescribeAvailabilityZonesRequest();
		dazreq.withZoneNames(sAZ);

		try {
			ec2.describeAvailabilityZones(dazreq).getAvailabilityZones().get(0)
					.getZoneName();
			return true;
		} catch (AmazonServiceException Ex) {
			// Means that the given Availability Zone is not valid
			if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
				return false;
			} else {
				throw Ex;
			}
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return false;
		}
	}

	/**
	 * <p>
	 * Get the state of an Aws Instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is requested Aws Instance Identifier.
	 * 
	 * @return an {@link InstanceState}'s constant, which represents the state
	 *         of the Aws Instance, or <code>null</code> if the given Aws
	 *         Instance Identifier doesn't match any Aws Instance.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static InstanceState getInstanceState(AmazonEC2 ec2,
			String sAwsInstanceId) {
		Instance i = getInstance(ec2, sAwsInstanceId);
		if (i == null) {
			return null;
		}
		int state = i.getState().getCode();
		try {
			return InstanceStateConverter.parse(state);
		} catch (IllegalInstanceStateException Ex) {
			throw new RuntimeException("Unexpected error while creating an "
					+ "InstanceState Enum based on the value '" + state + "'. "
					+ "Because this value was given by the AWS API, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Tests if an Aws Instance is 'live'.
	 * </p>
	 * 
	 * <p>
	 * <i> 'Live' means that an Aws Instance with the given Aws Instance
	 * Identifier exists and this Aws Instance's state is neither '
	 * {@link InstanceState#SHUTTING_DOWN}' nor '
	 * {@link InstanceState#TERMINATED}'. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the Aws Instance Identifier.
	 * 
	 * @return <code>true</code> if the Aws Instance is 'live',
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean instanceLives(AmazonEC2 ec2, String sAwsInstanceId) {
		InstanceState cs = getInstanceState(ec2, sAwsInstanceId);
		if (cs == null) {
			return false;
		}
		return cs != InstanceState.SHUTTING_DOWN
				&& cs != InstanceState.TERMINATED;
	}

	/**
	 * <p>
	 * Tests if an Aws Instance is 'running'.
	 * </p>
	 * 
	 * <p>
	 * <i> 'Running' means that an Aws Instance with the given Aws Instance
	 * Identifier exists and this Aws Instance's state is either '
	 * {@link InstanceState#PENDING}' or ' {@link InstanceState#RUNNING}'. </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the Aws Instance Identifier.
	 * 
	 * @return <code>true</code> if the Aws Instance is 'running',
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static boolean instanceRuns(AmazonEC2 ec2, String sAwsInstanceId) {
		InstanceState cs = getInstanceState(ec2, sAwsInstanceId);
		if (cs == null) {
			return false;
		}
		return cs == InstanceState.PENDING || cs == InstanceState.RUNNING;
	}

	/**
	 * <p>
	 * Wait until an Aws Instance reaches the given state.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the requested Aws Instance doesn't exist, this call return
	 * <code>false</code> after all the timeout elapsed. <BR/>
	 * * If the given timeout is equal to 0, this call will wait forever. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * @param state
	 *            is the state to reach.
	 * @param timeout
	 *            is the maximal amount of time to wait for the requested Aws
	 *            Instance to reach the given state.
	 * @param sleepfirst
	 *            is an extra initial amount of time to wait.
	 * 
	 * @return <code>true</code> if the requested Aws Instance reaches the given
	 *         state before the given timeout expires, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws IllegalArgumentException
	 *             if sleepfirst is a negative long.
	 */
	public static boolean waitUntilInstanceStatusBecomes(AmazonEC2 ec2,
			String sAwsInstanceId, InstanceState state, long timeout,
			long sleepfirst) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}
		if (sleepfirst < 0) {
			throw new IllegalArgumentException(sleepfirst + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;

		Thread.sleep(sleepfirst);
		InstanceState is = null;
		while ((is = getInstanceState(ec2, sAwsInstanceId)) != state) {
			log.debug(Messages.bind(Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE,
					new Object[] { sAwsInstanceId, state, is }));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Messages.bind(
						Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE_FAILED,
						new Object[] { sAwsInstanceId, state, timeout }));
				return false;
			}
		}
		log.info(Messages.bind(
				Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE_SUCCEED,
				new Object[] { sAwsInstanceId, state, timeout }));
		return true;
	}

	/**
	 * <p>
	 * Create one new Aws Instance, with the specified options.
	 * </p>
	 * 
	 * <p>
	 * <i> * If input values contains invalid datas, an
	 * {@link AmazonServiceException} will be raised. It is the caller's
	 * responsibility to validate input values before calling this method. <BR/>
	 * * After a call to this method, the caller can wait for the instance to
	 * reach a particular state using
	 * {@link #waitUntilInstanceStatusBecomes(AmazonEC2, String, InstanceState, long, long)}
	 * . </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param type
	 *            is the instanceType.
	 * @param sImageId
	 *            is the AMI ID.
	 * @param sSGName
	 *            is an AWS Security Group Name.
	 * @param sAZ
	 *            if <code>null</code>, the default Availability Zone will be
	 *            selected (e.g. AWS EC2 will select the default AZ).
	 * @param sKeyName
	 *            is the AWS Key Name.
	 * 
	 * @return an {@link Instance} which represents the newly created Aws
	 *         Instance if the operation succeed, <code>null</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static Instance newAwsInstance(AmazonEC2 ec2, InstanceType type,
			String sImageId, String sSGName, String sAZ, String sKeyName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}

		RunInstancesRequest rireq = new RunInstancesRequest();
		rireq.withInstanceType(type.toString());
		rireq.withImageId(sImageId);
		rireq.withSecurityGroups(sSGName);
		rireq.withKeyName(sKeyName);
		rireq.withMinCount(1);
		rireq.withMaxCount(1);
		if (sAZ != null) {
			rireq.withPlacement(new Placement(sAZ));
		}

		try {
			return ec2.runInstances(rireq).getReservation().getInstances()
					.get(0);
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return null;
		}
	}

	/**
	 * <p>
	 * Starts the specified Aws Instance, and wait for the instance to reached
	 * the RUNNING state during the specified amount of time.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * @param timeout
	 * 
	 * @return <code>true</code> if the Aws Instance successfully starts before
	 *         the given timeout expires, <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws InterruptedException
	 *             if the operation is interrupted.
	 */
	public static boolean startAwsInstance(AmazonEC2 ec2,
			String sAwsInstanceId, long timeout) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}

		StartInstancesRequest sireq = new StartInstancesRequest();
		sireq.withInstanceIds(sAwsInstanceId);

		ec2.startInstances(sireq);

		return waitUntilInstanceStatusBecomes(ec2, sAwsInstanceId,
				InstanceState.RUNNING, timeout, 10000);
	}

	/**
	 * <p>
	 * Stops the specified Aws Instance, and wait for the instance to reached
	 * the STOPPED state during the specified amount of time.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * @param timeout
	 * 
	 * @return <code>true</code> if the Aws Instance successfully stops before
	 *         the given timeout expires, <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws InterruptedException
	 *             if the operation is interrupted.
	 */
	public static boolean stopAwsInstance(AmazonEC2 ec2, String sAwsInstanceId,
			long timeout) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}

		StopInstancesRequest sireq = new StopInstancesRequest();
		sireq.withInstanceIds(sAwsInstanceId);

		ec2.stopInstances(sireq);

		return waitUntilInstanceStatusBecomes(ec2, sAwsInstanceId,
				InstanceState.STOPPED, timeout, 0);
	}

	/**
	 * <p>
	 * Delete the specified Aws Instance, and wait for the instance to reached
	 * the TERMINATED state during the specified amount of time.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * @param timeout
	 * 
	 * @return <code>true</code> if the Aws Instance is successfully deleted
	 *         before the given timeout expires, <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws InterruptedException
	 *             if the operation is interrupted.
	 */
	public static boolean deleteAwsInstance(AmazonEC2 ec2,
			String sAwsInstanceId, long timeout) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}

		TerminateInstancesRequest tireq = null;
		tireq = new TerminateInstancesRequest();
		tireq.withInstanceIds(sAwsInstanceId);

		ec2.terminateInstances(tireq);

		return waitUntilInstanceStatusBecomes(ec2, sAwsInstanceId,
				InstanceState.TERMINATED, timeout, 0);
	}

	/**
	 * <p>
	 * Change the sizing of the specified Aws Instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the requested Aws Instance Identifier.
	 * @param timeout
	 * 
	 * @return <code>true</code> if the sizing of the specified Aws Instance is
	 *         successfully updated, <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 */
	public static boolean resizeAwsInstance(AmazonEC2 ec2,
			String sAwsInstanceId, InstanceType type) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}
		if (type == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be an InstanceType.");
		}

		ModifyInstanceAttributeRequest miareq = null;
		miareq = new ModifyInstanceAttributeRequest();
		miareq.withInstanceId(sAwsInstanceId);
		miareq.withInstanceType(type.toString());

		try {
			ec2.modifyInstanceAttribute(miareq);
		} catch (AmazonServiceException Ex) {
			// Means that the operating failed
			if (Ex.getErrorCode().indexOf("InternalError") != -1) {
				return false;
			} else {
				throw Ex;
			}
		}

		return true;
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
	public static void createSecurityGroup(AmazonEC2 ec2, String sSGName,
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
			ec2.createSecurityGroup(csgreq);
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
	public static void deleteSecurityGroup(AmazonEC2 ec2, String sSGName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sSGName == null || sSGName.trim().length() == 0) {
			throw new IllegalArgumentException(sSGName + ": Not accepted. "
					+ "Must be a String (an AWS Security Group name).");
		}

		DeleteSecurityGroupRequest dsgreq = new DeleteSecurityGroupRequest();
		dsgreq.withGroupName(sSGName);

		try {
			ec2.deleteSecurityGroup(dsgreq);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return;
			} else {
				throw Ex;
			}
		}
	}

	/**
	 * <p>
	 * Get the {@link IpPermission}s associated to the AWS Security Group which
	 * match the given name.
	 * </p>
	 * 
	 * @param ec2
	 * @param sSGName
	 *            is the name of the AWS Security Group to examine.
	 * 
	 * @return the {@link IpPermission}s associated to the AWS Security Group
	 *         which match the given name if such AWS Security Group exists and
	 *         if {@link IpPermission}s are associated to it, or
	 *         <code>null</code> otherwise.
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
	public static List<IpPermission> describeSecurityGroupRules(AmazonEC2 ec2,
			String sSGName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sSGName == null || sSGName.trim().length() == 0) {
			throw new IllegalArgumentException(sSGName + ": Not accepted. "
					+ "Must be a String (an AWS Security Group name).");
		}

		DescribeSecurityGroupsRequest dsgreq = null;
		dsgreq = new DescribeSecurityGroupsRequest();
		dsgreq.withGroupNames(sSGName);

		try {
			return ec2.describeSecurityGroups(dsgreq).getSecurityGroups()
					.get(0).getIpPermissions();
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
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
	 * Get all Aws {@link Volume} attached to the given Aws Instance.
	 * </p>
	 * 
	 * <p>
	 * <i> * Remove the RootDevice from the list ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param i
	 *            is the Aws Instance.
	 * 
	 * @return an Aws {@link List<Volume>}, which contains all Aws
	 *         {@link Volume} attached to the given Aws Instance (the list can
	 *         be empty).
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if i is <code>null</code>.
	 */
	public static List<Volume> getInstanceVolumes(AmazonEC2 ec2, Instance i) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance.");
		}
		Filter f = new Filter();
		f.withName("attachment.instance-id");
		f.withValues(i.getInstanceId());

		DescribeVolumesRequest dvreq = new DescribeVolumesRequest();
		dvreq.withFilters(f);

		List<Volume> aVolList = ec2.describeVolumes(dvreq).getVolumes();
		if (aVolList == null) {
			return new ArrayList<Volume>();
		}
		return aVolList;
	}

	/**
	 * <p>
	 * Get the Aws {@link Volume} designated by the given Aws Volume Identifier.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is the requested Aws Volume Identifier.
	 * 
	 * @return the Aws {@link Volume}, designated by the given Aws Volume
	 *         Identifier, or <code>null</code> if the given Aws Volume
	 *         Identifier doesn't match any Aws Volume.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static Volume getVolume(AmazonEC2 ec2, String sAwsVolumeId) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsVolumeId == null || sAwsVolumeId.trim().length() == 0) {
			return null;
		}

		DescribeVolumesRequest dvreq = new DescribeVolumesRequest();
		dvreq.withVolumeIds(sAwsVolumeId);

		try {
			return ec2.describeVolumes(dvreq).getVolumes().get(0);
		} catch (AmazonServiceException Ex) {
			// Means that the given AwsVolumeID is not valid
			if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
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
	 * Get the state of an Aws Volume.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is requested Aws Volume Identifier.
	 * 
	 * @return an {@link VolumeState}'s constant, which represents the state of
	 *         the Aws Volume, or <code>null</code> if the given Aws Volume
	 *         Identifier doesn't match any Aws Volume.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static VolumeState getVolumeState(AmazonEC2 ec2, String sAwsVolumeId) {
		Volume volume = getVolume(ec2, sAwsVolumeId);
		if (volume == null) {
			return null;
		}
		String state = volume.getState();
		try {
			return VolumeState.parseString(state);
		} catch (IllegalVolumeStateException Ex) {
			throw new RuntimeException("Unexpected error while creating a "
					+ "VolumeState Enum based on the value '" + state + "'. "
					+ "Because this value was given by the AWS API, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Wait until an Aws Volume reaches the given state.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the requested Aws Volume doesn't exist, this call return
	 * <code>false</code> after all the timeout elapsed. <BR/>
	 * * If the given timeout is equal to 0, this call will wait forever. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is the requested Aws Volume Identifier.
	 * @param state
	 *            is the state to reach.
	 * @param timeout
	 *            is the maximal amount of time to wait for the requested Aws
	 *            Volume to reach the given state.
	 * @param sleepfirst
	 *            is an extra initial amount of time to wait.
	 * 
	 * @return <code>true</code> if the requested Aws Volume reaches the given
	 *         state before the given timeout expires, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsVolumeId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws IllegalArgumentException
	 *             if sleepfirst is a negative long.
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static boolean waitUntilVolumeStatusBecomes(AmazonEC2 ec2,
			String sAwsVolumeId, VolumeState state, long timeout,
			long sleepfirst) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsVolumeId == null || sAwsVolumeId.trim().length() == 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a String (an Aws Volume Id).");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}
		if (sleepfirst < 0) {
			throw new IllegalArgumentException(sleepfirst + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;

		Thread.sleep(sleepfirst);
		VolumeState vs = null;
		while ((vs = getVolumeState(ec2, sAwsVolumeId)) != state) {
			log.debug(Messages.bind(Messages.CommonMsg_WAIT_FOR_VOLUME_STATE,
					new Object[] { sAwsVolumeId, state, vs }));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Messages.bind(
						Messages.CommonMsg_WAIT_FOR_VOLUME_STATE_FAILED,
						new Object[] { sAwsVolumeId, state, timeout }));
				return false;
			}
		}
		log.info(Messages.bind(
				Messages.CommonMsg_WAIT_FOR_VOLUME_STATE_SUCCEED, new Object[] {
						sAwsVolumeId, state, timeout }));
		return true;
	}

	/**
	 * <p>
	 * Get the Aws {@link VolumeAttachment} designated by the given Aws Volume
	 * Identifier and Aws Volume Attachment Index.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is the requested Aws Volume Identifier.
	 * @param iAttachmentIndex
	 *            is requested Aws Volume Attachment Index.
	 * 
	 * @return the Aws {@link VolumeAttachment}, designated by the given Aws
	 *         Volume Identifier and Aws Volume Attachment Index, or
	 *         <code>null</code> if the given Aws Volume Identifier and Aws
	 *         Volume Attachment Index doesn't match any Aws Volume Attachment.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static VolumeAttachment getVolumeAttachment(AmazonEC2 ec2,
			String sAwsVolumeId, int iAttachmentIndex) {
		try {
			return getVolume(ec2, sAwsVolumeId).getAttachments().get(
					iAttachmentIndex);
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return null;
		}
	}

	/**
	 * <p>
	 * Get the state of an Aws Volume Attachment.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is requested Aws Volume Identifier.
	 * @param iAttachmentIndex
	 *            is requested Aws Volume Attachment Index.
	 * 
	 * @return an {@link VolumeAttachmentState}'s constant, which represents the
	 *         state of the Aws Volume Attachment, or <code>null</code> if the
	 *         given Aws Volume Attachment doesn't match any Aws Volume
	 *         Attachment.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static VolumeAttachmentState getVolumeAttachmentState(AmazonEC2 ec2,
			String sAwsVolumeId, int iAttachmentIndex) {
		VolumeAttachment attachment = getVolumeAttachment(ec2, sAwsVolumeId,
				iAttachmentIndex);
		if (attachment == null) {
			return null;
		}
		String state = attachment.getState();
		try {
			return VolumeAttachmentState.parseString(state);
		} catch (IllegalVolumeAttachmentStateException Ex) {
			throw new RuntimeException("Unexpected error while creating a "
					+ "VolumeAttachmentState Enum based on the value '" + state
					+ "'. " + "Because this value was given by the AWS API, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Wait until an Aws Volume Attachment reaches the given state.
	 * </p>
	 * 
	 * <p>
	 * <i> * If the requested Aws Volume Attachment doesn't exist, this call
	 * return <code>false</code> after all the timeout elapsed. <BR/>
	 * * If the given timeout is equal to 0, this call will wait forever. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsVolumeId
	 *            is the requested Aws Volume Identifier.
	 * @param iAttachmentIndex
	 *            is requested Aws Volume Attachment Index.
	 * @param state
	 *            is the state to reach.
	 * @param timeout
	 *            is the maximal amount of time to wait for the requested Aws
	 *            Volume Attachment to reach the given state.
	 * @param sleepfirst
	 *            is an extra initial amount of time to wait.
	 * 
	 * @return <code>true</code> if the requested Aws Volume Attachment reaches
	 *         the given state before the given timeout expires,
	 *         <code>false</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsVolumeId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws IllegalArgumentException
	 *             if sleepfirst is a negative long.
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static boolean waitUntilVolumeAttachmentStatusBecomes(AmazonEC2 ec2,
			String sAwsVolumeId, int iAttachmentIndex,
			VolumeAttachmentState state, long timeout, long sleepfirst)
			throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsVolumeId == null || sAwsVolumeId.trim().length() == 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a String (an Aws Volume Id).");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException(timeout + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}
		if (sleepfirst < 0) {
			throw new IllegalArgumentException(sleepfirst + ": Not accepted. "
					+ "Must be a positive long (a timeout).");
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;

		Thread.sleep(sleepfirst);
		VolumeAttachmentState vas = null;
		while ((vas = getVolumeAttachmentState(ec2, sAwsVolumeId,
				iAttachmentIndex)) != state) {
			log.debug(Messages.bind(
					Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE,
					new Object[] { sAwsVolumeId, state, vas }));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Messages
						.bind(Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_FAILED,
								new Object[] { sAwsVolumeId, state, timeout }));
				return false;
			}
		}
		log.info(Messages.bind(
				Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_SUCCEED,
				new Object[] { sAwsVolumeId, state, timeout }));
		return true;
	}

	/**
	 * <p>
	 * Detach the given {@link Volume}s.
	 * 
	 * Also delete the given {@link Volume}s based on their
	 * <code>deleteOnTermination</code>'s flag.
	 * 
	 * Wait for the detached volumes to reach the state
	 * {@link VolumeState#AVAILABLE}.
	 * </p>
	 * 
	 * @param ec2
	 * @param aVolList
	 *            contains the {@link Volume}s to detach.
	 * @param detachTimeout
	 *            is the maximum time to wait for the detach operation to
	 *            complete. 0 means ifinite.
	 * 
	 * @throws WaitVolumeStatusException
	 *             if a volume is not detached in the given timeout.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if volumeList is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static void detachAndDeleteVolumes(AmazonEC2 ec2,
			List<Volume> aVolList, long detachTimeout)
			throws InterruptedException, WaitVolumeStatusException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (aVolList == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Volume>.");
		}
		for (Volume v : aVolList) {
			// Detach volume
			DetachVolumeRequest detvreq = new DetachVolumeRequest();
			detvreq.withVolumeId(v.getVolumeId());
			ec2.detachVolume(detvreq);
			if (!waitUntilVolumeStatusBecomes(ec2, v.getVolumeId(),
					VolumeState.AVAILABLE, detachTimeout, 5000)) {
				Disk disk = new Disk();
				disk.setDeleteOnTermination(v.getAttachments().get(0)
						.getDeleteOnTermination());
				try {
					disk.setDevice(v.getAttachments().get(0).getDevice());
				} catch (IllegalDiskException Ex) {
					throw new RuntimeException("TODO impossible");
				}
				disk.setGiga(v.getSize());
				throw new WaitVolumeStatusException(disk, v.getVolumeId(),
						VolumeState.AVAILABLE, detachTimeout);
			}
			// Delete volume if deleteOnTermination is true
			if (v.getAttachments().get(0).getDeleteOnTermination()) {
				DeleteVolumeRequest delvreq = new DeleteVolumeRequest();
				delvreq.withVolumeId(v.getVolumeId());
				ec2.deleteVolume(delvreq);
			}
		}
	}

	/**
	 * <p>
	 * Create and attach {@Volume}s according to the given {@link Disk}
	 * s specifications.
	 * 
	 * Wait for the created volumes to reach the state
	 * {@link VolumeState#AVAILABLE}.
	 * 
	 * Once created, wait for the attached volumes to reach the state
	 * {@link VolumeAttachmentState#ATTACHED}.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the Aws Instance Identifier of the Aws Instance to create
	 *            and attach the disk to.
	 * @param sAZ
	 *            is the Aws Availbility Zone Name of the Aws Instance, where
	 *            the {@link Volume}s will be created.
	 * @param diskList
	 *            contains the {@link Disk}s to create and attach.
	 * @param createTimeout
	 *            is the maximum time to wait for the create operation to
	 *            complete. 0 means ifinite.
	 * @param attachTimeout
	 *            is the maximum time to wait for the attach operation to
	 *            complete. 0 means ifinite.
	 * 
	 * @throws WaitVolumeStatusException
	 *             if a new volume is not available in the given timeout.
	 * @throws WaitVolumeAttachmentStatusException
	 *             if a new volume is not attached in the given timeout.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if sAZ is <code>null</code> or an empty <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if volumeList is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if timeout is a negative long.
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static void createAndAttachVolumes(AmazonEC2 ec2,
			String sAwsInstanceId, String sAZ, DiskList diskList,
			long createTimeout, long attachTimeout)
			throws InterruptedException, WaitVolumeStatusException,
			WaitVolumeAttachmentStatusException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws Instance Id).");
		}
		if (sAZ == null || sAZ.trim().length() == 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws Availability Zone).");
		}
		if (diskList == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DiskList.");
		}
		for (Disk disk : diskList) {
			// Create volume
			CreateVolumeRequest cvreq = new CreateVolumeRequest();
			cvreq.withAvailabilityZone(sAZ);
			cvreq.withSize(disk.getGiga());
			String sVolId = ec2.createVolume(cvreq).getVolume().getVolumeId();
			if (!waitUntilVolumeStatusBecomes(ec2, sVolId,
					VolumeState.AVAILABLE, createTimeout, 2000)) {
				throw new WaitVolumeStatusException(disk, sVolId,
						VolumeState.AVAILABLE, createTimeout);
			}
			// Attach volume
			AttachVolumeRequest avreq = new AttachVolumeRequest();
			avreq.withVolumeId(sVolId);
			avreq.withInstanceId(sAwsInstanceId);
			avreq.withDevice(disk.getDevice());
			ec2.attachVolume(avreq);
			/*
			 * TODO : bug : sometimes, the attachment is somehow "freezed", and
			 * stay in state 'attaching' forever.
			 */
			if (!waitUntilVolumeAttachmentStatusBecomes(ec2, sVolId, 0,
					VolumeAttachmentState.ATTACHED, attachTimeout, 2000)) {
				throw new WaitVolumeAttachmentStatusException(disk, sVolId, 0,
						VolumeAttachmentState.ATTACHED, attachTimeout);
			}
		}
	}

	/**
	 * <p>
	 * Change the DelteOnTerminstion Flag of the given disks for the given Aws
	 * Instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param sAwsInstanceId
	 *            is the Aws Instance Identifier of the Aws Instance to update.
	 * @param diskList
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sAwsInstanceId is <code>null</code> or an empty
	 *             <code>String</code>.
	 * @throws IllegalArgumentException
	 *             if diskList is <code>null</code>.
	 */
	public static void updateDeleteOnTerminationFlag(AmazonEC2 ec2,
			String sAwsInstanceId, DiskList diskList) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws Instance Id).");
		}
		if (diskList == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DiskList.");
		}
		for (Disk disk : diskList) {
			// Modify the deleteOnTermimation flag
			EbsInstanceBlockDeviceSpecification eibds = null;
			eibds = new EbsInstanceBlockDeviceSpecification();
			eibds.withDeleteOnTermination(disk.isDeletedOnTermination());

			InstanceBlockDeviceMappingSpecification ibdms = null;
			ibdms = new InstanceBlockDeviceMappingSpecification();
			ibdms.withDeviceName(disk.getDevice());
			ibdms.withEbs(eibds);

			ModifyInstanceAttributeRequest miareq = null;
			miareq = new ModifyInstanceAttributeRequest();
			miareq.withInstanceId(sAwsInstanceId);
			miareq.withBlockDeviceMappings(ibdms);

			ec2.modifyInstanceAttribute(miareq);
		}
	}

}