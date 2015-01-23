package com.wat.melody.cloud.network.activation.ssh.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	@Override
	public SshNetworkActivationDatas load(Element instanceElmt)
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
		NetworkActivationProtocol ac = loadNetworkActivationProtocol(instanceElmt);
		if (ac != NetworkActivationProtocol.SSH) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not SSH ...");
		}
		boolean enable = loadNetworkActivationEnabled(instanceElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(instanceElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(instanceElmt);
		Host host = loadNetworkActivationHost(instanceElmt);
		Port port = loadNetworkActivationPort(instanceElmt);

		return new SshNetworkActivationDatas(enable, timeout, devname, host,
				port);
	}

}