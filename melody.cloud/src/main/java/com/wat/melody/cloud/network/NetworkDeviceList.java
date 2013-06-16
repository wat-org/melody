package com.wat.melody.cloud.network;

import java.util.ArrayList;

import com.wat.melody.cloud.network.exception.IllegalNetworkDeviceListException;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
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
		NetworkDeviceName devname = nd.getNetworkDeviceName();
		for (NetworkDevice netdev : this) {
			if (devname.equals(netdev.getNetworkDeviceName())) {
				// Detects duplicated NetworkDeviceName declaration
				throw new IllegalNetworkDeviceListException(Msg.bind(
						Messages.NetworkDeviceListEx_DEVICE_ALREADY_DEFINE,
						devname));
			}
		}
		return super.add(nd);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("");
		for (NetworkDevice netdev : this) {
			str.append(SysTool.NEW_LINE + "network device:" + netdev);
		}
		return str.length() == 0 ? SysTool.NEW_LINE + "no network devices"
				: str.toString();
	}

	/**
	 * @param target
	 *            is a network device list.
	 * 
	 * @return a {@link NetworkDeviceList}, which contains all
	 *         {@link NetworkDevice} which are in the given target
	 *         {@link NetworkDeviceList} and not in this object.
	 */
	public NetworkDeviceList delta(NetworkDeviceList target) {
		NetworkDeviceList delta = new NetworkDeviceList(target);
		delta.removeAll(this);
		return delta;
	}

}