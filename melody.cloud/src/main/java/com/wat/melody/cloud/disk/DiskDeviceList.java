package com.wat.melody.cloud.disk;

import java.util.ArrayList;

import com.wat.melody.cloud.disk.exception.IllegalDiskDeviceListException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceList extends ArrayList<DiskDevice> {

	private static final long serialVersionUID = 799928265740695276L;

	private DiskDevice moRootDevice;

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
				throw new IllegalDiskDeviceListException(Messages.bind(
						Messages.DiskListEx_DEVICE_ALREADY_DEFINE,
						dd.getDiskDeviceName()));
			}
		}
		if (dd.isRootDevice()) {
			if (getRootDevice() != null) {
				// Detects multiple RootDevice declaration
				throw new IllegalDiskDeviceListException(Messages.bind(
						Messages.DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE,
						dd.getDiskDeviceName()));
			}
			setRootDevice(dd);
		}
		return super.add(dd);
	}

	public DiskDevice getRootDevice() {
		return moRootDevice;
	}

	private DiskDevice setRootDevice(DiskDevice dd) {
		if (dd != null && !dd.isRootDevice()) {
			throw new IllegalArgumentException("device "
					+ dd.getDiskDeviceName()
					+ " cannot be the root device because it's RootDevice "
					+ "flag is false.");
		}
		DiskDevice previous = getRootDevice();
		moRootDevice = dd;
		return previous;
	}

}
