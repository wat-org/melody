package com.wat.melody.common.messages;

import org.eclipse.osgi.util.NLS;

import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Msg {

	/**
	 * <p>
	 * Bind the given message's substitution locations with the given values.
	 * </p>
	 * 
	 * @param message
	 *            the message to be manipulated.
	 * @param bindings
	 *            An array of objects to be inserted into the message. Each
	 *            object will be converted to <tt>String</tt> first, using
	 *            {@link String#valueOf(Object)}.
	 * 
	 * @return the manipulated String, where all the given message's
	 *         substitution locations are bound.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given message's substitution locations does not map to
	 *             '{an <tt>Integer</tt> '.
	 */
	public static String bind(String message, Object... bindings) {
		for (int i = 0; i < bindings.length; i++) {
			bindings[i] = bindings[i].toString().replaceAll(SysTool.NEW_LINE,
					SysTool.NEW_LINE + "  ");
		}
		return NLS.bind(message, bindings);
	}

}