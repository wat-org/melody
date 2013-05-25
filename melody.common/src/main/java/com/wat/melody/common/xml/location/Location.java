package com.wat.melody.common.xml.location;

import org.w3c.dom.Node;

import com.wat.melody.common.xml.Doc;

/**
 * <p>
 * Give access to a {@link Node}'s location data.
 * </p>
 * 
 * <p>
 * If the {@link Node} used to build this object is not originated from a
 * {@link Doc}, this object will not be able to provide reliable informations.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface Location {

	/**
	 * @return the file path used to parse the owner {@link Doc} of the given
	 *         {@link Node}, or <tt>null</tt> if the given {@link Node} is not
	 *         originated form a {@link Doc} .
	 */
	public String getSource();

	/**
	 * @return the line number where the given {@link Node} was located at parse
	 *         time if the given {@link Node} is originated form a {@link Doc},
	 *         or <tt>null</tt> if the given {@link Node} is not originated form
	 *         a {@link Doc} .
	 */
	public Integer getLine();

	/**
	 * @return the column number where the given {@link Node} was located at
	 *         parse time if the given {@link Node} is originated form a
	 *         {@link Doc}, or <tt>null</tt> if the given {@link Node} is not
	 *         originated form a {@link Doc}.
	 */
	public int getColumn();

	/**
	 * @return the location data (line number and column number) of the given
	 *         {@link Node}.
	 */
	@Override
	public String toString();

	/**
	 * @return the full location data (source file, line number and column
	 *         number) of the given {@link Node}..
	 */
	public String toFullString();

}