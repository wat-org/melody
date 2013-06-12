package com.wat.melody.cloud.network.activation.winrm.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	public WinRmNetworkActivationDatas load(Element instanceElmt)
			throws NodeRelatedException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		NetworkActivationProtocol ac = loadMgmtMethod(mgmtElmt);
		if (ac != NetworkActivationProtocol.WINRM) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not WINRM ...");
		}
		boolean enable = loadNetworkActivationEnabled(mgmtElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(mgmtElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(
				instanceElmt, mgmtElmt);
		Host host = loadNetworkActivationHost(instanceElmt, mgmtElmt);
		Port port = loadNetworkActivationPort(mgmtElmt);

		return new WinRmNetworkActivationDatas(enable, timeout, devname, host,
				port);
	}
}