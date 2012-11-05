package com.wat.melody.cli;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.LevelRangeFilter;

import com.wat.melody.api.IPluginConfiguration;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ISequenceDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ProcessorManagerFactory;
import com.wat.melody.api.exception.IllegalOrderException;
import com.wat.melody.api.exception.IllegalResourcesFilterException;
import com.wat.melody.api.exception.IllegalTargetFilterException;
import com.wat.melody.api.exception.PluginConfigurationException;
import com.wat.melody.api.exception.ProcessorManagerFactoryException;
import com.wat.melody.cli.exception.CommandLineParsingException;
import com.wat.melody.cli.exception.ConfigurationLoadingException;
import com.wat.melody.common.utils.Filter;
import com.wat.melody.common.utils.LogThreshold;
import com.wat.melody.common.utils.OrderName;
import com.wat.melody.common.utils.OrderNameSet;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.common.utils.exception.IllegalFileException;
import com.wat.melody.common.utils.exception.IllegalFilterException;
import com.wat.melody.common.utils.exception.IllegalLogThresholdException;
import com.wat.melody.common.utils.exception.IllegalPropertiesSetFileFormatException;
import com.wat.melody.common.utils.exception.MelodyException;

/**
 * <p>
 * This class is especially designed to create and initialize an
 * {@link IProcessorManager} with Configuration Directives found in a Global
 * Configuration File and/or with Options found in a Command Line.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 * @see com.wat.melody.api.IProcessorManager
 */
public class ProcessorManagerLoader {

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

	public static final String RESOURCES_DESCRIPTORS = "resourcesDescriptors";
	public static final String BATCH_MODE = "batchMode";
	public static final String PRESERVE_TEMPORARY_FILES_MODE = "preserveTemporaryFilesMode";
	public static final String RUN_DRY_MODE = "runDryMode";
	public static final String SEQUENCE_DESCRIPTOR_FILE_PATH = "sequenceDescriptorFilePath";
	public static final String ORDERS = "orders";
	public static final String PROPERTIES = "properties";
	public static final String RESOURCES_FILTERS = "resourcesFilters";
	public static final String TARGETS_FILTERS = "targetFilters";

	private IProcessorManager moProcessorManager;

	private static final String CONSOLE_APPENDER = "console";

	/**
	 * <p>
	 * Create a new {@link ProcessorManagerLoader} object, which can create and
	 * initialize a {@link IProcessorManager} object with either Configuration
	 * Directives found in Global Configuration File (call
	 * {@link #loadGlobalConfigurationFile(String)}) and/or with Options found
	 * in a Command Line (call {@link #parseCommandLine(String[])}.
	 * </p>
	 * 
	 * @param pm
	 *            is the <code>ProcessorManager</code> object whose members will
	 *            be set.
	 * 
	 */
	public ProcessorManagerLoader() {
	}

	/**
	 * <p>
	 * Get the inner <code>IProcessorManager</code> instance (see
	 * {@link #ProcessorManagerLoader(IProcessorManager)}).
	 * </p>
	 * 
	 * @return the previous <code>IProcessorManager</code> object.
	 * 
	 */
	public IProcessorManager getProcessorManager() {
		return moProcessorManager;
	}

