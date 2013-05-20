package com.wat.melody.cloud.network;

import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmManagementNetworkDatas extends ManagementNetworkDatas {

	public static Port DEFAULT_PORT = Port.WINRM;

	public static ManagementNetworkEnableTimeout DEFAULT_ENABLE_TIMEOUT = createEnableTimeout(240000);

	public WinRmManagementNetworkDatas(boolean enable,
			ManagementNetworkEnableTimeout enableTimeout,
			NetworkDeviceName netdev, Host host, Port port) {
		super(enable, enableTimeout, netdev, host, port);
	}

	@Override
	public String toString() {
		return "{ " + super.toString() + " }";
	}

	public ManagementNetworkMethod getManagementNetworkMethod() {
		return ManagementNetworkMethod.WINRM;
	}

}
