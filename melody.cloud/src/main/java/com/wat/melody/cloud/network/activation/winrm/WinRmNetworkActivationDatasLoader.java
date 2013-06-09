package com.wat.melody.cloud.network.activation.winrm;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.NetworkDevicesHelper;
import com.wat.melody.cloud.network.activation.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.DocHelper;
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
		log.debug(Messages.bind(Messages.NetworkActivatorMsg_INTRO, DocHelper
				.getNodeLocation(instanceElmt).toFullString()));

		Element mgmtElmt = NetworkDevicesHelper
				.findNetworkManagementElement(instanceElmt);

		NetworkActivationProtocol method = loadMgmtMethod(mgmtElmt);
		if (method != NetworkActivationProtocol.WINRM) {
			throw new IllegalArgumentException(
					"The instance Network Activation Protocol is not WINRM ...");
		}
		boolean enable = loadNetworkActivationEnabled(mgmtElmt);
		NetworkActivationTimeout timeout = loadNetworkActivationTimeout(mgmtElmt);
		NetworkDeviceName devname = loadNetworkActivationNetDevName(
				instanceElmt, mgmtElmt);
		Host host = loadNetworkActivationHost(instanceElmt, mgmtElmt);
		Port port = loadNetworkActivationPort(mgmtElmt);
		WinRmNetworkActivationDatas datas = new WinRmNetworkActivationDatas(
				enable, timeout, devname, host, port);

		log.debug(Messages.bind(Messages.NetworkActivatorMsg_RESUME, datas));

		return datas;
	}
}