package com.wat.melody.plugin.aws.ec2;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.DiskDeviceHelper;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.DiskDevicesLoader;
import com.wat.melody.cloud.disk.DiskManagementHelper;
import com.wat.melody.cloud.disk.exception.DiskDeviceException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.plugin.aws.ec2.common.AbstractAwsOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateDiskDevices extends AbstractAwsOperation {

	private static Log log = LogFactory.getLog(UpdateDiskDevices.class);

	/**
	 * The 'UpdateDiskDevices' XML element
	 */
	public static final String UPDATE_DISK_DEVICES = "UpdateDiskDevices";

	/**
	 * The 'diskDeviceNodesSelector' XML attribute
	 */
	public static final String DISK_DEVICE_NODES_SELECTOR_ATTR = DiskManagementHelper.DISK_DEVICE_NODES_SELECTOR_ATTR;

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

	private String msDiskDeviceNodesSelector;
	private DiskDeviceList maDiskDeviceList;
	private long mlDetachTimeout;
	private long mlCreateTimeout;
	private long mlAttachTimeout;

	public UpdateDiskDevices() {
		super();
		initDiskDeviceList();
		setDiskDeviceNodesSelector(DiskManagementHelper.DEFAULT_DISK_DEVICES_NODE_SELECTOR);
		try {
			setDetachTimeout(getTimeout());
			setCreateTimeout(getTimeout());
			setAttachTimeout(getTimeout());
		} catch (AwsException Ex) {
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
	public void validate() throws AwsException {
		super.validate();

		// Disk Device Nodes Selector found in the RD override Disk Device Nodes
		// Selector defined in the SD
		try {
			String sTargetSpecificDiskDevicesSelector = DiskManagementHelper
					.findDiskDevicesSelector(getTargetNode());
			if (sTargetSpecificDiskDevicesSelector == null) {
				setDiskDeviceNodesSelector(sTargetSpecificDiskDevicesSelector);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}

		// Build a DiskDeviceList with Disk Device Nodes found in the RD
		try {
			NodeList nl = XPathHelper.getHeritedContent(getTargetNode(),
					getDiskDeviceNodesSelector());
			DiskDevicesLoader dl = new DiskDevicesLoader(getContext());
			setDiskDeviceList(dl.load(nl));
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskDevEx_INVALID_DISK_DEVICES_SELECTOR,
					getDiskDeviceNodesSelector()), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.UpdateDiskDevMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Tools.getUserFriendlyStackTrace(new AwsException(
					Messages.UpdateDiskDevMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		DiskDeviceList iDisks = getInstanceDiskDevices(i);
		try {
			DiskDeviceHelper.ensureDiskDevicesUpdateIsPossible(iDisks,
					getDiskDeviceList());
		} catch (DiskDeviceException Ex) {
			throw new AwsException("[" + getTargetNodeLocation()
					+ "] Disk device update is not possible till following "
					+ "errors are not corrected. ", Ex);
		}

		DiskDeviceList disksToAdd = null;
		DiskDeviceList disksToRemove = null;
		disksToAdd = DiskDeviceHelper.computeDiskDevicesToAdd(iDisks,
				getDiskDeviceList());
		disksToRemove = DiskDeviceHelper.computeDiskDevicesToRemove(iDisks,
				getDiskDeviceList());

		log.info(Messages.bind(Messages.UpdateDiskDevMsg_DISK_DEVICES_RESUME,
				new Object[] { getAwsInstanceID(), getDiskDeviceList(),
						disksToAdd, disksToRemove, getTargetNodeLocation() }));

		detachAndDeleteDiskDevices(i, disksToRemove, getDetachTimeout());
		createAndAttachDiskDevices(i, disksToAdd, getCreateTimeout(),
				getAttachTimeout());

		updateDeleteOnTerminationFlag(i, getDiskDeviceList());
	}

	private String getDiskDeviceNodesSelector() {
		return msDiskDeviceNodesSelector;
	}

	@Attribute(name = DISK_DEVICE_NODES_SELECTOR_ATTR)
	public String setDiskDeviceNodesSelector(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		String previous = getDiskDeviceNodesSelector();
		msDiskDeviceNodesSelector = v;
		return previous;
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
	public long setDetachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
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
	public long setCreateTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
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
	public long setAttachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getAttachTimeout();
		mlAttachTimeout = timeout;
		return previous;
	}

}