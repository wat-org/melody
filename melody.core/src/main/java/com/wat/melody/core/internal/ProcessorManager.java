package com.wat.melody.core.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.api.IPlugInConfiguration;
import com.wat.melody.api.IPlugInConfigurations;
import com.wat.melody.api.IProcessorListener;
import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.IRegisteredTasks;
import com.wat.melody.api.IShareProperties;
import com.wat.melody.api.ITask;
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
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.XPathFunctionResolver;
import com.wat.melody.common.xpath.XPathNamespaceContextResolver;
import com.wat.melody.common.xpath.XPathResolver;
import com.wat.melody.core.nativeplugin.sequence.exception.SequenceException;

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
 * <p>
 * <i> * The processing is performed by a dedicated thread, which is created in
 * its own ThreadGroup. <BR/>
 * * This ThreadGroup is a child of the calling thread's parent ThreadGroup,
 * meaning that if the calling thread's parent ThreadGroup is interrupted, the
 * processing dedicated thread will receive the interruption too. </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public final class ProcessorManager implements IProcessorManager, Runnable {

	private static Log log = LogFactory.getLog(ProcessorManager.class);

	private TaskFactory _taskFactory;
	private String _workingFolderPath;
	private int _maxSimultaneousStep;
	private int _hardKillTimeout;
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
		try {
			setHardKillTimeout(0);
		} catch (ProcessorManagerConfigurationException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "maximal kill timeout to 0. "
					+ "Because this value is hard coded, such error cannot "
					+ "happened."
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced. ", Ex);
		}

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
					+ "Must be a valid TaskFactory.");
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
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_HARD_KILL_TIMEOUT, v));
		}
		int previous = _maxSimultaneousStep;
		_maxSimultaneousStep = v;
		return previous;
	}

	@Override
	public int getHardKillTimeout() {
		return _hardKillTimeout;
	}

	@Override
	public int setHardKillTimeout(int v)
			throws ProcessorManagerConfigurationException {
		if (v < 0) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_MAX_PAR, v));
		}
		int previous = _hardKillTimeout;
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
					+ "Must be a valid ISequenceDescriptor.");
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
					+ "Must be a valid IResourcesDescriptor.");
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
					+ "Must be a valid Map<String, PropertiesSet>.");
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
			throw new PlugInConfigurationException(Messages.bind(
					Messages.ConfEx_CONF_NOT_REGISTERED, key));
		}
		return pc;
	}

	@Override
	public XPathResolver getXPathResolver() {
		return _xpathResolver;
	}

	private XPathResolver setXPathResolver(XPathResolver xpathResolver) {
		// can be null
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
				&& getSequenceDescriptor().getFileFullPath().equals(
						getParentProcessorManager().getSequenceDescriptor()
								.getFileFullPath());
	}

	public IProcessorManager createSubProcessorManager(PropertiesSet ps) {
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
		dest.getSequenceDescriptor().setProperties(ps.copy());

		return dest;
	}

	@Override
	public String toString() {
		String s;
		// Mandatory Configuration Directive
		s = "Working Folder Path" + "=" + getWorkingFolderPath()
				+ Util.NEW_LINE;
		s += "Max Simultaneous Order" + "=" + getMaxSimultaneousStep()
				+ Util.NEW_LINE;
		s += "Hard Kill Timeout" + "=" + getHardKillTimeout() + Util.NEW_LINE;

		// Optional Configuration Directive
		s += getResourcesDescriptor().toString() + Util.NEW_LINE;
		s += "Batch Mode" + "=" + isBatchModeEnable() + Util.NEW_LINE;
		s += "Preserve Temporary Resources Mode" + "="
				+ isPreserveTemporaryFilesModeEnable() + Util.NEW_LINE;
		s += "Run Dry Mode" + "=" + isRunDryModeEnable() + Util.NEW_LINE;
		s += "Sequence Descriptor File Path" + "="
				+ getSequenceDescriptor().getFileFullPath() + Util.NEW_LINE;
		s += "Order(s)" + "=";
		for (int i = 0; i < getSequenceDescriptor().countOrders(); i++)
			s += getSequenceDescriptor().getOrder(i) + " ";
		s += Util.NEW_LINE;
		return s;
	}

	/**
	 * <p>
	 * Throws a {@link ProcessorManagerConfigurationException} if this object is
	 * not correctly configured.
	 * </p>
	 * <p>
	 * Should be called after one or more member's update, in order to validate
	 * this object is correctly configured.
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
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Working Folder Path"));
		}
		// If the Sequence Descriptor File Path is not provided => raise an
		// error
		if (getSequenceDescriptor().getFileFullPath() == null) {
			throw new ProcessorManagerConfigurationException(Messages.bind(
					Messages.ProcMgrEx_UNDEF_MANDOTORY_DIRECTIVE,
					"Sequence Descriptor Path"));
		}
	}

	private void setStopRequested(boolean bStopRequested) {
		_stopRequested = bStopRequested;
	}

	private void setPauseRequested(boolean bPauseRequested) {
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

	@Override
	public List<IProcessorListener> getListeners() {
		return _listeners;
	}

	@Override
	public List<IProcessorListener> setListeners(List<IProcessorListener> l) {
		if (l == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid List<"
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
			InterruptedException e = new InterruptedException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.INTERRUPTED,
					getSequenceDescriptor().getFileFullPath()));
			fireProcessorFinishedEvent(State.INTERRUPTED, e);
			setFinalError(e);
		} catch (TaskException Ex) {
			ProcessorException e = new ProcessorException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.FAILED,
					getSequenceDescriptor().getFileFullPath()), Ex);
			fireProcessorFinishedEvent(State.FAILED, e);
			if (isSameSequenceDescriptorAsParent()) {
				setFinalError(Ex);
			} else {
				setFinalError(e);
			}
		} catch (Throwable Ex) {
			ProcessorException e = new ProcessorException(Messages.bind(
					Messages.ProcMgrEx_PROCESS_FINAL_STATE, State.CRITICAL,
					getSequenceDescriptor().getFileFullPath()), Ex);
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
				getSequenceDescriptor().getProperties());
	}

	private void deleteTemporaryResources() {
		try {
			if (isPreserveTemporaryFilesModeEnable()) {
				log.debug("Temporary resources not cleaned "
						+ "(because 'Preserve Temporary Files Mode' is enabled).");
				return;
			}
			if (getWorkingFolderPath() != null) {
				FS.deleteDirectoryAndEmptyParentDirectory(getWorkingFolderPath());
			}
			log.debug("Temporary resources cleaned.");
		} catch (Throwable Ex) {
			log.warn(Util.getUserFriendlyStackTrace(new Exception(
					"Failed to delete temporary resources.", Ex)));
		}
	}

	protected void createAndProcessTask(Node n, PropertiesSet ps)
			throws TaskException, InterruptedException {
		processTask(newTask(n, ps));
	}

	protected ITask newTask(Node n, PropertiesSet ps) throws TaskException {
		boolean pushed = false;
		try {
			Class<? extends ITask> c = getTaskFactory().identifyTask(n);
			// Duplicate the PropertiesSet, so the Task can work with its own
			// PropertiesSet
			// Doesn't apply to ITask which implements IShareProperties
			PropertiesSet ownPs = TaskFactory.implementsInterface(c,
					IShareProperties.class) ? ps : ps.copy();
			Melody.pushContext(new TaskContext(n, ownPs, this));
			pushed = true;
			ITask t = getTaskFactory().newTask(c, n, ps);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.SUCCESS,
					null);
			return t;
		} catch (TaskFactoryException Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_INIT_FINAL_STATE, n.getNodeName()
							.toLowerCase(), State.FAILED, Doc
							.getNodeLocation(n)), Ex);
			fireTaskCreatedEvent(n.getNodeName().toLowerCase(), State.FAILED, e);
			if (pushed) {
				Melody.popContext();
			}
			throw e;
		} catch (Throwable Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_INIT_FINAL_STATE, n.getNodeName()
							.toLowerCase(), State.CRITICAL, Doc
							.getNodeLocation(n)), Ex);
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
			InterruptedException e = new InterruptedException(Messages.bind(
					Messages.TaskEx_PROCESS_FINAL_STATE, task.getClass()
							.getSimpleName().toLowerCase(), State.INTERRUPTED,
					Doc.getNodeLocation(Melody.getContext().getNode())));
			fireTaskFinishedEvent(task, State.INTERRUPTED, e);
			throw e;
		} catch (TaskException Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_PROCESS_FINAL_STATE, task.getClass()
							.getSimpleName().toLowerCase(), State.FAILED,
					Doc.getNodeLocation(Melody.getContext().getNode())), Ex);
			fireTaskFinishedEvent(task, State.FAILED, e);
			if (Ex instanceof SequenceException) {
				throw Ex;
			} else {
				throw e;
			}
		} catch (Throwable Ex) {
			TaskException e = new TaskException(Messages.bind(
					Messages.TaskEx_PROCESS_FINAL_STATE, task.getClass()
							.getSimpleName().toLowerCase(), State.CRITICAL,
					Doc.getNodeLocation(Melody.getContext().getNode())), Ex);
			fireTaskFinishedEvent(task, State.CRITICAL, e);
			throw e;
		} finally {
			Melody.popContext();
		}
	}
}