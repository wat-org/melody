package com.wat.melody.common.telnet.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <P>
 * Contains a {@link Set} of {@link Pattern}. The method
 * {@link #matches(String)} will indicate if one of the {@link Pattern} contains
 * by this object matches the given <tt>String</tt>.
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetResponsesMatcher {

	private LinkedHashSet<Pattern> _regexs = new LinkedHashSet<Pattern>();

	public TelnetResponsesMatcher(String... regexs) {
		if (regexs == null) {
			return;
		}
		for (String regex : regexs) {
			if (regex != null) {
				_regexs.add(Pattern.compile(regex));
			}
		}
	}

	@Override
	public int hashCode() {
		return _regexs.hashCode();
	}

	@Override
	public String toString() {
		return _regexs.toString();
	}

	/**
	 * @param telnetOutput
	 * 
	 * @return <tt>null</tt> if none of the <tt>String</tt> contains by this
	 *         object matches the given <tt>String</tt>, or a <tt>String</tt>
	 *         Array, where case 0 contains the character prior to the matcher
	 *         and case 1 contains the matcher.
	 */
	public String[] matches(String telnetOutput) {
		for (Pattern regex : _regexs) {
			Matcher matcher = regex.matcher(telnetOutput);
			if (matcher.find()) {
				String res[] = { telnetOutput.substring(0, matcher.start()),
						telnetOutput.substring(matcher.start()) };
				return res;
			}
		}
		return null;
	}

}