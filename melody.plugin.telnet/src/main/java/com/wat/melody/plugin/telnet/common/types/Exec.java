package com.wat.melody.plugin.telnet.common.types;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.TextContent;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.plugin.telnet.common.Messages;
import com.wat.melody.plugin.telnet.common.exception.TelnetException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Exec {

	/**
	 * The 'exec' XML element used in the Sequence Descriptor
	 */
	public static final String EXEC = "exec";

	/**
	 * Exec's attribute, which specifies an 'in-line' command.
	 */
	public static final String INLINE_COMMAND_ATTR = "command";

	private String _inlineCommand = null;
	private String _cdataCommand = null;
	private int _validator = 0;

	public String getDosCommand() throws TelnetException {
		if (_validator == 0 || _validator > 1) {
			throw new TelnetException(Msg.bind(
					Messages.TelnetEx_VALIDATION_ERR, INLINE_COMMAND_ATTR));
		}

		if (getInlineCommand() != null) {
			return getInlineCommand() + "\n";
		} else {
			return getCDataCommand() + "\n";
		}
	}

	public String getInlineCommand() {
		return _inlineCommand;
	}

	@Attribute(name = INLINE_COMMAND_ATTR)
	public String setInLineCommand(String c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a shell command).");
		}
		_validator++;
		String previous = getInlineCommand();
		_inlineCommand = c;
		return previous;
	}

	public String getCDataCommand() {
		return _cdataCommand;
	}

	@TextContent
	public String setCDataCommand(String c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + String.class.getCanonicalName()
					+ " (a shell command).");
		}
		_validator++;
		String previous = getInlineCommand();
		_cdataCommand = c;
		return previous;
	}

}