package com.wat.melody.cloud.network;

import java.util.ArrayList;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;

public class NetworkDeviceList extends ArrayList<NetworkDevice> {

	private static final long serialVersionUID = 985446445095495276L;

	public NetworkDeviceList() {
		super();
	}

	public NetworkDeviceList(NetworkDeviceList ndl) {
		super(ndl);
	}

	public boolean addNetworkDevice(NetworkDevice nd)
			throws IllegalNetworkDeviceListException {
		for (NetworkDevice d : this) {
			if (d.getDeviceName().equals(nd.getDeviceName())) {
				// Detects duplicated deviceName declaration
				throw new IllegalNetworkDeviceListException(Messages.bind(
						Messages.NetworkListEx_DEVICE_ALREADY_DEFINE,
						nd.getDeviceName()));
			}
		}
		return super.add(nd);
	}

}