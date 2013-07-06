package com.wat.melody.plugin.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.ssh.common.AbstractSshManagedOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.plugin.ssh.common.types.Exec;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Ssh extends AbstractSshManagedOperation {

	private static Logger log = LoggerFactory.getLogger(Ssh.class);

	/**
	 * Task's name
	 */
	public static final String SSH = "ssh";

	/**
	 * Task's nested element, which specifies a variable declaration.
	 */
	public static final String DECLARE_NE = "declare";

	/**
	 * Task's nested element, which specifies an export declaration.
	 */
	public static final String EXPORT_NE = "export";

	/**
	 * Task's attribute, which defines a short description. This description
	 * will be included in each message logged. It can help to grep the log
	 * file.
	 */
	public static final String DESCRIPTION_ATTR = "description";

	/**
	 * Task's attribute, which specifies if the ssh command execution will set a
	 * tty or not.
	 */
	public static final String REQUIRETTY_ATTR = "requiretty";

	private String _commandToExecute;
	private String _description;
	private boolean _requiretty;

	public Ssh() {
		super();
		_commandToExecute = "";
		setDescription("[exec ssh]");
		setRequiretty(false);
	}

	@Override
	public void validate() throws SshException {
		super.validate();
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		int exitStatus = execSshCommand(getCommandToExecute(), getRequiretty(),
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
			throw new SshException(recapMsg + "--->   FAIL   <---");
		default:
			log.error(recapMsg + "---> CRITICAL <---  [" + exitStatus + "]");
			throw new SshException(recapMsg + "---> CRITICAL <---  ["
					+ exitStatus + "]" + SysTool.NEW_LINE
					+ "Here is the complete script which generates the error :"
					+ SysTool.NEW_LINE + "-----" + SysTool.NEW_LINE
					+ getCommandToExecute().trim() + SysTool.NEW_LINE + "-----"
					+ SysTool.NEW_LINE);
		}
	}

	public String getCommandToExecute() {
		return _commandToExecute;
	}

	public String getDescription() {
		return _description;
	}

	@Attribute(name = DESCRIPTION_ATTR)
	public String setDescription(String d) {
		String previous = getDescription();
		_description = d;
		return previous;
	}

	public boolean getRequiretty() {
		return _requiretty;
	}

	@Attribute(name = REQUIRETTY_ATTR)
	public boolean setRequiretty(boolean requiretty) {
		boolean previous = getRequiretty();
		_requiretty = requiretty;
		return previous;
	}

	@NestedElement(name = Exec.EXEC, type = Type.ADD)
	public void addInsludeScript(Exec is) throws SshException {
		if (is == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid IncludeScript.");
		}
		if (is.getCommand() != null && is.getFile() != null) {
			throw new SshException(Msg.bind(
					Messages.SshEx_BOTH_COMMAND_OR_SCRIPT_ATTR,
					Exec.COMMAND_ATTR, Exec.FILE_ATTR));
		} else if (is.getCommand() != null) {
			_commandToExecute += is.getCommand() + "\n";
		} else if (is.getFile() != null) {
			try {
				String fileContent = null;
				if (is.getTemplate()) {
					try {
						fileContent = Melody.getContext().expand(
								Paths.get(is.getFile().toString()));
					} catch (IllegalFileException Ex) {
						throw new RuntimeException("Unexpected error while "
								+ "templating the file " + is.getFile() + "."
								+ "Source code has certainly been modified "
								+ "and a bug have been introduced.", Ex);
					} catch (ExpressionSyntaxException Ex) {
						throw new SshException(Ex);
					}
				} else {
					fileContent = new String(Files.readAllBytes(Paths.get(is
							.getFile().toString())));
				}
				_commandToExecute += fileContent + "\n";
			} catch (IOException Ex) {
				throw new SshException(Msg.bind(
						Messages.TransferEx_READ_IO_ERROR, is.getFile()), Ex);
			}
		} else {
			throw new SshException(Msg.bind(
					Messages.SshEx_MISSING_COMMAND_OR_SCRIPT_ATTR,
					Exec.COMMAND_ATTR, Exec.FILE_ATTR));
		}
	}

	@NestedElement(name = DECLARE_NE, type = Type.ADD, description = "The '"
			+ DECLARE_NE
			+ "' nested element of the '"
			+ SSH
			+ "' Task allow to declare a bash variable and to assign it a value.")
	public void addDeclareVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid DeclareVariable.");
		}
		_commandToExecute += "declare " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

	@NestedElement(name = EXPORT_NE, type = Type.ADD)
	public void addExportVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid DeclareVariable.");
		}
		_commandToExecute += "export " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

}