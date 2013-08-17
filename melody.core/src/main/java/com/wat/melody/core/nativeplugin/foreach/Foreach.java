package com.wat.melody.core.nativeplugin.foreach;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.WrapperInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.Property;
import com.wat.melody.common.properties.PropertyName;
import com.wat.melody.common.properties.PropertySet;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.core.nativeplugin.foreach.exception.ForeachException;

/**
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
	 * The 'item-name' XML attribute of the Foreach Task
	 */
	public static final String ITEMNAME_ATTR = "item-name";

	/**
	 * The 'max-par' XML attribute of the Foreach Task
	 */
	public static final String MAXPAR_ATTR = "max-par";

	public static final short NEW = 16;
	public static final short RUNNING = 8;
	public static final short SUCCEED = 0;
	public static final short FAILED = 1;
	public static final short INTERRUPTED = 2;
	public static final short CRITICAL = 4;

	private String _items = null;
	private PropertyName _itemName = null;
	private List<Element> _targets = null;
	private int _maxPar = 0;
	private Set<Element> _innerTasks;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<ForeachThread> _threadsList;
	private ConsolidatedException _exceptions;

	public Foreach() {
		setInnerTasks(new LinkedHashSet<Element>());
		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<ForeachThread>());
		setExceptions(new ConsolidatedException());
	}

	/**
	 * <p>
	 * Register the given task (in its native {@link Element} format) as an
	 * inner-task of this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given {@link Element} is <tt>null</tt>.
	 */
	@Override
	public void registerInnerTask(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		_innerTasks.add(n);
	}

	@Override
	public void validate() {
	}

	private short markState(short state) {
		return _state |= state;
	}

	private boolean isFailed() {
		return FAILED == (_state & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (_state & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (_state & CRITICAL);
	}

	/**
	 * <p>
	 * Process all inner-task against all selected targets (see
	 * {@link #getTargets()}). Each targets is proceed in a dedicated
	 * {@link ForeachThread}.
	 * </p>
	 * 
	 * @throws ForeachException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
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
				getExceptions().addCause(Ex);
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
		for (Element target : getTargets()) {
			PropertySet ps = Melody.getContext().getProperties().clone();
			// Add the property '<item-name>=<XPath position of currentItem>',
			// so
			// that '§[<item-name>]§' will be expanded with the item's XPath
			// position
			Property p = new Property(getItemName(),
					DocHelper.getXPathPosition(target), null);
			ps.put(p);
			ForeachThread ft = new ForeachThread(this, ps);
			if (!getThreadsList().add(ft)) {
				throw new RuntimeException("Didn't managed to register "
						+ "a new " + ForeachThread.class.getCanonicalName()
						+ ".");
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
				try {
					Melody.getContext().handleProcessorStateUpdates();
				} catch (InterruptedException Ex) {
					getExceptions().addCause(Ex);
					throw Ex;
				}
				ft.startProcessing();
			}
			return;
		}

		int threadToLaunchID = getThreadsList().size();
		List<ForeachThread> runningThreads = new ArrayList<ForeachThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			try {
				Melody.getContext().handleProcessorStateUpdates();
			} catch (InterruptedException Ex) {
				getExceptions().addCause(Ex);
				throw Ex;
			}
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
				+ "for " + FOREACH + " inner Task to finish.", getExceptions());
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
			short tfs = ft.getFinalState();
			markState(tfs);
			if (tfs == FAILED || tfs == CRITICAL || tfs == INTERRUPTED) {
				getExceptions().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new ForeachException(getExceptions());
		} else if (isFailed()) {
			throw new ForeachException(getExceptions());
		} else if (isInterrupted()) {
			throw new WrapperInterruptedException(getExceptions());
		}
	}

	/**
	 * @return the name of the property which will contains the XPath position
	 *         of the Target.
	 */
	private PropertyName getItemName() {
		return _itemName;
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
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 */
	@Attribute(name = ITEMNAME_ATTR, mandatory = true)
	public PropertyName setItemName(PropertyName propertyName) {
		if (propertyName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		PropertyName previous = getItemName();
		_itemName = propertyName;
		return previous;
	}

	/**
	 * @return the XPath expression which selects Targets.
	 */
	private String getItems() {
		return _items;
	}

	/**
	 * <p>
	 * Set the XPath Expression which selects targets.
	 * </p>
	 * 
	 * @param itemsExpr
	 *            the XPath expression which selects targets.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws ForeachException
	 *             if the given <tt>String</tt> is not a valid XPath Expression.
	 */
	@Attribute(name = ITEMS_ATTR, mandatory = true)
	public String setItems(String itemsExpr) throws ForeachException {
		if (itemsExpr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String.");
		}
		if (itemsExpr.trim().length() == 0) {
			throw new ForeachException(Msg.bind(
					Messages.ForeachEx_EMPTY_ITEMS_ATTR, itemsExpr));
		}
		try {
			setTargets(Melody.getContext().getProcessorManager()
					.getResourcesDescriptor().evaluateTargets(itemsExpr));
		} catch (XPathExpressionException Ex) {
			throw new ForeachException(Msg.bind(
					Messages.ForeachEx_INVALID_ITEMS_ATTR, itemsExpr), Ex);
		}
		String previous = getItems();
		_items = itemsExpr;
		return previous;
	}

	/**
	 * @return the targets selected by the XPath Expression which stands in
	 *         {@link #getItems()}.
	 */
	private List<Element> getTargets() {
		return _targets;
	}

	private List<Element> setTargets(List<Element> targets) {
		if (targets == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Element>.");
		}
		List<Element> previous = getTargets();
		_targets = targets;
		return previous;
	}

	/**
	 * @return the maximum number of thread this object can run simultaneously.
	 *         0 means there is no limit.
	 */
	private int getMaxPar() {
		return _maxPar;
	}

	/**
	 * <p>
	 * Set the maximum number of threads this object can run simultaneously. 0
	 * means there is no limit.
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
			throw new ForeachException(Msg.bind(
					Messages.ForeachEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		_maxPar = iMaxPar;
		return previous;
	}

	/**
	 * @return all inner-task (in their native {@link Element} format).
	 */
	protected Set<Element> getInnerTasks() {
		return _innerTasks;
	}

	private Set<Element> setInnerTasks(Set<Element> innerTasks) {
		if (innerTasks == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		Set<Element> previous = getInnerTasks();
		_innerTasks = innerTasks;
		return previous;
	}

	/**
	 * @return the {@link ThreadGroup} which holds all {@link ForeachThread}
	 *         managed by this object.
	 */
	protected ThreadGroup getThreadGroup() {
		return _threadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		_threadGroup = tg;
		return previous;
	}

	/**
	 * @return all the {@link ForeachThread} managed by this object.
	 */
	protected List<ForeachThread> getThreadsList() {
		return _threadsList;
	}

	private List<ForeachThread> setThreadsList(List<ForeachThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<ForeachThread>.");
		}
		List<ForeachThread> previous = getThreadsList();
		_threadsList = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the processing of this object.
	 */
	private ConsolidatedException getExceptions() {
		return _exceptions;
	}

	private ConsolidatedException setExceptions(ConsolidatedException cex) {
		if (cex == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConsolidatedException.class.getCanonicalName() + ".");
		}
		ConsolidatedException previous = getExceptions();
		_exceptions = cex;
		return previous;
	}

}