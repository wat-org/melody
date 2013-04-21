package com.wat.melody.core.nativeplugin.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

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
	 * The 'lock' XML attribute of the 'synchronize' XML element
	 */
	public static final String LOCK_ID_ATTR = "lockId";
	/**
	 * The 'scope' XML attribute of the 'synchronize' XML element
	 */
	public static final String SCOPE_ATTR = "scope";

	/**
	 * The 'maxPar' XML attribute of the 'synchronize' XML element
	 */
	public static final String MAXPAR_ATTR = "maxPar";

	private List<Node> _innerTasks;
	private LockId _lockId;
	private LockScope _lockScope;
	private MaxPar _maxPar;

	public Synchronize() {
		setInnerTask(new ArrayList<Node>());
		setLockId(LockId.DEFAULT_LOCK_ID);
		setLockScope(LockScope.CURRENT);
		setMaxPar(MaxPar.SEQUENTIAL);
	}

	/**
	 * <p>
	 * Register the given Task (in its native Node format) as an inner-task of
	 * this object.
	 * </p>
	 * 
	 * @throws IllegalArgumentException
	 *             if the given node is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given node is already registered.
	 */
	@Override
	public void registerInnerTask(Node n) throws TaskException {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (getInnerTask().contains(n)) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Node already present in list.");
		}
		getInnerTask().add(n);
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
			for (Node n : getInnerTask()) {
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
	 * <p>
	 * Get all inner-tasks (in their native {@link Node} format) of this task.
	 * </p>
	 * 
	 * @return all inner-task (in their native {@link Node} format).
	 */
	private List<Node> getInnerTask() {
		return _innerTasks;
	}

	private List<Node> setInnerTask(List<Node> nodes) {
		if (nodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Node.class.getCanonicalName() + ">.");
		}
		List<Node> previous = getInnerTask();
		_innerTasks = nodes;
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