package com.wat.melody.plugin.libvirt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.cloud.libvirt.LibVirtInstance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDevicesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.plugin.libvirt.common.AbstractLibVirtOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateDiskDevices extends AbstractLibVirtOperation {

	private static Log log = LogFactory.getLog(UpdateDiskDevices.class);

	/**
	 * The 'UpdateDiskDevices' XML element
	 */
	public static final String UPDATE_DISK_DEVICES = "UpdateDiskDevices";

	/**
	 * The 'detachTimeout' XML attribute
	 */
	public static final String DETACH_TIMEOUT_ATTR = "detachTimeout";

	/**
	 * The 'createTimeout' XML attribute
	 */
	public static final String CREATE_TIMEOUT_ATTR = "createTimeout";

	/**
	 * The 'attachTimeout' XML attribute
	 */
	public static final String ATTACH_TIMEOUT_ATTR = "attachTimeout";

	private DiskDeviceList maDiskDeviceList;
	private long mlDetachTimeout;
	private long mlCreateTimeout;
	private long mlAttachTimeout;

	public UpdateDiskDevices() {
		super();
		initDiskDeviceList();
		try {
			setDetachTimeout(getTimeout());
			setCreateTimeout(getTimeout());
			setAttachTimeout(getTimeout());
		} catch (LibVirtException Ex) {
			throw new RuntimeException("Unexpected error while setting "
					+ "timeouts. "
					+ "Because this value comes from the parent class, such "
					+ "error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
	}

	private void initDiskDeviceList() {
		maDiskDeviceList = null;
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a DiskDeviceList with Disk Device Nodes found in the RD
		try {
			setDiskDeviceList(new DiskDevicesLoader().load(getTargetNode()));
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		LibVirtInstance i = getInstance();
		if (i == null) {
			LibVirtException Ex = new LibVirtException(Messages.bind(
					Messages.UpdateDiskDevMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new LibVirtException(
					Messages.UpdateDiskDevMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		try {
			i.updateDiskDevices(getDiskDeviceList(), getDetachTimeout(),
					getCreateTimeout(), getAttachTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateDiskDevEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
	}

	private DiskDeviceList getDiskDeviceList() {
		return maDiskDeviceList;
	}

	private DiskDeviceList setDiskDeviceList(DiskDeviceList dd) {
		if (dd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ DiskDeviceList.class.getCanonicalName() + ".");
		}
		DiskDeviceList previous = getDiskDeviceList();
		maDiskDeviceList = dd;
		return previous;
	}

	public long getDetachTimeout() {
		return mlDetachTimeout;
	}

	@Attribute(name = DETACH_TIMEOUT_ATTR)
	public long setDetachTimeout(long timeout) throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getDetachTimeout();
		mlDetachTimeout = timeout;
		return previous;
	}

	public long getCreateTimeout() {
		return mlCreateTimeout;
	}

	@Attribute(name = CREATE_TIMEOUT_ATTR)
	public long setCreateTimeout(long timeout) throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getCreateTimeout();
		mlCreateTimeout = timeout;
		return previous;
	}

	public long getAttachTimeout() {
		return mlAttachTimeout;
	}

	@Attribute(name = ATTACH_TIMEOUT_ATTR)
	public long setAttachTimeout(long timeout) throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getAttachTimeout();
		mlAttachTimeout = timeout;
		return previous;
	}

}