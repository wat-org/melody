package com.wat.melody.cloud.network;

import java.util.ArrayList;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceNameList extends ArrayList<NetworkDeviceName> {

	private static final long serialVersionUID = 985446445095495276L;

	public NetworkDeviceNameList() {
		super();
	}

	public NetworkDeviceNameList(NetworkDeviceNameList ndl) {
		super(ndl);
	}

	public boolean addNetworkDevice(NetworkDeviceName nd)
			throws IllegalNetworkDeviceListException {
		for (NetworkDeviceName d : this) {
			if (d.getValue().equals(nd.getValue())) {
				// Detects duplicated deviceName declaration
				throw new IllegalNetworkDeviceListException(Messages.bind(
						Messages.NetworkListEx_DEVICE_ALREADY_DEFINE,
						nd.getValue()));
			}
		}
		return super.add(nd);
	}

}