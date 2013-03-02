package com.wat.cloud.libvirt;

import com.wat.melody.cloud.disk.DiskDeviceName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DiskDeviceNameConverter {

	/**
	 * <p>
	 * Converts the given {@link DiskDeviceName} to the libvirt name of the disk
	 * device.
	 * </p>
	 * 
	 * @param diskdev
	 *            is the {@link DiskDeviceName} to convert.
	 * 
	 * @return the libvirt name of the disk device.
	 */
	public static String convert(DiskDeviceName diskdev) {
		if (diskdev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceName.class.getCanonicalName() + ".");
		}
		return diskdev.getValue().replace("/dev/", "");
	}

}
