package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDeviceNamesLoader;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

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

	private NetworkDeviceNameList maNetworkDeviceList;
	private long mlDetachTimeout;
	private long mlAttachTimeout;

	public UpdateNetworkDevices() {
		super();
		initNetworkDeviceList();
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

	private void initNetworkDeviceList() {
		maNetworkDeviceList = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		// Build a NetworkDeviceList with Network Device Nodes found in the RD
		try {
			setNetworkDeviceList(new NetworkDeviceNamesLoader()
					.load(getTargetNode()));
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceNetworkDevicesAreUpToDate(
					getNetworkDeviceList(), getDetachTimeout(),
					getAttachTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
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
	public long setDetachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
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