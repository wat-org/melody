package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class NetworkManagerFactory {

	public static NetworkManager getManagementHelper(ITaskContext context,
			Node instanceNode) throws ResourcesDescriptorException {
		if (context == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		if (instanceNode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}

		NetworkManager mh = null;
		NetworkManagerInfos mi = new NetworkManagerInfos(instanceNode);
		NetworkManagementMethod mm = mi.getNetworkManagementMethod();
		switch (mm) {
		case SSH:
			mh = new SshNetworkManager();
			break;
		case WINRM:
			mh = new WinRmNetworkManager();
			break;
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "on an unknown management method '" + mm + "'. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}
		mh.setContext(context);
		mh.setManagementInfos(mi);
		return mh;
	}
}
