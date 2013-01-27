package com.wat.melody.plugin.echo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.plugin.echo.exception.EchoException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Echo implements ITask {

	private static Log log = LogFactory.getLog(Echo.class);

	/**
	 * The 'echo' XML element used in the Sequence Descriptor
	 */
	public static final String ECHO = "echo";

	/**
	 * The 'message' XML attribute of the 'echo' XML element
	 */
	public static final String MESSAGE_ATTR = "message";

	/**
	 * The 'file' XML attribute of the 'echo' XML element
	 */
	public static final String FILE_ATTR = "file";

	/**
	 * The 'append' XML attribute of the 'echo' XML element
	 */
	public static final String APPEND_ATTR = "append";

	/**
	 * The 'createParentDir' XML attribute of the 'echo' XML element
	 */
	public static final String CREATE_PARENT_DIR_ATTR = "createParentDir";

	/**
	 * The 'severity' XML attribute of the 'echo' XML element
	 */
	public static final String SEVERITY_ATTR = "severity";

	private ITaskContext moContext;
	private String msMessage;
	private File moFile;
	private boolean mbAppend;
	private boolean mbCreateParentDir;
	private LogThreshold moSeverity;

	public Echo() {
		initContext();
		setMessage("");
		initFile();
		setAppend(false);
		setCreateParentDir(false);
		setSeverity(null);
	}

	private void initContext() {
		moContext = null;
	}

	private void initFile() {
		moFile = null;
	}

	@Override
	public void validate() throws EchoException {
		if (getFile() != null && getFile().getParentFile().exists() == false) {
			if (getCreateParentDir() == true) {
				if (!getFile().getParentFile().mkdirs()) {
					throw new RuntimeException(Messages.bind(
							Messages.EchoEx_FAILED_TO_CRAETE_PARENT_DIR,
							getFile().getPath()));
				}
			} else {
				try {
					FS.validateDirExists(getFile().getParentFile().toString());
				} catch (IllegalDirectoryException Ex) {
					throw new EchoException(Messages.bind(
							Messages.EchoEx_PARENT_DIR_NOT_EXISTS,
							new Object[] { getFile().getPath(), ECHO,
									CREATE_PARENT_DIR_ATTR }), Ex);
				}
			}
		}
	}

	/**
	 * <p>
	 * Send an event which contains the specified message with the specified
	 * severity. Or write the specified message into the specified file.
	 * </p>
	 * 
	 * <p>
	 * <i> * If an event is sent, it will be treat by all Listeners. <BR/>
	 * * The default Listeners will print the message to the standard output,
	 * regarding its severity. <BR/>
	 * * If a file is specified, the message will be write in it. <BR/>
	 * * If the file doesn't exists, it will be created. <BR/>
	 * * If append is set to true, the message will be appended to end of the
	 * file. <BR/>
	 * * If createParentDir is set to true, all necessary parent directories
	 * will be created. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the processing of this task was interrupted.
	 */
	@Override
	public void doProcessing() throws EchoException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		String logMsg = ECHO + " message:'" + getMessage() + "', location:";
		if (getFile() != null) {
			log.debug(logMsg + getFile());
			FileWriter fw = null;
			try {
				try {
					fw = new FileWriter(getFile(), getAppend());
					fw.write(getMessage() + Util.NEW_LINE);
				} finally {
					if (fw != null) {
						fw.close();
					}
				}
			} catch (IOException Ex) {
				throw new RuntimeException(Messages.bind(
						Messages.EchoEx_IO_ERROR, ECHO), Ex);
			}
		} else if (getSeverity() != null) {
			log(getSeverity(), logMsg + "logger");
		} else {
			log.debug(logMsg + "console");
			System.out.println(getMessage());
		}
	}

	private void log(LogThreshold severity, String msg) {
		if (severity != null) {
			switch (severity) {
			case ALL:
				log.trace(msg);
				break;
			case TRACE:
				log.trace(msg);
				break;
			case DEBUG:
				log.debug(msg);
				break;
			case INFO:
				log.info(msg);
				break;
			case WARNING:
				log.warn(msg);
				break;
			case ERROR:
				log.error(msg);
				break;
			case FATAL:
				log.fatal(msg);
			case OFF:
				break;
			}
		}
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;
	}

	private String getMessage() {
		return msMessage;
	}

	@Attribute(name = MESSAGE_ATTR)
	public String setMessage(String v) {
		// can be null
		String previous = getMessage();
		msMessage = v;
		return previous;
	}

	private File getFile() {
		return moFile;
	}

	@Attribute(name = FILE_ATTR, description = "The '"
			+ FILE_ATTR
			+ "' attribute of the '"
			+ ECHO
			+ "' Task defines the file where the message will be written.\nIf this attribute is not specified, the message will be displayed in the standard output.")
	public File setFile(File f) throws IllegalFileException,
			IllegalDirectoryException {
		FS.validateFilePath(f.getPath());
		File previous = getFile();
		moFile = f;
		return previous;
	}

	private boolean getAppend() {
		return mbAppend;
	}

	@Attribute(name = APPEND_ATTR)
	public boolean setAppend(boolean b) {
		boolean previous = getAppend();
		mbAppend = b;
		return previous;
	}

	private boolean getCreateParentDir() {
		return mbCreateParentDir;
	}

	@Attribute(name = CREATE_PARENT_DIR_ATTR)
	public boolean setCreateParentDir(boolean b) {
		boolean previous = getCreateParentDir();
		mbCreateParentDir = b;
		return previous;
	}

	public LogThreshold getSeverity() {
		return moSeverity;
	}

	@Attribute(name = SEVERITY_ATTR)
	public LogThreshold setSeverity(LogThreshold l) {
		// can be null
		LogThreshold previous = getSeverity();
		moSeverity = l;
		return previous;
	}

}