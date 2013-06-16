package com.wat.melody.cloud.disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceSizeException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceSize {

	public static final String PATTERN = "([0-9]+)[\\s]?([tTgG])?";

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
	 * Convert the given size into a new {@link DiskDeviceSize}.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to convert.
	 * 
	 * @return a {@link DiskDeviceSize}, which is equal to the given size.
	 * 
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given <tt>int</tt> is negative or zero.
	 */
	public static DiskDeviceSize parseInt(int iDiskDeviceSize)
			throws IllegalDiskDeviceSizeException {
		return new DiskDeviceSize(iDiskDeviceSize);
	}

	/**
	 * <p>
	 * Convert the given size into a new {@link DiskDeviceSize}.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to convert.
	 * 
	 * @return a {@link DiskDeviceSize}, which is equal to the given size.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDiskDeviceSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             <li>if the given <tt>String</tt> is negative or zero ;</li>
	 *             </ul>
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
	 * @throws IllegalDiskDeviceSizeException
	 *             if the given <tt>int</tt> is negative or zero.
	 */
	public DiskDeviceSize(int size) throws IllegalDiskDeviceSizeException {
		setSize(size);
	}

	/**
	 * <p>
	 * Create a new {@link DiskDeviceSize}, which have the given size.
	 * </p>
	 * 
	 * @param size
	 *            is the size, in Go, to assign to this object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDiskDeviceSizeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             <li>if the given <tt>String</tt> is negative or zero ;</li>
	 *             </ul>
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
	 * @return the size, in Go, of this object.
	 */
	public int getSize() {
		return _size;
	}

	private int setSize(int size) throws IllegalDiskDeviceSizeException {
		if (size <= 0) {
			throw new IllegalDiskDeviceSizeException(Msg.bind(
					Messages.DiskDeviceSizeEx_NEGATIVE_SIZE, size, PATTERN));
		}
		int previous = getSize();
		_size = size;
		return previous;
	}

	private int setSize(String size) throws IllegalDiskDeviceSizeException {
		if (size == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a String (a Disk Device Size)");
		}
		if (size.trim().length() == 0) {
			throw new IllegalDiskDeviceSizeException(Msg.bind(
					Messages.DiskDeviceSizeEx_EMPTY_SIZE, size));
		}

		Pattern p = Pattern.compile("^" + PATTERN + "$");
		Matcher matcher = p.matcher(size);
		if (!matcher.matches()) {
			throw new IllegalDiskDeviceSizeException(Msg.bind(
					Messages.DiskDeviceSizeEx_INVALID_SIZE, size, PATTERN));
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