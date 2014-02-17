package com.wat.melody.common.ex;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.wat.melody.common.systool.SysTool;

/**
 * This class provides the methods getUserFriendlyStackTarce() and
 * getFulStackTrace(), which aims are to generates better stack trace than the
 * standard printStackTrace().
 * 
 * We consider that Exception and Throwable have a major sementic difference :
 * Exception are for users, and Throwable are for developpers.
 * 
 * Exception's error message should provide enough informations for the user to
 * understand the problem, without seeing the stack trace. When an Exception
 * raise, we will only print its message and its causes messages, and we will
 * not print their stack trace. This is important for an Exception to be
 * catched, enhanced and re-throw at every level of the program during its
 * raising process so that it will arrive at the top of the program with
 * necessary informations for the user to understand what's going on, without
 * seeing the stack trace.
 * 
 * The problem with a Throwable is that it will not be catched, enhanced and
 * re-throw at every level of the program during its raising process. It will
 * arrives at the top of the program with exactly the same informations it had
 * when first throw. For this reason, we consider that showing a Throwable to a
 * user will not help him so much. But if this Throwble is useless for the user,
 * it is very useful for the developper. Now that he know this problem can
 * happend, he can enhance his program, to provide a better exception handling
 * in this particular situation.
 * 
 * In order to accomplish this, we provide the MelodyExcepotion class.
 * MelodyException provides a the method getUserFriendlyStackTrace(), which will
 * only print the message and the causes messages of Exception, and will print
 * the stack trace of Throwble. All the 'what to print' logic is done in
 * getUserFriendlyStackTrace().
 * 
 * Because we're cool, MelodyException also provides the method
 * getFullStackTrace(), which will print the full stack trace.
 * 
 * We also provide a ConsolidatedException, which aim is to consolidated
 * multiple exceptions in one. This can be very useful in a multi-thread
 * program, you want all multiple error to be aggregated in one.
 * 
 * The problem of the ConsolidatedException is that its stack trace cannot be
 * printed using the standard method (because it doesn't use the inner cause
 * member, but our own dedicated causes[] member). Better use
 * getFullStackTrace().
 * 
 * @author Guillaume Cornet
 * 
 */
public class MelodyException extends Exception {

	/*
	 * This object can't override neither toString() nor getMessage() methods
	 * cause they are used by the standard printStackTrace method. In other
	 * words, if toString() or getMessage() were used, the output of the
	 * standard printStackTrace method will be crazy.
	 */

	private static final long serialVersionUID = -1184066155132415814L;

	public MelodyException(String msg) {
		super(msg);
	}

	public MelodyException(Throwable cause) {
		super(null, cause);
	}

