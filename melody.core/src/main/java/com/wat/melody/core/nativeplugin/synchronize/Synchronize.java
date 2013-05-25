package com.wat.melody.core.nativeplugin.synchronize;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.core.nativeplugin.order.exception.OrderException;
import com.wat.melody.core.nativeplugin.synchronize.exception.SynchronizeException;
import com.wat.melody.core.nativeplugin.synchronize.types.LockId;
import com.wat.melody.core.nativeplugin.synchronize.types.LockScope;
import com.wat.melody.core.nativeplugin.synchronize.types.MaxPar;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Synchronize implements ITask, ITaskContainer, LockCallback {

	/**
	 * The 'synchronize' XML element used in the Sequence Descriptor
	 */
	public static final String SYNCHRONIZE = "synchronize";

	/**
	 * The 'lock-id' XML attribute of the 'synchronize' XML element
	 */
	public static final String LOCK_ID_ATTR = "lock-id";

	/**
	 * The 'scope' XML attribute of the 'synchronize' XML element
	 */
	public static final String SCOPE_ATTR = "scope";

	/**
	 * The 'max-par' XML attribute of the 'synchronize' XML element
	 */
	public static final String MAXPAR_ATTR = "max-par";

	private Set<Element> _innerTasks;
	private LockId _lockId;
	private LockScope _lockScope;
	private MaxPar _maxPar;

	public Synchronize() {
		setInnerTask(new LinkedHashSet<Element>());
		setLockId(LockId.DEFAULT_LOCK_ID);
		setLockScope(LockScope.CURRENT);
		setMaxPar(MaxPar.SEQUENTIAL);
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
	public void validate() throws SynchronizeException {
		// nothing to do
	}

	/**
	 * <p>
	 * Process all inner-tasks registered in this object. Wait for a free place
	 * in the semaphore to start processing.
	 * </p>
	 * 
	 * @throws OrderException
	 *             if an error occurred during processing.
	 * @throws InterruptedException
	 *             if the processing was interrupted.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the processing.
	 */
	@Override
	public void doProcessing() throws SynchronizeException,
			InterruptedException {
		try {
			LockManager.run(this, getMaxPar(), getLockScope(), getLockId());
		} catch (MelodyException Ex) {
			throw new SynchronizeException(Ex);
		}
	}

	@Override
	public void doRun() throws SynchronizeException, InterruptedException {
		try {
			for (Element n : getInnerTask()) {
				Melody.getContext().processTask(n);
			}
		} catch (InterruptedException Ex) {
			throw new MelodyInterruptedException("Synchronize task have been "
					+ "interrupted.", Ex);
		} catch (TaskException Ex) {
			throw new SynchronizeException(Ex);
		}
	}

	/**
	 * @return all inner-task (in their native {@link Element} format).
	 */
	private Set<Element> getInnerTask() {
		return _innerTasks;
	}

	private Set<Element> setInnerTask(Set<Element> innerTasks) {
		if (innerTasks == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Element.class.getCanonicalName() + ">.");
		}
		Set<Element> previous = getInnerTask();
		_innerTasks = innerTasks;
		return previous;
	}

	private LockId getLockId() {
		return _lockId;
	}

	@Attribute(name = LOCK_ID_ATTR)
	public LockId setLockId(LockId lockId) {
		if (lockId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LockId.class.getCanonicalName()
					+ ".");
		}
		LockId previous = getLockId();
		_lockId = lockId;
		return previous;
	}

	private LockScope getLockScope() {
		return _lockScope;
	}

	@Attribute(name = SCOPE_ATTR)
	public LockScope setLockScope(LockScope maxPar) {
		if (maxPar == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LockScope.class.getCanonicalName()
					+ ".");
		}
		LockScope previous = getLockScope();
		_lockScope = maxPar;
		return previous;
	}

	private MaxPar getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public MaxPar setMaxPar(MaxPar maxPar) {
		if (maxPar == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + MaxPar.class.getCanonicalName()
					+ ".");
		}
		MaxPar previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

}