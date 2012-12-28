package com.wat.melody.cloud.disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.cloud.disk.exception.IllegalDiskException;

public class Disk {

	public static final String DEVICE_SIZE_PATTERN = "([0-9]+)[\\s]?([tTgG])";

	public static final String DEVICE_NAME_PATTERN = "/dev/[sv]d[a-z]+[1-9]*";

	private int miGiga;
	private String msDevice;
	private boolean mbDeleteOnTermination;
	private boolean mbRootDevice;

	public Disk() {
		initGiga();
		initDevice();
		initDeleteOnTermination();
		initRootDevice();
	}

	private void initGiga() {
		miGiga = 1;
	}

	private void initDevice() {
		msDevice = null;
	}

	private void initDeleteOnTermination() {
		mbDeleteOnTermination = true;
	}

	private void initRootDevice() {
		mbRootDevice = false;
	}

	@Override
	public String toString() {
		return "{ "
				+ "device:"
				+ getDevice()
				+ ", size:"
				+ getGiga()
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
		if (anObject instanceof Disk) {
			Disk d = (Disk) anObject;
			return (isRootDevice() && d.isRootDevice())
					|| (getGiga() == d.getGiga() && getDevice().equals(
							d.getDevice()));
		}
		return false;
	}

	/**
	 * <p>
	 * Set the disk device size of this object.
	 * </p>
	 * <ul>
	 * <li>The given disk device size should match the pattern
	 * {@link #DEVICE_SIZE_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDevice
	 *            is the disk device size to assign to this object.
	 * 
	 * @return the disk device size, in Go, before this operation.
	 * 
	 * @throws IllegalDiskException
	 *             if the given disk device size is invalid.
	 */
	public int setSize(String sSize) throws IllegalDiskException {
		if (sSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux device size)");
		}
		if (sSize.trim().length() == 0) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_EMPTY_SIZE_ATTR, sSize));
		}

		Pattern p = Pattern.compile("^" + DEVICE_SIZE_PATTERN + "$");
		Matcher matcher = p.matcher(sSize);
		if (!matcher.matches()) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_INVALID_SIZE_ATTR, sSize,
					DEVICE_SIZE_PATTERN));
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
		return setGiga(iSize);
	}

	public int getGiga() {
		return miGiga;
	}

	public int setGiga(int giga) {
		if (giga <= 0) {
			throw new IllegalArgumentException(giga + ": Not accepted. "
					+ "Must be a positive integer (a size, in gigabytes)");
		}
		int previous = getGiga();
		miGiga = giga;
		return previous;
	}

	public String getDevice() {
		return msDevice;
	}

	/**
	 * <p>
	 * Set the disk device name of this object.
	 * </p>
	 * <ul>
	 * <li>The given disk device name should match the pattern
	 * {@link #DEVICE_NAME_PATTERN} ;</li>
	 * </ul>
	 * 
	 * @param sDevice
	 *            is the disk device name to assign to this object.
	 * 
	 * @return the disk device name, before this operation.
	 * 
	 * @throws IllegalDiskException
	 *             if the given disk device name is invalid.
	 */
	public String setDevice(String sDevice) throws IllegalDiskException {
		if (sDevice == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux device name)");
		}
		if (sDevice.trim().length() == 0) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_EMPTY_DEVICE_ATTR, sDevice));
		}
		if (!sDevice.matches("^" + DEVICE_NAME_PATTERN + "$")) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_INVALID_DEVICE_ATTR, sDevice,
					DEVICE_NAME_PATTERN));
		}
		String previous = getDevice();
		msDevice = sDevice;
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
