package com.wat.melody.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.LevelRangeFilter;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ProcessorManagerFactory;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetsFilterException;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ProcessorManagerFactoryException;
import com.wat.melody.cli.exception.CommandLineParsingException;
import com.wat.melody.cli.exception.ConfigurationLoadingException;
import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.filter.Filter;
import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.log.LogThreshold;
import com.wat.melody.common.log.exception.IllegalLogThresholdException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.order.OrderName;
import com.wat.melody.common.order.OrderNameSet;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.properties.exception.IllegalPropertiesSetException;
import com.wat.melody.common.systool.SysTool;
import com.wat.melody.common.xpath.exception.XPathFunctionResolverLoadingException;
import com.wat.melody.common.xpath.exception.XPathNamespaceContextResolverLoadingException;

/**
 * <p>
 * Create and initialize an {@link IProcessorManager} with Configuration
 * Directives found in a Global Configuration File and/or with Options found in
 * a Command Line.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 * @see com.wat.melody.api.IProcessorManager
 */
public class ProcessorManagerLoader {

	/**
	 * Name of the java option which specifies the Default Global Configuration
	 * File.
	 */
	public static String DEFAULT_CONFIGURATION_FILE_PROPERTY = "melody.default.global.configuration.file";

	/**
	 * Hard-Coded Default Global Configuration File. Use if no User-Defined
	 * Global Configuration File is defined (via option -C), and if no Default
	 * Global Configuration File is defined (via java option).
	 */
	public static String DEFAULT_CONFIGURATION_FILE = SysTool.CWD
			+ "/config/melody.properties";

	// MANDATORY CONFIGURATION DIRECTIVE
	// must be defined in the global configuration file
	public static final String PROCESSOR_MANAGER_CLASS = "processorManagerCanonicalClassName";
	public static final String LOGGING_CONFIG_FILE = "loggingConfigurationFile";
	public static final String LOGGING_VARIABLES_TO_SUBSTITUTE = "loggingVariablesToSubstitute";
	public static final String WORKING_FOLDER_PATH = "workingFolderPath";
	public static final String MAX_SIMULTANEOUS_STEP = "maxSimultaneousStep";
	public static final String HARD_KILL_TIMEOUT = "hardKillTimeout";

	// OPTIONNAL CONFIGURATION DIRECTIVE
	// can be defined in the global configuration file
	public static final String TASK_DIRECTIVES = "tasks.directives";
	public static final String PLUGIN_CONF_DIRECTIVES = "plugin.configuration.directives";
	public static final String XPATH_NAMESPACE_DIRECTIVES = "xpath.namespace.directives";
	public static final String XPATH_FUNCTION_DIRECTIVES = "xpath.function.directives";

	public static final String RESOURCES_DESCRIPTORS = "resourcesDescriptors";
	public static final String BATCH_MODE = "batchMode";
	public static final String PRESERVE_TEMPORARY_FILES_MODE = "preserveTemporaryFilesMode";
	public static final String RUN_DRY_MODE = "runDryMode";
	public static final String SEQUENCE_DESCRIPTOR_FILE_PATH = "sequenceDescriptorFilePath";
	public static final String ORDERS = "orders";
	public static final String PROPERTIES = "properties";
	public static final String RESOURCES_FILTERS = "resourcesFilters";
	public static final String TARGETS_FILTERS = "targetFilters";

	private IProcessorManager _processorManager;

	private static final String CONSOLE_APPENDER = "console";

	/**
	 * <p>
	 * Create a new {@link ProcessorManagerLoader} object.
	 * </p>
	 */
	public ProcessorManagerLoader() {
	}

	/**
	 * @return the inner {@link IProcessorManager} object.
	 */
	public IProcessorManager getProcessorManager() {
		return _processorManager;
	}

