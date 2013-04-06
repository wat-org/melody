package com.wat.melody.cloud.network;

import com.wat.melody.cloud.network.exception.NetworkManagementException;

/**
 * <p>
 * This implementation of the {@link NetworkManager} will :
 * <ul>
 * <li>On enablement : ?? ;</li>
 * <li>On disablement : ?? ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmNetworkManager implements NetworkManager {

	private WinRmManagementNetworkDatas moManagementDatas;

	public WinRmNetworkManager(WinRmManagementNetworkDatas datas) {
		setManagementDatas(datas);
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
	public void enableNetworkManagement() throws NetworkManagementException,
			InterruptedException {
		throw new NetworkManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkManagement() throws NetworkManagementException {
		throw new NetworkManagementException(
				Messages.NetMgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

}