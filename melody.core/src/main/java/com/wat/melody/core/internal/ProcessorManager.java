package com.wat.melody.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IPlugInConfigurations;
import com.wat.melody.api.IProcessorListener;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.IShareProperties;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITopLevelTask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.MelodyThread;
import com.wat.melody.api.Messages;
import com.wat.melody.api.event.ProcessingFinishedEvent;
import com.wat.melody.api.event.ProcessingStartedEvent;
import com.wat.melody.api.event.RequestProcessingToPauseEvent;
import com.wat.melody.api.event.RequestProcessingToResumeEvent;
import com.wat.melody.api.event.RequestProcessingToStartEvent;
import com.wat.melody.api.event.RequestProcessingToStopEvent;
import com.wat.melody.api.event.State;
import com.wat.melody.api.event.TaskCreatedEvent;
import com.wat.melody.api.event.TaskFinishedEvent;
import com.wat.melody.api.event.TaskStartedEvent;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ProcessorException;
import com.wat.melody.api.exception.ProcessorManagerConfigurationException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.reflection.ReflectionHelper;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.exception.SimpleNodeRelatedException;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.XPathFunctionResolver;
import com.wat.melody.common.xpath.XPathNamespaceContextResolver;
import com.wat.melody.common.xpath.XPathResolver;

