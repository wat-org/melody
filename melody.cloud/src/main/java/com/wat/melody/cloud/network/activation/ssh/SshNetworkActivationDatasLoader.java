package com.wat.melody.cloud.network.activation.ssh;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.NetworkDevicesHelper;
import com.wat.melody.cloud.network.activation.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.NetworkActivationProtocol;
import com.wat.melody.cloud.network.activation.NetworkActivationTimeout;
import com.wat.melody.common.ex.MelodyException;
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
public class SshNetworkActivationDatasLoader extends
		NetworkActivationDatasLoader {

	public SshNetworkActivationDatas load(Element instanceElmt)
			throws NodeRelatedException {
		log.debug(Messages.bind(Messages.NetworkActivatorMsg_INTRO, DocHelper
				.getNodeLocation(instanceElmt).toFullString()));
		try {
			Element mgmtElmt = NetworkDevicesHelper
					.findNetworkManagementElement(instanceElmt);

			NetworkActivationProtocol method = loadMgmtMethod(mgmtElmt);
			if (method != NetworkActivationProtocol.SSH) {
				throw new IllegalArgumentException(
						"The instance Network Activation Protocol is not SSH ...");
			}
			boolean enable = loadNetworkActivationEnabled(mgmtElmt);
			NetworkActivationTimeout timeout = loadNetworkActivationTimeout(mgmtElmt);
			NetworkDeviceName devname = loadNetworkActivationNetDevName(
					instanceElmt, mgmtElmt);
			Host host = loadNetworkActivationHost(instanceElmt, mgmtElmt);
			Port port = loadNetworkActivationPort(mgmtElmt);

			SshNetworkActivationDatas datas = new SshNetworkActivationDatas(
					enable, timeout, devname, host, port);

			log.debug(Messages.bind(Messages.NetworkActivatorMsg_RESUME, datas));

			return datas;
		} catch (NodeRelatedException Ex) {
			log.debug(new MelodyException(Messages.bind(
					Messages.NetworkActivatorMsg_FAILED, DocHelper
							.getNodeLocation(instanceElmt).toFullString()), Ex)
					.toString());
			throw Ex;
		}
	}

}