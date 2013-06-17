package com.wat.melody.cloud.network.activation.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkActivationHostUndefined extends
		IllegalNetworkActivationDatasException {

	private static final long serialVersionUID = -3879879879867654657L;

	public NetworkActivationHostUndefined(String msg) {
		super(msg);
	}

	public NetworkActivationHostUndefined(Throwable cause) {
		super(cause);
	}

	public NetworkActivationHostUndefined(String msg, Throwable cause) {
		super(msg, cause);
	}

}