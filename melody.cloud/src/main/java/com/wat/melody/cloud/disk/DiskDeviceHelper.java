package com.wat.melody.cloud.disk;

import com.wat.melody.cloud.disk.exception.DiskDeviceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DiskDeviceHelper {

	public static void ensureDiskDevicesUpdateIsPossible(
			DiskDeviceList current, DiskDeviceList target)
			throws DiskDeviceException {
		if (current == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		if (current.size() == 0) {
			throw new RuntimeException("Current Disk Device List is empty. It "
					+ "should at least contains one device, which is the root "
					+ "device. "
					+ "There must be a bug in the current disk list creation.");
		}
		if (target.size() == 0) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_EMPTY_DEVICE_LIST,
					DiskDevicesLoader.DEVICE_ATTR, current.getRootDevice()
							.getDiskDeviceName()));
		}
		if (target.getRootDevice() == null) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_UNDEF_ROOT_DEVICE,
					DiskDevicesLoader.ROOTDEVICE_ATTR, current.getRootDevice()
							.getDiskDeviceName()));
		}
		if (!current.getRootDevice().equals(target.getRootDevice())) {
			throw new DiskDeviceException(Messages.bind(
					Messages.DiskDefEx_INCORRECT_ROOT_DEVICE, new Object[] {
							DiskDevicesLoader.ROOTDEVICE_ATTR,
							target.getRootDevice().getDiskDeviceName(),
							current.getRootDevice().getDiskDeviceName() }));
		}
	}

	public static DiskDeviceList computeDiskDevicesToAdd(
			DiskDeviceList current, DiskDeviceList target) {
		DiskDeviceList disksToAdd = new DiskDeviceList(target);
		disksToAdd.removeAll(current);
		return disksToAdd;
	}

	public static DiskDeviceList computeDiskDevicesToRemove(
			DiskDeviceList current, DiskDeviceList target) {
		DiskDeviceList disksToRemove = new DiskDeviceList(current);
		disksToRemove.removeAll(target);
		return disksToRemove;
	}

}