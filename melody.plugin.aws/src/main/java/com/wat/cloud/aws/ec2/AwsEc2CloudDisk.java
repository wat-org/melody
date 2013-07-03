package com.wat.cloud.aws.ec2;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.DeleteVolumeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDeviceSpecification;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMappingSpecification;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeAttachment;
import com.wat.cloud.aws.ec2.exception.IllegalVolumeAttachmentStateException;
import com.wat.cloud.aws.ec2.exception.IllegalVolumeStateException;
import com.wat.cloud.aws.ec2.exception.WaitVolumeAttachmentStatusException;
import com.wat.cloud.aws.ec2.exception.WaitVolumeStatusException;
import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDeviceName;
import com.wat.melody.cloud.disk.DiskDeviceSize;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsEc2CloudDisk {

	private static Logger log = LoggerFactory.getLogger(AwsEc2CloudDisk.class);

	/**
	 * <p>
	 * Get all Aws {@link Volume} attached to the given Aws Instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param i
	 *            is the Aws Instance.
	 * 
	 * @return an Aws {@link List<Volume>}, which contains all Aws
	 *         {@link Volume} attached to the given Aws Instance (the list
	 *         cannot be empty. It contains at least the root disk device).
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
			throw new RuntimeException("Unexpected error retreiving the Aws "
					+ "Instance Volumes List. "
					+ "The resulting Volumes List is empty. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
		return aVolList;
	}

	/**
	 * <p>
	 * Get all Aws {@link Volume} attached to the given Aws Instance.
	 * </p>
	 * 
	 * @param ec2
	 * @param i
	 *            is the Aws Instance.
	 * 
	 * @return a {@link DiskDeviceList}, which contains all Aws
	 *         {@link DiskDevice} attached to the given Aws Instance (the list
	 *         cannot be empty. It contains at least the root disk device).
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
	public static DiskDeviceList getInstanceDisks(AmazonEC2 ec2, Instance i) {
		List<Volume> volumes = getInstanceVolumes(ec2, i);
		DiskDeviceList disks = new DiskDeviceList();
		try {
			for (Volume volume : volumes) {
				DiskDeviceName devname = DiskDeviceName.parseString(volume
						.getAttachments().get(0).getDevice());
				DiskDeviceSize devsize = DiskDeviceSize.parseInt(volume
						.getSize());
				Boolean delonterm = volume.getAttachments().get(0)
						.getDeleteOnTermination();
				Boolean isroot = devname.getValue().equals(
						i.getRootDeviceName());
				disks.addDiskDevice(new DiskDevice(devname, devsize, delonterm,
						isroot, null, null, null));
			}
		} catch (IllegalDiskDeviceNameException
				| IllegalDiskDeviceSizeException
				| IllegalDiskDeviceListException Ex) {
			throw new RuntimeException("Unexpected error while building "
					+ "DiskList from Aws Instance Volumes List. "
					+ "Because Aws Instance Volumes List is valid, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
		return disks;
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
			log.debug(Msg.bind(Messages.CommonMsg_WAIT_FOR_VOLUME_STATE,
					sAwsVolumeId, state, vs));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Msg.bind(
						Messages.CommonMsg_WAIT_FOR_VOLUME_STATE_FAILED,
						sAwsVolumeId, state, timeout));
				return false;
			}
		}
		log.info(Msg.bind(Messages.CommonMsg_WAIT_FOR_VOLUME_STATE_SUCCEED,
				sAwsVolumeId, state, timeout));
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
			log.debug(Msg.bind(
					Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE,
					sAwsVolumeId, state, vas));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				log.warn(Msg
						.bind(Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_FAILED,
								sAwsVolumeId, state, timeout));
				return false;
			}
		}
		log.info(Msg.bind(
				Messages.CommonMsg_WAIT_FOR_VOLUME_ATTACHEMENT_STATE_SUCCEED,
				sAwsVolumeId, state, timeout));
		return true;
	}

	/**
	 * <p>
	 * Detach the given {@link DiskDeviceList}.
	 * 
	 * Also delete the given {@link DiskDeviceList} based on their
	 * <code>deleteOnTermination</code>'s flag.
	 * 
	 * Wait for the detached volumes to reach the state
	 * {@link VolumeState#AVAILABLE}.
	 * </p>
	 * 
	 * @param ec2
	 * @param instance
	 *            the Aws Instance which is the Disk onwer.
	 * @param volumes
	 *            contains the {@link DiskDeviceList} to detach and delete.
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
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static void detachAndDeleteDiskDevices(AmazonEC2 ec2,
			Instance instance, DiskDeviceList volumes)
			throws InterruptedException, WaitVolumeStatusException {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		if (volumes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Volume>.");
		}
		for (DiskDevice disk : volumes) {
			// Detach volume
			DetachVolumeRequest detvreq = new DetachVolumeRequest();
			detvreq.withInstanceId(instance.getInstanceId());
			detvreq.withDevice(disk.getDiskDeviceName().getValue());
			DetachVolumeResult detvres = null;
			detvres = ec2.detachVolume(detvreq);
			String volumeId = detvres.getAttachment().getVolumeId();
			if (!waitUntilVolumeStatusBecomes(ec2, volumeId,
					VolumeState.AVAILABLE, disk.getDetachTimeout()
							.getTimeoutInMillis(), 5000)) {
				throw new WaitVolumeStatusException(disk, volumeId,
						VolumeState.AVAILABLE, disk.getDetachTimeout()
								.getTimeoutInMillis());
			}
			// Delete volume if deleteOnTermination is true
			if (disk.isDeletedOnTermination()) {
				DeleteVolumeRequest delvreq = new DeleteVolumeRequest();
				delvreq.withVolumeId(volumeId);
				ec2.deleteVolume(delvreq);
			}
		}
	}

	/**
	 * <p>
	 * Create and attach {@Volume}s according to the given
	 * {@link DiskDevice} s specifications.
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
	 *            contains the {@link DiskDevice}s to create and attach.
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
	 * @throws InterruptedException
	 *             if the current thread is interrupted during this call.
	 */
	public static void createAndAttachDiskDevices(AmazonEC2 ec2,
			String sAwsInstanceId, String sAZ, DiskDeviceList diskList)
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
		for (DiskDevice disk : diskList) {
			// Create volume
			CreateVolumeRequest cvreq = new CreateVolumeRequest();
			cvreq.withAvailabilityZone(sAZ);
			cvreq.withSize(disk.getSize());
			String sVolId = ec2.createVolume(cvreq).getVolume().getVolumeId();
			if (!waitUntilVolumeStatusBecomes(ec2, sVolId,
					VolumeState.AVAILABLE, disk.getCreateTimeout()
							.getTimeoutInMillis(), 2000)) {
				throw new WaitVolumeStatusException(disk, sVolId,
						VolumeState.AVAILABLE, disk.getCreateTimeout()
								.getTimeoutInMillis());
			}
			// Attach volume
			AttachVolumeRequest avreq = new AttachVolumeRequest();
			avreq.withVolumeId(sVolId);
			avreq.withInstanceId(sAwsInstanceId);
			avreq.withDevice(disk.getDiskDeviceName().getValue());
			ec2.attachVolume(avreq);
			/*
			 * TODO : bug : sometimes, the attachment is somehow "freezed", and
			 * stay in state 'attaching' forever.
			 */
			if (!waitUntilVolumeAttachmentStatusBecomes(ec2, sVolId, 0,
					VolumeAttachmentState.ATTACHED, disk.getAttachTimeout()
							.getTimeoutInMillis(), 2000)) {
				throw new WaitVolumeAttachmentStatusException(disk, sVolId, 0,
						VolumeAttachmentState.ATTACHED, disk.getAttachTimeout()
								.getTimeoutInMillis());
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
			String sAwsInstanceId, DiskDeviceList diskList) {
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
		for (DiskDevice disk : diskList) {
			// Modify the deleteOnTermimation flag
			EbsInstanceBlockDeviceSpecification eibds = null;
			eibds = new EbsInstanceBlockDeviceSpecification();
			eibds.withDeleteOnTermination(disk.isDeletedOnTermination());

			InstanceBlockDeviceMappingSpecification ibdms = null;
			ibdms = new InstanceBlockDeviceMappingSpecification();
			ibdms.withDeviceName(disk.getDiskDeviceName().getValue());
			ibdms.withEbs(eibds);

			ModifyInstanceAttributeRequest miareq = null;
			miareq = new ModifyInstanceAttributeRequest();
			miareq.withInstanceId(sAwsInstanceId);
			miareq.withBlockDeviceMappings(ibdms);

			ec2.modifyInstanceAttribute(miareq);
		}
	}

}