package com.wat.melody.common.ex;

import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MelodyConsolidatedException extends MelodyException {

	private static final long serialVersionUID = -8976543243258798614L;

	private Set<Throwable> _causes;

	public MelodyConsolidatedException(Set<Throwable> causes) {
		super((String) null);
		setCauses(causes);
	}

	public MelodyConsolidatedException(String msg, Set<Throwable> causes) {
		super(msg);
		setCauses(causes);
	}

	public Set<Throwable> getCauses() {
		return _causes;
	}

	private Set<Throwable> setCauses(Set<Throwable> causes) {
		if (causes == null) {
			throw new IllegalArgumentException("null:Not accepted. "
					+ "Must be a Set<Throwable>.");
		}
		Set<Throwable> previous = getCauses();
		_causes = causes;
		return previous;
	}

	public void addCause(Throwable ex) {
		getCauses().add(ex);
	}

	@Override
	public String getMessage() {
		if (getCauses().size() == 0) {
			return null;
		}
		Iterator<Throwable> iter = getCauses().iterator();
		if (getCauses().size() == 1) {
			String err = Util.getUserFriendlyStackTrace(iter.next());
			return err.replaceAll(Util.NEW_LINE + "    ", Util.NEW_LINE);
		}
		String err = "";
		for (int i = 0; iter.hasNext(); i++) {
			Throwable ex = iter.next();
			err += Util.NEW_LINE + "Error " + (i + 1) + " : "
					+ Util.getUserFriendlyStackTrace(ex);
		}
		return err.replaceAll(Util.NEW_LINE, Util.NEW_LINE + "  ");
	}

}
