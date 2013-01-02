package com.wat.melody.plugin.libvirt;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.Instance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.NetworkDeviceHelper;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.NetworkDevicesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.plugin.libvirt.common.AbstractLibVirtOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateNetworkDevices extends AbstractLibVirtOperation {

	private static Log log = LogFactory.getLog(UpdateNetworkDevices.class);

	/**
	 * The 'UpdateNetworkDevices' XML element
	 */
	public static final String UPDATE_NETWORK_DEVICES = "UpdateNetworkDevices";

	/**
	 * The 'networkDeviceNodeSelector' XML attribute
	 */
	/*
	 * TODO : remove this attribute. It conflicts with NetworkManagementHelper
	 * methods.
	 */
	public static final String NETWORK_DEVICES_NODE_SELECTOR_ATTR = NetworkManagementHelper.NETWORK_DEVICE_NODES_SELECTOR_ATTR;

	/**
	 * The 'detachTimeout' XML attribute
	 */
	public static final String DETACH_TIMEOUT_ATTR = "detachTimeout";

	/**
	 * The 'attachTimeout' XML attribute
	 */
	public static final String ATTACH_TIMEOUT_ATTR = "attachTimeout";

	private String msNetworkDeviceNodesSelector;
	private NetworkDeviceList maNetworkDeviceList;
	private long mlDetachTimeout;
	private long mlAttachTimeout;

	public UpdateNetworkDevices() {
		super();
		initNetworkDeviceList();
		setNetworkDeviceNodesSelector(NetworkManagementHelper.DEFAULT_NETOWRK_DEVICE_NODES_SELECTOR);
		try {
			setDetachTimeout(getTimeout());
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

	private void initNetworkDeviceList() {
		maNetworkDeviceList = null;
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Disk Nodes Selector found in the RD override Disk Nodes Selector
		// defined in the SD
		String sTargetSpecificNetworkDevicesSelector = NetworkManagementHelper
				.findNetworkDevicesSelector(getTargetNode());
		if (sTargetSpecificNetworkDevicesSelector != null) {
			setNetworkDeviceNodesSelector(sTargetSpecificNetworkDevicesSelector);
		}

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			NodeList nl = Doc.evaluateAsNodeList("."
					+ getNetworkDeviceNodesSelector(), getTargetNode());
			NetworkDevicesLoader ndl = new NetworkDevicesLoader(getContext());
			setNetworkDeviceList(ndl.load(nl));
		} catch (XPathExpressionException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateNetDevEx_INVALID_NETWORK_DEVICES_SELECTOR,
					getNetworkDeviceNodesSelector()), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			LibVirtException Ex = new LibVirtException(Messages.bind(
					Messages.UpdateNetDevMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Tools.getUserFriendlyStackTrace(new LibVirtException(
					Messages.UpdateNetDevMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		}

		NetworkDeviceList nds = getInstanceNetworkDevices(i);
		NetworkDeviceList disksToAdd = null;
		NetworkDeviceList disksToRemove = null;
		disksToAdd = NetworkDeviceHelper.computeNetworkDevicesToAdd(nds,
				getNetworkDeviceList());
		disksToRemove = NetworkDeviceHelper.computeNetworkDevicesToRemove(nds,
				getNetworkDeviceList());

		log.info(Messages.bind(Messages.UpdateNetDevMsg_NETWORK_DEVICES_RESUME,
				new Object[] { getInstanceID(), getNetworkDeviceList(),
						disksToAdd, disksToRemove, getTargetNodeLocation() }));

		detachNetworkDevices(i, disksToRemove, getDetachTimeout());
		attachNetworkDevices(i, disksToAdd, getAttachTimeout());

		setInstanceRelatedInfosToED(i);
	}

	private String getNetworkDeviceNodesSelector() {
		return msNetworkDeviceNodesSelector;
	}

	@Attribute(name = NETWORK_DEVICES_NODE_SELECTOR_ATTR)
	public String setNetworkDeviceNodesSelector(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getNetworkDeviceNodesSelector();
		msNetworkDeviceNodesSelector = v;
		return previous;
	}

	private NetworkDeviceList getNetworkDeviceList() {
		return maNetworkDeviceList;
	}

	private NetworkDeviceList setNetworkDeviceList(NetworkDeviceList fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid NetworkDeviceList.");
		}
		NetworkDeviceList previous = getNetworkDeviceList();
		maNetworkDeviceList = fwrs;
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