package com.wat.melody.plugin.libvirt;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.LibVirtInstance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDevicesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.xml.Doc;
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
	 * The 'detachTimeout' XML attribute
	 */
	public static final String DETACH_TIMEOUT_ATTR = "detachTimeout";

	/**
	 * The 'attachTimeout' XML attribute
	 */
	public static final String ATTACH_TIMEOUT_ATTR = "attachTimeout";

	private NetworkDeviceNameList maNetworkDeviceList;
	private long mlDetachTimeout;
	private long mlAttachTimeout;

	public UpdateNetworkDevices() {
		super();
		initNetworkDeviceList();
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

		// Find Disk Nodes Selector in the RD
		String networkDevicesSelector = NetworkManagementHelper
				.findNetworkDevicesSelector(getTargetNode());

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			NodeList nl = Doc.evaluateAsNodeList("." + networkDevicesSelector,
					getTargetNode());
			NetworkDevicesLoader ndl = new NetworkDevicesLoader(getContext());
			setNetworkDeviceList(ndl.load(nl));
		} catch (XPathExpressionException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateNetDevEx_INVALID_NETWORK_DEVICES_SELECTOR,
					networkDevicesSelector), Ex);
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
					Messages.UpdateNetDevMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new LibVirtException(
					Messages.UpdateNetDevMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		}

		try {
			i.updateNetworkDevices(getNetworkDeviceList(), getDetachTimeout(),
					getAttachTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateNetDevEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
		setInstanceRelatedInfosToED(i);
	}

	private NetworkDeviceNameList getNetworkDeviceList() {
		return maNetworkDeviceList;
	}

	private NetworkDeviceNameList setNetworkDeviceList(
			NetworkDeviceNameList fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid NetworkDeviceList.");
		}
		NetworkDeviceNameList previous = getNetworkDeviceList();
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