package com.wat.melody.cloud.network;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.exception.ManagementException;
import com.wat.melody.plugin.ssh.common.Configuration;
import com.wat.melody.plugin.ssh.common.exception.ConfigurationException;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;

public class SshNetworkManager implements NetworkManager {

	private NetworkManagerInfos moManagementInfos;
	private ITaskContext moContext;

	public NetworkManagerInfos getManagementInfos() {
		return moManagementInfos;
	}

	@Override
	public void setManagementInfos(NetworkManagerInfos mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkManagerInfos.class.getCanonicalName() + ".");
		}
		moManagementInfos = mi;
	}

	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moContext = mi;
	}

	@Override
	public void enableManagement(long timeout) throws ManagementException,
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
					getManagementInfos().getHost(), getManagementInfos()
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
	public void disableManagement() throws ManagementException {
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
		sshPlugInConf.removeKnownHostsHost(getManagementInfos().getHost());
	}

}
