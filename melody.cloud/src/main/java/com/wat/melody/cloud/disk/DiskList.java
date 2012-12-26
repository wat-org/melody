package com.wat.melody.cloud.disk;

import java.util.ArrayList;

import com.wat.melody.cloud.disk.exception.IllegalDiskListException;

public class DiskList extends ArrayList<Disk> {

	private static final long serialVersionUID = 799928265740695276L;

	private Disk moRootDevice;

	public DiskList() {
		super();
		setRootDevice(null);
	}

	public DiskList(DiskList dl) {
		super(dl);
		setRootDevice(dl.getRootDevice());
	}

	public boolean addDisk(Disk disk) throws IllegalDiskListException {
		for (Disk d : this) {
			if (d.getDevice().equals(disk.getDevice())) {
				// Detects duplicated deviceName declaration
				throw new IllegalDiskListException(Messages.bind(
						Messages.DiskListEx_DEVICE_ALREADY_DEFINE,
						disk.getDevice()));
			}
		}
		if (disk.isRootDevice()) {
			if (getRootDevice() != null) {
				// Detects multiple RootDevice declaration
				throw new IllegalDiskListException(Messages.bind(
						Messages.DiskListEx_MULTIPLE_ROOT_DEVICE_DEFINE,
						disk.getDevice()));
			}
			setRootDevice(disk);
		}
		return super.add(disk);
	}

	public Disk getRootDevice() {
		return moRootDevice;
	}

	private Disk setRootDevice(Disk d) {
		if (d != null && !d.isRootDevice()) {
			throw new IllegalArgumentException("device " + d.getDevice()
					+ " cannot be the root device because it's RootDevice "
					+ "flag is false.");
		}
		Disk previous = getRootDevice();
		moRootDevice = d;
		return previous;
	}

}
