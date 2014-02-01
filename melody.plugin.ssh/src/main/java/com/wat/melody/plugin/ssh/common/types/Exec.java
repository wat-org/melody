package com.wat.melody.plugin.ssh.common.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.TextContent;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.WrapperFile;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;

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
	 * Exec's attribute, which specifies the path of a script.
	 */
	public static final String FILE_ATTR = "file";

	/**
	 * Exec's attribute, which indicates if the script contains Melody
	 * Expression to be resolved.
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * Exec's attribute, which specifies an 'in-line' command.
	 */
	public static final String INLINE_COMMAND_ATTR = "command";

	private String _inlineCommand = null;
	private String _cdataCommand = null;
	private File _file = null;
	private boolean _template = false;
	private int _validator = 0;

	public String getShellCommand() throws SshException {
		if (_validator == 0 || _validator > 1) {
			throw new SshException(Msg.bind(Messages.SshEx_VALIDATION_ERR,
					INLINE_COMMAND_ATTR, FILE_ATTR));
		}

		if (getInlineCommand() != null) {
			return getInlineCommand() + "\n";
		}
		if (getCDataCommand() != null) {
			return getCDataCommand() + "\n";
		}
		// if getFile() != null
		String fileContent = null;
		if (getTemplate()) {
			try {
				fileContent = Melody.getContext().expand(
						Paths.get(getFile().toString()));
			} catch (IllegalFileException Ex) {
				throw new RuntimeException("Unexpected error while "
						+ "templating the file " + getFile() + "."
						+ "Source code has certainly been modified "
						+ "and a bug have been introduced.", Ex);
			} catch (ExpressionSyntaxException Ex) {
				throw new SshException(Ex);
			} catch (IOException Ex) {
				throw new SshException(Msg.bind(Messages.SshEx_READ_IO_ERROR,
						getFile()), Ex);
			}
		} else {
			try {
				fileContent = new String(Files.readAllBytes(Paths.get(getFile()
						.toString())));
			} catch (IOException Ex) {
				throw new SshException(Msg.bind(Messages.SshEx_READ_IO_ERROR,
						getFile()), Ex);
			}
		}
		return fileContent + "\n";
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

	public File getFile() {
		return _file;
	}

	@Attribute(name = FILE_ATTR)
	public File setFile(WrapperFile f) throws IllegalFileException {
		if (f == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + File.class.getCanonicalName()
					+ " (which list shell commands).");
		}
		FS.validateFileExists(f.toString());
		_validator++;
		File previous = getFile();
		_file = f;
		return previous;
	}

	public boolean getTemplate() {
		return _template;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean c) {
		boolean previous = getTemplate();
		_template = c;
		return previous;
	}

}