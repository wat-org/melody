package com.wat.melody.core.nativeplugin.foreach;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.common.utils.Property;
import com.wat.melody.common.utils.PropertyName;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.core.nativeplugin.foreach.exception.ForeachException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Foreach implements ITask, ITaskContainer {

	/**
	 * The 'foreach' XML element used in the Sequence Descriptor
	 */
	public static final String FOREACH = "foreach";

	/**
	 * The 'items' XML attribute of the Foreach Task
	 */
	public static final String ITEMS_ATTR = "items";

	/**
	 * The 'itemName' XML attribute of the Foreach Task
	 */
	public static final String ITEMNAME_ATTR = "itemName";

	/**
	 * The 'maxpar' XML attribute of the Foreach Task
	 */
	public static final String MAXPAR_ATTR = "maxpar";

	public static final short NEW = 16;
	public static final short RUNNING = 8;
	public static final short SUCCEED = 0;
	public static final short FAILED = 1;
	public static final short INTERRUPTED = 2;
	public static final short CRITICAL = 4;

	private ITaskContext moContext;
	private String msItems;
	private PropertyName msItemName;
	private int miMaxPar;
	private List<Node> maNodes;

	private short miState;
	private ThreadGroup moThreadGroup;
	private List<ForeachThread> maThreadsList;
	private List<Throwable> maExceptionsList;

	public Foreach() {
		// Initialize members
		initContext();
		initItems();
		initItemName();
		try {
			setMaxPar(0);
		} catch (ForeachException Ex) {
			throw new RuntimeException("TODO impossible");
		}
		setNodes(new ArrayList<Node>());
		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<ForeachThread>());
		setExceptionsList(new ArrayList<Throwable>());
	}

	private void initContext() {
		moContext = null;
	}

	private void initItems() {
		msItems = null;
	}

	private void initItemName() {
		msItemName = null;
	}

	/**
	 * <p>
	 * Register the given Task (in its native Node format) as an inner Task of
	 * this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given node is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if the given node is already registered.
	 */
	@Override
	public void addNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (maNodes.contains(n)) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Node already present in list.");
		}
		maNodes.add(n);
	}

	@Override
	public void validate() {
	}

	private short markState(short state) {
		return miState |= state;
	}

	private boolean isFailed() {
		return FAILED == (miState & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (miState & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (miState & CRITICAL);
	}

	/**
	 * <p>
	 * Process the {@link Foreach} Task.
	 * </p>
	 * <p>
	 * All Inner Task of the {@link Foreach} Task are proceed for each item
	 * specified by the XPath expression given the {@link #ITEMS_ATTR} XML
	 * Attribute.
	 * </p>
	 * 
	 * @throws ForeachException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 * 
	 * @see {@link ForeachThread}
	 */
	@Override
	public void doProcessing() throws ForeachException, InterruptedException {
		try {
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">" + FOREACH));
			getThreadGroup().setDaemon(true);
			initializeForeachThreads();
			try {
				startForeachThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptionsList().add(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForForeachThreadsToBeDone();
				quit();
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	/**
	 * <p>
	 * Create a {@link ForeachThread} for each target.
	 * </p>
	 */
	private void initializeForeachThreads() {
		List<Node> targets = null;
		try {
			targets = getContext().getProcessorManager()
					.getResourcesDescriptor().evaluateTargets(getItems());
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexecpted error while evaluating "
					+ "an XPath Expression. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		int index = 1;
		for (Node target : targets) {
			PropertiesSet ps = getContext().getProperties().copy();
			// Add the property '<ItemName>=<XPath position of currentItem>', so
			// that '§[<ItemName>]§' will be expanded with the item's XPath
			// position
			Property p = new Property(getItemName(),
					Doc.getXPathPosition(target), null);
			ps.put(p);
			ForeachThread ft = new ForeachThread(this, index++, ps);
			if (!getThreadsList().add(ft)) {
				throw new RuntimeException("Didn't managed to register "
						+ "a new ForeachThread.");
			}
		}
	}

	/**
	 * <p>
	 * Start all Threads, according to the maximum number of Thread this object
	 * can run simultaneously (see {@link #getMaxPar()}).
	 * </p>
	 * 
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 */
	private void startForeachThreads() throws InterruptedException {
		if (getMaxPar() == 0) {
			for (ForeachThread ft : getThreadsList()) {
				getContext().handleProcessorStateUpdates();
				ft.startProcessing();
			}
			return;
		}

		int threadToLaunchID = getThreadsList().size();
		List<ForeachThread> runningThreads = new ArrayList<ForeachThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			getContext().handleProcessorStateUpdates();
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				ForeachThread ft = getThreadsList().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				ForeachThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	/**
	 * <p>
	 * Wait for all threads to end.
	 * </p>
	 */
	private void waitForForeachThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (ForeachThread ft : getThreadsList())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				markState(INTERRUPTED);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + FOREACH + " inner Task to finish.");
	}

	/**
	 * <p>
	 * Inspect all threads final state and raise the appropriate exception.
	 * </p>
	 * 
	 * @throws ForeachException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 */
	private void quit() throws ForeachException, InterruptedException {
		for (ForeachThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsList().add(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new ForeachException(buildForeachTrace());
		} else if (isFailed()) {
			throw new ForeachException(buildForeachTrace());
		} else if (isInterrupted()) {
			throw new InterruptedException(Messages.bind(
					Messages.ForeachEx_INTERRUPTED, FOREACH));
		}
	}

	/**
	 * <p>
	 * Build an exception which message represents all errors raised during this
	 * object processing.
	 * </p>
	 * 
	 * @return an an exception which message represents all errors raised during
	 *         this object processing.
	 */
	private ForeachException buildForeachTrace() {
		if (getExceptionsList().size() == 0) {
			return null;
		} else if (getExceptionsList().size() == 1) {
			return new ForeachException(getExceptionsList().get(0));
		}
		String err = "";
		for (int i = 0; i < getExceptionsList().size(); i++) {
			err += Tools.NEW_LINE
					+ "Error "
					+ (i + 1)
					+ " : "
					+ Tools.getUserFriendlyStackTrace(getExceptionsList()
							.get(i));
		}
		err = err.replaceAll(Tools.NEW_LINE, Tools.NEW_LINE + "   ");
		return new ForeachException(err);
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext context) {
		if (context == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = context;
	}

	/**
	 * <p>
	 * Get the XPath expression which allow to retrieve Targets.
	 * </p>
	 * 
	 * @return the XPath expression which allow to retrieve Targets.
	 */
	private String getItems() {
		return msItems;
	}

	/**
	 * <p>
	 * Set the XPath expression which allow to retrieve targets.
	 * </p>
	 * 
	 * @param sItems
	 *            the XPath expression which allow to retrieve targets.
	 * 
	 * @throws ForeachException
	 *             if the given XPath expression is an empty <code>String</code>
	 *             .
	 * @throws ForeachException
	 *             if the given XPath expression is not a valid XPath
	 *             Expression.
	 * @throws IllegalArgumentException
	 *             if the given XPath expression is <code>null</code>.
	 */
	@Attribute(name = ITEMS_ATTR, mandatory = true)
	public String setItems(String sItems) throws ForeachException {
		if (sItems == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		if (sItems.trim().length() == 0) {
			throw new ForeachException(Messages.bind(
					Messages.ForeachEx_EMPTY_ITEMS_ATTR, sItems));
		}
		try {
			getContext().getProcessorManager().getResourcesDescriptor()
					.evaluateTargets(sItems);
		} catch (XPathExpressionException Ex) {
			throw new ForeachException(Messages.bind(
					Messages.ForeachEx_INVALID_ITEMS_ATTR, sItems), Ex);
		}
		String previous = getItems();
		msItems = sItems;
		return previous;
	}

	/**
	 * <p>
	 * Get the name of the property which will contains the XPath position of
	 * the current Target.
	 * </p>
	 * 
	 * @return the name of the property which will contains the XPath position
	 *         of the current Target.
	 */
	private PropertyName getItemName() {
		return msItemName;
	}

	/**
	 * <p>
	 * Set the name of the property which will contains the XPath position of
	 * the current Target.
	 * </p>
	 * 
	 * @param propertyName
	 *            is the name of the property which will contains the XPath
	 *            position of the current Target.
	 * 
	 * @throws ForeachException
	 *             if the given name is not a valid property name.
	 * @throws ForeachException
	 *             if a property with the same name was already defined.
	 * @throws IllegalArgumentException
	 *             if the given name is <code>null</code>.
	 */
	@Attribute(name = ITEMNAME_ATTR, mandatory = true)
	public PropertyName setItemName(PropertyName propertyName)
			throws ForeachException {
		if (propertyName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		PropertyName previous = getItemName();
		msItemName = propertyName;
		return previous;
	}

	/**
	 * <p>
	 * Get the maximum number of thread this object can run simultaneously.
	 * </p>
	 * 
	 * @return the maximum number of thread this object can run simultaneously. <BR/>
	 *         0 means that there is not limit. <BR/>
	 */
	private int getMaxPar() {
		return miMaxPar;
	}

	/**
	 * <p>
	 * Set the maximum number of threads this object can run simultaneously.
	 * </p>
	 * <p>
	 * <i> * 0 means that there is not limit. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param iMaxPar
	 *            is the maximum number of thread this object can run
	 *            simultaneously.
	 * 
	 * @throws ForeachException
	 *             if the given value is negative.
	 */
	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int iMaxPar) throws ForeachException {
		if (iMaxPar < 0) {
			throw new ForeachException(Messages.bind(
					Messages.ForeachEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		miMaxPar = iMaxPar;
		return previous;
	}

	/**
	 * <p>
	 * Get all inner Task (in their native Node format) of this object.
	 * </p>
	 * 
	 * @return all inner Task (in their native Node format).
	 */
	protected List<Node> getNodes() {
		return maNodes;
	}

	private List<Node> setNodes(List<Node> an) {
		if (an == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Node>.");
		}
		List<Node> previous = getNodes();
		maNodes = an;
		return previous;
	}

	protected ThreadGroup getThreadGroup() {
		return moThreadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		moThreadGroup = tg;
		return previous;
	}

	/**
	 * <p>
	 * Get the {@link ForeachThread} list.
	 * </p>
	 * 
	 * @return the {@link ForeachThread} list.
	 */
	private List<ForeachThread> getThreadsList() {
		return maThreadsList;
	}

	private List<ForeachThread> setThreadsList(List<ForeachThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<ForeachThread>.");
		}
		List<ForeachThread> previous = getThreadsList();
		maThreadsList = aft;
		return previous;
	}

	/**
	 * <p>
	 * Get the list of exceptions that append during the processing of this
	 * object.
	 * </p>
	 * 
	 * @return the list of exceptions that append during the processing of this
	 *         object.
	 */
	private List<Throwable> getExceptionsList() {
		return maExceptionsList;
	}

	private List<Throwable> setExceptionsList(List<Throwable> at) {
		if (at == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Throwable>.");
		}
		List<Throwable> previous = getExceptionsList();
		maExceptionsList = at;
		return previous;
	}

}