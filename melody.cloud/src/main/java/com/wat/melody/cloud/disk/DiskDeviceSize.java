package com.wat.melody.cloud.disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceSize {

	public static final String DISK_DEVICE_SIZE_PATTERN = "([0-9]+)[\\s]?([tTgG])?";

	public static final DiskDeviceSize SIZE_1G = createDiskDeviceSize(1);

	private static DiskDeviceSize createDiskDeviceSize(int size) {
		try {
			return new DiskDeviceSize(size);
		} catch (IllegalDiskDeviceSizeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a DiskDeviceSize with value '" + size + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Create a new {@link DiskDeviceSize}, which have the given size.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is negative or zero.
	 */
	public static DiskDeviceSize parseInt(int iDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		return new DiskDeviceSize(iDiskDeviceSize);
	}

	/**
	 * <p>
	 * Create a new {@link DiskDeviceSize}, which have the given size.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given size should match the pattern
	 * {@link #DISK_DEVICE_SIZE_PATTERN} ;</li>
	 * <li>If no unit is provided, the unit is Go ;</li>
	 * </ul>
	 * 
	 * @param size
	 *            is the size to assign to this object.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
	 */
	public static DiskDeviceSize parseString(String sDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		return new DiskDeviceSize(sDiskDeviceSize);
	}

	private int _size;

	/**
	 * <p>
	 * Create a new {@link DiskDeviceSize}, which have the given size.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is negative or zero.
	 */
	public DiskDeviceSize(int size) throws IllegalDiskDeviceSizeException {
		setSize(size);
	}

	/**
	 * <p>
	 * Create a new {@link DiskDeviceSize}, which have the given size.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given size should match the pattern
	 * {@link #DISK_DEVICE_SIZE_PATTERN} ;</li>
	 * <li>If no unit is provided, the unit is Go ;</li>
	 * </ul>
	 * 
	 * @param size
	 *            is the size to assign to this object.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
	 */
	public DiskDeviceSize(String size) throws IllegalDiskDeviceSizeException {
		setSize(size);
	}

	@Override
	public int hashCode() {
		return _size;
	}

	@Override
	public String toString() {
		return String.valueOf(getSize()) + " Go";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof DiskDeviceSize) {
			DiskDeviceSize d = (DiskDeviceSize) anObject;
			return getSize() == d.getSize();
		}
		return false;
	}

	/**
	 * 
	 * @return the size, in Go, of this object.
	 */
	public int getSize() {
		return _size;
	}

	/**
	 * <p>
	 * Set the size of this object.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is negative or zero.
	 */
	private int setSize(int size) throws IllegalDiskDeviceSizeException {
		if (size <= 0) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_NEGATIVE_SIZE, size,
					DISK_DEVICE_SIZE_PATTERN));
		}
		int previous = getSize();
		_size = size;
		return previous;
	}

	/**
	 * <p>
	 * Set the size of this object.
	 * </p>
	 * 
	 * <ul>
	 * <li>The given size should match the pattern
	 * {@link #DISK_DEVICE_SIZE_PATTERN} ;</li>
	 * <li>If no unit is provided, the unit is Go ;</li>
	 * </ul>
	 * 
	 * @param size
	 *            is the size to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given size is invalid.
	 * @throws IllegalArgumentException
	 *             if the given size is <tt>null</tt>.
	 */
	private int setSize(String size) throws IllegalDiskDeviceSizeException {
		if (size == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a Disk Device Size)");
		}
		if (size.trim().length() == 0) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_EMPTY_SIZE, size));
		}

		Pattern p = Pattern.compile("^" + DISK_DEVICE_SIZE_PATTERN + "$");
		Matcher matcher = p.matcher(size);
		if (!matcher.matches()) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_INVALID_SIZE, size,
					DISK_DEVICE_SIZE_PATTERN));
		}

		int iSize = Integer.parseInt(matcher.group(1));
		char unit = 'g';
		if (matcher.group(2) != null) {
			unit = matcher.group(2).charAt(0);
		}
		switch (unit) {
		case 't':
		case 'T':
			iSize *= 1024;
			break;
		case 'g':
		case 'G':
			break;
		}
		return setSize(iSize);
	}

}