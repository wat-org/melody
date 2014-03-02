package com.wat.melody.cloud.protectedarea;

import java.util.LinkedHashSet;
import java.util.Set;

import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.ex.ConsolidatedException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class BaseProtectedAreaController implements
		ProtectedAreaController {

	private Set<ProtectedAreaControllerListener> _listeners;

	public BaseProtectedAreaController() {
		setListeners(new LinkedHashSet<ProtectedAreaControllerListener>());
	}

	private Set<ProtectedAreaControllerListener> getListeners() {
		return _listeners;
	}

	private Set<ProtectedAreaControllerListener> setListeners(
			Set<ProtectedAreaControllerListener> listeners) {
		if (listeners == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ ProtectedAreaControllerListener.class.getCanonicalName()
					+ ">.");
		}
		Set<ProtectedAreaControllerListener> previous = getListeners();
		_listeners = listeners;
		return previous;
	}

	@Override
	public void addListener(ProtectedAreaControllerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaControllerListener.class.getCanonicalName()
					+ ".");
		}
		getListeners().add(listener);
	}

	@Override
	public void removeListener(ProtectedAreaControllerListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaControllerListener.class.getCanonicalName()
					+ ".");
		}
		getListeners().remove(listener);
	}

	protected void fireProtectedAreaCreated() throws ProtectedAreaException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (ProtectedAreaControllerListener listener : getListeners()) {
			try {
				listener.onProtectedAreaCreated();
			} catch (ProtectedAreaException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new ProtectedAreaException(cex);
		}
	}

	protected void fireProtectedAreaDestroyed() throws ProtectedAreaException,
			InterruptedException {
		ConsolidatedException cex = new ConsolidatedException();
		for (ProtectedAreaControllerListener listener : getListeners()) {
			try {
				listener.onProtectedAreaDestroyed();
			} catch (ProtectedAreaException Ex) {
				cex.addCause(Ex);
			}
		}
		if (cex.countCauses() != 0) {
			throw new ProtectedAreaException(cex);
		}
	}

}