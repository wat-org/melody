package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDevicesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateDiskDevices.UPDATE_DISK_DEVICES)
public class UpdateDiskDevices extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_DISK_DEVICES = "update-disk-devices";

	private DiskDeviceList _diskDeviceList = null;

	public UpdateDiskDevices() {
		super();
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a DiskDeviceList with Disk Device Nodes found in the RD
		try {
			setDiskDeviceList(new DiskDevicesLoader().load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceDiskDevicesAreUpToDate(
					getDiskDeviceList());
		} catch (OperationException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.UpdateDiskDevEx_GENERIC_FAIL,
					Ex));
		}
	}

	protected DiskDeviceList getDiskDeviceList() {
		return _diskDeviceList;
	}

	protected DiskDeviceList setDiskDeviceList(DiskDeviceList dd) {
		if (dd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		DiskDeviceList previous = getDiskDeviceList();
		_diskDeviceList = dd;
		return previous;
	}

}