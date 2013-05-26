package com.wat.melody.cloud.network;

import org.w3c.dom.Element;

import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmManagementNetworkDatasLoader extends
		ManagementNetworkDatasLoader {

	public WinRmManagementNetworkDatas load(Element instanceNode)
			throws NodeRelatedException {
		log.debug(Messages.bind(Messages.NetMgmtMsg_INTRO,
				Doc.getNodeLocation(instanceNode).toFullString()));

		Element mgmtNode = NetworkManagementHelper
				.findNetworkManagementNode(instanceNode);

		ManagementNetworkMethod method = loadMgmtMethod(mgmtNode);
		if (method != ManagementNetworkMethod.WINRM) {
			throw new IllegalArgumentException(
					"The instance management network method is not WINRM ...");
		}
		boolean enable = loadMgmtEnable(mgmtNode);
		ManagementNetworkEnableTimeout enableTimeout = loadMgmtEnableTimeout(mgmtNode);
		NetworkDeviceName netdev = loadMgmtNetDev(instanceNode, mgmtNode);
		Host host = loadMgmtHost(instanceNode, mgmtNode);
		Port port = loadMgmtPort(mgmtNode);
		WinRmManagementNetworkDatas datas = new WinRmManagementNetworkDatas(
				enable, enableTimeout, netdev, host, port);

		log.debug(Messages.bind(Messages.NetMgmtMsg_RESUME, datas));

		return datas;
	}
}
