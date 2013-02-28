package com.wat.melody.cloud.disk;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceName {

	public static final String DISK_DEVICE_NAME_PATTERN = "/dev/[sv]d[a-z]+[1-9]*";

	public static DiskDeviceName parseString(String sDiskDeviceName)
			throws IllegalDiskDeviceNameException {
		return new DiskDeviceName(sDiskDeviceName);
	}

	private String msValue;

	public DiskDeviceName() {
		initValue();
	}

	public DiskDeviceName(String sDiskDeviceName)
			throws IllegalDiskDeviceNameException {
		setValue(sDiskDeviceName);
	}

	private void initValue() {
		msValue = null;
	}

	@Override
	public String toString() {
		return getValue();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof DiskDeviceName) {
			DiskDeviceName d = (DiskDeviceName) anObject;
			return getValue().equals(d.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	/**
	 * <p>
	 * Set the Disk device name of this object.
	 * </p>
	 * <ul>
	 * <li>The given Disk device name should match the pattern
	 * {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDiskDeviceName
	 *            is the Disk device name to assign to this object.
	 * 
	 * @return the Disk device name, before this operation.
	 * 
	 * @throws IllegalDiskDeviceNameException
	 *             if the given Disk device name is invalid.
	 */
	private String setValue(String sDiskDeviceName)
			throws IllegalDiskDeviceNameException {
		if (sDiskDeviceName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Disk Device name)");
		}
		if (sDiskDeviceName.trim().length() == 0) {
			throw new IllegalDiskDeviceNameException(Messages.bind(
					Messages.DiskDeviceNameEx_EMPTY, sDiskDeviceName));
		}
		if (!sDiskDeviceName.matches("^" + DISK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalDiskDeviceNameException(Messages.bind(
					Messages.DiskDeviceNameEx_INVALID, sDiskDeviceName,
					DISK_DEVICE_NAME_PATTERN));
		}
		String previous = getValue();
		msValue = sDiskDeviceName;
		return previous;
	}

}