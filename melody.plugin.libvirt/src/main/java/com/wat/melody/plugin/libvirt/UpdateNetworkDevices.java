package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDeviceNamesLoader;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateNetworkDevices extends AbstractOperation {

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

	private NetworkDeviceNameList maNetworkDeviceList = null;
	private long mlDetachTimeout;
	private long mlAttachTimeout;

	public UpdateNetworkDevices() {
		super();
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

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			setNetworkDeviceList(new NetworkDeviceNamesLoader()
					.load(getTargetNode()));
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceNetworkDevicesAreUpToDate(
					getNetworkDeviceList(), getDetachTimeout(),
					getAttachTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateNetDevEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
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