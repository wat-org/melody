package com.wat.cloud.aws.ec2.exception;

import com.wat.cloud.aws.ec2.VolumeState;
import com.wat.melody.cloud.disk.DiskDevice;
import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WaitVolumeStatusException extends MelodyException {

	private static final long serialVersionUID = 4897604537494850805L;

	private final DiskDevice _disk;
	private final String _volumeId;
	private final VolumeState _expectedState;
	private final long _timeout;

	public WaitVolumeStatusException(DiskDevice disk, String sVolumeId,
			VolumeState expectedState, long timeout) {
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
		if (expectedState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + VolumeState.class.getCanonicalName()
					+ ".");
		}
		if (timeout < 0) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a positive " + Integer.class.getCanonicalName()
					+ " (a timeout).");
		}
		_disk = disk;
		_volumeId = sVolumeId;
		_expectedState = expectedState;
		_timeout = timeout;
	}

	public DiskDevice getDisk() {
		return _disk;
	}

	public String getVolumeId() {
		return _volumeId;
	}

	public VolumeState getExpectedState() {
		return _expectedState;
	}

	public long getTimeout() {
		return _timeout;
	}

	@Override
	public String getMessage() {
		// TODO : return a message which contains WaitVolumeStatusException
		// details
		return "TODO";
	}

}