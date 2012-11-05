package com.wat.melody.plugin.aws.ec2.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.plugin.aws.ec2.common.exception.IllegalDiskException;

public class Disk {

	public static final String SIZE_PATTERN = "([0-9]+)[\\s]?([tTgG])";

	public static final String DEVICE_PATTERN = "/dev/sd[a-z]+[1-9]*";

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
		mbDeleteOnTermination = false;
	}

	private void initRootDevice() {
		mbRootDevice = false;
	}

	@Override
	public String toString() {
		return "{ " + "device:" + getDevice() + ", size:" + getGiga()
				+ " Go, rootDevice: " + getRootDevice() + " }";
	}

	public boolean equals(Integer iSize, String sDevice) {
		return iSize.equals(getGiga()) && sDevice.equals(getDevice());
	}

	public int setSize(String sSize) throws IllegalDiskException {
		if (sSize == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux device size)");
		}
		if (sSize.trim().length() == 0) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_EMPTY_SIZE_ATTR, sSize));
		}

		Pattern p = Pattern.compile("^" + SIZE_PATTERN + "$");
		Matcher matcher = p.matcher(sSize);
		if (!matcher.matches()) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_INVALID_SIZE_ATTR, sSize, SIZE_PATTERN));
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

	public String setDevice(String sDevice) throws IllegalDiskException {
		if (sDevice == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a linux device name)");
		}
		if (sDevice.trim().length() == 0) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_EMPTY_DEVICE_ATTR, sDevice));
		}
		if (!sDevice.matches("^" + DEVICE_PATTERN + "$")) {
			throw new IllegalDiskException(Messages.bind(
					Messages.DiskEx_INVALID_DEVICE_ATTR, sDevice,
					DEVICE_PATTERN));
		}
		String previous = getDevice();
		msDevice = sDevice;
		return previous;
	}

	public boolean getDeleteOnTermination() {
		return mbDeleteOnTermination;
	}

	public boolean setDeleteOnTermination(boolean deleteOnTermination) {
		boolean previous = getDeleteOnTermination();
		mbDeleteOnTermination = deleteOnTermination;
		return previous;
	}

	public boolean getRootDevice() {
		return mbRootDevice;
	}

	public boolean setRootDevice(boolean rootDevice) {
		boolean previous = getRootDevice();
		mbRootDevice = rootDevice;
		return previous;
	}

}
