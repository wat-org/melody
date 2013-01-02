package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.NetworkManagementException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmNetworkManager implements NetworkManager {

	private WinRmManagementNetworkDatas moManagementDatas;

	public WinRmNetworkManager(Node instanceNode)
			throws ResourcesDescriptorException {
		setManagementDatas(new WinRmManagementNetworkDatas(instanceNode));
	}

	public WinRmManagementNetworkDatas getManagementDatas() {
		return moManagementDatas;
	}

	private void setManagementDatas(WinRmManagementNetworkDatas mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshManagementNetworkDatas.class.getCanonicalName() + ".");
		}
		moManagementDatas = mi;
	}

	@Override
	public void enableNetworkManagement(long timeout)
			throws NetworkManagementException, InterruptedException {
		throw new NetworkManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkManagement() throws NetworkManagementException {
		throw new NetworkManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

}