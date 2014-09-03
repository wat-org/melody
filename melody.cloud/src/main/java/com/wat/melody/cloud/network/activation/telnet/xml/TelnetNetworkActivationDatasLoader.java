package com.wat.melody.cloud.network.activation.telnet.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.cloud.network.activation.telnet.TelnetNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	@Override
	public TelnetNetworkActivationDatas load(Element instanceElmt)
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);
		NetworkActivationProtocol ac = loadNetworkActivationProtocol(mgmtElmt);
		if (ac != NetworkActivationProtocol.TELNET) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not TELNET ...");
		}
		boolean enable = loadNetworkActivationEnabled(mgmtElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(mgmtElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(
				instanceElmt, mgmtElmt);
		Host host = loadNetworkActivationHost(instanceElmt, mgmtElmt);
		Port port = loadNetworkActivationPort(mgmtElmt);

		return new TelnetNetworkActivationDatas(enable, timeout, devname, host,
				port);
	}

}