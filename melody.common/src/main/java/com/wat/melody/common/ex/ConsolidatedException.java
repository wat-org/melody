package com.wat.melody.common.ex;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * This exception have the ability to store multiple causes. This is
 * particularly useful in a multi-thread environment, when many threads can
 * raise exception. All these exceptions can be grouped in a single
 * {@link ConsolidatedException}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class ConsolidatedException extends MelodyException {

	private static final long serialVersionUID = -8976543243258798614L;

	private Set<Throwable> _causes;

	public ConsolidatedException() {
		this((String) null);
	}

	public ConsolidatedException(String msg) {
		this(msg, new LinkedHashSet<Throwable>());
	}

	public ConsolidatedException(Set<Throwable> causes) {
		this(null, causes);
	}

	public ConsolidatedException(String msg, Set<Throwable> causes) {
		super(msg);
		setCauses(causes);
	}

	public Set<Throwable> getCauses() {
		return _causes;
	}

	private Set<Throwable> setCauses(Set<Throwable> causes) {
		if (causes == null) {
			throw new IllegalArgumentException("null:Not accepted. "
					+ "Must be a " + Set.class.getCanonicalName() + "<"
					+ Throwable.class.getCanonicalName() + ">.");
		}
		Set<Throwable> previous = getCauses();
		_causes = causes;
		return previous;
	}

	public synchronized int countCauses() {
		return getCauses().size();
	}

	public synchronized void addCause(Throwable ex) {
		getCauses().add(ex);
	}

	@Override
	public String getUserFriendlyStackTrace() {
		String msg = super.getMessage();
		if (msg != null && msg.length() == 0) {
			msg = null;
		}
		if (getCauses().size() == 0) {
			return msg;
		}
		Iterator<Throwable> iter = getCauses().iterator();
		if (getCauses().size() == 1) {
			StringBuilder err = getUserFriendlyStackTrace(iter.next());
			SysTool.replaceAll(err, SysTool.NEW_LINE + "    ", SysTool.NEW_LINE);
			return msg != null ? msg + SysTool.NEW_LINE + "Caused by: " + err
					: err.toString();
		}
		StringBuilder err = new StringBuilder();
		for (int i = 0; iter.hasNext(); i++) {
			err.append(SysTool.NEW_LINE + "Error " + (i + 1) + " : ");
			err.append(getUserFriendlyStackTrace(iter.next()));
		}
		SysTool.replaceAll(err, SysTool.NEW_LINE, SysTool.NEW_LINE + "  ");
		return msg != null ? msg + err : err.toString();
	}

	protected String getCausesStackTrace() {
		if (getCauses().size() == 0) {
			return "";
		}
		Iterator<Throwable> iter = getCauses().iterator();
		StringBuilder err = new StringBuilder();
		for (int i = 0; iter.hasNext(); i++) {
			StringBuilder part = new StringBuilder();
			part.append("  Error " + (i + 1) + " : ");
			part.append(getFullStackTrace(iter.next()));
			SysTool.replaceAll(part, SysTool.NEW_LINE, SysTool.NEW_LINE + "  ");
			err.append(SysTool.NEW_LINE + part);
		}
		err.delete(0, SysTool.NEW_LINE.length());
		return err.toString();
	}

}