package com.wat.melody.cloud.network;

import java.util.ArrayList;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceNameListException;
import com.wat.melody.common.ex.Util;

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
			throws IllegalNetworkDeviceNameListException {
		for (NetworkDeviceName d : this) {
			if (d.getValue().equals(nd.getValue())) {
				// Detects duplicated deviceName declaration
				throw new IllegalNetworkDeviceNameListException(Messages.bind(
						Messages.NetworkDeviceNameListEx_DEVICE_ALREADY_DEFINE,
						nd.getValue()));
			}
		}
		return super.add(nd);
	}

	@Override
	public String toString() {
		String res = "";
		for (NetworkDeviceName rule : this) {
			res += Util.NEW_LINE + "network device:" + rule;
		}
		return res.length() == 0 ? Util.NEW_LINE + "no network devices" : res;
	}

}