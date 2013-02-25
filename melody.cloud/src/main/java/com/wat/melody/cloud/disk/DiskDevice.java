package com.wat.melody.cloud.disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDevice {

	// TODO : create a DiskDeviceName
	public static final String DISK_DEVICE_SIZE_PATTERN = "([0-9]+)[\\s]?([tTgG])";

	// TODO : create a DiskDeviceSize
	public static final String DISK_DEVICE_NAME_PATTERN = "/dev/[sv]d[a-z]+[1-9]*";

	private int miSize;
	private String msDeviceName;
	private boolean mbDeleteOnTermination;
	private boolean mbRootDevice;

	/*
	 * TODO : remove this constructor.
	 * 
	 * Create a constructor DiskDeviceName, DiskDeviceSize, delete, root
	 */
	public DiskDevice() {
		try {
			setSize(1);
		} catch (IllegalDiskDeviceException Ex) {
			throw new RuntimeException("Unexpected error while setting "
					+ "the disk device size to 1 Go. "
					+ "Because this value is hard coded, such error cannot "
					+ "happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		initDeviceName();
		setDeleteOnTermination(true);
		setRootDevice(false);
	}

	private void initDeviceName() {
		msDeviceName = null;
	}

	@Override
	public String toString() {
		return "{ "
				+ "device:"
				+ getDeviceName()
				+ ", size:"
				+ getSize()
				+ " Go"
				+ (isRootDevice() == true ? ", rootDevice:true" : "")
				+ (isDeletedOnTermination() == false ? ", deleteOnTermination:false "
						: "") + " }";
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof DiskDevice) {
			DiskDevice d = (DiskDevice) anObject;
			return (isRootDevice() && d.isRootDevice())
					|| (getSize() == d.getSize() && getDeviceName().equals(
							d.getDeviceName()));
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
	 * @param iSize
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @return the size, in Go, of this object before this call.
	 * 
	 * @throws IllegalDiskDeviceException
	 *             if the given size is negative.
	 */
	public int setSize(int iSize) throws IllegalDiskDeviceException {
		if (iSize <= 0) {
			throw new IllegalDiskDeviceException(Messages.bind(
					Messages.DiskEx_NEGATIVE_SIZE, iSize,
					DISK_DEVICE_SIZE_PATTERN));
		}
		int previous = getSize();
		miSize = iSize;
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
	public int setSize(String sSize) throws IllegalDiskDeviceException {
		if (sSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux Disk Device size)");
		}
		if (sSize.trim().length() == 0) {
			throw new IllegalDiskDeviceException(Messages.bind(
					Messages.DiskEx_EMPTY_SIZE, sSize));
		}

		Pattern p = Pattern.compile("^" + DISK_DEVICE_SIZE_PATTERN + "$");
		Matcher matcher = p.matcher(sSize);
		if (!matcher.matches()) {
			throw new IllegalDiskDeviceException(Messages.bind(
					Messages.DiskEx_INVALID_SIZE, sSize,
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

	public String getDeviceName() {
		return msDeviceName;
	}

	/**
	 * <p>
	 * Set the disk device name of this object.
	 * </p>
	 * <ul>
	 * <li>The given disk device name should match the pattern
	 * {@link #DISK_DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDeviceName
	 *            is the disk device name to assign to this object.
	 * 
	 * @return the disk device name, before this operation.
	 * 
	 * @throws IllegalDiskDeviceException
	 *             if the given disk device name is invalid.
	 */
	public String setDeviceName(String sDeviceName)
			throws IllegalDiskDeviceException {
		if (sDeviceName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux device name)");
		}
		if (sDeviceName.trim().length() == 0) {
			throw new IllegalDiskDeviceException(Messages.bind(
					Messages.DiskEx_EMPTY_DEVICE_NAME, sDeviceName));
		}
		if (!sDeviceName.matches("^" + DISK_DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalDiskDeviceException(Messages.bind(
					Messages.DiskEx_INVALID_DEVICE_NAME, sDeviceName,
					DISK_DEVICE_NAME_PATTERN));
		}
		String previous = getDeviceName();
		msDeviceName = sDeviceName;
		return previous;
	}

	public boolean isDeletedOnTermination() {
		return mbDeleteOnTermination;
	}

	public boolean setDeleteOnTermination(boolean deleteOnTermination) {
		boolean previous = isDeletedOnTermination();
		mbDeleteOnTermination = deleteOnTermination;
		return previous;
	}

	public boolean isRootDevice() {
		return mbRootDevice;
	}

	public boolean setRootDevice(boolean rootDevice) {
		boolean previous = isRootDevice();
		mbRootDevice = rootDevice;
		return previous;
	}

}
