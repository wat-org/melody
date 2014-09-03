package com.wat.melody.cloud.network.activation.telnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
import com.wat.melody.cloud.network.activation.xml.NetworkActivationDatasLoader;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.telnet.ITelnetConnectionDatas;
import com.wat.melody.common.telnet.ITelnetSessionConfiguration;
import com.wat.melody.common.telnet.ITelnetUserDatas;
import com.wat.melody.common.telnet.ITetnetSession;
import com.wat.melody.common.telnet.exception.InvalidCredentialException;
import com.wat.melody.common.telnet.exception.TelnetSessionException;
import com.wat.melody.common.telnet.impl.TelnetConnectionDatas;
import com.wat.melody.common.telnet.impl.TelnetSession;
import com.wat.melody.common.telnet.impl.TelnetUserDatas;

/**
 * <p>
 * This implementation of the {@link NetworkActivator} will :
 * <ul>
 * <li>On enablement : wait for the Telnet Service to be up and running ;</li>
 * <li>On disablement : ?? ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetNetworkActivator implements NetworkActivator {

	private static Logger log = LoggerFactory
			.getLogger(TelnetNetworkActivator.class);

	private TelnetNetworkActivationDatas _activationDatas;
	private ITelnetSessionConfiguration _configuration;

	public TelnetNetworkActivator(TelnetNetworkActivationDatas datas,
			ITelnetSessionConfiguration sc) {
		setConfiguration(sc);
		setDatas(datas);
	}

	public TelnetNetworkActivationDatas getNetworkActivationDatas() {
		return _activationDatas;
	}

	private void setDatas(TelnetNetworkActivationDatas nad) {
		if (nad == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TelnetNetworkActivationDatas.class.getCanonicalName()
					+ ".");
		}
		_activationDatas = nad;
	}

	public ITelnetSessionConfiguration getConfiguration() {
		return _configuration;
	}

	private void setConfiguration(ITelnetSessionConfiguration sc) {
		if (sc == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetSessionConfiguration.class.getCanonicalName()
					+ ".");
		}
		_configuration = sc;
	}

	@Override
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException {
		disableNetworkActivation();
		TelnetNetworkActivationDatas datas = getNetworkActivationDatas();
		boolean result = false;
		try {
			result = connect(getConfiguration(), datas.getHost(),
					datas.getPort(), datas.getActivationTimeout()
							.getTimeoutInMillis());
		} catch (TelnetSessionException Ex) {
			throw new NetworkActivationException(Ex);
		}
		if (result == false) {
			throw new NetworkActivationException(Msg.bind(
					Messages.TelnetNetworkActivatorEx_ENABLEMENT_TIMEOUT,
					NetworkActivationDatasLoader.ACTIVATION_TIMEOUT_ATTR));
		}
	}

	private boolean connect(ITelnetSessionConfiguration sc, Host host,
			Port port, long timeout) throws TelnetSessionException,
			InterruptedException {
		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;
		boolean enablementDone = true;

		ITelnetUserDatas ud = new TelnetUserDatas();
		ud.setLogin("probetelnet");
		ud.setPassword("xxxxx");
		ITelnetConnectionDatas cd = new TelnetConnectionDatas();
		cd.setHost(host);
		cd.setPort(port);
		ITetnetSession session = new TelnetSession(ud, cd);
		session.setSessionConfiguration(sc);

		while (true) {
			try {
				session.connect();
				// connection succeed, and credential valid => ok
				break;
			} catch (InvalidCredentialException Ex) {
				// connection succeed, but credential invalid => ok
				break;
			} catch (TelnetSessionException Ex) {
				// if something bad happened => re throw
				if (Ex.getCause() == null || Ex.getCause().getMessage() == null) {
					throw Ex;
				} else if (Ex.getCause().getMessage().indexOf("refused") == -1
						&& Ex.getCause().getMessage().indexOf("timeout") == -1
						&& Ex.getCause().getMessage().indexOf("No route") == -1
						&& Ex.getCause().getMessage().indexOf("unreachable") == -1) {
					throw Ex;
				}
				// in other case => loop
			} finally {
				if (session != null) {
					session.disconnect();
				}
			}
			log.debug(Msg.bind(
					Messages.TelnetNetworkActivatorMsg_WAIT_FOR_ENABLEMENT,
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
		// nothing to do
	}

}