/**
 * <p>
 * Start, pause, resume and stop the processing, according to the given
 * instructions.
 * </p>
 * <p>
 * Processing instructions are described in a {@link SequenceDescriptor}.
 * </p>
 * <p>
 * Processing behavior can be configured using the meaningful setters of this
 * object.
 * </p>
 * 
 * <ul>
 * <li>The processing is performed by a dedicated thread, which is created in
 * its own ThreadGroup ;</li>
 * <li>This ThreadGroup is a child of the calling thread's parent ThreadGroup,
 * meaning that if the calling thread's parent ThreadGroup is interrupted, the
 * processing dedicated thread will receive the interruption too ;</li>
 * </ul>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class ProcessorManager implements IProcessorManager, Runnable {

	private static Logger log = LoggerFactory.getLogger(ProcessorManager.class);

	private static GenericTimeout createGenericTimeout(int timeout) {
		try {
			return new GenericTimeout(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenericTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static final GenericTimeout DEFAULT_KILL_TIMEOUT = createGenericTimeout(30000);

	private TaskFactory _taskFactory;
	private String _workingFolderPath;
	private int _maxSimultaneousStep;
	private GenericTimeout _hardKillTimeout;
	private boolean _batchMode;
	private boolean _preserveTemporayFilesMode;
	private boolean _runDryMode;
	private SequenceDescriptor _sequenceDescriptor;
	private ResourcesDescriptor _resourcesDescriptor;

	private PlugInConfigurations _pluginConfigurations;
	private XPathResolver _xpathResolver;
	private XPath _xpath;

	private boolean _stopRequested;
	private boolean _pauseRequested;
	private ThreadGroup _threadGroup;
	private MelodyThread _thread;
	private Throwable _finalError;
	private List<IProcessorListener> _listeners;
	private IProcessorManager _parentProcessorManager;

	public ProcessorManager() {
		// Mandatory Configuration Directives
		initWorkingFolderPath();
		try {
			setMaxSimultaneousStep(0);
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "maximal amount of simultaneous thread to 0. "
					+ "Because this value is hard coded, such error cannot "
					+ "happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. ", Ex);
		}
		setHardKillTimeout(DEFAULT_KILL_TIMEOUT);

		// Optional Configuration Directives
		setBatchMode(false);
		setPreserveTemporaryFilesMode(false);
		setRunDryMode(false);
		setTaskFactory(new TaskFactory());
		setSequenceDescriptor(new SequenceDescriptor());
		setResourcesDescriptor(new ResourcesDescriptor());
		setPluginConfigurations(new PlugInConfigurations());
		setXPathResolver(new XPathResolver(new XPathNamespaceContextResolver(),
				new XPathFunctionResolver()));

		// Processing members
		setStopRequested(false);
		setPauseRequested(false);
		setThreadGroup(null);
		setThread(null);
		setFinalError(null);
		setListeners(new ArrayList<IProcessorListener>());
		setParentProcessorManager(null);
	}

	private void initWorkingFolderPath() {
		_workingFolderPath = null;
	}

	private TaskFactory getTaskFactory() {
		return _taskFactory;
	}

	private TaskFactory setTaskFactory(TaskFactory tf) {
		if (tf == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + TaskFactory.class.getCanonicalName()
					+ ".");
		}
		TaskFactory previous = getTaskFactory();
		_taskFactory = tf;
		return previous;
	}

	private IProcessorManager getParentProcessorManager() {
		return _parentProcessorManager;
	}

	private void setParentProcessorManager(IProcessorManager ppm) {
		// can be null, if it is the master ProcessorManager
		_parentProcessorManager = ppm;
	}

	@Override
	public String getWorkingFolderPath() {
		return _workingFolderPath;
	}

	@Override
	public String setWorkingFolderPath(String v)
			throws IllegalDirectoryException {
		File f = new File(v);
		if (!f.isAbsolute()) {
			throw new IllegalDirectoryException(Msg.bind(
					com.wat.melody.common.files.Messages.DirEx_NOT_ABSOLUTE, v));
		}
		FS.validateDirPath(v);
		String previous = _workingFolderPath;
		_workingFolderPath = v;
		return previous;
	}

	@Override
	public int getMaxSimultaneousStep() {
		return _maxSimultaneousStep;
	}

	@Override
	public int setMaxSimultaneousStep(int v)
			throws ProcessorManagerConfigurationException {
		if (v < 0) {
			throw new ProcessorManagerConfigurationException(Msg.bind(
					Messages.ProcMgrEx_MAX_PAR, v));
		}
		int previous = _maxSimultaneousStep;
		_maxSimultaneousStep = v;
		return previous;
	}

	@Override
	public GenericTimeout getHardKillTimeout() {
		return _hardKillTimeout;
	}

	@Override
	public GenericTimeout setHardKillTimeout(GenericTimeout v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
		}
		GenericTimeout previous = _hardKillTimeout;
		_hardKillTimeout = v;
		return previous;
	}

	@Override
	public boolean setBatchMode(boolean v) {
		boolean previous = _batchMode;
		_batchMode = v;
		return previous;
	}

	@Override
	public boolean enableBatchMode() {
		boolean previous = _batchMode;
		_batchMode = true;
		return previous;
	}

	@Override
	public boolean disableBatchMode() {
		boolean previous = _batchMode;
		_batchMode = false;
		return previous;
	}

	@Override
	public boolean isBatchModeEnable() {
		return _batchMode;
	}

	@Override
	public boolean setPreserveTemporaryFilesMode(boolean v) {
		boolean previous = _preserveTemporayFilesMode;
		_preserveTemporayFilesMode = v;
		return previous;
	}

	@Override
	public boolean enablePreserveTemporaryFilesMode() {
		boolean previous = _preserveTemporayFilesMode;
		_preserveTemporayFilesMode = true;
		return previous;
	}

	@Override
	public boolean disablePreserveTemporaryFilesMode() {
		boolean previous = _preserveTemporayFilesMode;
		_preserveTemporayFilesMode = false;
		return previous;
	}

	@Override
	public boolean isPreserveTemporaryFilesModeEnable() {
		return _preserveTemporayFilesMode;
	}

	@Override
	public boolean setRunDryMode(boolean v) {
		boolean previous = _runDryMode;
		_runDryMode = v;
		return previous;
	}

	@Override
	public boolean enableRunDryMode() {
		boolean previous = _runDryMode;
		_runDryMode = true;
		return previous;
	}

	@Override
	public boolean disableRunDryMode() {
		boolean previous = _runDryMode;
		_runDryMode = false;
		return previous;
	}

	@Override
	public boolean isRunDryModeEnable() {
		return _runDryMode;
	}

	@Override
	public SequenceDescriptor getSequenceDescriptor() {
		return _sequenceDescriptor;
	}

	public SequenceDescriptor setSequenceDescriptor(SequenceDescriptor sd) {
		if (sd == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SequenceDescriptor.class.getCanonicalName() + ".");
		}
		SequenceDescriptor previous = _sequenceDescriptor;
		_sequenceDescriptor = sd;
		return previous;
	}

	@Override
	public ResourcesDescriptor getResourcesDescriptor() {
		return _resourcesDescriptor;
	}

	public ResourcesDescriptor setResourcesDescriptor(ResourcesDescriptor rds) {
		if (rds == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ResourcesDescriptor.class.getCanonicalName() + ".");
		}
		ResourcesDescriptor previous = getResourcesDescriptor();
		_resourcesDescriptor = rds;
		return previous;
	}

	@Override
	public IRegisteredTasks getRegisteredTasks() {
		return getTaskFactory().getRegisteredTasks();
	}

	public IRegisteredTasks setRegisteredTasks(IRegisteredTasks rts) {
		return getTaskFactory().setRegisteredTasks(rts);
	}

	@Override
	public PlugInConfigurations getPluginConfigurations() {
		return _pluginConfigurations;
	}

	public PlugInConfigurations setPluginConfigurations(PlugInConfigurations pcs) {
		if (pcs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ PlugInConfigurations.class.getCanonicalName() + ".");
		}
		PlugInConfigurations previous = getPluginConfigurations();
		_pluginConfigurations = pcs;
		return previous;
	}

	@Override
	public IPlugInConfiguration getPluginConfiguration(
			Class<? extends IPlugInConfiguration> key)
			throws PlugInConfigurationException {
		IPlugInConfigurations pcs = getPluginConfigurations();
		IPlugInConfiguration pc = null;
		pc = pcs.get(key);
		if (pc == null) {
			throw new PlugInConfigurationException(Msg.bind(
					Messages.ConfEx_CONF_NOT_REGISTERED, key));
		}
		return pc;
	}

	@Override
	public XPathResolver getXPathResolver() {
		return _xpathResolver;
	}

	private XPathResolver setXPathResolver(XPathResolver xpathResolver) {
		if (xpathResolver == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ XPathResolver.class.getCanonicalName() + ".");
		}
		XPathResolver previous = getXPathResolver();
		_xpathResolver = xpathResolver;
		setXPath(XPathExpander.newXPath(getXPathResolver()));
		getResourcesDescriptor().setXPath(getXPath());
		getSequenceDescriptor().setXPath(getXPath());
		return previous;
	}

	private XPath getXPath() {
		return _xpath;
	}

	private XPath setXPath(XPath xpath) {
		if (xpath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + XPath.class.getCanonicalName() + ".");
		}
		XPath previous = getXPath();
		_xpath = xpath;
		return previous;
	}

	private boolean isSubPM() {
		return getParentProcessorManager() != null;
	}

	private boolean isSameSequenceDescriptorAsParent() {
		return isSubPM()
				&& getSequenceDescriptor().getSourceFile().equals(
						getParentProcessorManager().getSequenceDescriptor()
								.getSourceFile());
	}

	public IProcessorManager createSubProcessorManager(PropertySet ps) {
		ProcessorManager dest = new ProcessorManager();
		dest.setParentProcessorManager(this);
		dest.setRegisteredTasks(getRegisteredTasks());
		dest.setXPathResolver(getXPathResolver());

		try {
			dest.setWorkingFolderPath(getWorkingFolderPath());
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexpected error while setting path "
					+ "of an sub-ProcessorManager. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. "
					+ "Or an external event made the path no more "
					+ "accessible (deleted, moved, read permission removed, "
					+ "...).", Ex);
		}

		try {
			dest.setHardKillTimeout(getHardKillTimeout());
			dest.setMaxSimultaneousStep(getMaxSimultaneousStep());
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "member of an sub-ProcessorManager. "
					+ "Since values are taken from another ProcessorManager, "
					+ "such error can not raise. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}

		dest.setListeners(getListeners());
		dest.setResourcesDescriptor(getResourcesDescriptor());
		dest.setPluginConfigurations(getPluginConfigurations());
		dest.setBatchMode(isBatchModeEnable());
		dest.setRunDryMode(isRunDryModeEnable());
		dest.setPreserveTemporaryFilesMode(isPreserveTemporaryFilesModeEnable());
		dest.getSequenceDescriptor().setPropertySet(ps.clone());

		return dest;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		// Mandatory Configuration Directive
		str.append("working-folder-path:");
		str.append(getWorkingFolderPath());
		str.append(", max-concurrent-thread:");
		str.append(getMaxSimultaneousStep());
		str.append(", hardkill-timeout:");
		str.append(getHardKillTimeout());
		str.append(", sequence-descriptor:");
		str.append(getSequenceDescriptor());
		// Optional Configuration Directive
		str.append(", resource-descriptor:");
		str.append(getResourcesDescriptor());
		str.append(", batch-mode-enabled:");
		str.append(isBatchModeEnable());
		str.append(", preserve-temp-resources-mode-enabled:");
		str.append(isPreserveTemporaryFilesModeEnable());
		str.append(", run-dry-mode-enabled:");
		str.append(isRunDryModeEnable());
		str.append(" }");
		return str.toString();
	}

	/**
	 * <p>
	 * Validate this object's configuration.
	 * </p>
	 * 
	 * @throws ProcessorManagerConfigurationException
	 *             if the Working Folder Path is not set.
	 * @throws ProcessorManagerConfigurationException
	 *             if the Sequence Descriptor is not set.
	 */
	private void validateConfigurationDirectives()
			throws ProcessorManagerConfigurationException {
		// If the Working Folder Path is not provided => raise an error
		if (getWorkingFolderPath() == null) {
			throw new ProcessorManagerConfigurationException(Msg.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Working Folder Path"));
		}
		// If the Sequence Descriptor File Path is not provided => raise an
		// error
		if (getSequenceDescriptor().getSourceFile() == null) {
			throw new ProcessorManagerConfigurationException(Msg.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Sequence Descriptor Path"));
		}
	}

	private synchronized void setStopRequested(boolean bStopRequested) {
		_stopRequested = bStopRequested;
	}

	private synchronized void setPauseRequested(boolean bPauseRequested) {
		_pauseRequested = bPauseRequested;
	}

	@Override
	public synchronized boolean isStopRequested() {
		return _stopRequested;
	}

	@Override
	public synchronized boolean isPauseRequested() {
		return _pauseRequested;
	}

	@Override
	public synchronized boolean isRunning() {
		return getThread() != null;
	}

	private MelodyThread getThread() {
		return _thread;
	}

	private MelodyThread setThread(MelodyThread v) {
		// Can be set to null, when cleaning
		MelodyThread previous = getThread();
		_thread = v;
		return previous;
	}

	private ThreadGroup getThreadGroup() {
		return _threadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be set to null, when cleaning
		ThreadGroup previous = getThreadGroup();
		_threadGroup = tg;
		return previous;
	}

	private Throwable setFinalError(Throwable e) {
		// Can be set to null, when there is no error
		Throwable previous = getProcessingFinalError();
		_finalError = e;
		return previous;
	}

	@Override
	public Throwable getProcessingFinalError() {
		return _finalError;
	}

	@Override
	public void startProcessing() throws ProcessorManagerConfigurationException {
		startProcessing(null, 1);
	}

	@Override
	public synchronized void startProcessing(ThreadGroup ptg, int index)
			throws ProcessorManagerConfigurationException {
		// Perform cross controls on GlobalConfiguration instance
		validateConfigurationDirectives();
		// Fire the event
		fireRequestProcessorToStartEvent();
		// If the Thread is not null, that mean that the processing has already
		// been started
		if (getThread() != null) {
			return;
		}
		// Create its own ThreadGroup, so that all child Thread can be
		// interrupted easily
		String sTGName = "PM" + "-" + "main";
		if (ptg == null) {
			setThreadGroup(new ThreadGroup(sTGName));
		} else {
			sTGName = ptg.getName() + ">" + "PM" + "-" + index;
			setThreadGroup(new ThreadGroup(ptg, sTGName));
		}
		// Will be automatically destroyed
		getThreadGroup().setDaemon(true);
		// Create a dedicated Thread which will perform the processing
		setThread(new CoreThread(getThreadGroup(), this, sTGName));
		// Start the dedicated thread (call the method run - see Thread)
		getThread().start();
	}

	@Override
	public synchronized void stopProcessing() {
		fireRequestProcessorToStopEvent();
		_stopRequested = true;
		// If the Thread is null, that mean that the processing has not been
		// started. In such condition, there is nothing to stop
		if (getThread() != null) {
			getThreadGroup().interrupt();
		}
	}

	@Override
	public synchronized void pauseProcessing() {
		fireRequestProcessorToPauseEvent();
		_pauseRequested = true;
	}

	@Override
	public synchronized void resumeProcessing() {
		fireRequestProcessorToResumeEvent();
		if (!isPauseRequested())
			return;
		_pauseRequested = false;
		notifyAll();
	}

	@Override
	public void waitTillProcessingIsDone(long millis, int nanos)
			throws InterruptedException {
		// If the Thread is null, that mean that the processing has not been
		// started. In such condition there is nothing to wait for
		if (getThread() == null)
			return;
		getThread().join(millis, nanos);
	}

	@Override
	public void waitTillProcessingIsDone(long millis)
			throws InterruptedException {
		waitTillProcessingIsDone(millis, 0);
	}

	@Override
	public void waitTillProcessingIsDone() throws InterruptedException {
		waitTillProcessingIsDone(0, 0);
	}

	@Override
	public boolean addListener(IProcessorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IProcessorListener.class.getCanonicalName() + ".");
		}
		return _listeners.add(l);
	}

	@Override
	public boolean removeListener(IProcessorListener l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ IProcessorListener.class.getCanonicalName() + ".");
		}
		return _listeners.remove(l);
	}

	public List<IProcessorListener> getListeners() {
		return _listeners;
	}

	public List<IProcessorListener> setListeners(List<IProcessorListener> l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ IProcessorListener.class.getCanonicalName() + ">.");
		}
		List<IProcessorListener> previous = _listeners;
		_listeners = l;
		return previous;
	}

	private void fireRequestProcessorToStartEvent() {
		RequestProcessingToStartEvent evt = null;
		evt = new RequestProcessingToStartEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStartRequested(evt);
		}
	}

	private void fireRequestProcessorToStopEvent() {
		RequestProcessingToStopEvent evt = null;
		evt = new RequestProcessingToStopEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStopRequested(evt);
		}
	}

	private void fireRequestProcessorToPauseEvent() {
		RequestProcessingToPauseEvent evt = null;
		evt = new RequestProcessingToPauseEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingPauseRequested(evt);
		}
	}

	private void fireRequestProcessorToResumeEvent() {
		RequestProcessingToResumeEvent evt = null;
		evt = new RequestProcessingToResumeEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingResumeRequested(evt);
		}
	}

	private void fireProcessorStartedEvent() {
		ProcessingStartedEvent evt = null;
		evt = new ProcessingStartedEvent(this);
		for (IProcessorListener l : getListeners()) {
			l.processingStarted(evt);
		}
	}

	private void fireProcessorFinishedEvent(State state, Throwable cause) {
		ProcessingFinishedEvent evt = null;
		evt = new ProcessingFinishedEvent(this, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.processingFinished(evt);
		}
	}

	private void fireTaskCreatedEvent(String taskName, State state,
			Throwable cause) {
		TaskCreatedEvent evt = null;
		evt = new TaskCreatedEvent(taskName, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.taskCreated(evt);
		}
	}

	private void fireTaskStartedEvent(ITask t) {
		TaskStartedEvent evt = null;
		evt = new TaskStartedEvent(t);
		for (IProcessorListener l : getListeners()) {
			l.taskStarted(evt);
		}
	}

	private void fireTaskFinishedEvent(ITask t, State state, Throwable cause) {
		TaskFinishedEvent evt = null;
		evt = new TaskFinishedEvent(t, state, cause);
		for (IProcessorListener l : getListeners()) {
			l.taskFinished(evt);
		}
	}

	public synchronized void handleProcessorStateUpdates()
			throws InterruptedException {
		while (true) {
			if (isStopRequested()) {
				throw new InterruptedException("Task processing interrupted");
			} else if (isPauseRequested())
				try {
					wait(1000);
				} catch (InterruptedException Ex) {
					throw new InterruptedException("Pause interrupted");
				}
			else {
				break;
			}
		}
	}

	@Override
	public void run() {
		try {
			fireProcessorStartedEvent();
			processSequenceDescriptor();
			fireProcessorFinishedEvent(State.SUCCESS, null);
		} catch (InterruptedException Ex) {
			String msg = Msg.bind(Messages.ProcMgrEx_PROCESS_FINAL_STATE,
					State.INTERRUPTED);
			InterruptedException e = new WrapperInterruptedException(
					new ProcessorException(getSequenceDescriptor()
							.getSourceFile(), msg, Ex));
			fireProcessorFinishedEvent(State.INTERRUPTED, e);
			if (isSameSequenceDescriptorAsParent()) {
				setFinalError(Ex);
			} else {
				setFinalError(e);
			}
		} catch (TaskException Ex) {
			String msg = Msg.bind(Messages.ProcMgrEx_PROCESS_FINAL_STATE,
					State.FAILED);
			ProcessorException e = new ProcessorException(
					getSequenceDescriptor().getSourceFile(), msg, Ex);
			fireProcessorFinishedEvent(State.FAILED, e);
			if (isSameSequenceDescriptorAsParent()) {
				setFinalError(Ex);
			} else {
				setFinalError(e);
			}
		} catch (Throwable Ex) {
			String msg = Msg.bind(Messages.ProcMgrEx_PROCESS_FINAL_STATE,
					State.CRITICAL);
			ProcessorException e = new ProcessorException(
					getSequenceDescriptor().getSourceFile(), msg, Ex);
			fireProcessorFinishedEvent(State.CRITICAL, e);
			setFinalError(e);
		} finally {
			if (!isSubPM()) {
				getResourcesDescriptor().store();
				deleteTemporaryResources();
			}
			// The thread is set to null, so it is possible to start the
			// processing again (it will certainly be useful to somebody)
			synchronized (this) {
				setThread(null);
				setThreadGroup(null);
			}
		}
	}

	private void processSequenceDescriptor() throws TaskException,
			InterruptedException {
		createAndProcessTask(getSequenceDescriptor().getRoot(),
				getSequenceDescriptor().getPropertySet());
	}

	private void deleteTemporaryResources() {
		try {
			if (isPreserveTemporaryFilesModeEnable()) {
				log.debug("Temporary resources not removed "
						+ "(because 'preserve-temporary-files-mode' is enabled).");
				return;
			}
			if (getWorkingFolderPath() != null) {
				FS.deleteDirectoryAndEmptyParentDirectory(getWorkingFolderPath());
			}
			log.debug("Temporary resources cleaned.");
		} catch (Throwable Ex) {
			log.warn(new MelodyException("Fail to remove temporary resources.",
					Ex).toString());
		}
	}

	protected void createAndProcessTask(Element n, PropertySet ps)
			throws TaskException, InterruptedException {
		processTask(newTask(n, ps));
	}

	protected ITask newTask(Element n, PropertySet ps) throws TaskException {
		boolean pushed = false;
		try {
			Class<? extends ITask> c = getTaskFactory().identifyTask(n);
			// Duplicate the PropertiesSet, so the Task can work with its own
			// PropertiesSet
			// Doesn't apply to ITask which implements IShareProperties
			PropertySet ownPs = ReflectionHelper.implement(c,
					IShareProperties.class) ? ps : ps.clone();
			Melody.pushContext(new TaskContext(n, ownPs, this));
			pushed = true;
			ITask t = getTaskFactory().newTask(c, n, ps);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.SUCCESS,
					null);
			return t;
		} catch (TaskFactoryException Ex) {
			String msg = Msg.bind(Messages.TaskEx_INIT_FINAL_STATE, n
					.getNodeName().toLowerCase(), State.FAILED);
			TaskException e = new TaskException(new SimpleNodeRelatedException(
					n, msg, Ex));
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.FAILED, e);
			if (pushed) {
				Melody.popContext();
			}
			throw e;
		} catch (Throwable Ex) {
			String msg = Msg.bind(Messages.TaskEx_INIT_FINAL_STATE, n
					.getNodeName().toLowerCase(), State.CRITICAL);
			TaskException e = new TaskException(new SimpleNodeRelatedException(
					n, msg, Ex));
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.CRITICAL,
					e);
			if (pushed) {
				Melody.popContext();
			}
			throw e;
		}
	}

	protected void processTask(ITask task) throws TaskException,
			InterruptedException {
		try {
			fireTaskStartedEvent(task);
			task.doProcessing();
			fireTaskFinishedEvent(task, State.SUCCESS, null);
		} catch (InterruptedException Ex) {
			String msg = Msg.bind(Messages.TaskEx_PROCESS_FINAL_STATE, task
					.getClass().getSimpleName().toLowerCase(),
					State.INTERRUPTED);
			InterruptedException e = new WrapperInterruptedException(
					new SimpleNodeRelatedException(Melody.getContext()
							.getRelatedElement(), msg, Ex));
			fireTaskFinishedEvent(task, State.INTERRUPTED, e);
			if (task instanceof ITopLevelTask) {
				throw Ex;
			} else {
				throw e;
			}
		} catch (TaskException Ex) {
			String msg = Messages
					.bind(Messages.TaskEx_PROCESS_FINAL_STATE, task.getClass()
							.getSimpleName().toLowerCase(), State.FAILED);
			TaskException e = new TaskException(new SimpleNodeRelatedException(
					Melody.getContext().getRelatedElement(), msg, Ex));
			fireTaskFinishedEvent(task, State.FAILED, e);
			if (task instanceof ITopLevelTask) {
				throw Ex;
			} else {
				throw e;
			}
		} catch (Throwable Ex) {
			String msg = Msg.bind(Messages.TaskEx_PROCESS_FINAL_STATE, task
					.getClass().getSimpleName().toLowerCase(), State.CRITICAL);
			TaskException e = new TaskException(new SimpleNodeRelatedException(
					Melody.getContext().getRelatedElement(), msg, Ex));
			fireTaskFinishedEvent(task, State.CRITICAL, e);
			throw e;
		} finally {
			Melody.popContext();
		}
	}

}