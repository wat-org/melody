package com.wat.melody.cloud.network.activation.ssh;

import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkActivationDatas extends NetworkActivationDatas {

	public static Port DEFAULT_PORT = Port.SSH;

	public static NetworkActivationTimeout DEFAULT_ACTIVATION_TIMEOUT = createEnableTimeout(120000);

	public SshNetworkActivationDatas(boolean activationEnabled,
			NetworkActivationTimeout activationTimeout,
			NetworkDeviceName devname, Host host, Port port) {
		super(activationEnabled, activationTimeout, devname, host, port);
	}

	@Override
	public String toString() {
		return "{ " + super.toString() + " }";
	}

	public NetworkActivationProtocol getNetworkActivationProtocol() {
		return NetworkActivationProtocol.SSH;
	}

}