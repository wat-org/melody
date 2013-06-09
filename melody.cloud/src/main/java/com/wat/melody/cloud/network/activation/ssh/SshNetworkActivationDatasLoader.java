package com.wat.melody.cloud.network.activation.ssh;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.NetworkDevicesHelper;
import com.wat.melody.cloud.network.activation.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	public SshNetworkActivationDatas load(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		NetworkActivationProtocol ac = loadMgmtMethod(mgmtElmt);
		if (ac != NetworkActivationProtocol.SSH) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not SSH ...");
		}
		boolean enable = loadNetworkActivationEnabled(mgmtElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(mgmtElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(
				instanceElmt, mgmtElmt);
		Host host = loadNetworkActivationHost(instanceElmt, mgmtElmt);
		Port port = loadNetworkActivationPort(mgmtElmt);

		return new SshNetworkActivationDatas(enable, timeout, devname, host,
				port);
	}

}