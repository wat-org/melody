package com.wat.melody.cloud.network;

import org.w3c.dom.Node;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.exception.ManagementException;
import com.wat.melody.plugin.ssh.common.Configuration;
import com.wat.melody.plugin.ssh.common.exception.ConfigurationException;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public class SshNetworkManager implements NetworkManager {

	private SshNetworkManagementDatas moManagementDatas;
	private ITaskContext moContext;

	public SshNetworkManager(Node instanceNode, ITaskContext context)
			throws ResourcesDescriptorException {
		setContext(context);
		setManagementDatas(new SshNetworkManagementDatas(instanceNode));
	}

	public SshNetworkManagementDatas getManagementDatas() {
		return moManagementDatas;
	}

	public void setManagementDatas(SshNetworkManagementDatas mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshNetworkManagementDatas.class.getCanonicalName() + ".");
		}
		moManagementDatas = mi;
	}

	public ITaskContext getContext() {
		return moContext;
	}

	private void setContext(ITaskContext tc) {
		if (tc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moContext = tc;
	}

	@Override
	public void enableNetworkManagement(long timeout) throws ManagementException,
			InterruptedException {
		Configuration sshPlugInConf = null;
		try {
			sshPlugInConf = Configuration.get(getContext()
					.getProcessorManager());
		} catch (ConfigurationException Ex) {
			throw new ManagementException(Ex);
		}
		disableManagement(sshPlugInConf);
		boolean result = false;
		try {
			result = sshPlugInConf.addKnownHostsHost(getContext(),
					getManagementDatas().getHost(), getManagementDatas()
							.getPort(), timeout);
		} catch (SshException Ex) {
			throw new ManagementException(Ex);
		}
		if (result == false) {
			throw new ManagementException(Messages.bind(
					Messages.MgmtEx_SSH_MGMT_ENABLE_TIMEOUT,
					NetworkManagementHelper.ENABLE_NETWORK_MGNT_TIMEOUT_ATTR));
		}
	}

	@Override
	public void disableNetworkManagement() throws ManagementException {
		Configuration sshPlugInConf = null;
		try {
			sshPlugInConf = Configuration.get(getContext()
					.getProcessorManager());
		} catch (ConfigurationException Ex) {
			throw new ManagementException(Ex);
		}
		disableManagement(sshPlugInConf);
	}

	public void disableManagement(Configuration sshPlugInConf) {
		if (sshPlugInConf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ Configuration.class.getCanonicalName() + ".");
		}
		sshPlugInConf.removeKnownHostsHost(getManagementDatas().getHost());
	}

}
