package com.wat.melody.cloud.instance;

import java.util.LinkedHashSet;
import java.util.Set;

import com.wat.melody.cloud.instance.exception.OperationException;

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
					+ "Must be a valid Set<InstanceControllerListener>.");
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
		for (InstanceControllerListener listener : getListeners()) {
			// TODO : consolidate errors and loop next
			listener.onInstanceCreated();
		}
	}

	protected void fireInstanceDestroyed() throws OperationException,
			InterruptedException {
		for (InstanceControllerListener listener : getListeners()) {
			// TODO : consolidate errors and loop next
			listener.onInstanceDestroyed();
		}
	}

	protected void fireInstanceStarted() throws OperationException,
			InterruptedException {
		for (InstanceControllerListener listener : getListeners()) {
			// TODO : consolidate errors and loop next
			listener.onInstanceStarted();
		}
	}

	protected void fireInstanceStopped() throws OperationException,
			InterruptedException {
		for (InstanceControllerListener listener : getListeners()) {
			// TODO : consolidate errors and loop next
			listener.onInstanceStopped();
		}
	}

}
