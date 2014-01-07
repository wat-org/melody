package com.wat.melody.common.ex;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.wat.melody.common.systool.SysTool;

/**
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
	 * 
	 * For this reason, we're providing the method getUserFriendlyStackTarce().
	 * 
	 * For the same reason, we can't override getStackTrace() and we're
	 * providing getFulStackTrace().
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
	 * override printStackTrace : replace TAB by 4 space and print
	 * ConsolidatedException stackTrace
	 */
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		s.append(getConsolidatedExceptionStackTrace());
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		s.append(getConsolidatedExceptionStackTrace());
	}

	protected String getConsolidatedExceptionStackTrace() {
		Throwable ex = this;
		while (ex != null && ex.getCause() != null) {
			ex = ex.getCause();
		}
		if (ex instanceof ConsolidatedException) {
			return ((ConsolidatedException) ex).getCausesStackTrace();
		}
		return "";
	}

	/**
	 * <p>
	 * Return the user-oriented stack trace of this object as a <tt>String</tt>.
	 * </p>
	 * 
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
	 * <p>
	 * Return the full stack trace of this object as a <tt>String</tt>.
	 * </p>
	 * 
	 * @return a <tt>String</tt> which represent the stack trace of this object.
	 */
	public String getFullStackTrace() {
		return getFullStackTrace(this);
	}

	/**
	 * <p>
	 * Return the stack trace of the given {@link Throwable} object as a
	 * <tt>String</tt>.
	 * </p>
	 * 
	 * @param ex
	 *            is the {@link Throwable} object.
	 * 
	 * @return a <tt>String</tt> which represent the stack trace of the given
	 *         {@link Throwable} object, or an empty <tt>String</tt> if the
	 *         given {@link Throwable} object is <tt>null</tt>.
	 */
	protected static String getFullStackTrace(Throwable ex) {
		if (ex == null) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.close();
		String s = sw.toString().replaceAll("\t", "    ")
				.replaceAll(SysTool.NEW_LINE, SysTool.NEW_LINE + "    ");
		// Remove the last CRLF or LF
		if (s.endsWith(SysTool.NEW_LINE + "    ")) {
			s = s.substring(0,
					s.length() - (SysTool.NEW_LINE + "    ").length());
		}
		return s;
	}

	/**
	 * <p>
	 * Return the user-oriented stack trace of the given {@link Throwable}
	 * object as a <tt>String</tt>.
	 * </p>
	 * 
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
	 *            is the {@link Throwable} object.
	 * 
	 * @return a <tt>String</tt> which represent the user-oriented stack trace
	 *         of the given {@link Throwable} object, or an empty
	 *         <tt>String</tt> if the given {@link Throwable} object is
	 *         <tt>null</tt>.
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