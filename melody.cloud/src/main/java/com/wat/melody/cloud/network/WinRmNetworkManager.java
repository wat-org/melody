package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.ManagementException;

public class WinRmNetworkManager implements NetworkManager {

	private WinRmNetworkManagementDatas moManagementDatas;

	public WinRmNetworkManager(Node instanceNode)
			throws ResourcesDescriptorException {
		setManagementDatas(new WinRmNetworkManagementDatas(instanceNode));
	}

	public WinRmNetworkManagementDatas getManagementDatas() {
		return moManagementDatas;
	}

	private void setManagementDatas(WinRmNetworkManagementDatas mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshNetworkManagementDatas.class.getCanonicalName() + ".");
		}
		moManagementDatas = mi;
	}

	@Override
	public void enableNetworkManagement(long timeout)
			throws ManagementException, InterruptedException {
		throw new ManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkManagement() throws ManagementException {
		throw new ManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

}