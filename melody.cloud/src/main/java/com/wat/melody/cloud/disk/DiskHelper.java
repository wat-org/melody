package com.wat.melody.cloud.disk;

import com.wat.melody.cloud.disk.exception.DiskException;

public abstract class DiskHelper {

	public static void ensureDiskUpdateIsPossible(DiskList current,
			DiskList target) throws DiskException {
		if (target.getRootDevice() == null) {
			throw new DiskException(Messages.bind(
					Messages.DiskDefEx_UNDEF_ROOT_DEVICE,
					DisksLoader.ROOTDEVICE_ATTR, current.getRootDevice()
							.getDevice()));
		}
		if (!current.getRootDevice().equals(target.getRootDevice())) {
			throw new DiskException(Messages.bind(
					Messages.DiskDefEx_INCORRECT_ROOT_DEVICE, new Object[] {
							DisksLoader.ROOTDEVICE_ATTR,
							target.getRootDevice().getDevice(),
							current.getRootDevice().getDevice() }));
		}
	}

	public static DiskList computeDiskToAdd(DiskList current, DiskList target) {
		DiskList disksToAdd = new DiskList(target);
		disksToAdd.removeAll(current);
		return disksToAdd;
	}

	public static DiskList computeDiskToRemove(DiskList current, DiskList target) {
		DiskList disksToRemove = new DiskList(current);
		disksToRemove.removeAll(target);
		return disksToRemove;
	}

}
