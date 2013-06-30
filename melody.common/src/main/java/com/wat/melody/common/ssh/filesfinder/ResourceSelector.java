package com.wat.melody.common.ssh.filesfinder;

import com.wat.melody.api.annotation.Attribute;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResourceSelector {

	/**
	 * Attribute, which specifies what to search.
	 */
	public static final String MATCH_ATTR = "match";

	private String _match = "**";

	public ResourceSelector() {
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("match:");
		str.append(getMatch());
		str.append(" }");
		return str.toString();
	}

	public String getMatch() {
		return _match;
	}

	@Attribute(name = MATCH_ATTR, mandatory = true)
	public String setMatch(String match) {
		if (match == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " " + "(a Path Matcher Glob Pattern).");
		}
		String previous = getMatch();
		_match = match;
		return previous;
	}

}