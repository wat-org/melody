package com.wat.melody.cloud.disk;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceName {

	public static final String PATTERN = "/dev/[sv]d[a-z]+[1-9]*";

	/**
	 * <p>
	 * Convert the given name in a {@link DiskDeviceName}.
	 * </p>
	 * 
	 * @param name
	 *            is the name to convert.
	 * 
	 * @return a {@link DiskDeviceName}, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDiskDeviceNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 *             </ul>
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
	 * @param name
	 *            is the name to assign to this object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDiskDeviceNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
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

	private String setValue(String name) throws IllegalDiskDeviceNameException {
		if (name == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Disk Device name)");
		}
		if (name.trim().length() == 0) {
			throw new IllegalDiskDeviceNameException(Msg.bind(
					Messages.DiskDeviceNameEx_EMPTY, name));
		}
		if (!name.matches("^" + PATTERN + "$")) {
			throw new IllegalDiskDeviceNameException(Msg.bind(
					Messages.DiskDeviceNameEx_INVALID, name, PATTERN));
		}
		String previous = getValue();
		_value = name;
		return previous;
	}

}