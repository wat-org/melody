package com.wat.cloud.aws.ec2.exception;

import com.wat.cloud.aws.ec2.VolumeAttachmentState;
import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WaitVolumeAttachmentStatusException extends MelodyException {

	private static final long serialVersionUID = 870139684684091797L;

	private final DiskDevice _disk;
	private final String _volumeId;
	private final int _attachmentIndex;
	private final VolumeAttachmentState _expectedState;
	private final long _timeout;

	public WaitVolumeAttachmentStatusException(DiskDevice disk,
			String sVolumeId, int iAttachmentIndex,
			VolumeAttachmentState expectedState, long timeout) {
		super((String) null);
		if (disk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + DiskDevice.class.getCanonicalName()
					+ ".");
		}
		if (sVolumeId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (an Aws Volume Id).");
		}
		if (iAttachmentIndex < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive " + Integer.class.getCanonicalName()
					+ " (an Attachment Index).");
		}
		if (expectedState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ VolumeAttachmentState.class.getCanonicalName() + ".");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive " + Integer.class.getCanonicalName()
					+ " (a timeout).");
		}
		_disk = disk;
		_volumeId = sVolumeId;
		_attachmentIndex = iAttachmentIndex;
		_expectedState = expectedState;
		_timeout = timeout;
	}

	public DiskDevice getDisk() {
		return _disk;
	}

	public String getVolumeId() {
		return _volumeId;
	}

	public int getAttachmentIndex() {
		return _attachmentIndex;
	}

	public VolumeAttachmentState getExpectedState() {
		return _expectedState;
	}

	public long getTimeout() {
		return _timeout;
	}

}