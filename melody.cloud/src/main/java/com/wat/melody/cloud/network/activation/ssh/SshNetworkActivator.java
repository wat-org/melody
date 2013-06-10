package com.wat.melody.cloud.network.activation.ssh;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivationDatasLoader;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
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
 * <p>
 * This implementation of the {@link NetworkActivator} will :
 * <ul>
 * <li>On enablement : add the ip/fqdn of the target system in the known_host
 * file ;</li>
 * <li>On disablement : remove the ip/fqdn of the target system in the
 * known_host file ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshNetworkActivator implements NetworkActivator {

	private static Log log = LogFactory.getLog(SshNetworkActivator.class);

	private SshNetworkActivationDatas _activationDatas;
	private ISshSessionConfiguration _configuration;

	public SshNetworkActivator(SshNetworkActivationDatas datas,
			ISshSessionConfiguration sc) {
		setConfiguration(sc);
		setDatas(datas);
	}

	public SshNetworkActivationDatas getDatas() {
		return _activationDatas;
	}

	public void setDatas(SshNetworkActivationDatas nmd) {
		if (nmd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SshNetworkActivationDatas.class.getCanonicalName() + ".");
		}
		_activationDatas = nmd;
	}

	public ISshSessionConfiguration getConfiguration() {
		return _configuration;
	}

	private void setConfiguration(ISshSessionConfiguration sc) {
		if (sc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ISshSessionConfiguration.class.getCanonicalName() + ".");
		}
		_configuration = sc;
	}

	@Override
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException {
		disableNetworkActivation();
		SshNetworkActivationDatas datas = getDatas();
		boolean result = false;
		try {
			result = addKnownHostsHost(getConfiguration(), datas.getHost(),
					datas.getPort(), datas.getActivationTimeout()
							.getTimeoutInMillis());
		} catch (SshSessionException Ex) {
			throw new NetworkActivationException(Ex);
		}
		if (result == false) {
			throw new NetworkActivationException(Messages.bind(
					Messages.SshNetworkActivatorEx_ENABLEMENT_TIMEOUT,
					NetworkActivationDatasLoader.ACTIVATION_TIMEOUT_ATTR));
		}
		log.debug(Messages.bind(
				Messages.SshNetworkActivatorMsg_ENABLEMENT_DONE, datas
						.getHost().getAddress(), datas.getHost().getName()));
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
		ISshSession session = new SshSession(ud, cd);
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
					Messages.SshNetworkActivatorMsg_WAIT_FOR_ENABLEMENT,
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
	public void disableNetworkActivation() throws NetworkActivationException {
		SshNetworkActivationDatas datas = getDatas();
		getConfiguration().getKnownHosts().remove(datas.getHost());
		log.debug(Messages.bind(
				Messages.SshNetworkActivatorMsg_DISABLEMENT_DONE, datas
						.getHost().getAddress(), datas.getHost().getName()));
	}

}