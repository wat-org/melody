package com.wat.melody.common.telnet.impl;

import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.messages.Msg;

/**
 * <P>
 * Contains a list of command that are refused. The method
 * {@link #matches(String)} will indicate if the given command match one of the
 * refused commands.
 * 
 * Command are refused because they will make the {@link TelnetSesion} fail :
 * <ul>
 * <li><tt>prompt somestring</tt> : because the prompt detection is our way to
 * detect the command execution is completed. For this reason we can't tolerate
 * a prompt modification ;</li></li>
 * <li><tt>echo off</tt> : because the prompt will no more be displayed, and the
 * prompt detection is our way to detect the command execution is completed ;</li>
 * <li>multiline commands (e.g. commands containing '^') : because the logic is
 * : execute a command and return the result code. Multiline command have no
 * return code. So we should implement a different logic to support them ;</li>
 * </ul>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetCommandFilter {

	private static Logger log = LoggerFactory.getLogger(TelnetSession.class);

	private LinkedHashSet<String[]> _values = new LinkedHashSet<String[]>();

	public TelnetCommandFilter() {
		addFilter("^.*[\\^]$", Messages.ExecMsg_MULTILINE_REFUSED);
		addFilter("(?i)^\\s*echo\\s+off\\s*$", Messages.ExecMsg_ECHO_REFUSED);
		addFilter("(?i)^\\s*prompt\\s+[^\\s]+.*$",
				Messages.ExecMsg_PROMPT_REFUSED);
		addFilter("^.*([\\r\\n].*)?[\\r\\n].*$", Messages.ExecMsg_CRLF_REFUSED);
		// add more pattern here
	}

	public void addFilter(String pattern, String errorMessage) {
		String[] filter = { pattern, errorMessage };
		_values.add(filter);
	}

	public boolean matches(String telnetCommand) {
		for (String[] filter : _values) {
			String pattern = filter[0];
			String msg = filter[1];
			if (telnetCommand.matches(pattern)) {
				log.warn(Msg.bind(msg, telnetCommand));
				return true;
			}
		}
		return false;
	}

}
