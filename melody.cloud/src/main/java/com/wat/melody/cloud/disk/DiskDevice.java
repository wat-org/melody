package com.wat.melody.cloud.disk;

import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDevice {

	private static GenericTimeout createTimeout(int timeout) {
		try {
			return GenericTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenericTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static DiskDeviceSize DEFAULT_DISK_DEVICE_SIZE = DiskDeviceSize.SIZE_1G;
	private static boolean DEFAULT_IS_ROOT_DEVICE = false;
	private static boolean DEFAULT_DELETE_ON_TERMINATION = true;
	private static GenericTimeout DEFAULT_TIMEOUT = createTimeout(90000);

	private DiskDeviceSize _size;
	private DiskDeviceName _name;
	private boolean _deleteOnTermination;
	private boolean _rootDevice;
	private GenericTimeout _createTimeout;
	private GenericTimeout _attachTimeout;
	private GenericTimeout _detachTimeout;

	/**
	 * @throws IllegalArgumentException
	 *             if the given disk device name is <tt>null</tt>.
	 */
	public DiskDevice(DiskDeviceName devname, DiskDeviceSize devsize,
			Boolean delOnTermination, Boolean isRootDevice,
			GenericTimeout createTimeout, GenericTimeout attachTimeout,
			GenericTimeout detachTimeout) {
		setDiskDeviceName(devname);
		setDiskDeviceSize(devsize);
		setDeleteOnTermination(delOnTermination);
		setRootDevice(isRootDevice);
		setCreateTimeout(createTimeout);
		setAttachTimeout(attachTimeout);
		setDetachTimeout(detachTimeout);
	}

	@Override
	public int hashCode() {
		return getDiskDeviceName().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("device-name:");
		str.append(getDiskDeviceName());
		str.append(", size:");
		str.append(getDiskDeviceSize());
		str.append(", root-device::");
		str.append(isRootDevice());
		str.append(", delete-on-termination:");
		str.append(isDeletedOnTermination());
		str.append(" }");
		return str.toString();
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

	public GenericTimeout getCreateTimeout() {
		return _createTimeout;
	}

	public GenericTimeout setCreateTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		GenericTimeout previous = getCreateTimeout();
		_createTimeout = timeout;
		return previous;
	}

	public GenericTimeout getAttachTimeout() {
		return _attachTimeout;
	}

	public GenericTimeout setAttachTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		GenericTimeout previous = getAttachTimeout();
		_attachTimeout = timeout;
		return previous;
	}

	public GenericTimeout getDetachTimeout() {
		return _detachTimeout;
	}

	public GenericTimeout setDetachTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		GenericTimeout previous = getDetachTimeout();
		_detachTimeout = timeout;
		return previous;
	}

}