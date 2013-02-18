package com.wat.melody.cloud.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceHelper {

	public static NetworkDeviceNameList computeNetworkDevicesToAdd(
			NetworkDeviceNameList current, NetworkDeviceNameList target) {
		NetworkDeviceNameList networkToAdd = new NetworkDeviceNameList(target);
		networkToAdd.removeAll(current);
		return networkToAdd;
	}

	public static NetworkDeviceNameList computeNetworkDevicesToRemove(
			NetworkDeviceNameList current, NetworkDeviceNameList target) {
		NetworkDeviceNameList networkToRemove = new NetworkDeviceNameList(current);
		networkToRemove.removeAll(target);
		return networkToRemove;
	}

}
