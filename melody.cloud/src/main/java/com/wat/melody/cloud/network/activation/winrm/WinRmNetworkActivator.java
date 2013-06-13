package com.wat.melody.cloud.network.activation.winrm;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

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

	private WinRmNetworkActivationDatas _activationDatas;

	public WinRmNetworkActivator(WinRmNetworkActivationDatas datas) {
		setDatas(datas);
	}

	public WinRmNetworkActivationDatas getNetworkActivationDatas() {
		return _activationDatas;
	}

	private void setDatas(WinRmNetworkActivationDatas ad) {
		if (ad == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ WinRmNetworkActivationDatas.class.getCanonicalName()
					+ ".");
		}
		_activationDatas = ad;
	}

	@Override
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException {
		throw new NetworkActivationException(
				Messages.WinRmNetworkActivatorEx_NOT_SUPPORTED);
	}

	@Override
	public void disableNetworkActivation() throws NetworkActivationException {
		throw new NetworkActivationException(
				Messages.WinRmNetworkActivatorEx_NOT_SUPPORTED);
	}

}