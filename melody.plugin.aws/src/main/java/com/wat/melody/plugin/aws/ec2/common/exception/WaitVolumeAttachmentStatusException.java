package com.wat.melody.plugin.aws.ec2.common.exception;

import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.plugin.aws.ec2.common.VolumeAttachmentState;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WaitVolumeAttachmentStatusException extends AwsException {

	private static final long serialVersionUID = 870139684684091797L;

	private final DiskDevice moDisk;
	private final String msVolumeId;
	private final int miAttachmentIndex;
	private final VolumeAttachmentState moExpectedState;
	private final long mlTimeout;

	public WaitVolumeAttachmentStatusException(DiskDevice disk,
			String sVolumeId, int iAttachmentIndex,
			VolumeAttachmentState expectedState, long timeout) {
		if (disk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Disk.");
		}
		if (sVolumeId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an Aws Volume Id).");
		}
		if (iAttachmentIndex < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive Integer (an Attachment Index).");
		}
		if (expectedState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid VolumeAttachmentState.");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive Integer (a timeout).");
		}
		moDisk = disk;
		msVolumeId = sVolumeId;
		miAttachmentIndex = iAttachmentIndex;
		moExpectedState = expectedState;
		mlTimeout = timeout;
	}

	public DiskDevice getDisk() {
		return moDisk;
	}

	public String getVolumeId() {
		return msVolumeId;
	}

	public int getAttachmentIndex() {
		return miAttachmentIndex;
	}

	public VolumeAttachmentState getExpectedState() {
		return moExpectedState;
	}

	public long getTimeout() {
		return mlTimeout;
	}
}
