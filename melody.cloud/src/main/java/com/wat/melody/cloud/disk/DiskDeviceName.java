package com.wat.melody.cloud.disk;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceName {

	public static final String DISK_DEVICE_NAME_PATTERN = "/dev/[sv]d[a-z]+[1-9]*";

	/**
	 * <p>
	 * Create a new {@link DiskDeviceName}, with the given name.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @throws IllegalDiskDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 */
	public static DiskDeviceName parseString(String name)
			throws IllegalDiskDeviceNameException {
		return new DiskDeviceName(name);
	}

	private String _value;

	/**
	 * <p>
	 * Create a new {@link DiskDeviceName}, with the given name.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @throws IllegalDiskDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 */
	public DiskDeviceName(String name) throws IllegalDiskDeviceNameException {
		setValue(name);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
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
		return _value;
	}

	/**
	 * <p>
	 * Set the name of this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given name should match the pattern
	 * {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @return the name of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceNameException
	 *             if the given name is invalid.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 */
	private String setValue(String name) throws IllegalDiskDeviceNameException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Disk Device name)");
		}
		if (name.trim().length() == 0) {
			throw new IllegalDiskDeviceNameException(Messages.bind(
					Messages.DiskDeviceNameEx_EMPTY, name));
		}
		if (!name.matches("^" + DISK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalDiskDeviceNameException(Messages.bind(
					Messages.DiskDeviceNameEx_INVALID, name,
					DISK_DEVICE_NAME_PATTERN));
		}
		String previous = getValue();
		_value = name;
		return previous;
	}

}