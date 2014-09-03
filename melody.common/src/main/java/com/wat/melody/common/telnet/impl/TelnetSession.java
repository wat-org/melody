package com.wat.melody.common.telnet.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.telnet.ITelnetConnectionDatas;
import com.wat.melody.common.telnet.ITelnetSessionConfiguration;
import com.wat.melody.common.telnet.ITelnetUserDatas;
import com.wat.melody.common.telnet.ITetnetSession;
import com.wat.melody.common.telnet.exception.InvalidCredentialException;
import com.wat.melody.common.telnet.exception.TelnetSessionException;
import com.wat.melody.common.telnet.impl.exception.UnexpectedResultReceived;
import com.wat.melody.common.timeout.Timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetSession implements ITetnetSession {

	private static Logger log = LoggerFactory.getLogger(TelnetSession.class);

	private static Logger ex = LoggerFactory.getLogger("exception."
			+ TelnetSession.class.getName());

	private static String MELODY_PROMPT = "melody\\telnet\\ ";
	private static TelnetResponsesMatcher LOGIN_MATCHER = new TelnetResponsesMatcher(
			"(?s)login:");
	private static TelnetResponsesMatcher PASSWORD_MATHCER = new TelnetResponsesMatcher(
			"(?s)password:");
	private static TelnetResponsesMatcher STANDARD_PROMPT_MATCHER = new TelnetResponsesMatcher(
			"(?s)>");
	private static TelnetResponsesMatcher MELODY_PROMPT_MATCHER = new TelnetResponsesMatcher(
			"(?s)(\\r|\\n|\\r\\n)?melody\\\\telnet\\\\ ");
	private static TelnetResponsesMatcher SESSION_TIMEOUT_MATCHER = new TelnetResponsesMatcher(
			"(?s)session timed out");
	private static TelnetResponsesMatcher LOGIN_FAIL_MATCHER = new TelnetResponsesMatcher(
			"(?s)session timed out", "(?s)[lL]ogin [fF]ailed",
			"(?s)[lL]ogon [fF]ailure");
	private static TelnetCommandFilter _filter = new TelnetCommandFilter();

	private ITelnetClient _tc = null;

	private ITelnetSessionConfiguration _sshSessionConfiguration = null;
	private ITelnetUserDatas _sshUserDatas = null;
	private ITelnetConnectionDatas _sshConnectionDatas = null;

	public TelnetSession(ITelnetUserDatas ud, ITelnetConnectionDatas cd) {
		setUserDatas(ud);
		setConnectionDatas(cd);
	}

	@Override
	public ITelnetSessionConfiguration getSessionConfiguration() {
		return _sshSessionConfiguration;
	}

	@Override
	public ITelnetSessionConfiguration setSessionConfiguration(
			ITelnetSessionConfiguration sc) {
		// can be null
		ITelnetSessionConfiguration previous = getSessionConfiguration();
		_sshSessionConfiguration = sc;
		return previous;
	}

	@Override
	public ITelnetUserDatas getUserDatas() {
		return _sshUserDatas;
	}

	@Override
	public ITelnetUserDatas setUserDatas(ITelnetUserDatas ud) {
		if (ud == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetUserDatas.class.getCanonicalName() + ".");
		}
		// the user must be defined
		if (ud.getLogin() == null) {
			throw new IllegalArgumentException("No login defined ! "
					+ "The caller should define a login.");
		}
		ITelnetUserDatas previous = getUserDatas();
		_sshUserDatas = ud;
		return previous;
	}

	@Override
	public ITelnetConnectionDatas getConnectionDatas() {
		return _sshConnectionDatas;
	}

	@Override
	public ITelnetConnectionDatas setConnectionDatas(ITelnetConnectionDatas cd) {
		if (cd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITelnetConnectionDatas.class.getCanonicalName() + ".");
		}
		ITelnetConnectionDatas previous = getConnectionDatas();
		_sshConnectionDatas = cd;
		return previous;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("protocol:");
		str.append("telnet");
		str.append(" }");
		return str.toString();
	}

	/**
	 * @throws InvalidCredentialException
	 *             on authentication failure.
	 * @throws TelnetSessionException
	 *             if the connection fail for any other reason (no route to
	 *             host, dns failure, network unreachable, ...).
	 */
	@Override
	public synchronized void connect() throws TelnetSessionException,
			InvalidCredentialException, InterruptedException {
		if (isConnected()) {
			return;
		}
		log.trace(Msg.bind(Messages.SessionMsg_CNX, this, getConnectionDatas(),
				getUserDatas(), getSessionConfiguration()));
		applyDatas();
		applySessionConfiguration();
		_connect();
		log.trace(Msg.bind(Messages.SessionMsg_CNX_OK, this));
	}

	@Override
	public synchronized void disconnect() {
		if (_tc != null) {
			if (_tc.isConnected()) {
				try {
					_tc.disconnect();
				} catch (IOException ignore) {
				}
			}
			_tc = null;
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return _tc != null && _tc.isConnected();
	}

	/**
	 * @param command
	 *            is the given command to execute.
	 * @param out
	 *            will receive the output generated by the given command
	 *            execution.
	 * @param killTimeout
	 *            is the maximum time the command execution will be wait, after
	 *            it was interrupted. If the given timeout is reached, an
	 *            {@link InterruptedException} will be throw.
	 * 
	 * @return the return value of the given command
	 * 
	 * @throws TelnetSessionException
	 *             if on I/O error occurred (ex : socket error).
	 * @throws InterruptedException
	 *             if the command execution is not completed during the given
	 *             timeout after the current {@link Thread} was interrupted.
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if <tt>command</tt> is <tt>null</tt> ;</li>
	 *             <li>if <tt>out</tt> is <tt>null</tt> ;</li>
	 *             </ul>
	 */
	@Override
	public int execRemoteCommand(String command, OutputStream out,
			Timeout<Long> killTimeout) throws TelnetSessionException,
			InterruptedException {
		if (command == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (out == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ OutputStream.class.getCanonicalName() + ".");
		}
		if (_filter.matches(command)) {
			return 0;
		}
		if (killTimeout != null) {
			_tc.setKillTimeout(killTimeout);
		}
		try {
			/*
			 * At the end of the connection process, we set the prompt to a
			 * custom one, which is highly recognizable. After the command was
			 * sent, We will receive data until we find our custom prompt. It
			 * will be the sign that the command execution is completed.
			 */
			_tc.send(command);
			_tc.waitUntil(out, command + "\r\n", MELODY_PROMPT_MATCHER, null);

			/*
			 * Send a command to get the return code of the previous command.
			 */
			OutputStream tmp = new ByteArrayOutputStream();
			_tc.send("echo %errorlevel%");
			_tc.waitUntil(tmp, "echo %errorlevel%" + "\r\n",
					MELODY_PROMPT_MATCHER, null);

			// if interrupted, trace it
			if (_tc.wasInterrupted()) {
				log.info(Messages.ExecMsg_FORCE_STOP_AVOID);
			}
			// parse the return value
			String tmpStr = TelnetResponseAnalyzer.removeTrailingCrLf(tmp
					.toString());
			try {
				return Integer.parseInt(tmpStr);
			} catch (NumberFormatException silence) {
				throw new TelnetSessionException(tmpStr
						+ ": Not accepted. Cannot be parsed as an Interger.");
			}
		} catch (IOException Ex) {
			throw new TelnetSessionException(Ex);
		}
	}

	protected String getHost() {
		return getConnectionDatas().getHost().getAddress();
	}

	protected int getPort() {
		return getConnectionDatas().getPort().getValue();
	}

	protected String getLogin() {
		return getUserDatas().getLogin();
	}

	protected String getPassword() {
		return getUserDatas().getPassword();
	}

	private void applyDatas() throws TelnetSessionException {
		_tc = new TelnetClientAsynchAdapter(new TelnetClient(), this);
	}

	private void applySessionConfiguration() throws TelnetSessionException {
		ITelnetSessionConfiguration conf = getSessionConfiguration();
		if (getSessionConfiguration() == null) {
			// no session configuration defined, will use defaults
			return;
		}
		try {
			_tc.setConnectTimeout(conf.getConnectionTimeout()
					.getTimeoutInMillis());
			if (conf.getSendBufferSize().isDefined()) {
				_tc.setSendBufferSize(conf.getSendBufferSize().getSize());
			}
			if (conf.getReceiveBufferSize().isDefined()) {
				_tc.setReceiveBufferSize(conf.getReceiveBufferSize().getSize());
			}
		} catch (SocketException Ex) {
			throw new TelnetSessionException(
					"Fail to apply configuration to telnet session.", Ex);
		}
	}

	private void _connect() throws TelnetSessionException,
			InvalidCredentialException, InterruptedException {
		int cnxRetry = 0;
		int cnxDelay = 3;
		if (getSessionConfiguration() != null) {
			cnxRetry = getSessionConfiguration().getConnectionRetry()
					.getValue();
		}
		while (true) {
			try {
				_tc.connect();
				ITelnetSessionConfiguration conf = getSessionConfiguration();
				if (conf != null) {
					_tc.setSoTimeout(conf.getReadTimeout().getTimeoutInMillis());
					_tc.setSoLinger(conf.getSoLinger().isEnabled(), conf
							.getSoLinger().getTimeout());
					_tc.setTcpNoDelay(conf.getTcpNoDelay());
				}
				// _tr = new TelnetReader(this, _tc.getInputStream());
				_tc.waitUntil(null, null, LOGIN_MATCHER,
						SESSION_TIMEOUT_MATCHER);
				// send login
				_tc.send(getLogin());
				_tc.waitUntil(null, " " + getLogin(), PASSWORD_MATHCER,
						SESSION_TIMEOUT_MATCHER);
				// send password
				_tc.send(getPassword());
				_tc.waitUntil(null, null, STANDARD_PROMPT_MATCHER,
						LOGIN_FAIL_MATCHER);
				// set our custom prompt
				_tc.send("prompt " + MELODY_PROMPT);
				_tc.waitUntil(null, "prompt " + MELODY_PROMPT + "\r\n",
						MELODY_PROMPT_MATCHER, null);
				// success => exit
				return;
			} catch (IOException Ex) {
				String msg = Ex.getMessage();
				msg = msg != null ? msg : "";
				// authentication error => fast fail
				if (Ex instanceof UnexpectedResultReceived
						&& msg.matches("(?i)^log.*")) {
					// will match message 'Login Failed' and
					// 'Login Failure'
					// => throw dedicated exception if credentials error
					throw new InvalidCredentialException(Msg.bind(
							Messages.SessionEx_FAILED_TO_CONNECT, this,
							getConnectionDatas(), getUserDatas()), Ex);
				}
				// no retry left => fail
				if (cnxRetry <= 0) {
					throw new TelnetSessionException(Msg.bind(
							Messages.SessionEx_FAILED_TO_CONNECT, this,
							getConnectionDatas(), getUserDatas()), Ex);
				}
				/*
				 * If the login or password submission is really, really slow,
				 * we will receive an UnexpectedResultReceived with message
				 * 'session timed out'. In order to resolve this issue, when we
				 * receive an UnexpectedResultReceived with message 'session
				 * timed out', we disconnect and we rebuild a new telnetclient
				 * session. After that, we retry.
				 */
				// session timed out => create a new session and retry
				if (Ex instanceof UnexpectedResultReceived
						&& msg.indexOf("session timed out") != -1) {
					disconnect();
					applyDatas();
					applySessionConfiguration();
					// => retry
				}
				// retrying
				TelnetSessionException pex = new TelnetSessionException(
						Msg.bind(Messages.SessionMsg_RETRY_TO_CONNECT, this,
								getConnectionDatas(), getUserDatas(), cnxRetry),
						Ex);
				log.info(pex.getUserFriendlyStackTrace());
				ex.info(pex.getFullStackTrace());
				cnxRetry -= 1;
				cnxDelay += 3;
				try {
					Thread.sleep(cnxDelay * 1000);
				} catch (InterruptedException e) {
					throw new WrapperInterruptedException(
							Messages.SessionEx_CNX_INTERRUPTED, e);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException(
							Messages.SessionEx_CNX_INTERRUPTED);
				}
			}
		}
	}

}