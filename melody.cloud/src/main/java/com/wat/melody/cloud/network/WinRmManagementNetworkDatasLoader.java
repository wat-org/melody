package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.xml.Doc;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmManagementNetworkDatasLoader extends
		ManagementNetworkDatasLoader {

	public WinRmManagementNetworkDatas load(Node instanceNode)
			throws ResourcesDescriptorException {
		log.debug(Messages.bind(Messages.NetMgmtMsg_INTRO,
				Doc.getNodeLocation(instanceNode).toFullString()));

		Node mgmtNode = NetworkManagementHelper
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
