package com.wat.melody.cloud.network.activation.winrm;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
import com.wat.melody.cloud.network.activation.ssh.SshNetworkActivationDatas;

/**
 * <p>
 * This implementation of the {@link NetworkActivator} will :
 * <ul>
 * <li>On enablement : ?? ;</li>
 * <li>On disablement : ?? ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class WinRmNetworkActivator implements NetworkActivator {

	private WinRmNetworkActivationDatas _managementDatas;

	public WinRmNetworkActivator(WinRmNetworkActivationDatas datas) {
		setManagementDatas(datas);
	}

	public WinRmNetworkActivationDatas getManagementDatas() {
		return _managementDatas;
	}

	private void setManagementDatas(WinRmNetworkActivationDatas mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshNetworkActivationDatas.class.getCanonicalName() + ".");
		}
		_managementDatas = mi;
	}

	@Override
	public void enableNetworkManagement() throws NetworkActivationException,
			InterruptedException {
		throw new NetworkActivationException(
				Messages.WinRmNetworkActivatorEx_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkManagement() throws NetworkActivationException {
		throw new NetworkActivationException(
				Messages.WinRmNetworkActivatorEx_NOT_SUPPORTED);
	}

}