	public MelodyException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/*
	 * keep the compatibility with the standard methods.
	 */
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/*
	 * keep the compatibility with the standard methods.
	 */
	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		s.append(getConsolidatedExceptionStackTrace());
	}

	/*
	 * keep the compatibility with the standard methods.
	 */
	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		s.append(getConsolidatedExceptionStackTrace());
	}

	/**
	 * @return a <tt>String</tt>, which holds the stack trace of the deeper
	 *         cause this object contains, if this deeper cause is a
	 *         {@link ConsolidatedException}, or an empty <tt>String</tt>, if
	 *         this deeper cause is not a {@link ConsolidatedException}.
	 */
	protected String getConsolidatedExceptionStackTrace() {
		return getConsolidatedExceptionStackTrace(this);
	}

	/**
	 * @return a <tt>String</tt>, which holds the stack trace of this object.
	 */
	public String getFullStackTrace() {
		return getFullStackTrace(this);
	}

	/**
	 * <p>
	 * Output sample :
	 * 
	 * <pre>
	 * [file:./melody.tests/tests/call/UC_1/sd.xml] Processing finished &lt;FAILED&gt;.
	 *     Caused by: [line:6, column:21] Task 'order' finished &lt;FAILED&gt;.
	 *     Caused by: [line:7, column:33] Task 'call' finished &lt;FAILED&gt;.
	 *     Caused by: 
	 *       Error 1 : [file:./melody.tests/tests/call/UC_1/sd1.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:7, column:56] Task 'echo' created &lt;FAILED&gt;.
	 *           Caused by: [line:7, column:56, attribute:rejected_attr] The XML Attribute 'rejected_attr' is not accepted. To solve this issue, remove this XML Attribute.
	 *       Error 2 : [file:./melody.tests/tests/call/UC_1/sd2.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:67] Task 'call' created &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:67, attribute:sequence-descriptor] XML Attribute 'sequence-descriptor' set &lt;FAILED&gt;.
	 *           Caused by: './melody.tests/tests/call/UC_1/file.not_exists': Not accepted. File doesn't exist.
	 *       Error 3 : [file:./melody.tests/tests/call/UC_1/sd3.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:39] Task 'call' created &lt;FAILED&gt;.
	 *           Caused by: 'call' XML element should either contain an 'orders' XML attribute or one (at least) inner 'ref' XML Element.
	 *       Error 4 : [file:./melody.tests/tests/call/UC_1/sd4.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:13] Task 'foreach' created &lt;FAILED&gt;.
	 *           Caused by: The mandatory XML Attribute 'item-name' is missing. To solve this issue, declare the missing XML Attribute.
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return a <tt>String</tt>, which holds the user-oriented stack trace of
	 *         this object.
	 */
	public String getUserFriendlyStackTrace() {
		return getUserFriendlyStackTrace(this).toString();
	}

	/**
	 * @param ex
	 *            is a {@link Throwable}.
	 * 
	 * @return a <tt>String</tt>, which holds the stack trace of the deeper
	 *         cause the given {@link Throwable} contains, if this deeper cause
	 *         is a {@link ConsolidatedException}, or an empty <tt>String</tt>,
	 *         if this deeper cause is not a {@link ConsolidatedException} or if
	 *         the given {@link Throwable} is <tt>null</tt>.
	 */
	protected static String getConsolidatedExceptionStackTrace(Throwable ex) {
		// deep dive into the cause, to find the last one
		while (ex != null && ex.getCause() != null) {
			ex = ex.getCause();
		}
		// if the last cause is a ConsolidatedException, get its stack trace
		if (ex instanceof ConsolidatedException) {
			return ((ConsolidatedException) ex).getCausesStackTrace();
		}
		return "";
	}

	/**
	 * @param ex
	 *            is a {@link Throwable}.
	 * 
	 * @return a <tt>String</tt>, which holds the stack trace of the given
	 *         {@link Throwable}, or an empty <tt>String</tt> if the given
	 *         {@link Throwable} is <tt>null</tt>.
	 */
	protected static String getFullStackTrace(Throwable ex) {
		if (ex == null) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		// this will print the last ConsolidatedEx if ex is a MelodyEx
		ex.printStackTrace(pw);
		pw.close();
		String s = sw.toString().replaceAll("\t", "    ")
				.replaceAll(SysTool.NEW_LINE, SysTool.NEW_LINE + "    ");
		// this will print the last ConsolidatedEx if ex is not a MelodyEx
		if (!(ex instanceof MelodyException)) {
			s += getConsolidatedExceptionStackTrace(ex).replaceAll(
					SysTool.NEW_LINE, SysTool.NEW_LINE + "    ");
		}
		// Remove the last CRLF or LF
		if (s.endsWith(SysTool.NEW_LINE + "    ")) {
			s = s.substring(0,
					s.length() - (SysTool.NEW_LINE + "    ").length());
		}
		return s;
	}

	/**
	 * <p>
	 * Output sample :
	 * 
	 * <pre>
	 * [file:./melody.tests/tests/call/UC_1/sd.xml] Processing finished &lt;FAILED&gt;.
	 *     Caused by: [line:6, column:21] Task 'order' finished &lt;FAILED&gt;.
	 *     Caused by: [line:7, column:33] Task 'call' finished &lt;FAILED&gt;.
	 *     Caused by: 
	 *       Error 1 : [file:./melody.tests/tests/call/UC_1/sd1.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:7, column:56] Task 'echo' created &lt;FAILED&gt;.
	 *           Caused by: [line:7, column:56, attribute:rejected_attr] The XML Attribute 'rejected_attr' is not accepted. To solve this issue, remove this XML Attribute.
	 *       Error 2 : [file:./melody.tests/tests/call/UC_1/sd2.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:67] Task 'call' created &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:67, attribute:sequence-descriptor] XML Attribute 'sequence-descriptor' set &lt;FAILED&gt;.
	 *           Caused by: './melody.tests/tests/call/UC_1/file.not_exists': Not accepted. File doesn't exist.
	 *       Error 3 : [file:./melody.tests/tests/call/UC_1/sd3.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:39] Task 'call' created &lt;FAILED&gt;.
	 *           Caused by: 'call' XML element should either contain an 'orders' XML attribute or one (at least) inner 'ref' XML Element.
	 *       Error 4 : [file:./melody.tests/tests/call/UC_1/sd4.xml] Processing finished &lt;FAILED&gt;.
	 *           Caused by: [line:6, column:23] Task 'order' finished &lt;FAILED&gt;.
	 *           Caused by: [line:8, column:13] Task 'foreach' created &lt;FAILED&gt;.
	 *           Caused by: The mandatory XML Attribute 'item-name' is missing. To solve this issue, declare the missing XML Attribute.
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param ex
	 *            is a {@link Throwable}.
	 * 
	 * @return a <tt>String</tt>, which holds the user-oriented stack trace of
	 *         the given {@link Throwable}, or an empty <tt>String</tt> if the
	 *         given {@link Throwable} is <tt>null</tt>.
	 */
	protected static StringBuilder getUserFriendlyStackTrace(Throwable ex) {
		StringBuilder err = new StringBuilder("");
		String current;
		while (ex != null) {
			if (ex instanceof RuntimeException || !(ex instanceof Exception)) {
				current = getFullStackTrace(ex).replaceAll(
						SysTool.NEW_LINE + "    ", SysTool.NEW_LINE);
				ex = null; // break loop
			} else if (ex instanceof ConsolidatedException) {
				current = ((ConsolidatedException) ex)
						.getUserFriendlyStackTrace();
				ex = ex.getCause();
			} else if (ex instanceof HiddenException) {
				current = ex.getMessage(); // HiddenEx.getMessage is always null
				ex = null; // break loop
			} else {
				current = ex.getMessage();
				ex = ex.getCause();
			}
			if (current != null && current.length() != 0) {
				if (err.length() == 0) {
					err = new StringBuilder(current);
				} else {
					err.append(SysTool.NEW_LINE + "Caused by: " + current);
				}
			}
		}
		return SysTool.replaceAll(err, SysTool.NEW_LINE, SysTool.NEW_LINE
				+ "    ");
	}

}