	/**
	 * <p>
	 * Set the inner {@link IProcessorManager} object to the given
	 * {@link IProcessorManager}.
	 * </p>
	 * 
	 * @return the previous inner {@link IProcessorManager} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link IProcessorManager} is <tt>null</tt>.
	 */
	private IProcessorManager setProcessorManager(IProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ IProcessorManager.class.getCanonicalName() + ".");
		}
		IProcessorManager previous = getProcessorManager();
		_processorManager = pm;
		return previous;
	}

	/**
	 * <p>
	 * Initialize the inner {@link IProcessorManager} object with the
	 * Configuration Directives found in the Global Configuration File and with
	 * the Options found in the Command Line.
	 * </p>
	 * <p>
	 * First, load Configuration Directives found in the Global Configuration
	 * File, then load all Options found in the Command Line.
	 * </p>
	 * <p>
	 * <i> A User-Defined Global Configuration File can be provided in the
	 * Command Line using the Option </i><tt>-C</tt><i>. If a User-Defined
	 * Global Configuration File is not explicitly provided in the Command Line,
	 * then the Default Global Configuration File is used. This Default Global
	 * Configuration File is located in {@link #DEFAULT_CONFIGURATION_FILE},
	 * relatively to the current working directory.</i>
	 * </p>
	 * <p>
	 * Command Line's available Options are :
	 * <ul>
	 * <li><tt>-C &lt;Global Configuration File Path&gt;</tt><br/>
	 * Load the specified Global Configuration File (see
	 * {@link #loadGlobalConfigurationFile(String)} ;</li>
	 * 
	 * <li><tt>-q</tt><br/>
	 * Decrease the log threshold (see
	 * {@link IProcessorManager#decreaseLogThreshold()} ;</li>
	 * 
	 * <li><tt>-v</tt><br/>
	 * Increase the log threshold (see
	 * {@link IProcessorManager#increaseLogThreshold()} ;</li>
	 * 
	 * <li><tt>-E &lt;Resources Descriptor File Path&gt;</tt><br/>
	 * Add the given resource to the Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#add(String)}) ;</li>
	 * 
	 * <li><tt>-f &lt;Sequence Descriptor File Path&gt;</tt><br/>
	 * Set the path of the Sequence Descriptor with the given value (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#load(String)}) ;</li>
	 * 
	 * <li><tt>-F &lt;Filter&gt;</tt><br/>
	 * Add the given Filter to the Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#addFilter(String)}) ;</li>
	 * 
	 * <li><tt>-T &lt;Filter&gt;</tt><br/>
	 * Add the given target Filter to the Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#addTargetFilter(String)}) ;</li>
	 * 
	 * <li><tt>-o &lt;Order&gt;</tt><br/>
	 * Add the given Order to the Sequence Descriptor (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addOrder(String)}) ;</li>
	 * 
	 * <li><tt>-B</tt><br/>
	 * Enable 'Batch Mode' (see {@link IProcessorManager#enableBatchMode()}) ;</li>
	 * 
	 * <li><tt>-b</tt><br/>
	 * Disable 'Batch Mode' (see {@link IProcessorManager#disableBatchMode()}) ;
	 * </li>
	 * 
	 * <li><tt>-P</tt><br/>
	 * Enable 'Preserve Temporary Files Mode' (see
	 * {@link IProcessorManager#enablePreserveTemporaryFilesMode()}) ;</li>
	 * 
	 * <li><tt>-p</tt><br/>
	 * Disable 'Preserve Temporary Files Mode' (see
	 * {@link IProcessorManager#disablePreserveTemporaryFilesMode()}) ;</li>
	 * 
	 * <li><tt>-D</tt><br/>
	 * Enable 'Run Dry Mode' (see {@link IProcessorManager#enableRunDryMode()})
	 * ;</li>
	 * 
	 * <li><tt>-d</tt><br/>
	 * Disable 'Run Dry Mode' (see {@link IProcessorManager#disableRunDryMode()}
	 * ) ;</li>
	 * 
	 * <li><tt>-V &lt;Property&gt;</tt><br/>
	 * Add the given Property to the Sequence Descriptor (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addProperty(Property)}) ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line to parse.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Command Line is <tt>null</tt>.
	 * @throws IOException
	 *             if an IO error occurred.
	 * @throws CommandLineParsingException
	 *             <ul>
	 *             <li>if the Command Line is not properly formatted (e.g. an
	 *             Option's value is missing, an Option's specifier is missing)
	 *             ;</li>
	 *             <li>if Option </tt>-C</tt> appears multiple times in the
	 *             Command Line ;</li>
	 *             <li>if Option </tt>-C</tt>'s value is missing ;</li>
	 *             <li>if an Option's specifier is not accepted ;</li>
	 *             <li>if an Option's value is not accepted ;</li>
	 *             </ul>
	 * @throws ConfigurationLoadingException
	 *             <ul>
	 *             <li>if Global Configuration File path doesn't points to a
	 *             valid file (e.g. a directory, a non readable file, a non
	 *             writable file, a non existent file) ;</li>
	 *             <li>if Global Configuration File is not a valid
	 *             {@link PropertySet} (e.g. Configuration Directives
	 *             duplicated, invalid Configuration Directive name, invalid
	 *             escape character, circular reference during expansion
	 *             process, ...) ;</li>
	 *             <li>if Global Configuration File's content is not valid (e.g.
	 *             mandatory Configuration Directives are missing, a
	 *             Configuration Directives's value is not valid, ...) ;</li>
	 *             </ul>
	 */
	public IProcessorManager parseCommandLine(String[] cmdLine)
			throws ConfigurationLoadingException, CommandLineParsingException,
			IOException {
		if (cmdLine == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid String[] (a Command Line).");
		}
		try {
			String sGCFilePath = retrieveUserDefinedGlobalConfigurationFilePath(cmdLine);
			if (sGCFilePath == null) {
				/*
				 * If the user don't explicitly specify a Global Configuration
				 * File (using option -C ) => the default one is used, defined
				 * in a java option. If not provided as a java option => a hard
				 * coded one is used.
				 */
				sGCFilePath = System.getProperty(
						DEFAULT_CONFIGURATION_FILE_PROPERTY,
						DEFAULT_CONFIGURATION_FILE);
			}
			loadGlobalConfigurationFile(sGCFilePath);

			int firstArg = parseOptions(cmdLine);
			// If the Command Line contains Arguments
			// => raise an error
			if (firstArg < cmdLine.length) {
				throw new CommandLineParsingException(Msg.bind(
						Messages.CmdEx_UNKNOWN_ARGUMENT_ERROR,
						cmdLine[firstArg]));
			}

			// ensure a sequence descriptor have been defined
			if (getProcessorManager().getSequenceDescriptor().getSourceFile() == null) {
				throw new CommandLineParsingException(Messages.CmdEx_MISSING_SD);
			}

			return getProcessorManager();
		} catch (CommandLineParsingException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_GENERIC_PARSE, Arrays.asList(cmdLine)), Ex);
		}
	}

	private Pattern C_OPTION_FINDER = Pattern.compile("^-(\\w*)C\\w*$");

	/**
	 * <p>
	 * Extract the User-Defined Global Configuration File from the given Command
	 * Line.
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line.
	 * 
	 * @return the Global Configuration File Path (if provided through the
	 *         Command Line using the option <tt>-C</tt>), or <tt>null</tt> (if
	 *         option <tt>-C</tt> was not provided in the Command Line).
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Command Line is <tt>null</tt>.
	 * @throws CommandLineParsingException
	 *             <ul>
	 *             <li>if Option </tt>-C</tt> appears multiple times in the
	 *             Command Line ;</li>
	 *             <li>if Option </tt>-C</tt>'s value is missing ;</li>
	 *             </ul>
	 */
	public String retrieveUserDefinedGlobalConfigurationFilePath(
			String[] cmdLine) throws CommandLineParsingException {
		if (cmdLine == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid String[] (a Command Line).");
		}
		String sUserDefinedGCFilePath = null;
		for (int i = 0; i < cmdLine.length; i++) {
			Matcher match = C_OPTION_FINDER.matcher(cmdLine[i]);
			if (match.matches()) {
				if (sUserDefinedGCFilePath != null) {
					throw new CommandLineParsingException(
							Messages.CmdEx_MULTIPLE_GLOBAL_CONF_FILE_ERROR);
				}
				i += match.group(1).length() + 1;
				try {
					if (cmdLine[i].equals("--")) {
						throw new CommandLineParsingException(Msg.bind(
								Messages.CmdEx_MISSING_OPTION_VALUE, 'C'));
					}
					sUserDefinedGCFilePath = cmdLine[i];
				} catch (ArrayIndexOutOfBoundsException Ex) {
					throw new CommandLineParsingException(Msg.bind(
							Messages.CmdEx_MISSING_OPTION_VALUE, 'C'));
				}
			}
		}
		return sUserDefinedGCFilePath;
	}

	/**
	 * <p>
	 * Initialize the inner {@link IProcessorManager}'s members with the Options
	 * found in the Command Line.
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line.
	 * 
	 * @return the index of the first Argument of the Command Line.
	 * 
	 * @throws NullPointerException
	 *             if the given Command Line is <tt>null</tt>.
	 * @throws IOException
	 *             if an IO error occurred.
	 * @throws CommandLineParsingException
	 *             <ul>
	 *             <li>if the Command Line is not properly formatted (e.g. an
	 *             Option's value is missing, an Option's specifier is missing)
	 *             ;</li>
	 *             <li>if an Option's specifier is not accepted ;</li>
	 *             <li>if an Option's value is not accepted ;</li>
	 *             </ul>
	 */
	private int parseOptions(String[] cmdLine)
			throws CommandLineParsingException, IOException {
		for (int i = 0; i < cmdLine.length; i++) {
			// end of option list => exit option parsing loop
			if (cmdLine[i].length() == 0 || cmdLine[i].charAt(0) != '-') {
				return i;
			}
			String sOpt = cmdLine[i].substring(1);
			// If the option is empty
			// => raise an error
			if (sOpt.length() == 0) {
				throw new CommandLineParsingException(
						Messages.CmdEx_MISSING_OPTION_SPECIFIER);
			}
			// '--' means this is the end of option list
			// => exit option parsing loop
			if (sOpt.charAt(0) == '-') {
				return i + 1; // +1 because : must go forward the '--'
			}
			// For each option in the Command Line ...
			for (int j = 0; j < sOpt.length(); j++)
				try {
					i = parseOption(cmdLine, i, sOpt.charAt(j));
				} catch (ArrayIndexOutOfBoundsException Ex) {
					throw new CommandLineParsingException(
							Msg.bind(Messages.CmdEx_MISSING_OPTION_VALUE,
									sOpt.charAt(j)));
				}
		}
		return cmdLine.length;
	}

	private int parseOption(String[] cmdLine, int i, char cOpt)
			throws CommandLineParsingException, IOException {
		IProcessorManager pm = getProcessorManager();
		switch (cOpt) {
		case 'B':
			pm.enableBatchMode();
			return i;
		case 'b':
			pm.disableBatchMode();
			return i;
		case 'P':
			pm.enablePreserveTemporaryFilesMode();
			return i;
		case 'p':
			pm.disablePreserveTemporaryFilesMode();
			return i;
		case 'D':
			pm.enableRunDryMode();
			return i;
		case 'd':
			pm.disableRunDryMode();
			return i;
		case 'q':
			return parseIncreaseLogThreshold(cmdLine, i);
		case 'v':
			return parseDecreaseLogThreshold(cmdLine, i);
		case 'F':
			return parseResourcesFilter(cmdLine, i);
		case 'T':
			return parseTargetFilter(cmdLine, i);
		case 'o':
			return parseOrder(cmdLine, i);
		case 'E':
			return parseResourcesDescriptor(cmdLine, i);
		case 'f':
			return parseSequenceDescriptor(cmdLine, i);
		case 'V':
			return parseProperties(cmdLine, i);
		case 'C': // Configuration File has already been loaded
			return ++i;
		default:
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_UNKNOWN_OPTION_SPECIFIER, cOpt));
		}
	}

	private int parseIncreaseLogThreshold(String[] cmdLine, int i)
			throws CommandLineParsingException {
		try {
			Logger logger = Logger.getRootLogger();
			Appender appender = logger.getAppender(CONSOLE_APPENDER);
			LevelRangeFilter filter = null;
			filter = (LevelRangeFilter) appender.getFilter();
			filter.setLevelMin(LogThreshold.increase(filter.getLevelMin()));
			filter.activateOptions();
		} catch (IllegalLogThresholdException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_TOOMUCH_LOG_THRESHOLD, 'q'), Ex);
		}

		return i;
	}

	private int parseDecreaseLogThreshold(String[] cmdLine, int i)
			throws CommandLineParsingException {
		try {
			Logger logger = Logger.getRootLogger();
			Appender appender = logger.getAppender(CONSOLE_APPENDER);
			LevelRangeFilter filter = (LevelRangeFilter) appender.getFilter();
			filter.setLevelMin(LogThreshold.decrease(filter.getLevelMin()));
			filter.activateOptions();
		} catch (IllegalLogThresholdException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_TOOMUCH_LOG_THRESHOLD, 'v'), Ex);
		}
		return i;
	}

	private int parseResourcesFilter(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'F'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addFilter(
					Filter.parseFilter(cmdLine[i]));
		} catch (IllegalTargetsFilterException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'F'), Ex);
		}
		return i;
	}

	private int parseTargetFilter(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'T'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addTargetFilter(
					Filter.parseFilter(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		}
		return i;
	}

	private int parseOrder(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'o'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().addOrder(
					OrderName.parseString(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'o'), Ex);
		}
		return i;
	}

	private int parseResourcesDescriptor(String[] cmdLine, int i)
			throws CommandLineParsingException, IOException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'E'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().add(cmdLine[i]);
		} catch (IllegalTargetsFilterException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		} catch (IllegalResourcesFilterException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'F'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'E'), Ex);
		}
		return i;
	}

	private int parseSequenceDescriptor(String[] cmdLine, int i)
			throws CommandLineParsingException, IOException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'f'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().load(cmdLine[i]);
		} catch (IllegalOrderException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'o'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'f'), Ex);
		}
		return i;
	}

	private int parseProperties(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'V'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().addProperty(
					Property.parseProperty(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Msg.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'V'), Ex);
		}
		return i;
	}

	/**
	 * <p>
	 * Initialize the inner {@link IProcessorManager}'s members with the
	 * Configuration Directives found in the Global Configuration File.
	 * </p>
	 * <p>
	 * The Global Configuration File must be a properties set, as defined in by
	 * the {@link PropertySet} class.
	 * </p>
	 * <p>
	 * Global Configuration File's available Configuration Directives are :
	 * <ul>
	 * <li><tt>processorManagerCanonicalClassName</tt><br/>
	 * Defines the {@link IProcessorManager} implementation to use ;</li>
	 * 
	 * <li><tt>tasks.directives</tt><br/>
	 * Defines the canonical class name of all {@link ITask} implementation to
	 * use ;</li>
	 * 
	 * <li><tt>plugin.configuration.directives</tt><br/>
	 * Defines the path of the Plug-In configuration file to load ;</li>
	 * 
	 * <li><tt>xpath.namespace.directives</tt><br/>
	 * Defines the namespaces of the custom XPath Functions ;</li>
	 * 
	 * <li><tt>xpath.function.directives</tt><br/>
	 * Defines the custom XPath Functions ;</li>
	 * 
	 * <li><tt>workingFolderPath</tt><br/>
	 * Set the Working Folder Path to the given value (see
	 * {@link IProcessorManager#setWorkingFolderPath(String)}) ;</li>
	 * 
	 * <li><tt>maxSimultaneousStep</tt><br/>
	 * Set the maximum number of parallel worker (see
	 * {@link IProcessorManager#setMaxSimultaneousStep(int)}) ;</li>
	 * 
	 * <li><tt>hardKillTimeout</tt><br/>
	 * Set the maximum amount of seconds a worker will be waited before killed
	 * (see {@link IProcessorManager#setHardKillTimeout(int)}) ;</li>
	 * 
	 * <li><tt>loggingConfigurationFile</tt><br/>
	 * Set the path of the log4j configuration file ;</li>
	 * 
	 * <li><tt>loggingVariablesToSubstitute</tt><br/>
	 * Set the name of log4j properties to add to system properties, so that
	 * they can be substitute in the log4j configuration file ;</li>
	 * 
	 * <li><tt>resourcesDescriptors</tt><br/>
	 * add the given resources to the Resources Descriptors (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#add(String)}) ;</li>
	 * 
	 * <li><tt>batchMode</tt><br/>
	 * Enable/disable 'Batch Mode' (see
	 * {@link IProcessorManager#disableBatchMode()} ;</li>
	 * 
	 * <li><tt>preserveTemporaryFilesMode</tt><br/>
	 * Enable/disable 'Preserve Temporary Files Mode' (see
	 * {@link IProcessorManager#disablePreserveTemporaryFilesMode()}) ;</li>
	 * 
	 * <li><tt>runDryMode</tt><br/>
	 * Enable/disable 'Run Dry Mode' (see
	 * {@link IProcessorManager#disableRunDryMode()} ;</li>
	 * 
	 * <li><tt>sequenceDescriptorFilePath</tt><br/>
	 * Set the path of the Sequence Descriptor with the given value (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#load(String)}) ;</li>
	 * 
	 * <li><tt>orders</tt><br/>
	 * Add the given orders to the Sequence Descriptor (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addOrders(String)}) ;</li>
	 * 
	 * <li><tt>properties</tt><br/>
	 * Add the given properties to the Sequence Descriptor (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addProperty(Property)}) ;</li>
	 * 
	 * <li><tt>resourcesFilters</tt><br/>
	 * Add the given filters to the Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#setFilter(String)}) ;</li>
	 * 
	 * <li><tt>targetFilters</tt><br>
	 * Add the given target filters to the Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#setTargetFilter(String)}) ;<br/>
	 * </ul>
	 * </p>
	 * 
	 * @param gcfPath
	 *            is the path of the Global Configuration File.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IOException
	 *             if an IO error occurred.
	 * @throws ConfigurationLoadingException
	 *             <ul>
	 *             <li>if the given path doesn't points to a valid file (e.g. a
	 *             directory, a non readable file, a non writable file, a non
	 *             existent file) ;</li>
	 *             <li>if the given path points to a file which is not a valid
	 *             {@link PropertySet} (e.g. Configuration Directives
	 *             duplicated, invalid Configuration Directive name, invalid
	 *             escape character, circular reference during expansion
	 *             process, ...) ;</li>
	 *             <li>if the given path points to a file which is not a valid
	 *             Global Configuration File (e.g. mandatory Configuration
	 *             Directives are missing, a Configuration Directives's value is
	 *             not valid, ...) ;</li>
	 *             </ul>
	 */
	public void loadGlobalConfigurationFile(String gcfPath)
			throws ConfigurationLoadingException, IOException {
		try {
			PropertySet oProps = new PropertySet(gcfPath);

			// Mandatory Configuration Directives
			loadLoggingVariablesToSubstitute(oProps);
			loadLoggingConfigFile(oProps);
			loadProcessorManagerClass(oProps);
			loadWorkingFolderPath(oProps);
			loadMaxPar(oProps);
			loadHardKillTimeout(oProps);

			// Optional Configuration Directives
			loadAllXPathNamespaceDefinitions(oProps);
			loadAllXPathFunctionDefinitions(oProps);

			loadResourcesDescriptors(oProps);
			loadBatchMode(oProps);
			loadPreserveTmpFileMode(oProps);
			loadRunDryMode(oProps);
			loadSequenceDescriptor(oProps);
			loadOrderNames(oProps);
			loadProperties(oProps);
			loadResourcesFilters(oProps);
			loadTargetsFilters(oProps);

			registerAllPlugIns(oProps);
			loadAllPlugInsConfiguration(oProps);
		} catch (IllegalFileException | IllegalPropertiesSetException
				| ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_GENERIC_GLOBAL_CONF_LOAD, gcfPath), Ex);
		}
	}

	private void loadLoggingVariablesToSubstitute(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(LOGGING_VARIABLES_TO_SUBSTITUTE)) {
			return;
		}
		String val = oProps.get(LOGGING_VARIABLES_TO_SUBSTITUTE);
		if (val.trim().length() == 0) {
			return;
		}
		try {
			for (String vtsd : val.split(",")) {
				vtsd = vtsd.trim();
				if (vtsd.length() == 0) {
					continue;
				}
				loadLoggingVariableToSubstitute(oProps, vtsd);
			}
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					LOGGING_VARIABLES_TO_SUBSTITUTE), Ex);
		}
	}

	private void loadLoggingVariableToSubstitute(PropertySet oProps, String vtsd)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(vtsd)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, vtsd));
		}
		try {
			String val = oProps.get(vtsd);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			System.setProperty(vtsd, val);
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, vtsd), Ex);
		}
	}

	private void loadLoggingConfigFile(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(LOGGING_CONFIG_FILE)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, LOGGING_CONFIG_FILE));
		}
		try {
			String val = oProps.get(LOGGING_CONFIG_FILE);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			FS.validateFileExists(val);
			org.apache.log4j.xml.DOMConfigurator.configure(val);
		} catch (MelodyException | FactoryConfigurationError Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, LOGGING_CONFIG_FILE), Ex);
		}
	}

	private void loadProcessorManagerClass(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(PROCESSOR_MANAGER_CLASS)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, PROCESSOR_MANAGER_CLASS));
		}
		try {
			String val = oProps.get(PROCESSOR_MANAGER_CLASS);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			System.setProperty(
					ProcessorManagerFactory.PROCESSOR_MANAGER_IMPL_KEY, val);
			ProcessorManagerFactory pmf = null;
			pmf = ProcessorManagerFactory.newInstance();
			setProcessorManager(pmf.newProcessorManager());
		} catch (ProcessorManagerFactoryException
				| ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(
					Msg.bind(Messages.ConfEx_INVALID_DIRECTIVE,
							PROCESSOR_MANAGER_CLASS), Ex);
		}

	}

	private void loadWorkingFolderPath(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(WORKING_FOLDER_PATH)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, WORKING_FOLDER_PATH));
		}
		try {
			String val = oProps.get(WORKING_FOLDER_PATH);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.setWorkingFolderPath(val);
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, WORKING_FOLDER_PATH), Ex);
		}

	}

	private void loadMaxPar(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(MAX_SIMULTANEOUS_STEP)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, MAX_SIMULTANEOUS_STEP));
		}
		try {
			String val = oProps.get(MAX_SIMULTANEOUS_STEP);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			try {
				IProcessorManager pm = getProcessorManager();
				pm.setMaxSimultaneousStep(Integer.parseInt(val));
			} catch (NumberFormatException Ex) {
				throw new ConfigurationLoadingException(Msg.bind(
						Messages.ConfEx_INVALID_INTEGER_FORMAT, val));
			}
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MAX_SIMULTANEOUS_STEP),
					Ex);
		}
	}

	private void loadHardKillTimeout(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(HARD_KILL_TIMEOUT)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, HARD_KILL_TIMEOUT));
		}
		try {
			String val = oProps.get(HARD_KILL_TIMEOUT);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			try {
				IProcessorManager pm = getProcessorManager();
				pm.setHardKillTimeout(Integer.parseInt(val));
			} catch (NumberFormatException Ex) {
				throw new ConfigurationLoadingException(Msg.bind(
						Messages.ConfEx_INVALID_INTEGER_FORMAT, val));
			}
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, HARD_KILL_TIMEOUT), Ex);
		}
	}

	private void loadResourcesDescriptors(PropertySet oProps)
			throws ConfigurationLoadingException, IOException {
		if (!oProps.containsKey(RESOURCES_DESCRIPTORS)) {
			return;
		}
		String val = oProps.get(RESOURCES_DESCRIPTORS);
		if (val.trim().length() == 0) {
			return;
		}
		try {
			for (String rdd : val.split(",")) {
				rdd = rdd.trim();
				if (rdd.length() == 0) {
					continue;
				}
				loadResourcesDescriptor(oProps, rdd);
			}
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_DESCRIPTORS),
					Ex);
		}
	}

	private void loadResourcesDescriptor(PropertySet oProps, String rdd)
			throws ConfigurationLoadingException, IOException {
		if (!oProps.containsKey(rdd)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, rdd));
		}
		try {
			String val = oProps.get(rdd);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().add(val);
		} catch (IllegalTargetsFilterException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		} catch (IllegalResourcesFilterException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_FILTERS), Ex);
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, rdd), Ex);
		}
	}

	private void loadBatchMode(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(BATCH_MODE)) {
			return;
		}
		try {
			String val = oProps.get(BATCH_MODE);
			IProcessorManager pm = getProcessorManager();
			pm.setBatchMode(Bool.parseString(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, BATCH_MODE), Ex);
		}
	}

	private void loadPreserveTmpFileMode(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(PRESERVE_TEMPORARY_FILES_MODE)) {
			return;
		}
		try {
			String val = oProps.get(PRESERVE_TEMPORARY_FILES_MODE);
			IProcessorManager pm = getProcessorManager();
			pm.setPreserveTemporaryFilesMode(Bool.parseString(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					PRESERVE_TEMPORARY_FILES_MODE), Ex);
		}
	}

	private void loadRunDryMode(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(RUN_DRY_MODE)) {
			return;
		}
		try {
			String val = oProps.get(RUN_DRY_MODE);
			IProcessorManager pm = getProcessorManager();
			pm.setRunDryMode(Bool.parseString(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RUN_DRY_MODE), Ex);
		}
	}

	private void loadSequenceDescriptor(PropertySet oProps)
			throws ConfigurationLoadingException, IOException {
		if (!oProps.containsKey(SEQUENCE_DESCRIPTOR_FILE_PATH)) {
			return;
		}
		try {
			String val = oProps.get(SEQUENCE_DESCRIPTOR_FILE_PATH);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().load(val);
		} catch (IllegalOrderException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ORDERS), Ex);
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					SEQUENCE_DESCRIPTOR_FILE_PATH), Ex);
		}
	}

	private void loadOrderNames(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(ORDERS)) {
			return;
		}
		try {
			String val = oProps.get(ORDERS);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().addOrders(
					OrderNameSet.parseOrderNameSet(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ORDERS), Ex);
		}
	}

	private void loadProperties(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(PROPERTIES)) {
			return;
		}
		try {
			String val = oProps.get(PROPERTIES);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			for (String p : val.split(",")) {
				p = p.trim();
				if (p.length() == 0) {
					continue;
				}
				loadProperty(oProps, p);
			}
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROPERTIES), Ex);
		}
	}

	private void loadProperty(PropertySet oProps, String p)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(p)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, p));
		}
		IProcessorManager pm = getProcessorManager();
		pm.getSequenceDescriptor().addProperty(oProps.getProperty(p));
	}

	private void loadResourcesFilters(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(RESOURCES_FILTERS)) {
			return;
		}
		try {
			String val = oProps.get(RESOURCES_FILTERS);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			for (String f : val.split(",")) {
				f = f.trim();
				if (f.length() == 0) {
					continue;
				}
				loadResourcesFilter(oProps, f);
			}
		} catch (IllegalTargetsFilterException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_FILTERS), Ex);
		}
	}

	private void loadResourcesFilter(PropertySet oProps, String f)
			throws ConfigurationLoadingException, IllegalTargetsFilterException {
		if (!oProps.containsKey(f)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, f));
		}
		try {
			String val = oProps.get(f);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addFilter(Filter.parseFilter(val));
		} catch (IllegalTargetsFilterException Ex) {
			throw Ex;
		} catch (ConfigurationLoadingException | IllegalFilterException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, f), Ex);
		}
	}

	private void loadTargetsFilters(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(TARGETS_FILTERS)) {
			return;
		}
		try {
			String val = oProps.get(TARGETS_FILTERS);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			for (String f : val.split(",")) {
				f = f.trim();
				if (f.length() == 0) {
					continue;
				}
				loadTargetsFilter(oProps, f);
			}
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		}
	}

	private void loadTargetsFilter(PropertySet oProps, String f)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(f)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, f));
		}
		try {
			String val = oProps.get(f);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor()
					.addTargetFilter(Filter.parseFilter(val));
		} catch (ConfigurationLoadingException | IllegalFilterException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, f), Ex);
		}
	}

	private void registerAllPlugIns(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(TASK_DIRECTIVES)) {
			return;
		}
		String tds = oProps.get(TASK_DIRECTIVES);
		if (tds.trim().length() == 0) {
			return;
		}
		for (String pi : tds.split(",")) {
			pi = pi.trim();
			if (pi.length() == 0) {
				continue;
			}
			registerAllPlugInClasses(oProps, pi);
		}
	}

	private void registerAllPlugInClasses(PropertySet oProps, String pi)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(pi)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_TASKS_DIRECTIVE, TASK_DIRECTIVES,
					pi, oProps.getSourceFile()));
		}
		String pics = oProps.get(pi);
		if (pics.trim().length() == 0) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_EMPTY_TASKS_DIRECTIVE, pi, TASK_DIRECTIVES));
		}
		for (String pic : pics.split(",")) {
			pic = pic.trim();
			if (pic.length() == 0) {
				continue;
			}
			registerPlugInClass(oProps, pi, pic);
		}
	}

	@SuppressWarnings("unchecked")
	private void registerPlugInClass(PropertySet oProps, String pi, String pic)
			throws ConfigurationLoadingException {
		IProcessorManager pm = getProcessorManager();
		IRegisteredTasks rts = pm.getRegisteredTasks();

		Class<ITask> c = null;
		try {
			c = (Class<ITask>) Class.forName(pic);
		} catch (ClassNotFoundException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_CNF_TASKS_DIRECTIVE, pi, pic));
		} catch (NoClassDefFoundError Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_NCDF_TASKS_DIRECTIVE, pi, pic, Ex
							.getMessage().replaceAll("/", ".")));
		}
		try {
			c.getConstructor().newInstance();
		} catch (NoSuchMethodException | IllegalAccessException
				| InstantiationException | ClassCastException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_IS_TASKS_DIRECTIVE, pi, pic,
					ITask.class.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_IE_TASKS_DIRECTIVE, pi, pic), Ex.getCause());
		}
		/*
		 * Will throw a RuntimeException if the given Class<ITask> doesn't
		 * respect ITask specification. Should only happened during Task
		 * Development. No need to catch/re-throw.
		 */
		rts.put(c);
	}

	private void loadAllPlugInsConfiguration(PropertySet oProps)
			throws ConfigurationLoadingException, IOException {
		if (!oProps.containsKey(PLUGIN_CONF_DIRECTIVES)) {
			return;
		}

		String pcds = oProps.get(PLUGIN_CONF_DIRECTIVES);
		if (pcds.trim().length() == 0) {
			return;
		}

		for (String pcd : pcds.split(",")) {
			pcd = pcd.trim();
			if (pcd.length() == 0) {
				continue;
			}
			loadPlugInConfiguration(oProps, pcd);
		}
	}

	private void loadPlugInConfiguration(PropertySet oProps, String pcd)
			throws ConfigurationLoadingException, IOException {
		String pcf = loadPlugInConfigurationDirective(oProps, pcd);
		PropertySet pcps = loadPlugInConfigurationFile(oProps, pcd, pcf);
		String pcc = findPlugInConfigurationClassName(oProps, pcd, pcps);
		Class<? extends IPlugInConfiguration> c = convertPlugInConfigurationClass(
				pcps, pcc);
		IPlugInConfiguration pc = instanciatePlugInConfiguration(pcps, c);
		registerPlugInConfiguration(pc);
	}

	private String loadPlugInConfigurationDirective(PropertySet oProps,
			String pcd) throws ConfigurationLoadingException {
		if (!oProps.containsKey(pcd)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_PLUGINS_DIRECTIVE,
					PLUGIN_CONF_DIRECTIVES, pcd, oProps.getSourceFile()));
		}
		String pcf = oProps.get(pcd);
		if (pcf.trim().length() == 0) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_EMPTY_PLUGINS_DIRECTIVE, pcd,
					PLUGIN_CONF_DIRECTIVES));
		}
		return pcf;
	}

	private PropertySet loadPlugInConfigurationFile(PropertySet oProps,
			String pcd, String pcf) throws ConfigurationLoadingException,
			IOException {
		PropertySet pcps = null;
		try {
			pcps = new PropertySet(pcf);
		} catch (IllegalFileException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_NVF_PLUGINS_DIRECTIVE, pcd, pcf), Ex);
		} catch (IllegalPropertiesSetException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_NVPS_PLUGINS_DIRECTIVE, pcd, pcf), Ex);
		}
		return pcps;
	}

	private String findPlugInConfigurationClassName(PropertySet oProps,
			String pcd, PropertySet pcps) throws ConfigurationLoadingException {
		if (!pcps.containsKey(IPlugInConfiguration.PLUGIN_CONF_CLASS)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_PCC_DIRECTIVE, pcd,
					pcps.getSourceFile(),
					IPlugInConfiguration.PLUGIN_CONF_CLASS));
		}
		String pcc = pcps.get(IPlugInConfiguration.PLUGIN_CONF_CLASS);
		if (pcc.trim().length() == 0) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_EMPTY_PCC_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile()));
		}
		return pcc;
	}

	@SuppressWarnings("unchecked")
	private Class<? extends IPlugInConfiguration> convertPlugInConfigurationClass(
			PropertySet pcps, String pcc) throws ConfigurationLoadingException {
		Class<? extends IPlugInConfiguration> c = null;
		try {
			c = (Class<? extends IPlugInConfiguration>) Class.forName(pcc);
		} catch (ClassNotFoundException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_CNF_CONF_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile(), pcc));
		} catch (NoClassDefFoundError Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_NCDF_CONF_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile(), pcc,
					Ex.getMessage().replaceAll("/", ".")));
		}
		IProcessorManager pm = getProcessorManager();
		if (pm.getPluginConfigurations().contains(c)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_DUPLICATE_CONF_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile(), c));
		}
		return c;
	}

	private IPlugInConfiguration instanciatePlugInConfiguration(
			PropertySet pcps, Class<? extends IPlugInConfiguration> c)
			throws ConfigurationLoadingException {
		IPlugInConfiguration pc = null;
		try {
			pc = c.getConstructor().newInstance();
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | ClassCastException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_IS_CONF_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile(), c.getClass().getCanonicalName(),
					IPlugInConfiguration.class.getCanonicalName()));
		} catch (InvocationTargetException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_IE_CONF_DIRECTIVE,
					IPlugInConfiguration.PLUGIN_CONF_CLASS,
					pcps.getSourceFile(), c.getClass().getCanonicalName()),
					Ex.getCause());
		}
		try {
			pc.load(pcps);
		} catch (PlugInConfigurationException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_GENERIC_PLUGIN_LOAD, pcps.getSourceFile()),
					Ex);
		}
		return pc;
	}

	private void registerPlugInConfiguration(IPlugInConfiguration pc) {
		IProcessorManager pm = getProcessorManager();
		pm.getPluginConfigurations().put(pc);
	}

	private void loadAllXPathNamespaceDefinitions(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(XPATH_NAMESPACE_DIRECTIVES)) {
			return;
		}

		String nscds = oProps.get(XPATH_NAMESPACE_DIRECTIVES);
		if (nscds.trim().length() == 0) {
			return;
		}

		for (String nscd : nscds.split(",")) {
			nscd = nscd.trim();
			if (nscd.length() == 0) {
				continue;
			}
			loadXPathNamespaceDefinitions(oProps, nscd);
		}
	}

	private void loadXPathNamespaceDefinitions(PropertySet oProps, String nscd)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(nscd)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, nscd));
		}

		try {
			String nss = oProps.get(nscd);
			if (nss.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			getProcessorManager().getXPathResolver().loadNamespaceDefinitions(
					oProps, nss.split(","));
		} catch (XPathNamespaceContextResolverLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, nscd), Ex);
		}
	}

	private void loadAllXPathFunctionDefinitions(PropertySet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(XPATH_FUNCTION_DIRECTIVES)) {
			return;
		}

		String fcds = oProps.get(XPATH_FUNCTION_DIRECTIVES);
		if (fcds.trim().length() == 0) {
			return;
		}

		for (String fcd : fcds.split(",")) {
			fcd = fcd.trim();
			if (fcd.length() == 0) {
				continue;
			}
			loadXPathFunctionDefinitions(oProps, fcd);
		}
	}

	private void loadXPathFunctionDefinitions(PropertySet oProps, String fcd)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(fcd)) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, fcd));
		}

		try {
			String nss = oProps.get(fcd);
			if (nss.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			getProcessorManager().getXPathResolver().loadFunctionDefinitions(
					oProps, nss.split(","));
		} catch (XPathFunctionResolverLoadingException Ex) {
			throw new ConfigurationLoadingException(Msg.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, fcd), Ex);
		}
	}

	/**
	 * <p>
	 * Delete temporary resources generated by this object.
	 * </p>
	 * 
	 * <p>
	 * <b>Should be called when this object and its inner
	 * {@link IProcessorManager} are no more necessary, in order to clean the
	 * hard disk.</b>
	 * </p>
	 * 
	 * @throws IOException
	 *             if an IO error occurred while deleting files.
	 */
	public void deleteTemporaryResources() throws IOException {
	}

}