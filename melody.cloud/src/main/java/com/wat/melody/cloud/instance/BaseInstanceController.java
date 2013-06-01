package com.wat.melody.cloud.instance;

import java.util.LinkedHashSet;
import java.util.Set;

import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.ex.ConsolidatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract class BaseInstanceController implements InstanceController {

	private Set<InstanceControllerListener> _listeners;

	public BaseInstanceController() {
		setListeners(new LinkedHashSet<InstanceControllerListener>());
	}

	private Set<InstanceControllerListener> getListeners() {
		return _listeners;
	}

	private Set<InstanceControllerListener> setListeners(
			Set<InstanceControllerListener> listeners) {
		if (listeners == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ InstanceControllerListener.class.getCanonicalName()
					+ ">.");
		}
		Set<InstanceControllerListener> previous = getListeners();
		_listeners = listeners;
		return previous;
	}

	@Override
	public void addListener(InstanceControllerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceControllerListener.class.getCanonicalName() + ".");
		}
		getListeners().add(listener);
	}

	@Override
	public void removeListener(InstanceControllerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceControllerListener.class.getCanonicalName() + ".");
		}
		getListeners().remove(listener);
	}

	protected void fireInstanceCreated() throws OperationException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (InstanceControllerListener listener : getListeners()) {
			try {
				listener.onInstanceCreated();
			} catch (OperationException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new OperationException(cex);
		}
	}

	protected void fireInstanceDestroyed() throws OperationException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (InstanceControllerListener listener : getListeners()) {
			try {
				listener.onInstanceDestroyed();
			} catch (OperationException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new OperationException(cex);
		}
	}

	protected void fireInstanceStarted() throws OperationException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (InstanceControllerListener listener : getListeners()) {
			try {
				listener.onInstanceStarted();
			} catch (OperationException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new OperationException(cex);
		}
	}

	protected void fireInstanceStopped() throws OperationException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (InstanceControllerListener listener : getListeners()) {
			try {
				listener.onInstanceStopped();
			} catch (OperationException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new OperationException(cex);
		}
	}

}
