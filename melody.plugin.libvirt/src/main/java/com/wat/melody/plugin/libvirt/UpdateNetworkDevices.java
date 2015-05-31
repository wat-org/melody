package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.xml.NetworkDevicesLoader;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateNetworkDevices.UPDATE_NETWORK_DEVICES)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class UpdateNetworkDevices extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_NETWORK_DEVICES = "update-network-devices";

	private NetworkDeviceList _networkDeviceList = null;

	public UpdateNetworkDevices() {
		super();
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			setNetworkDeviceList(new NetworkDevicesLoader()
					.load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceNetworkDevicesAreUpToDate(
					getNetworkDeviceList());
		} catch (OperationException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.UpdateNetDevEx_GENERIC_FAIL,
					Ex));
		}
	}

	protected NetworkDeviceList getNetworkDeviceList() {
		return _networkDeviceList;
	}

	protected NetworkDeviceList setNetworkDeviceList(NetworkDeviceList fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkDeviceList.class.getCanonicalName() + ".");
		}
		NetworkDeviceList previous = getNetworkDeviceList();
		_networkDeviceList = fwrs;
		return previous;
	}

}