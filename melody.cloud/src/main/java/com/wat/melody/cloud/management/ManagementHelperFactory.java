package com.wat.melody.cloud.management;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class ManagementHelperFactory {

	public static ManagementHelper getManagementHelper(ITaskContext context,
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

		ManagementHelper mh = null;
		ManagementInfos mi = new ManagementInfos(context, instanceNode);
		ManagementMethod mm = mi.getManagementMethod();
		switch (mm) {
		case SSH:
			mh = new SshManagementHelper();
			break;
		case WINRM:
			mh = new WinRmManagementHelper();
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
