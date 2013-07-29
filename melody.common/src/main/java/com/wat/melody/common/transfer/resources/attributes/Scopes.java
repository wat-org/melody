package com.wat.melody.common.transfer.resources.attributes;

import java.util.LinkedHashSet;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.Messages;
import com.wat.melody.common.transfer.exception.IllegalScopeException;
import com.wat.melody.common.transfer.exception.IllegalScopesException;

public class Scopes extends LinkedHashSet<Scope> {

	private static final long serialVersionUID = -876754243245457292L;

	public static final String SCOPES_SEPARATOR = ",";

	private static final String _ALL = "all";

	public static Scopes ALL = createScopes(Scope.FILES, Scope.DIRECTORIES,
			Scope.LINKS);

	private static Scopes createScopes(Scope... scopes) {
		try {
			return new Scopes(scopes);
		} catch (IllegalScopesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Scopes with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Scopes} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>scope(','scope)*</tt>
	 * <ul>
	 * <li><Each <tt>scope</tt> must be a valid {@link Scope} (see
	 * {@link Scope#parseString(String)}) ;</li>
	 * <li>The given <tt>String</tt> can be equals to 'all', which is equivalent
	 * to {@link Scope#FILES}, {@link Scope#DIRECTORIES} and {@link Scope#LINKS}
	 * ;</li>
	 * </ul>
	 * 
	 * @param scopes
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Scopes} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalScopesException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>scope</tt> is neither a valid {@link Scope} nor
	 *             equals to 'all' ;</li>
	 *             </ul>
	 */
	public static Scopes parseString(String scopes)
			throws IllegalScopesException {
		return new Scopes(scopes);
	}

	public Scopes(String scopes) throws IllegalScopesException {
		super();
		setScopes(scopes);
	}

	public Scopes(Scope... scopes) throws IllegalScopesException {
		super();
		setScopes(scopes);
	}

	private void setScopes(Scope... scopes) throws IllegalScopesException {
		clear();
		if (scopes == null) {
			return;
		}
		for (Scope scope : scopes) {
			if (scope == null) {
				continue;
			} else {
				add(scope);
			}
		}
		if (size() == 0) {
			throw new IllegalScopesException(Msg.bind(Messages.ScopesEx_EMPTY,
					(Object[]) scopes));
		}
	}

	private void setScopes(String scopes) throws IllegalScopesException {
		if (scopes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Scopes.class.getCanonicalName() + ").");
		}
		clear();
		for (String scope : scopes.split(SCOPES_SEPARATOR)) {
			scope = scope.trim();
			if (scope.length() == 0) {
				throw new IllegalScopesException(Msg.bind(
						Messages.ScopesEx_EMPTY_SCOPE, scopes));
			}
			if (scope.equalsIgnoreCase(_ALL)) {
				add(Scope.FILES);
				add(Scope.DIRECTORIES);
				add(Scope.LINKS);
				continue;
			}
			try {
				add(Scope.parseString(scope));
			} catch (IllegalScopeException Ex) {
				throw new IllegalScopesException(Msg.bind(
						Messages.ScopesEx_INVALID_SCOPE, scopes), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalScopesException(Msg.bind(Messages.ScopesEx_EMPTY,
					scopes));
		}
	}

}