package com.wat.melody.common.ex;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Util {

	/**
	 * Is equal to System.getProperty("line.separator")
	 */
	public static final String NEW_LINE = System.getProperty("line.separator");

	/**
	 * <p>
	 * Return the stack trace of the given {@link Throwable} object as a
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param Ex
	 *            is the {@link Throwable} object.
	 * 
	 * @return a <tt>String</tt> which represent the stack trace of the given
	 *         {@link Throwable} object. or an empty <tt>String</tt> if the
	 *         given input {@link Throwable} object is <tt>null</tt>.
	 * 
	 */
	public static String getStackTrace(Throwable Ex) {
		if (Ex == null) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Ex.printStackTrace(pw);
		pw.close();
		String s = sw.toString();
		// Remove the last CRLF or LF
		if (s.endsWith(Util.NEW_LINE)) {
			s = s.substring(0, s.length() - Util.NEW_LINE.length());
		}
		return s;
	}

	public static String getUserFriendlyStackTrace(Throwable Ex) {
		String res = "";
		String current;
		while (Ex != null) {
			if (Ex instanceof RuntimeException || !(Ex instanceof Exception)) {
				current = Util.getStackTrace(Ex);
				Ex = null;
			} else {
				current = Ex.getMessage();
				Ex = Ex.getCause();
			}
			if (current != null && current.length() != 0) {
				if (res.length() == 0) {
					res = current;
				} else {
					res += Util.NEW_LINE + "Caused by : " + current;
				}
			}
		}
		return res.replaceAll(Util.NEW_LINE, Util.NEW_LINE + "    ");
	}

}