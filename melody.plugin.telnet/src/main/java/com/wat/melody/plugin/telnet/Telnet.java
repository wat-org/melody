package com.wat.melody.plugin.telnet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.telnet.ITelnetConnectionDatas;
import com.wat.melody.common.telnet.ITelnetUserDatas;
import com.wat.melody.common.telnet.exception.InvalidCredentialException;
import com.wat.melody.common.telnet.exception.TelnetSessionException;
import com.wat.melody.common.telnet.impl.LoggerOutputStream;
import com.wat.melody.common.telnet.impl.TelnetConnectionDatas;
import com.wat.melody.common.telnet.impl.TelnetSession;
import com.wat.melody.common.telnet.impl.TelnetUserDatas;
import com.wat.melody.plugin.telnet.common.Messages;
import com.wat.melody.plugin.telnet.common.TelnetPlugInConfiguration;
import com.wat.melody.plugin.telnet.common.exception.TelnetException;
import com.wat.melody.plugin.telnet.common.types.Exec;

public class Telnet implements ITask {

	private static Logger log = LoggerFactory.getLogger(Telnet.class);

	/**
	 * Task's name
	 */
	public static final String TELNET = "telnet";

	/**
	 * Defines the remote system ip or fqdn.
	 */
	public static final String HOST_ATTR = "host";

	/**
	 * Defines the remote system port (e.g. the port of telnet daemon on the
	 * remote system).
	 */
	public static final String PORT_ATTR = "port";

	/**
	 * Defines the user to connect with on the remote system.
	 */
	public static final String LOGIN_ATTR = "login";

	/**
	 * Defines the password of the user used to connect to the remote system.
	 */
	public static final String PASS_ATTR = "password";

	/**
	 * Task's attribute, which defines a short description. This description
	 * will be included in each message logged. It can help to grep the log
	 * file.
	 */
	public static final String DESCRIPTION_ATTR = "description";

	private String _commandToExecute = "";
	private String _description = "[exec telnet]";
	private ITelnetUserDatas _userDatas;
	private ITelnetConnectionDatas _cnxDatas;

	public Telnet() {
		setUserDatas(new TelnetUserDatas());
		setConnectionDatas(new TelnetConnectionDatas());
	}

	private ITelnetUserDatas getUserDatas() {
		return _userDatas;
	}

	private ITelnetUserDatas setUserDatas(ITelnetUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetUserDatas.class.getCanonicalName() + ".");
		}
		ITelnetUserDatas previous = getUserDatas();
		_userDatas = ud;
		return previous;
	}

	private ITelnetConnectionDatas getConnectionDatas() {
		return _cnxDatas;
	}

	private ITelnetConnectionDatas setConnectionDatas(ITelnetConnectionDatas cd) {
		if (cd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetConnectionDatas.class.getCanonicalName() + ".");
		}
		ITelnetConnectionDatas previous = getConnectionDatas();
		_cnxDatas = cd;
		return previous;
	}

	@Override
	public void validate() throws TelnetException {
		if (getCommandToExecute().length() == 0) {
			throw new TelnetException(Msg.bind(Messages.TelnetEx_MISSING_CMD,
					Exec.EXEC));
		}
	}

	protected int execTelnetCommand(String cmdML, String outputPrefix)
			throws TelnetException, InterruptedException {
		TelnetSession session = new TelnetSession(getUserDatas(),
				getConnectionDatas());
		session.setSessionConfiguration(getSshPlugInConf());

		try {
			session.connect();
			LoggerOutputStream out = new LoggerOutputStream(outputPrefix
					+ " [STDOUT]", LogThreshold.DEBUG);
			// execute command line by line
			for (String cmd : cmdML.split("\\n")) {
				int res = session.execRemoteCommand(cmd, out, Melody
						.getContext().getProcessorManager()
						.getHardKillTimeout());
				// exit if a single command exit with error
				if (res != 0) {
					return res;
				}
			}
			// all commands succeed! return 0
			return 0;
		} catch (InvalidCredentialException Ex) {
			throw new TelnetException(Msg.bind(Messages.TelnetEx_AUTH_FAIL,
					LOGIN_ATTR, PASS_ATTR), Ex);

		} catch (TelnetSessionException Ex) {
			throw new TelnetException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	@Override
	public void doProcessing() throws TelnetException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		int exitStatus = execTelnetCommand(getCommandToExecute(),
				getDescription());
		String recapMsg = getDescription() + " " + "[STATUS] ";
		switch (exitStatus) {
		case 0:
			log.info(recapMsg + "--->    OK    <---");
			break;
		case 200:
			log.warn(recapMsg + "--->   WARN   <---");
			break;
		case 201:
			log.warn(recapMsg + "--->   KILL   <---");
			break;
		case 202:
			log.error(recapMsg + "--->   FAIL   <---");
			throw new TelnetException(recapMsg + "--->   FAIL   <---");
		default:
			log.error(recapMsg + "---> CRITICAL <---  [" + exitStatus + "]");
			throw new TelnetException(recapMsg + "---> CRITICAL <---  ["
					+ exitStatus + "]" + SysTool.NEW_LINE
					+ "Here is the complete script which generates the error :"
					+ SysTool.NEW_LINE + "-----" + SysTool.NEW_LINE
					+ getCommandToExecute().trim() + SysTool.NEW_LINE + "-----"
					+ SysTool.NEW_LINE);
		}
	}

	protected TelnetPlugInConfiguration getSshPlugInConf()
			throws TelnetException {
		try {
			return TelnetPlugInConfiguration.get();
		} catch (PlugInConfigurationException Ex) {
			throw new TelnetException(Ex);
		}
	}

	public String getCommandToExecute() {
		return _commandToExecute;
	}

	public String getDescription() {
		return _description;
	}

	@Attribute(name = DESCRIPTION_ATTR)
	public String setDescription(String description) {
		// if null => convert it to ""
		if (description == null) {
			description = "";
		}
		String previous = getDescription();
		_description = description;
		return previous;
	}

	public Host getHost() {
		return getConnectionDatas().getHost();
	}

	@Attribute(name = HOST_ATTR, mandatory = true)
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		return getConnectionDatas().setHost(host);
	}

	public Port getPort() {
		return getConnectionDatas().getPort();
	}

	@Attribute(name = PORT_ATTR)
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		return getConnectionDatas().setPort(port);
	}

	public String getLogin() {
		return getUserDatas().getLogin();
	}

	@Attribute(name = LOGIN_ATTR, mandatory = true)
	public String setLogin(String login) {
		if (login == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a login).");
		}
		return getUserDatas().setLogin(login);
	}

	public String getPassword() {
		return getUserDatas().getPassword();
	}

	@Attribute(name = PASS_ATTR)
	public String setPassword(String password) {
		return getUserDatas().setPassword(password);
	}

	@NestedElement(name = Exec.EXEC, type = Type.ADD)
	public void addInsludeScript(Exec is) throws TelnetException {
		if (is == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Exec.class.getCanonicalName() + ".");
		}
		_commandToExecute += is.getDosCommand();
	}

}