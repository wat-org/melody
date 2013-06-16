package com.wat.melody.cloud.disk;

import java.util.ArrayList;

import com.wat.melody.cloud.disk.exception.DiskDeviceException;
import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;
import com.wat.melody.cloud.disk.xml.DiskDevicesLoader;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceList extends ArrayList<DiskDevice> {

	private static final long serialVersionUID = 799928265740695276L;

	private DiskDevice _rootDevice;

	public DiskDeviceList() {
		super();
		setRootDevice(null);
	}

	public DiskDeviceList(DiskDeviceList ddl) {
		super(ddl);
		setRootDevice(ddl.getRootDevice());
	}

	public boolean addDiskDevice(DiskDevice dd)
			throws IllegalDiskDeviceListException {
		for (DiskDevice d : this) {
			if (d.getDiskDeviceName().equals(dd.getDiskDeviceName())) {
				// Detects duplicated deviceName declaration
				throw new IllegalDiskDeviceListException(Msg.bind(
						Messages.DiskListEx_DEVICE_ALREADY_DEFINE,
						dd.getDiskDeviceName()));
			}
		}
		if (dd.isRootDevice()) {
			if (getRootDevice() != null) {
				// Detects multiple RootDevice declaration
				throw new IllegalDiskDeviceListException(Msg.bind(
						Messages.DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE,
						dd.getDiskDeviceName()));
			}
			setRootDevice(dd);
		}
		return super.add(dd);
	}

	public DiskDevice getRootDevice() {
		return _rootDevice;
	}

	private DiskDevice setRootDevice(DiskDevice dd) {
		if (dd != null && !dd.isRootDevice()) {
			throw new IllegalArgumentException("device "
					+ dd.getDiskDeviceName()
					+ " cannot be the root device because it's RootDevice "
					+ "flag is false.");
		}
		DiskDevice previous = getRootDevice();
		_rootDevice = dd;
		return previous;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("");
		for (DiskDevice rule : this) {
			str.append(SysTool.NEW_LINE + "disk device:" + rule);
		}
		return str.length() == 0 ? SysTool.NEW_LINE + "no disk devices" : str
				.toString();

	}

	/**
	 * @param target
	 *            is a disk device list.
	 * 
	 * @return a {@link DiskDeviceList}, which contains all {@link DiskDevice}
	 *         which are in the given target list and not in this object.
	 */
	public DiskDeviceList delta(DiskDeviceList target) {
		DiskDeviceList delta = new DiskDeviceList(target);
		delta.removeAll(this);
		return delta;
	}

	/**
	 * <p>
	 * Will throw error if this object is not compatible with the given target
	 * list.
	 * </p>
	 * 
	 * @param target
	 *            is a disk device list.
	 * 
	 * @throws RuntimeException
	 *             <ul>
	 *             <li>if this object is an empty list ;</li>
	 *             <li>if this object has no root device ;</li>
	 *             </ul>
	 * @throws DiskDeviceException
	 *             <ul>
	 *             <li>if the given list is not a valid {@link DiskDeviceList}
	 *             (empty list, no root device) ;</li>
	 *             <li>if this object's root device is not the same than the
	 *             given list's root device ;</li>
	 *             </ul>
	 */
	public void isCompatible(DiskDeviceList target) throws DiskDeviceException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (size() == 0) {
			throw new RuntimeException("Disk Device List is empty. It should "
					+ "at least contains a root device.");
		}
		if (getRootDevice() == null) {
			throw new RuntimeException("Disk Device List doesn't contains a "
					+ "root device.");
		}
		if (target.size() == 0) {
			throw new DiskDeviceException(Msg.bind(
					Messages.DiskDefEx_EMPTY_DEVICE_LIST,
					DiskDevicesLoader.DEVICE_NAME_ATTR, getRootDevice()
							.getDiskDeviceName()));
		}
		if (target.getRootDevice() == null) {
			throw new DiskDeviceException(Msg.bind(
					Messages.DiskDefEx_UNDEF_ROOT_DEVICE,
					DiskDevicesLoader.ROOTDEVICE_ATTR, getRootDevice()
							.getDiskDeviceName()));
		}
		if (!getRootDevice().equals(target.getRootDevice())) {
			throw new DiskDeviceException(Msg.bind(
					Messages.DiskDefEx_INCORRECT_ROOT_DEVICE,
					DiskDevicesLoader.ROOTDEVICE_ATTR, target.getRootDevice()
							.getDiskDeviceName(), getRootDevice()
							.getDiskDeviceName()));
		}
	}

}