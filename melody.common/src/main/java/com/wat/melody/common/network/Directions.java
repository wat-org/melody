package com.wat.melody.common.network;

import java.util.LinkedHashSet;

import com.wat.melody.common.network.exception.IllegalDirectionException;
import com.wat.melody.common.network.exception.IllegalDirectionsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Directions extends LinkedHashSet<Direction> {

	private static final long serialVersionUID = -354376569879565432L;

	public static final String DIRECTIONS_SEPARATOR = ",";

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
	 * Convert the given <code>String</code> to a {@link Directions} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>Direction(','Direction)*</code>. <BR/>
	 * * Direction must be a valid {@link Direction} (see
	 * {@link Direction#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sDirections
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link Directions} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalDirectionsException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Directions}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Directions parseString(String sDirections)
			throws IllegalDirectionsException {
		return new Directions(sDirections);
	}

	public Directions(String sDirections) throws IllegalDirectionsException {
		super();
		setDirections(sDirections);
	}

	public Directions(Direction... directions)
			throws IllegalDirectionsException {
		super();
		setDirections(directions);
	}

	public void setDirections(Direction... directions)
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
			throw new IllegalDirectionsException(Messages.bind(
					Messages.DirectionsEx_EMPTY, directions));
		}
	}

	public void setDirections(String sDirections)
			throws IllegalDirectionsException {
		if (sDirections == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Directions.class.getCanonicalName() + ").");
		}
		clear();
		for (String direction : sDirections.split(DIRECTIONS_SEPARATOR)) {
			direction = direction.trim();
			if (direction.length() == 0) {
				throw new IllegalDirectionsException(Messages.bind(
						Messages.DirectionsEx_EMPTY_DIRECTION, sDirections));
			}
			try {
				add(Direction.parseString(direction));
			} catch (IllegalDirectionException Ex) {
				throw new IllegalDirectionsException(Messages.bind(
						Messages.DirectionsEx_INVALID_DIRECTION, sDirections),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalDirectionsException(Messages.bind(
					Messages.DirectionsEx_EMPTY, sDirections));
		}
	}

}