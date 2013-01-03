package com.wat.melody.cloud.network;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.exception.NetworkManagementException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.jsch.JSchConnectionDatas;
import com.wat.melody.plugin.ssh.common.jsch.JSchHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkManager implements NetworkManager {

	/*
	 * TODO : remove all reference to Ssh Plug-In.
	 */
	private static Log log = LogFactory.getLog(SshNetworkManager.class);

	private SshManagementNetworkDatas moManagementDatas;
	private SshPlugInConfiguration moContext;

	public SshNetworkManager(Node instanceNode, SshPlugInConfiguration context)
			throws ResourcesDescriptorException {
		setConfiguration(context);
		setManagementDatas(new SshManagementNetworkDatas(instanceNode));
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

	public SshPlugInConfiguration getConfiguration() {
		return moContext;
	}

	private void setConfiguration(SshPlugInConfiguration conf) {
		if (conf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshPlugInConfiguration.class.getCanonicalName() + ".");
		}
		moContext = conf;
	}

	@Override
	public void enableNetworkManagement(long timeout)
			throws NetworkManagementException, InterruptedException {
		disableNetworkManagement();
		boolean result = false;
		try {
			result = addKnownHostsHost(getConfiguration(), getManagementDatas()
					.getHost(), getManagementDatas().getPort(), timeout);
		} catch (SshException Ex) {
			throw new NetworkManagementException(Ex);
		}
		if (result == false) {
			throw new NetworkManagementException(Messages.bind(
					Messages.NetMgmtEx_SSH_MGMT_ENABLE_TIMEOUT,
					NetworkManagementHelper.ENABLE_NETWORK_MGNT_TIMEOUT_ATTR));
		}
	}

	public static boolean addKnownHostsHost(
			SshPlugInConfiguration sshPlugInConf, Host host, Port port,
			long timeout) throws SshException, InterruptedException {
		JSchConnectionTester sshCnxTester = new JSchConnectionTester(host, port);

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;
		boolean enablementDone = true;

		Session session = null;
		while (true) {
			try {
				session = JSchHelper.openSession(sshCnxTester, sshPlugInConf);
				break;
			} catch (Throwable Ex) {
				if (Ex.getCause() == null || Ex.getCause().getMessage() == null) {
					throw new SshException(Ex);
				} else if (Ex.getCause().getMessage()
						.indexOf("Incorrect credentials") != -1) {
					// connection succeed
					break;
				} else if (Ex.getCause().getMessage().indexOf("refused") == -1
						&& Ex.getCause().getMessage().indexOf("timeout") == -1
						&& Ex.getCause().getMessage().indexOf("No route") == -1) {
					throw new SshException(Ex);
				}
			} finally {
				if (session != null) {
					session.disconnect();
				}
			}
			log.debug(Messages.bind(
					Messages.NetMgmtMsg_SSH_WAIT_FOR_MGMT_ENABLE, host
							.getValue().getHostAddress(), port.getValue()));
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

		String k = sshPlugInConf.getKnownHostsHostKey(
				host.getValue().getHostAddress()).getKey();
		try {
			sshPlugInConf
					.addKnownHostsHostKey(host.getValue().getHostName(), k);
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while adding an "
					+ "host with the HostKey '" + k + "' into the KnownHosts "
					+ "file. "
					+ "Because this HostKey have been retrieve from the "
					+ "KnownHosts file, this key should be valid. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}

		return enablementDone;
	}

	@Override
	public void disableNetworkManagement() throws NetworkManagementException {
		removeKnownHostsHost(getConfiguration(), getManagementDatas().getHost());
	}

	public static void removeKnownHostsHost(SshPlugInConfiguration conf,
			Host host) {
		if (conf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshPlugInConfiguration.class.getCanonicalName() + ".");
		}
		conf.removeKnownHostsHostKey(host.getValue().getHostAddress());
		conf.removeKnownHostsHostKey(host.getValue().getHostName());
	}

}

class JSchConnectionTester implements JSchConnectionDatas {

	private Host moHost;
	private Port moPort;

	public JSchConnectionTester(Host host, Port port) {
		setHost(host);
		setPort(port);
	}

	@Override
	public String getPassphrase() {
		return "crazyssh";
	}

	@Override
	public String getPassword() {
		return "crazyssh";
	}

	@Override
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		return true;
	}

	@Override
	public void showMessage(String message) {
	}

	@Override
	public String[] promptKeyboardInteractive(String destination, String name,
			String instruction, String[] prompt, boolean[] echo) {
		return null;
	}

	@Override
	public String getLogin() {
		return "crazyssh";
	}

	@Override
	public Host getHost() {
		return moHost;
	}

	public void setHost(Host h) {
		moHost = h;
	}

	@Override
	public Port getPort() {
		return moPort;
	}

	public void setPort(Port p) {
		moPort = p;
	}

}