	private IProcessorManager setProcessorManager(IProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid IProcessorManager.");
		}
		IProcessorManager previous = getProcessorManager();
		moProcessorManager = pm;
		return previous;
	}

	/**
	 * <p>
	 * Initialize the inner <code>IProcessorManager</code> object with the
	 * Configuration Directives found in the Global Configuration File and with
	 * the Options found in the Command Line.
	 * </p>
	 * <p>
	 * First, load Configuration Directives found in the Global Configuration
	 * File, then load all Options found in the Command Line.
	 * </p>
	 * <p>
	 * <i> The first argument of the Command Line must be the Default Global
	 * Configuration File Path (the Default Global Configuration File Path is
	 * automatically inserted by the Shell Wrapper at the first position). A
	 * user-defined Global Configuration File can be provided in the Command
	 * Line using the Option </i><code>-C</code><i>. If a user-defined Global
	 * Configuration File is not explicitly provided in the Command Line, then
	 * the Default Global Configuration File is used. </i>
	 * </p>
	 * <p>
	 * Command Line's available Options are :
	 * <ul>
	 * <li><code>-C < Global Configuration File Path></code></BR> Load the
	 * specified Global Configuration File (see
	 * {@link #loadGlobalConfigurationFile(String)} ;</BR>
	 * <li><code>-q</code></BR> Decrease the log threshold (see
	 * {@link IProcessorManager#decreaseLogThreshold()} ;</BR>
	 * <li><code>-v</code></BR> Increase the log threshold (see
	 * {@link IProcessorManager#increaseLogThreshold()} ;</BR>
	 * <li><code>-E < Resources Descriptor File Path ></code></BR> Set the path
	 * of the Resources Descriptor with the given value (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#load(String)}) ;</BR>
	 * <li><code>-f < Sequence Descriptor File Path ></code></BR> Set the path
	 * of the Sequence Descriptor with the given value (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#load(String)}) ;</BR>
	 * <li><code>-F < Filter ></code></BR> Add the given Filter to the Resources
	 * Descriptor (see {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#addFilter(String)}) ;</BR>
	 * <li><code>-T < Filter ></code></BR> Add the given Filter to the Target
	 * Descriptor (see {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#addTargetsFilter(String)}) ;</BR>
	 * <li><code>-o < Order ></code></BR> Add the given Order to the Sequence
	 * Descriptor (see {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addOrder(String)}) ;</BR>
	 * <li><code>-S < Specific Configuration File Path ></code></BR> Set the
	 * path of the Specific Configuration with the given value (see
	 * {@link IProcessorManager#getSpecificConfiguration()},
	 * {@link ISpecificConfiguration#load(String)}) ;</BR>
	 * <li><code>-l < Listing Options ></code></BR> Set the listing options with
	 * the given value (see {@link IProcessorManager#setListingOption(String)})
	 * ;</BR>
	 * <li><code>-B</code></BR> Enable 'Batch Mode' (see
	 * {@link IProcessorManager#enableBatchMode()}) ;</BR>
	 * <li><code>-b</code></BR> Disable 'Batch Mode' (see
	 * {@link IProcessorManager#disableBatchMode()}) ;</BR>
	 * <li><code>-P</code></BR> Enable 'Preserve Temporary Files Mode' (see
	 * {@link IProcessorManager#enablePreserveTemporaryFilesMode()}) ;</BR>
	 * <li><code>-p</code></BR> Disable 'Preserve Temporary Files Mode' (see
	 * {@link IProcessorManager#disablePreserveTemporaryFilesMode()}) ;</BR>
	 * <li><code>-D</code></BR> Enable 'Run Dry Mode' (see
	 * {@link IProcessorManager#enableRunDryMode()}) ;</BR>
	 * <li><code>-d</code></BR> Disable 'Run Dry Mode' (see
	 * {@link IProcessorManager#disableRunDryMode()}) ;</BR>
	 * <li><code>-V < Property ></code></BR> Add the given Property to the
	 * Sequence Descriptor (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addProperty(Property)}) ;</BR>
	 * </ul>
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line.
	 * 
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a directory.
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a non
	 *             readable file.
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a non
	 *             writable file.
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a non
	 *             existent file.
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a file which
	 *             contains a malformed Unicode escape sequence.
	 * @throws ConfigurationLoadingException
	 *             if the Global Configuration File Path points to a file which
	 *             is not a valid GlobalConfiguration File (e.g. mandatory
	 *             Configuration Directives are missing, or a Configuration
	 *             Directives's value refers to a non-existent file, or a
	 *             Configuration Directives's value is not valid, ...).
	 * @throws ConfigurationLoadingException
	 *             if a file mentioned in a Configuration Directive cannot be
	 *             found.
	 * @throws CommandLineParsingException
	 *             if the Command Line is not properly formatted (e.g. a
	 *             Option's value refers to a non-existent file, or a Option's
	 *             value is missing, or a Option's value is not valid, ...).
	 * @throws CommandLineParsingException
	 *             if the given Command Line is an empty string array.
	 * @throws CommandLineParsingException
	 *             if a file mentioned in the Command Line cannot be found.
	 * @throws IllegalArgumentException
	 *             if the Global Configuration File Path is null.
	 * @throws IllegalArgumentException
	 *             if the given Command Line is null.
	 * @throws IOException
	 *             if an IO error occurred while reading the Global
	 *             Configuration File.
	 * @throws IOException
	 *             if an IO error occurred while reading a file mentioned in a
	 *             Configuration Directive.
	 * @throws IOException
	 *             if an IO error occurred while reading a Configuration File.
	 * 
	 */
	public IProcessorManager parseCommandLine(String[] cmdLine)
			throws ConfigurationLoadingException, CommandLineParsingException,
			IOException {
		if (cmdLine == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid String[] (a Command Line).");
		}
		try {
			/*
			 * TODO : put the default configuration file into the classpath ?
			 * instead of in the command line. Copy the mechanism of log4J with
			 * log4j.xml
			 */
			/*
			 * The Melody Shell/Bat Wrapper must add the Default Global
			 * Configuration File Path at the first position in the Command Line
			 */
			if (cmdLine.length == 0) {
				throw new CommandLineParsingException(
						Messages.CmdEx_MISSING_DEFAULT_GLOBAL_CONF_FILE);
			}
			try {
				Tools.validateFileExists(cmdLine[0]);
			} catch (IllegalFileException Ex) {
				throw new CommandLineParsingException(
						Messages.CmdEx_INVALID_DEFAULT_GLOBAL_CONF_FILE, Ex);
			}

			/*
			 * If the user don't explicitly specify a Global Configuration File
			 * (using option -C ) => the default one (which is located at the
			 * first position in the Command Line) is used
			 */
			String sGCFilePath = retrieveUserDefinedGlobalConfigurationFilePath(cmdLine);
			if (sGCFilePath == null) {
				sGCFilePath = cmdLine[0];
			}
			loadGlobalConfigurationFile(sGCFilePath);

			int firstArg = parseOptions(cmdLine);
			// If the Command Line contains Arguments
			// => raise an error
			if (firstArg < cmdLine.length) {
				throw new CommandLineParsingException(Messages.bind(
						Messages.CmdEx_UNKNOWN_ARGUMENT_ERROR,
						cmdLine[firstArg]));
			}

			return getProcessorManager();
		} catch (CommandLineParsingException Ex) {
			throw new CommandLineParsingException(Messages.CmdEx_GENERIC_PARSE,
					Ex);
		}
	}

	/**
	 * <p>
	 * Extract the user-defined Global Configuration File from the given Command
	 * Line.
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line.
	 * 
	 * @return a String which is the Global Configuration File Path (if provided
	 *         through the Command Line using the option <code>-C</code>), or
	 *         <code>null</code> (if option <code>-C</code> was not provided in
	 *         the Command Line).
	 * 
	 * @throws CommandLineParsingException
	 *             if Option </code>-C</code> appears multiple times in the
	 *             Command Line.
	 * @throws CommandLineParsingException
	 *             if Option </code>-C</code>'s value is missing.
	 */
	public String retrieveUserDefinedGlobalConfigurationFilePath(
			String[] cmdLine) throws CommandLineParsingException {
		String sUserDefinedGCFilePath = null;
		// Begin at position 1 because the parameter at position 0 is the
		// Default Global Configuration File Path
		for (int i = 1; i < cmdLine.length; i++) {
			if (cmdLine[i].matches("^-\\w*C$")) {
				if (sUserDefinedGCFilePath != null) {
					throw new CommandLineParsingException(
							Messages.CmdEx_MULTIPLE_GLOBAL_CONF_FILE_ERROR);
				}
				try {
					if (cmdLine[++i].equals("--")) {
						throw new CommandLineParsingException(Messages.bind(
								Messages.CmdEx_MISSING_OPTION_VALUE, 'C'));
					}
					sUserDefinedGCFilePath = cmdLine[i];
				} catch (ArrayIndexOutOfBoundsException Ex) {
					throw new CommandLineParsingException(Messages.bind(
							Messages.CmdEx_MISSING_OPTION_VALUE, 'C'));
				}
			}
		}
		return sUserDefinedGCFilePath;
	}

	/**
	 * <p>
	 * Initialize the inner <code>ProcessorManager</code> instance's members
	 * with the Options found in the Command Line.
	 * </p>
	 * 
	 * @param cmdLine
	 *            is the Command Line.
	 * 
	 * @return the index of the first Argument of the Command Line.
	 * 
	 * @throws CommandLineParsingException
	 *             if the Command Line is not properly formatted (e.g. a
	 *             Option's value refers to a non-existent file, or a Option's
	 *             value is missing, or a Option's value is not valid, ...).
	 * @throws IOException
	 *             if an IO error occurred while reading a file mentioned in a
	 *             Configuration Directive.
	 */
	private int parseOptions(String[] cmdLine)
			throws CommandLineParsingException, IOException {
		// Begin at position 1 because the parameter at position 0 is the
		// Default Global Configuration File Path
		for (int i = 1; i < cmdLine.length; i++) {
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
							Messages.bind(Messages.CmdEx_MISSING_OPTION_VALUE,
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
			throw new CommandLineParsingException(Messages.bind(
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
			throw new CommandLineParsingException(Messages.bind(
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
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_TOOMUCH_LOG_THRESHOLD, 'v'), Ex);
		}
		return i;
	}

	private int parseResourcesFilter(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'F'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addFilter(
					Filter.parseFilter(cmdLine[i]));
		} catch (IllegalTargetFilterException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'F'), Ex);
		}
		return i;
	}

	private int parseTargetFilter(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'T'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addTargetsFilter(
					Filter.parseFilter(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		}
		return i;
	}

	private int parseOrder(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'o'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().addOrder(
					OrderName.parseString(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'o'), Ex);
		}
		return i;
	}

	private int parseResourcesDescriptor(String[] cmdLine, int i)
			throws CommandLineParsingException, IOException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'E'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().add(cmdLine[i]);
		} catch (IllegalTargetFilterException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'T'), Ex);
		} catch (IllegalResourcesFilterException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'F'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'E'), Ex);
		}
		return i;
	}

	private int parseSequenceDescriptor(String[] cmdLine, int i)
			throws CommandLineParsingException, IOException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'f'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().load(cmdLine[i]);
		} catch (IllegalOrderException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'o'), Ex);
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'f'), Ex);
		}
		return i;
	}

	private int parseProperties(String[] cmdLine, int i)
			throws CommandLineParsingException {
		if (cmdLine[++i].equals("--")) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_MISSING_OPTION_VALUE, 'V'));
		}
		try {
			IProcessorManager pm = getProcessorManager();
			pm.getSequenceDescriptor().addProperty(
					Property.parseProperty(cmdLine[i]));
		} catch (MelodyException Ex) {
			throw new CommandLineParsingException(Messages.bind(
					Messages.CmdEx_INVALID_OPTION_VALUE, 'V'), Ex);
		}
		return i;
	}

	/**
	 * <p>
	 * Initialize the inner <code>ProcessorManager</code> instance's members
	 * with the Configuration Directives found in the Global Configuration File.
	 * </p>
	 * <p>
	 * The Global Configuration File must be a properties set, as defined in by
	 * the {@link ConfigurationFile} class.
	 * </p>
	 * <p>
	 * Global Configuration File's available Configuration Directives are :
	 * <ul>
	 * <li><code>archivedLogsFolderPath</code></BR> Set the
	 * archivedLogsFolderPath with the given value (see
	 * {@link #setArchivedLogsFolderPath(String)}) ;</BR>
	 * <li><code>logsFolderPath</code></BR> Set the logsFolderPath with the
	 * given value (see {@link #setLogsFolderPath(String)}) ;</BR>
	 * <li><code>workingFolderPath</code><BR>
	 * Set the Working Folder Path to the given value (see
	 * {@link IProcessorManager#setWorkingFolderPath(String)}) ;</BR>
	 * <li><code>pluginConfigurationFolderPath</code></BR> Set the PlugIn
	 * Configuration Folder Path to the given value (see
	 * {@link IProcessorManager#setPluginConfigurationFolderPath(String)})
	 * ;</BR>
	 * <li><code>maxSimultaneousStep</code></BR> Set the maximum number of
	 * parallel worker (see
	 * {@link IProcessorManager#setMaxSimultaneousStep(int)}) ;</BR>
	 * <li><code>hardKillTimeout</code></BR> Set the maximum amount of seconds a
	 * worker will be waited before killed (see
	 * {@link IProcessorManager#setHardKillTimeout(int)}) ;</BR>
	 * <li><code>logThreshold</code></BR> Set the log threshold to the given
	 * value (see {@link IProcessorManager#setLogThreshold(LogThreshold)})
	 * ;</BR>
	 * <li><code>resourcesDescriptorFilePath</code></BR> Set the path of the
	 * Resources Descriptor with the given value (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#load(String)}) ;</BR>
	 * <li><code>batchMode</code></BR> Enable/disable 'Batch Mode' (see
	 * {@link IProcessorManager#disableBatchMode()} ;</BR>
	 * <li><code>preserveTemporaryFilesMode</code></BR> Enable/disable 'Preserve
	 * Temporary Files Mode' (see
	 * {@link IProcessorManager#disablePreserveTemporaryFilesMode()}) ;</BR>
	 * <li><code>runDryMode</code></BR> Enable/disable 'Run Dry Mode' (see
	 * {@link IProcessorManager#disableRunDryMode()} ;</BR>
	 * <li><code>sequenceDescriptorFilePath</code></BR> Set the path of the
	 * Sequence Descriptor with the given value (see
	 * {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#load(String)}) ;</BR>
	 * <li><code>specificConfigurationFilePath</code></BR> Set the path of the
	 * Specific Configuration with the given value (see
	 * {@link IProcessorManager#getSpecificConfiguration()},
	 * {@link ISpecificConfiguration#load(String)}) ;</BR>
	 * <li><code>listingOptions</code></BR> Set the listing options with the
	 * given value (see {@link IProcessorManager#setListingOption(String)})
	 * ;</BR>
	 * <li><code>order</code></BR> Add the given order to the Sequence
	 * Descriptor (see {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addOrder(String)}) ;</BR>
	 * <li><code>properties</code></BR> Add the given properties to the Sequence
	 * Descriptor (see {@link IProcessorManager#getSequenceDescriptor()},
	 * {@link ISequenceDescriptor#addProperty(Property)}) ;</BR>
	 * <li><code>resourcesFilters</code></BR> Add the given filters to the
	 * Resources Descriptor (see
	 * {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#setFilter(String)}) ;</BR>
	 * <li><code>targetFilters</code></BR> Add the given filters to the Target
	 * Descriptor (see {@link IProcessorManager#getResourcesDescriptor()},
	 * {@link IResourcesDescriptor#setTargetFilter(String)}) ;</BR>
	 * </ul>
	 * </p>
	 * 
	 * @param gcfPath
	 *            is the path of the Global Configuration File.
	 * 
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a directory.
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a non readable file.
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a non writable file.
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a non existent file.
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a file which contains a malformed
	 *             Unicode escape sequence.
	 * @throws ConfigurationLoadingException
	 *             if the given path points to a file which is not valid (e.g.
	 *             mandatory Configuration Directives are missing, or a
	 *             Configuration Directives's value refers to a non-existent
	 *             file, or a Configuration Directives's value is not valid,
	 *             ...).
	 * @throws IllegalArgumentException
	 *             if the given path is null.
	 * @throws IOException
	 *             if an IO error occurred while reading the Global
	 *             Configuration File.
	 * 
	 */
	public void loadGlobalConfigurationFile(String gcfPath)
			throws ConfigurationLoadingException, IOException {
		try {
			PropertiesSet oProps = new PropertiesSet(gcfPath);

			// Mandatory Configuration Directives
			loadLoggingVariablesToSubstitute(oProps);
			loadLoggingConfigFile(oProps);
			loadProcessorManagerClass(oProps);
			loadWorkingFolderPath(oProps);
			loadMaxPar(oProps);
			loadHardKillTimeout(oProps);

			// Optional Configuration Directives
			loadResourcesDescriptors(oProps);
			loadBatchMode(oProps);
			loadPreserveTmpFileMode(oProps);
			loadRunDryMode(oProps);
			loadSequenceDescriptorFilePath(oProps);
			loadOrderNames(oProps);
			loadProperties(oProps);
			loadResourcesFilters(oProps);
			loadTargetsFilters(oProps);

			registerAllPlugIns(oProps);
			loadAllPlugInsConfiguration(oProps);
		} catch (IllegalFileException | IllegalPropertiesSetFileFormatException
				| ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_GENERIC_GLOBAL_CONF_LOAD, gcfPath), Ex);
		}
	}

	private void loadLoggingVariablesToSubstitute(PropertiesSet oProps)
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
				delcareLoggingVariableToSubstitute(oProps, vtsd);
			}
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					LOGGING_VARIABLES_TO_SUBSTITUTE), Ex);
		}
	}

	private void delcareLoggingVariableToSubstitute(PropertiesSet oProps,
			String vtsd) throws ConfigurationLoadingException {
		if (!oProps.containsKey(vtsd)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, vtsd), Ex);
		}
	}

	private void loadLoggingConfigFile(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(LOGGING_CONFIG_FILE)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, LOGGING_CONFIG_FILE));
		}
		try {
			String val = oProps.get(LOGGING_CONFIG_FILE);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			Tools.validateFileExists(val);
			org.apache.log4j.xml.DOMConfigurator.configure(val);
		} catch (MelodyException | FactoryConfigurationError Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, LOGGING_CONFIG_FILE), Ex);
		}
	}

	private void loadProcessorManagerClass(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(PROCESSOR_MANAGER_CLASS)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
					Messages.bind(Messages.ConfEx_INVALID_DIRECTIVE,
							PROCESSOR_MANAGER_CLASS), Ex);
		}

	}

	private void loadWorkingFolderPath(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(WORKING_FOLDER_PATH)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, WORKING_FOLDER_PATH), Ex);
		}

	}

	private void loadMaxPar(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(MAX_SIMULTANEOUS_STEP)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
				throw new ConfigurationLoadingException(Messages.bind(
						Messages.ConfEx_INVALID_INTEGER_FORMAT, val));
			}
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, MAX_SIMULTANEOUS_STEP),
					Ex);
		}
	}

	private void loadHardKillTimeout(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(HARD_KILL_TIMEOUT)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
				throw new ConfigurationLoadingException(Messages.bind(
						Messages.ConfEx_INVALID_INTEGER_FORMAT, val));
			}
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, HARD_KILL_TIMEOUT), Ex);
		}
	}

	private void loadResourcesDescriptors(PropertiesSet oProps)
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
				delcareResourcesDescriptorFilePath(oProps, rdd);
			}
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_DESCRIPTORS),
					Ex);
		}
	}

	private void delcareResourcesDescriptorFilePath(PropertiesSet oProps,
			String rdd) throws ConfigurationLoadingException, IOException {
		if (!oProps.containsKey(rdd)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
		} catch (IllegalTargetFilterException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		} catch (IllegalResourcesFilterException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_FILTERS), Ex);
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, rdd), Ex);
		}
	}

	private void loadBatchMode(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(BATCH_MODE)) {
			return;
		}
		try {
			String val = oProps.get(BATCH_MODE);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.setBatchMode(Boolean.parseBoolean(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, BATCH_MODE), Ex);
		}
	}

	private void loadPreserveTmpFileMode(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(PRESERVE_TEMPORARY_FILES_MODE)) {
			return;
		}
		try {
			String val = oProps.get(PRESERVE_TEMPORARY_FILES_MODE);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.setPreserveTemporaryFilesMode(Boolean.parseBoolean(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					PRESERVE_TEMPORARY_FILES_MODE), Ex);
		}
	}

	private void loadRunDryMode(PropertiesSet oProps)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(RUN_DRY_MODE)) {
			return;
		}
		try {
			String val = oProps.get(RUN_DRY_MODE);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.setRunDryMode(Boolean.parseBoolean(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RUN_DRY_MODE), Ex);
		}
	}

	private void loadSequenceDescriptorFilePath(PropertiesSet oProps)
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
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ORDERS), Ex);
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE,
					SEQUENCE_DESCRIPTOR_FILE_PATH), Ex);
		}
	}

	private void loadOrderNames(PropertiesSet oProps)
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
			pm.getSequenceDescriptor().setOrders(
					OrderNameSet.parseOrdersSet(val));
		} catch (MelodyException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, ORDERS), Ex);
		}
	}

	private void loadProperties(PropertiesSet oProps)
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
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, PROPERTIES), Ex);
		}
	}

	private void loadProperty(PropertiesSet oProps, String p)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(p)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, p));
		}
		IProcessorManager pm = getProcessorManager();
		pm.getSequenceDescriptor().addProperty(oProps.getProperty(p));
	}

	private void loadResourcesFilters(PropertiesSet oProps)
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
		} catch (IllegalTargetFilterException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		} catch (ConfigurationLoadingException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, RESOURCES_FILTERS), Ex);
		}
	}

	private void loadResourcesFilter(PropertiesSet oProps, String f)
			throws ConfigurationLoadingException, IllegalTargetFilterException {
		if (!oProps.containsKey(f)) {
			throw new ConfigurationLoadingException(Messages.bind(
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
		} catch (IllegalTargetFilterException Ex) {
			throw Ex;
		} catch (ConfigurationLoadingException | IllegalFilterException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, f), Ex);
		}
	}

	private void loadTargetsFilters(PropertiesSet oProps)
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
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, TARGETS_FILTERS), Ex);
		}
	}

	private void loadTargetsFilter(PropertiesSet oProps, String f)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(f)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_DIRECTIVE, f));
		}
		try {
			String val = oProps.get(f);
			if (val.trim().length() == 0) {
				throw new ConfigurationLoadingException(
						Messages.ConfEx_EMPTY_DIRECTIVE);
			}
			IProcessorManager pm = getProcessorManager();
			pm.getResourcesDescriptor().addTargetsFilter(
					Filter.parseFilter(val));
		} catch (ConfigurationLoadingException | IllegalFilterException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_DIRECTIVE, f), Ex);
		}
	}

	private void registerAllPlugIns(PropertiesSet oProps)
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

	private void registerAllPlugInClasses(PropertiesSet oProps, String pi)
			throws ConfigurationLoadingException {
		if (!oProps.containsKey(pi)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_TASKS_DIRECTIVE, new Object[] {
							TASK_DIRECTIVES, pi, oProps.getFilePath() }));
		}
		String pics = oProps.get(pi);
		if (pics.trim().length() == 0) {
			throw new ConfigurationLoadingException(Messages.bind(
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
	private void registerPlugInClass(PropertiesSet oProps, String pi, String pic)
			throws ConfigurationLoadingException {
		IProcessorManager pm = getProcessorManager();
		IRegisteredTasks rts = pm.getRegisteredTasks();

		Class<ITask> c = null;
		try {
			c = (Class<ITask>) Class.forName(pic);
		} catch (ClassNotFoundException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_CNF_TASKS_DIRECTIVE, pi, pic));
		} catch (NoClassDefFoundError Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_NCDF_TASKS_DIRECTIVE, new Object[] { pi,
							pic, Ex.getMessage().replaceAll("/", ".") }));
		} catch (ClassCastException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_CC_TASKS_DIRECTIVE, pi, pic));
		}
		/*
		 * Will throw a RuntimeException if the given Class<ITask> doesn't
		 * respect ITask specification. Should only happened during Task
		 * Development. No need to catch/re-throw.
		 */
		rts.registerTaskClass(c);
	}

	private void loadAllPlugInsConfiguration(PropertiesSet oProps)
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

	private void loadPlugInConfiguration(PropertiesSet oProps, String pcd)
			throws ConfigurationLoadingException, IOException {
		String pcf = loadPlugInConfigurationDirective(oProps, pcd);
		PropertiesSet pcps = loadPlugInConfigurationFile(oProps, pcd, pcf);
		String pcn = findPlugInRegistrationName(oProps, pcd, pcps);
		String pcc = findPlugInConfigurationClassName(oProps, pcd, pcps);
		Class<IPluginConfiguration> c = convertPlugInConfigurationClass(pcps,
				pcn, pcc);
		IPluginConfiguration pc = instanciatePlugInConfiguration(pcps, c);
		registerPlugInConfiguration(pcn, pc);
	}

	private String loadPlugInConfigurationDirective(PropertiesSet oProps,
			String pcd) throws ConfigurationLoadingException {
		if (!oProps.containsKey(pcd)) {
			throw new ConfigurationLoadingException(
					Messages.bind(
							Messages.ConfEx_MISSING_PLUGINS_DIRECTIVE,
							new Object[] { PLUGIN_CONF_DIRECTIVES, pcd,
									oProps.getFilePath() }));
		}
		String pcf = oProps.get(pcd);
		if (pcf.trim().length() == 0) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_EMPTY_PLUGINS_DIRECTIVE, pcd,
					PLUGIN_CONF_DIRECTIVES));
		}
		return pcf;
	}

	private PropertiesSet loadPlugInConfigurationFile(PropertiesSet oProps,
			String pcd, String pcf) throws ConfigurationLoadingException,
			IOException {
		PropertiesSet pcps = null;
		try {
			pcps = new PropertiesSet(pcf);
		} catch (IllegalFileException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_NVF_PLUGINS_DIRECTIVE, pcd, pcf), Ex);
		} catch (IllegalPropertiesSetFileFormatException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_NVPS_PLUGINS_DIRECTIVE, pcd, pcf), Ex);
		}
		return pcps;
	}

	private String findPlugInRegistrationName(PropertiesSet oProps, String pcd,
			PropertiesSet pcps) throws ConfigurationLoadingException {
		if (!pcps.containsKey(IPluginConfiguration.PLUGIN_CONF_NAME)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_PCN_DIRECTIVE, new Object[] { pcd,
							pcps.getFilePath(),
							IPluginConfiguration.PLUGIN_CONF_NAME }));
		}
		String pcn = pcps.get(IPluginConfiguration.PLUGIN_CONF_NAME);
		if (pcn.trim().length() == 0) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_EMPTY_PCN_DIRECTIVE,
					IPluginConfiguration.PLUGIN_CONF_NAME, pcps.getFilePath()));
		}
		IProcessorManager pm = getProcessorManager();
		if (pm.getPluginConfigurations().containsKey(pcn)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_INVALID_PCN_DIRECTIVE,
					new Object[] { IPluginConfiguration.PLUGIN_CONF_NAME,
							pcps.getFilePath(), pcn }));
		}
		return pcn;
	}

	private String findPlugInConfigurationClassName(PropertiesSet oProps,
			String pcd, PropertiesSet pcps)
			throws ConfigurationLoadingException {
		if (!pcps.containsKey(IPluginConfiguration.PLUGIN_CONF_CLASS)) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_MISSING_PCC_DIRECTIVE, new Object[] { pcd,
							pcps.getFilePath(),
							IPluginConfiguration.PLUGIN_CONF_CLASS }));
		}
		String pcc = pcps.get(IPluginConfiguration.PLUGIN_CONF_CLASS);
		if (pcc.trim().length() == 0) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_EMPTY_PCC_DIRECTIVE,
					IPluginConfiguration.PLUGIN_CONF_CLASS, pcps.getFilePath()));
		}
		return pcc;
	}

	@SuppressWarnings("unchecked")
	private Class<IPluginConfiguration> convertPlugInConfigurationClass(
			PropertiesSet pcps, String pcn, String pcc)
			throws ConfigurationLoadingException {
		Class<?> c = null;
		try {
			c = Class.forName(pcc);
		} catch (ClassNotFoundException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_CNF_CONF_DIRECTIVE,
					new Object[] { IPluginConfiguration.PLUGIN_CONF_CLASS,
							pcps.getFilePath(), pcc, pcn }));
		} catch (NoClassDefFoundError Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_NCDF_CONF_DIRECTIVE,
					new Object[] { IPluginConfiguration.PLUGIN_CONF_CLASS,
							pcps.getFilePath(), pcc,
							Ex.getMessage().replaceAll("/", ".") }));
		}
		Class<IPluginConfiguration> cc = null;
		try {
			cc = (Class<IPluginConfiguration>) c;
		} catch (ClassCastException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_CC_CONF_DIRECTIVE,
					new Object[] { IPluginConfiguration.PLUGIN_CONF_CLASS,
							pcps.getFilePath(), pcc }));
		}
		return cc;
	}

	private IPluginConfiguration instanciatePlugInConfiguration(
			PropertiesSet pcps, Class<IPluginConfiguration> c)
			throws ConfigurationLoadingException {
		IPluginConfiguration pc = null;
		try {
			pc = (IPluginConfiguration) c.newInstance();
		} catch (InstantiationException | IllegalAccessException Ex) {
			throw new ConfigurationLoadingException(
					Messages.bind(Messages.ConfEx_CC_CONF_DIRECTIVE,
							new Object[] {
									IPluginConfiguration.PLUGIN_CONF_CLASS,
									pcps.getFilePath(),
									c.getClass().getCanonicalName() }));
		}
		try {
			pc.load(pcps);
		} catch (PluginConfigurationException Ex) {
			throw new ConfigurationLoadingException(Messages.bind(
					Messages.ConfEx_GENERIC_PLUGIN_LOAD, pcps.getFilePath()),
					Ex);
		}
		return pc;
	}

	private void registerPlugInConfiguration(String pcn, IPluginConfiguration pc) {
		IProcessorManager pm = getProcessorManager();
		pm.getPluginConfigurations().put(pcn, pc);
	}

	/**
	 * <p>
	 * Delete temporary resources generated by this object.
	 * </p>
	 * 
	 * <p>
	 * <b> Should be called when this object and its inner IProcessorManager are
	 * no more necessary, in order to clean the hard disk. </b>
	 * </p>
	 * 
	 * @throws IOException
	 *             if an IO error occurred while deleting files.
	 * 
	 */
	public void deleteTemporaryResources() throws IOException {
	}

}