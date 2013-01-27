package com.wat.melody.plugin.ssh.common.types;

import java.io.File;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;

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
	 * The 'command' XML Attribute
	 */
	public static final String FILE_ATTR = "file";

	/**
	 * The 'command' XML Attribute
	 */
	public static final String TEMPLATE_ATTR = "template";

	/**
	 * The 'command' XML Attribute
	 */
	public static final String COMMAND_ATTR = "command";

	private String msCommand;
	private File msFile;
	private boolean mbTemplate;

	public String getCommand() {
		return msCommand;
	}

	@Attribute(name = COMMAND_ATTR)
	public String setCommand(String c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a String (a shell command).");
		}
		String previous = getCommand();
		msCommand = c;
		return previous;
	}

	public File getFile() {
		return msFile;
	}

	@Attribute(name = FILE_ATTR)
	public File setFile(File c) throws IllegalFileException {
		if (c == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a File (which list shell commands).");
		}
		FS.validateFileExists(c.toString());
		File previous = getFile();
		msFile = c;
		return previous;
	}

	public boolean getTemplate() {
		return mbTemplate;
	}

	@Attribute(name = TEMPLATE_ATTR)
	public boolean setTemplate(boolean c) throws IllegalFileException {
		boolean previous = getTemplate();
		mbTemplate = c;
		return previous;
	}

}
