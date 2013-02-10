package com.wat.melody.core.nativeplugin.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContainer;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.MelodyInterruptedException;
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

	private ITaskContext moContext;
	private List<Node> maNodes;
	private LockId moLockId;
	private LockScope moLockScope;
	private MaxPar moMaxPar;

	public Synchronize() {
		initContext();
		setNodes(new ArrayList<Node>());
		setLockId(LockId.DEFAULT_LOCK_ID);
		setLockScope(LockScope.CURRENT);
		setMaxPar(MaxPar.SEQUENTIAL);
	}

	private void initContext() {
		moContext = null;
	}

	@Override
	public void addNode(Node n) throws TaskException {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		if (getNodes().contains(n)) {
			throw new IllegalArgumentException(n.getNodeName()
					+ ": Not accepted. " + "Node already present in list.");
		}
		getNodes().add(n);
	}

	@Override
	public void validate() throws SynchronizeException {
		// nothing to do
	}

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
			for (Node n : getNodes()) {
				getContext().processTask(n);
			}
		} catch (InterruptedException Ex) {
			throw new MelodyInterruptedException("Synchronize task have been "
					+ "interrupted.", Ex);
		} catch (TaskException Ex) {
			throw new SynchronizeException(Ex);
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
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moContext = p;
	}

	private List<Node> getNodes() {
		return maNodes;
	}

	private List<Node> setNodes(List<Node> nodes) {
		if (nodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Node.class.getCanonicalName() + ">.");
		}
		List<Node> previous = getNodes();
		maNodes = nodes;
		return previous;
	}

	private LockId getLockId() {
		return moLockId;
	}

	@Attribute(name = LOCK_ID_ATTR)
	public LockId setLockId(LockId lockId) {
		if (lockId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LockId.class.getCanonicalName()
					+ ".");
		}
		LockId previous = getLockId();
		moLockId = lockId;
		return previous;
	}

	private LockScope getLockScope() {
		return moLockScope;
	}

	@Attribute(name = SCOPE_ATTR)
	public LockScope setLockScope(LockScope maxPar) {
		if (maxPar == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + LockScope.class.getCanonicalName()
					+ ".");
		}
		LockScope previous = getLockScope();
		moLockScope = maxPar;
		return previous;
	}

	private MaxPar getMaxPar() {
		return moMaxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public MaxPar setMaxPar(MaxPar maxPar) {
		if (maxPar == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + MaxPar.class.getCanonicalName()
					+ ".");
		}
		MaxPar previous = getMaxPar();
		moMaxPar = maxPar;
		return previous;
	}

}