package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.network.exception.NetworkManagementException;

/**
 * <p>
 * This implementation of the {@link NetworkManager} don't do anything.
 * </p>
 * <p>
 * It is called when Specialized Network Manager cannot be created.
 * </p>
 * 
 * @author gcornet
 * 
 */
public class NullNetworkManager implements NetworkManager {

	private static Log log = LogFactory.getLog(NullNetworkManager.class);

	@Override
	public ManagementNetworkDatas getManagementDatas() {
		return null;
	}

	@Override
	public void enableNetworkManagement() throws NetworkManagementException,
			InterruptedException {
		log.debug(Messages.NullNetMgrMsg_ENABLEMENT);
	}

	@Override
	public void disableNetworkManagement() throws NetworkManagementException {
		log.debug(Messages.NullNetMgrMsg_DISABLEMENT);
	}

}
