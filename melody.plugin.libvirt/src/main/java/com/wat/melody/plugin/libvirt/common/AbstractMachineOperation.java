package com.wat.melody.plugin.libvirt.common;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.NetworkManagerFactory;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractMachineOperation extends AbstractLibVirtOperation {

	public AbstractMachineOperation() {
		super();
	}

	public void enableNetworkManagement() throws LibVirtException,
			InterruptedException {
		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(this,
					getTargetNode());
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
		try {
			getInstance().enableNetworkManagement(mh);
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_ENABLE_MANAGEMENT_ERROR,
					getTargetNodeLocation()), Ex);
		}
	}

	public void disableNetworkManagement() throws LibVirtException,
			InterruptedException {
		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(this,
					getTargetNode());
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
		try {
			getInstance().disableNetworkManagement(mh);
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_DISABLE_MANAGEMENT_ERROR,
					getTargetNodeLocation()), Ex);
		}
	}

}