package com.wat.melody.cloud.network.activation.winrm.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.cloud.network.activation.winrm.WinRmNetworkActivationDatas;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	@Override
	public WinRmNetworkActivationDatas load(Element instanceElmt)
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
		NetworkActivationProtocol ac = loadNetworkActivationProtocol(instanceElmt);
		if (ac != NetworkActivationProtocol.WINRM) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not WINRM ...");
		}
		boolean enable = loadNetworkActivationEnabled(instanceElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(instanceElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(instanceElmt);
		Host host = loadNetworkActivationHost(instanceElmt);
		Port port = loadNetworkActivationPort(instanceElmt);

		return new WinRmNetworkActivationDatas(enable, timeout, devname, host,
				port);
	}

}