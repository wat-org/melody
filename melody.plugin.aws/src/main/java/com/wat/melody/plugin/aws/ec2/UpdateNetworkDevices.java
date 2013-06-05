package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDeviceNamesLoader;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateNetworkDevices.UPDATE_NETWORK_DEVICES)
public class UpdateNetworkDevices extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_NETWORK_DEVICES = "update-network-devices";

	/**
	 * Task's attribute, which specifies the timeout of the detachment
	 * operation.
	 */
	public static final String DETACH_TIMEOUT_ATTR = "detach-timeout";

	/**
	 * Task's attribute, which specifies the timeout of the attachment
	 * operation.
	 */
	public static final String ATTACH_TIMEOUT_ATTR = "attach-timeout";

	private NetworkDeviceNameList _networkDeviceList = null;
	private long _detachTimeout;
	private long _attachTimeout;

	public UpdateNetworkDevices() {
		super();
		try {
			setDetachTimeout(getTimeout());
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

	@Override
	public void validate() throws AwsException {
		super.validate();

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			setNetworkDeviceList(new NetworkDeviceNamesLoader()
					.load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceNetworkDevicesAreUpToDate(
					getNetworkDeviceList(), getDetachTimeout(),
					getAttachTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateNetDevEx_GENERIC_FAIL,
					getTargetElementLocation()), Ex);
		}
	}

	private NetworkDeviceNameList getNetworkDeviceList() {
		return _networkDeviceList;
	}

	private NetworkDeviceNameList setNetworkDeviceList(
			NetworkDeviceNameList fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid NetworkDeviceList.");
		}
		NetworkDeviceNameList previous = getNetworkDeviceList();
		_networkDeviceList = fwrs;
		return previous;
	}

	public long getDetachTimeout() {
		return _detachTimeout;
	}

	@Attribute(name = DETACH_TIMEOUT_ATTR)
	public long setDetachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getDetachTimeout();
		_detachTimeout = timeout;
		return previous;
	}

	public long getAttachTimeout() {
		return _attachTimeout;
	}

	@Attribute(name = ATTACH_TIMEOUT_ATTR)
	public long setAttachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getAttachTimeout();
		_attachTimeout = timeout;
		return previous;
	}

}