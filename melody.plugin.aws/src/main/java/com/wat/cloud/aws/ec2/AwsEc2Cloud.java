package com.wat.cloud.aws.ec2;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsEc2Cloud {

	private static Logger log = LoggerFactory.getLogger(AwsEc2Cloud.class);

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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidInstanceID") != -1) {
				// Means that the given AwsInstanceID is not valid
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sRegion == null || sRegion.trim().length() == 0) {
			return null;
		}

		DescribeRegionsRequest drreq = new DescribeRegionsRequest();
		drreq.withRegionNames(sRegion);

		try {
			return ec2.describeRegions(drreq).getRegions().get(0).getEndpoint();
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
				// Means that the given region is not valid
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sImageId == null || sImageId.trim().length() == 0) {
			return null;
		}
		DescribeImagesRequest direq = new DescribeImagesRequest();
		direq.withImageIds(sImageId);

		try {
			return ec2.describeImages(direq).getImages().get(0);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidAMIID") != -1) {
				// Means that the given AMI Id is not valid
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidParameterValue") != -1) {
				// Means that the given Availability Zone is not valid
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

	public static InstanceType getInstanceType(Instance i) {
		if (i == null) {
			return null;
		}
		String sType = i.getInstanceType();
		try {
			return InstanceType.parseString(sType);
		} catch (IllegalInstanceTypeException Ex) {
			throw new RuntimeException("Unexpected error while parsing "
					+ "the InstanceType '" + sType + "'. "
					+ "Because this value have just been retreive from "
					+ "AWS, such error cannot happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}

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
	 *            Instance to reach the given state, in millis.
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
			log.debug(Msg.bind(Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE,
					sAwsInstanceId, state, is));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Msg.bind(
						Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE_FAILED,
						sAwsInstanceId, state, timeout / 1000));
				return false;
			}
		}
		log.info(Msg.bind(Messages.CommonMsg_WAIT_FOR_INSTANCE_STATE_SUCCEED,
				sAwsInstanceId, state, System.currentTimeMillis() - start));
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
	 * @param sAZ
	 *            if <code>null</code>, the default Availability Zone will be
	 *            selected (e.g. AWS EC2 will select the default AZ).
	 * @param keyPairName
	 *            is the AWS Key Pair Name to attach to the new instance.
	 * @param protectedAreaIds
	 *            is the AWS Security Group Id Name to attach to the new
	 *            instance.
	 * 
	 * @return a String which represents the newly created Aws Instance Id if
	 *         the operation succeed.
	 * 
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 */
	public static String newAwsInstance(AmazonEC2 ec2, InstanceType type,
			String sImageId, String sAZ, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}

		String sgid = AwsEc2CloudNetwork.createSelfProtectedArea(ec2);

		RunInstancesRequest rireq = new RunInstancesRequest();
		rireq.withInstanceType(type.toString());
		rireq.withImageId(sImageId);
		rireq.withSecurityGroupIds(sgid);
		for (ProtectedAreaId protectedAreaId : protectedAreaIds) {
			rireq.withSecurityGroupIds(protectedAreaId.getValue());
		}
		rireq.withKeyName(keyPairName.getValue());
		rireq.withMinCount(1);
		rireq.withMaxCount(1);
		if (sAZ != null) {
			rireq.withPlacement(new Placement(sAZ));
		}

		try {
			String sInstanceId = ec2.runInstances(rireq).getReservation()
					.getInstances().get(0).getInstanceId();
			/*
			 * Store the automatically created security group's id in the
			 * instance's tags, so that it could be retrieve easily (see
			 * AwsEc2CloudNetwork#getSelfProtectedAreaIdTag).
			 */
			CreateTagsRequest ctreq = new CreateTagsRequest();
			ctreq = ctreq.withResources(sInstanceId);
			ctreq = ctreq.withTags(AwsEc2CloudNetwork
					.createSelfProtectedAreaIdTag(sgid));
			ec2.createTags(ctreq);

			return sInstanceId;
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			AwsEc2CloudNetwork.deleteSecurityGroup(ec2, sgid);
			throw new RuntimeException("Fail to retrieve new Aws Instance "
					+ "details (Aws Instance may have been created).");
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
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
	 * @param i
	 *            is the Aws Instance to delete.
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
	 *             if i is <code>null</code>.
	 * @throws InterruptedException
	 *             if the operation is interrupted.
	 */
	public static boolean deleteAwsInstance(AmazonEC2 ec2, Instance i,
			long timeout) throws InterruptedException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + Instance.class.getCanonicalName() + ".");
		}
		NetworkDeviceList netdevs = AwsEc2CloudNetwork.getNetworkDevices(i);

		TerminateInstancesRequest tireq = null;
		tireq = new TerminateInstancesRequest();
		tireq.withInstanceIds(i.getInstanceId());

		ec2.terminateInstances(tireq);

		try {
			return waitUntilInstanceStatusBecomes(ec2, i.getInstanceId(),
					InstanceState.TERMINATED, timeout, 0);
		} finally {
			for (NetworkDevice netdev : netdevs) {
				String sgid = AwsEc2CloudNetwork.getProtectedAreaId(ec2, i,
						netdev.getNetworkDeviceName());
				AwsEc2CloudNetwork.deleteSecurityGroup(ec2, sgid);
			}
		}
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
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sAwsInstanceId == null || sAwsInstanceId.trim().length() == 0) {
			throw new IllegalArgumentException(sAwsInstanceId
					+ ": Not accepted. "
					+ "Must be a String (an Aws Instance Id).");
		}
		if (type == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceType.class.getCanonicalName() + ".");
		}

		ModifyInstanceAttributeRequest miareq = null;
		miareq = new ModifyInstanceAttributeRequest();
		miareq.withInstanceId(sAwsInstanceId);
		miareq.withInstanceType(type.toString());

		try {
			ec2.modifyInstanceAttribute(miareq);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InternalError") != -1) {
				// Means that the operating failed
				return false;
			} else {
				throw Ex;
			}
		}

		return true;
	}

}