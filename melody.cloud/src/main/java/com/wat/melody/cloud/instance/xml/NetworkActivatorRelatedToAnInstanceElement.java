package com.wat.melody.cloud.instance.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.NetworkActivatorConfigurationCallback;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationDatasException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;

/**
 * <p>
 * Implementation of {@link NetworkActivator}, which, onces created, abstract
 * the related Instance {@link Element}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkActivatorRelatedToAnInstanceElement implements
		NetworkActivator {

	private Element _instanceElement;
	private NetworkActivatorConfigurationCallback _networkActivatorConfCallback;
	private NetworkActivator _networkActivator = null;

	public NetworkActivatorRelatedToAnInstanceElement(
			NetworkActivatorConfigurationCallback networkActivatorConfCallback,
			Element instanceElmt) {
		setNetworkActivatorConfigurationCallback(networkActivatorConfCallback);
		setInstanceElement(instanceElmt);
	}

	private NetworkActivatorConfigurationCallback getNetworkManagerFactoryConfigurationCallback() {
		return _networkActivatorConfCallback;
	}

	private NetworkActivatorConfigurationCallback setNetworkActivatorConfigurationCallback(
			NetworkActivatorConfigurationCallback confCallack) {
		if (confCallack == null) {
			throw new IllegalArgumentException(
					"null: Not accepted. Must be a valid "
							+ NetworkActivatorConfigurationCallback.class
									.getCanonicalName() + ".");
		}
		NetworkActivatorConfigurationCallback previous = getNetworkManagerFactoryConfigurationCallback();
		_networkActivatorConfCallback = confCallack;
		return previous;
	}

	private Element getInstanceElement() {
		return _instanceElement;
	}

	private Element setInstanceElement(Element rd) {
		if (rd == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getInstanceElement();
		_instanceElement = rd;
		return previous;
	}

	private NetworkActivator getNetworkActivator() {
		return _networkActivator;
	}

	private NetworkActivator setNetworkActivator(
			NetworkActivator networkActivator) {
		// can be null
		// When null, it is not possible to enable/disable Network Activation
		NetworkActivator previous = getNetworkActivator();
		_networkActivator = networkActivator;
		return previous;
	}

	private void createAndSetNetworkManager()
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException {
		setNetworkActivator(NetworkActivatorFactoryRelatedToAnInstanceElement
				.createNetworkActivator(
						getNetworkManagerFactoryConfigurationCallback(),
						getInstanceElement()));
	}

	@Override
	public NetworkActivationDatas getNetworkActivationDatas()
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException, NetworkActivationException {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		return getNetworkActivator().getNetworkActivationDatas();
	}

	@Override
	public void enableNetworkActivation()
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException, NetworkActivationException,
			InterruptedException {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		getNetworkActivator().enableNetworkActivation();
	}

	@Override
	public void disableNetworkActivation()
			throws NetworkActivationHostUndefined,
			IllegalNetworkActivationDatasException, NetworkActivationException,
			InterruptedException {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		getNetworkActivator().disableNetworkActivation();
	}

}