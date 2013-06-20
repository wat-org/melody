package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalDirectionException;
import com.wat.melody.common.firewall.exception.IllegalDirectionsException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Directions extends LinkedHashSet<Direction> {

	private static final long serialVersionUID = -354376569879565432L;

	public static final String DIRECTIONS_SEPARATOR = ",";

	private static final String _ALL = "all";

	public static final Directions ALL = createDirections(Direction.IN,
			Direction.OUT);

	private static Directions createDirections(Direction... directions) {
		try {
			return new Directions(directions);
		} catch (IllegalDirectionsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a Directions with value '" + directions + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Directions} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>direction(','direction)*</tt>
	 * <ul>
	 * <li>Each <tt>direction</tt> must be a valid {@link Direction} (see
	 * {@link Direction#parseString(String)}) ;</li>
	 * <li>The given <tt>String</tt> can also be equal to 'all', which is equal
	 * to {@link Direction#IN} and {@link Direction#OUT} ;</li>
	 * </ul>
	 * 
	 * @param directions
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Directions} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalDirectionsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>direction</tt> is neither a valid
	 *             {@link Direction} nor equals to 'all' ;</li>
	 *             </ul>
	 */
	public static Directions parseString(String directions)
			throws IllegalDirectionsException {
		return new Directions(directions);
	}

	public Directions(String directions) throws IllegalDirectionsException {
		super();
		setDirections(directions);
	}

	public Directions(Direction... directions)
			throws IllegalDirectionsException {
		super();
		setDirections(directions);
	}

	private void setDirections(Direction... directions)
			throws IllegalDirectionsException {
		clear();
		if (directions == null) {
			return;
		}
		for (Direction direction : directions) {
			if (direction == null) {
				continue;
			} else {
				add(direction);
			}
		}
		if (size() == 0) {
			throw new IllegalDirectionsException(Msg.bind(
					Messages.DirectionsEx_EMPTY, (Object[]) directions));
		}
	}

	private void setDirections(String directions)
			throws IllegalDirectionsException {
		if (directions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Directions.class.getCanonicalName() + ").");
		}
		clear();
		for (String direction : directions.split(DIRECTIONS_SEPARATOR)) {
			direction = direction.trim();
			if (direction.length() == 0) {
				throw new IllegalDirectionsException(Msg.bind(
						Messages.DirectionsEx_EMPTY_DIRECTION, directions));
			}
			if (direction.equalsIgnoreCase(_ALL)) {
				add(Direction.IN);
				add(Direction.OUT);
				continue;
			}
			try {
				add(Direction.parseString(direction));
			} catch (IllegalDirectionException Ex) {
				throw new IllegalDirectionsException(Msg.bind(
						Messages.DirectionsEx_INVALID_DIRECTION, directions),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalDirectionsException(Msg.bind(
					Messages.DirectionsEx_EMPTY, directions));
		}
	}

}