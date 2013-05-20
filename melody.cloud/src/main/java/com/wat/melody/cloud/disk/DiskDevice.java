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

	private DiskDeviceSize _size;
	private DiskDeviceName _name;
	private boolean _deleteOnTermination;
	private boolean _rootDevice;

	/**
	 * @throws IllegalArgumentException
	 *             if the given disk device name is <tt>null</tt>.
	 */
	public DiskDevice(DiskDeviceName devname, DiskDeviceSize devsize,
			Boolean delOnTermination, Boolean isRootDevice) {
		setDiskDeviceName(devname);
		setDiskDeviceSize(devsize);
		setDeleteOnTermination(delOnTermination);
		setRootDevice(isRootDevice);
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "{ "
				+ "device:"
				+ getDiskDeviceName()
				+ ", size:"
				+ getSize()
				+ " Go"
				+ (isRootDevice() == true ? ", root-device:true" : "")
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
			if (isRootDevice()) {
				return d.isRootDevice()
						&& getDiskDeviceName().equals(d.getDiskDeviceName());
			}
			return !d.isRootDevice()
					&& getDiskDeviceName().equals(d.getDiskDeviceName())
					&& getSize() == d.getSize();
		}
		return false;
	}

	/**
	 * 
	 * @return the size, in Go, of this object.
	 */
	public int getSize() {
		return _size.getSize();
	}

	public DiskDeviceSize getDiskDeviceSize() {
		return _size;
	}

	public DiskDeviceSize setDiskDeviceSize(DiskDeviceSize devsize) {
		if (devsize == null) {
			devsize = DEFAULT_DISK_DEVICE_SIZE;
		}
		DiskDeviceSize previous = getDiskDeviceSize();
		_size = devsize;
		return previous;
	}

	public DiskDeviceName getDiskDeviceName() {
		return _name;
	}

	/**
	 * <p>
	 * Set the disk device name of this object.
	 * </p>
	 * 
	 * @param devname
	 *            is the disk device name to assign to this object.
	 * 
	 * @return the disk device name, before this operation.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given disk device name is <tt>null</tt>.
	 */
	public DiskDeviceName setDiskDeviceName(DiskDeviceName devname) {
		if (devname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a " + DiskDeviceName.class.getCanonicalName()
					+ ".");
		}
		DiskDeviceName previous = getDiskDeviceName();
		_name = devname;
		return previous;
	}

	public boolean isDeletedOnTermination() {
		return _deleteOnTermination;
	}

	public boolean setDeleteOnTermination(Boolean deleteOnTermination) {
		if (deleteOnTermination == null) {
			deleteOnTermination = DEFAULT_DELETE_ON_TERMINATION;
		}
		boolean previous = isDeletedOnTermination();
		_deleteOnTermination = deleteOnTermination;
		return previous;
	}

	public boolean isRootDevice() {
		return _rootDevice;
	}

	public boolean setRootDevice(Boolean isRootDevice) {
		if (isRootDevice == null) {
			isRootDevice = DEFAULT_IS_ROOT_DEVICE;
		}
		boolean previous = isRootDevice();
		_rootDevice = isRootDevice;
		return previous;
	}

}
