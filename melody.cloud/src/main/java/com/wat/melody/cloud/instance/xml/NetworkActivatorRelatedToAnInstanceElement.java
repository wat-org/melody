package com.wat.melody.cloud.instance.xml;

import org.w3c.dom.Element;

import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.NetworkActivatorConfigurationCallback;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

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
		setNetworkManagerFactoryConfigurationCallback(networkActivatorConfCallback);
		setInstanceElement(instanceElmt);
	}

	private NetworkActivatorConfigurationCallback getNetworkManagerFactoryConfigurationCallback() {
		return _networkActivatorConfCallback;
	}

	private NetworkActivatorConfigurationCallback setNetworkManagerFactoryConfigurationCallback(
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

	private void createAndSetNetworkManager() {
		setNetworkActivator(NetworkActivatorFactoryRelatedToAnInstanceElement
				.createNetworkActivator(
						getNetworkManagerFactoryConfigurationCallback(),
						getInstanceElement()));
	}

	@Override
	public NetworkActivationDatas getNetworkActivationDatas() {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		if (getNetworkActivator() != null) {
			return getNetworkActivator().getNetworkActivationDatas();
		} else {
			return null;
		}
	}

	@Override
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		if (getNetworkActivator() != null) {
			getNetworkActivator().enableNetworkActivation();
		}
	}

	@Override
	public void disableNetworkActivation() throws NetworkActivationException,
			InterruptedException {
		if (getNetworkActivator() == null) {
			createAndSetNetworkManager();
		}
		if (getNetworkActivator() != null) {
			getNetworkActivator().disableNetworkActivation();
		}
	}

}