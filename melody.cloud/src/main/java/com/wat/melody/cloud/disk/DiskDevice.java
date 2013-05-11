package com.wat.melody.cloud.disk;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDevice {

	private static DiskDeviceSize DEFAULT_DISK_DEVICE_SIZE = DiskDeviceSize.SIZE_1G;
	private static boolean DEFAULT_IS_ROOT_DEVICE = false;
	private static boolean DEFAULT_DELETE_ON_TERMINATION = true;

	private DiskDeviceSize moDiskDeviceSize;
	private DiskDeviceName moDiskDeviceName;
	private boolean mbDeleteOnTermination;
	private boolean mbRootDevice;

	public DiskDevice(DiskDeviceName devname, DiskDeviceSize devsize,
			Boolean delOnTermination, Boolean isRootDevice) {
		setDiskDeviceName(devname);
		setDiskDeviceSize(devsize);
		setDeleteOnTermination(delOnTermination);
		setRootDevice(isRootDevice);
	}

	@Override
	public String toString() {
		return "{ "
				+ "device:"
				+ getDiskDeviceName()
				+ ", size:"
				+ getSize()
				+ " Go"
				+ (isRootDevice() == true ? ", rootDevice:true" : "")
				+ (isDeletedOnTermination() == false ? ", delete-on-termination:false "
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
					|| (getSize() == d.getSize() && getDiskDeviceName().equals(
							d.getDiskDeviceName()));
		}
		return false;
	}

	/**
	 * 
	 * @return the size, in Go, of this object.
	 */
	public int getSize() {
		return moDiskDeviceSize.getSize();
	}

	public DiskDeviceSize getDiskDeviceSize() {
		return moDiskDeviceSize;
	}

	public DiskDeviceSize setDiskDeviceSize(DiskDeviceSize devsize) {
		if (devsize == null) {
			devsize = DEFAULT_DISK_DEVICE_SIZE;
		}
		DiskDeviceSize previous = getDiskDeviceSize();
		moDiskDeviceSize = devsize;
		return previous;
	}

	public DiskDeviceName getDiskDeviceName() {
		return moDiskDeviceName;
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
	 * @param devname
	 *            is the disk device name to assign to this object.
	 * 
	 * @return the disk device name, before this operation.
	 */
	public DiskDeviceName setDiskDeviceName(DiskDeviceName devname) {
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + DiskDeviceName.class.getCanonicalName()
					+ ".");
		}
		DiskDeviceName previous = getDiskDeviceName();
		moDiskDeviceName = devname;
		return previous;
	}

	public boolean isDeletedOnTermination() {
		return mbDeleteOnTermination;
	}

	public boolean setDeleteOnTermination(Boolean deleteOnTermination) {
		if (deleteOnTermination == null) {
			deleteOnTermination = DEFAULT_DELETE_ON_TERMINATION;
		}
		boolean previous = isDeletedOnTermination();
		mbDeleteOnTermination = deleteOnTermination;
		return previous;
	}

	public boolean isRootDevice() {
		return mbRootDevice;
	}

	public boolean setRootDevice(Boolean isRootDevice) {
		if (isRootDevice == null) {
			isRootDevice = DEFAULT_IS_ROOT_DEVICE;
		}
		boolean previous = isRootDevice();
		mbRootDevice = isRootDevice;
		return previous;
	}

}
