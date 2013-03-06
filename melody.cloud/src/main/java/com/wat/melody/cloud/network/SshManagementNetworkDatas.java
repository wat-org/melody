package com.wat.melody.cloud.network;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshManagementNetworkDatas extends ManagementNetworkDatas {

	public static Port DEFAULT_PORT = Port.SSH;

	public static ManagementNetworkEnableTimeout DEFAULT_ENABLE_TIMEOUT = createEnableTimeout(120000);

	public SshManagementNetworkDatas(boolean enable,
			ManagementNetworkEnableTimeout enableTimeout,
			NetworkDeviceName netdev, Host host, Port port) {
		super(enable, enableTimeout, netdev, host, port);
	}

	@Override
	public String toString() {
		return "{ " + super.toString() + " }";
	}

	public ManagementNetworkMethod getManagementNetworkMethod() {
		return ManagementNetworkMethod.SSH;
	}

}
