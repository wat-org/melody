package com.wat.melody.plugin.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.plugin.ssh.common.AbstractSshManagedOperation;
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

	private String _commandToExecute = "";
	private String _description = "[exec ssh]";
	private boolean _requiretty = false;

	public Ssh() {
		super();
	}

	@Override
	public void validate() throws SshException {
		super.validate();
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

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
	public String setDescription(String description) {
		// if null => convert it to ""
		if (description == null) {
			description = "";
		}
		String previous = getDescription();
		_description = description;
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
					+ "Must be a valid " + Exec.class.getCanonicalName() + ".");
		}
		_commandToExecute += is.getShellCommand();
	}

	@NestedElement(name = DECLARE_NE, type = Type.ADD, description = "The '"
			+ DECLARE_NE
			+ "' nested element of the '"
			+ SSH
			+ "' Task allow to declare a bash variable and to assign it a value.")
	public void addDeclareVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Property.class.getCanonicalName()
					+ ".");
		}
		_commandToExecute += "declare " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

	@NestedElement(name = EXPORT_NE, type = Type.ADD, description = "The '"
			+ EXPORT_NE
			+ "' nested element of the '"
			+ SSH
			+ "' Task allow to export a bash variable and to assign it a value.")
	public void addExportVariable(Property p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Property.class.getCanonicalName()
					+ ".");
		}
		_commandToExecute += "export " + p.getName() + "=" + p.getValue()
				+ "\n";
	}

}