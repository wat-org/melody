package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.network.exception.NetworkManagementException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.exception.InvalidCredentialException;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.impl.SshConnectionDatas;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.ssh.impl.SshUserDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkManager implements NetworkManager {

	private static Log log = LogFactory.getLog(SshNetworkManager.class);

	private SshManagementNetworkDatas moManagementDatas;
	private ISshSessionConfiguration moConfiguration;

	public SshNetworkManager(SshManagementNetworkDatas datas,
			ISshSessionConfiguration sc) {
		setConfiguration(sc);
		setManagementDatas(datas);
	}

	public SshManagementNetworkDatas getManagementDatas() {
		return moManagementDatas;
	}

	public void setManagementDatas(SshManagementNetworkDatas nmd) {
		if (nmd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshManagementNetworkDatas.class.getCanonicalName() + ".");
		}
		moManagementDatas = nmd;
	}

	public ISshSessionConfiguration getConfiguration() {
		return moConfiguration;
	}

	private void setConfiguration(ISshSessionConfiguration sc) {
		if (sc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshSessionConfiguration.class.getCanonicalName() + ".");
		}
		moConfiguration = sc;
	}

	@Override
	public void enableNetworkManagement() throws NetworkManagementException,
			InterruptedException {
		disableNetworkManagement();
		boolean result = false;
		try {
			SshManagementNetworkDatas datas = getManagementDatas();
			result = addKnownHostsHost(getConfiguration(), datas.getHost(),
					datas.getPort(), datas.getEnablementTimeout()
							.getTimeoutInMillis());
		} catch (SshSessionException Ex) {
			throw new NetworkManagementException(Ex);
		}
		if (result == false) {
			throw new NetworkManagementException(Messages.bind(
					Messages.NetMgmtEx_SSH_MGMT_ENABLE_TIMEOUT,
					ManagementNetworkDatasLoader.ENABLE_TIMEOUT_ATTR));
		}
	}

	public static boolean addKnownHostsHost(ISshSessionConfiguration sc,
			Host host, Port port, long timeout) throws SshSessionException,
			InterruptedException {
		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;
		boolean enablementDone = true;

		ISshUserDatas ud = new SshUserDatas();
		ud.setLogin("crazyssh");
		ud.setPassword("");
		ISshConnectionDatas cd = new SshConnectionDatas();
		cd.setHost(host);
		cd.setPort(port);
		cd.setTrust(true);
		ISshSession session = new SshSession();
		session.setUserDatas(ud);
		session.setConnectionDatas(cd);
		session.setSessionConfiguration(sc);

		while (true) {
			try {
				session.connect();
				// connection succeed, and credential valid => ok
				break;
			} catch (InvalidCredentialException Ex) {
				// connection succeed, but credential invalid => ok
				break;
			} catch (SshSessionException Ex) {
				// if something bad happened => re throw
				if (Ex.getCause() == null || Ex.getCause().getMessage() == null) {
					throw Ex;
				} else if (Ex.getCause().getMessage().indexOf("refused") == -1
						&& Ex.getCause().getMessage().indexOf("timeout") == -1
						&& Ex.getCause().getMessage().indexOf("No route") == -1) {
					throw Ex;
				}
				// in other case => loop
			} finally {
				if (session != null) {
					session.disconnect();
				}
			}
			log.debug(Messages.bind(
					Messages.NetMgmtMsg_SSH_WAIT_FOR_MGMT_ENABLE,
					host.getAddress(), port.getValue()));
			if (timeout == 0) {
				Thread.sleep(WAIT_STEP);
				continue;
			}
			left = timeout - (System.currentTimeMillis() - start);
			Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
			if (left < 0) {
				enablementDone = false;
				break;
			}
		}

		return enablementDone;
	}

	@Override
	public void disableNetworkManagement() throws NetworkManagementException {
		getConfiguration().getKnownHosts().remove(
				getManagementDatas().getHost());
	}

}