package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.exception.ManagementException;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public class WinRmNetworkManager implements NetworkManager {

	private WinRmNetworkManagementDatas moManagementDatas;
	private ITaskContext moContext;

	public WinRmNetworkManager(Node instanceNode, ITaskContext context)
			throws ResourcesDescriptorException {
		setContext(context);
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

	public ITaskContext getContext() {
		return moContext;
	}

	private void setContext(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moContext = tc;
	}

	@Override
	public void enableNetworkManagement(long timeout) throws ManagementException,
			InterruptedException {
		throw new ManagementException(Messages.MgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkManagement() throws ManagementException {
		throw new ManagementException(Messages.MgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

}
