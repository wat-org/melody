package com.wat.melody.cloud.disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceSize {

	public static final String DISK_DEVICE_SIZE_PATTERN = "([0-9]+)[\\s]?([tTgG])";

	public static final DiskDeviceSize SIZE_1G = createDiskDeviceSize(1);

	private static DiskDeviceSize createDiskDeviceSize(int devsize) {
		try {
			return new DiskDeviceSize(devsize);
		} catch (IllegalDiskDeviceSizeException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a DiskDeviceSize with value '" + devsize + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public static DiskDeviceSize parseInt(int iDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		return new DiskDeviceSize(iDiskDeviceSize);
	}

	public static DiskDeviceSize parseString(String sDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		return new DiskDeviceSize(sDiskDeviceSize);
	}

	private int miSize;

	public DiskDeviceSize(int iDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		setSize(iDiskDeviceSize);
	}

	public DiskDeviceSize(String sDiskDeviceName)
			throws IllegalDiskDeviceSizeException {
		setSize(sDiskDeviceName);
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
		return miSize;
	}

	/**
	 * 
	 * @param iDiskDeviceSize
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceException
	 *             if the given size is negative.
	 */
	private int setSize(int iDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		if (iDiskDeviceSize <= 0) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_NEGATIVE_SIZE, iDiskDeviceSize,
					DISK_DEVICE_SIZE_PATTERN));
		}
		int previous = getSize();
		miSize = iDiskDeviceSize;
		return previous;
	}

	/**
	 * <p>
	 * Set the disk device size of this object.
	 * </p>
	 * <ul>
	 * <li>The given disk device size should match the pattern
	 * {@link #DISK_DEVICE_SIZE_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDevice
	 *            is the disk device size to assign to this object.
	 * 
	 * @return the disk device size, in Go, before this operation.
	 * 
	 * @throws IllegalDiskDeviceException
	 *             if the given disk device size is invalid.
	 */
	private int setSize(String sDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		if (sDiskDeviceSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Disk Device size)");
		}
		if (sDiskDeviceSize.trim().length() == 0) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_EMPTY_SIZE, sDiskDeviceSize));
		}

		Pattern p = Pattern.compile("^" + DISK_DEVICE_SIZE_PATTERN + "$");
		Matcher matcher = p.matcher(sDiskDeviceSize);
		if (!matcher.matches()) {
			throw new IllegalDiskDeviceSizeException(Messages.bind(
					Messages.DiskDeviceSizeEx_INVALID_SIZE, sDiskDeviceSize,
					DISK_DEVICE_SIZE_PATTERN));
		}

		int iSize = Integer.parseInt(matcher.group(1));
		switch (matcher.group(2).charAt(0)) {
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