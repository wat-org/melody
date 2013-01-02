package com.wat.melody.cloud.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceHelper {

	public static NetworkDeviceList computeNetworkDevicesToAdd(
			NetworkDeviceList current, NetworkDeviceList target) {
		NetworkDeviceList networkToAdd = new NetworkDeviceList(target);
		networkToAdd.removeAll(current);
		return networkToAdd;
	}

	public static NetworkDeviceList computeNetworkDevicesToRemove(
			NetworkDeviceList current, NetworkDeviceList target) {
		NetworkDeviceList networkToRemove = new NetworkDeviceList(current);
		networkToRemove.removeAll(target);
		return networkToRemove;
	}

}
