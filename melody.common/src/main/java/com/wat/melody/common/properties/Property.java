package com.wat.melody.common.properties;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.properties.exception.IllegalPropertyNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Property {

	/**
	 * The 'Property' XML element used in the Sequence Descriptor
	 */
	public static final String PROPERTY = "property";

	/**
	 * The 'name' XML attribute of the Property
	 */
	public static final String NAME_ATTR = "name";

	/**
	 * The 'value' XML attribute of the Property
	 */
	public static final String VALUE_ATTR = "value";

	/**
	 * The 'comment' XML attribute of the Property
	 */
	public static final String COMMENT_ATTR = "comment";

	/**
	 * The pattern which a Property String must satisfied
	 */
	public static final String PATTERN = PropertyName.PATTERN + "=.*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Property} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Yields exactly the same result as {@link Property#Property(String)}
	 * . <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param v
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Property} object, whose 'name' is equal to the Name part
	 *         of the given <tt>String</tt> and whose 'value' is equal to the
	 *         Value part of the given <tt>String</tt>.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given <tt>String</tt> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*=.*$".
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is null.
	 * 
	 */
	public static Property parseProperty(String v)
			throws IllegalPropertyException {
		return new Property(v);
	}

	private PropertyName _name = null;
	private String _value = null;
	private String _comment = null;

	/**
	 * <p>
	 * Creates an empty {@link Property}.
	 * </p>
	 */
	public Property() {
	}

	/**
	 * <p>
	 * Creates an {@link Property} object, based on the given values.
	 * </p>
	 * 
	 * <p>
	 * <i> * Act as {@link Property#Property(String, String, String)}, where
	 * <code>comment</code> is null, except that it will not throw an
	 * {@link IllegalArgumentException}. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param name
	 *            is the name of the {@link Property} object.
	 * @param value
	 *            is the value corresponding to the key.
	 * 
	 * @throws IllegalPropertyException
	 *             if the the given <code>key</code> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*$".
	 * @throws IllegalArgumentException
	 *             if the given <code>key</code> is null.
	 * @throws IllegalArgumentException
	 *             if the given <code>value</code> is null.
	 * 
	 */
	public Property(String name, String value) throws IllegalPropertyException {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * <p>
	 * Creates an {@link Property} object, based on the given values.
	 * </p>
	 * 
	 * @param name
	 *            is the key of the {@link Property} object.
	 * @param value
	 *            is the value corresponding to the key.
	 * @param comment
	 *            is a comment.
	 * 
	 * @throws IllegalPropertyException
	 *             if the the given <code>key</code> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*$".
	 * @throws IllegalArgumentException
	 *             if the given <code>key</code> is null.
	 * @throws IllegalArgumentException
	 *             if the given <code>value</code> is null.
	 * 
	 */
	public Property(String name, String value, String comment)
			throws IllegalPropertyException {
		this(name, value);
		setComment(comment);
	}

	/**
	 * <p>
	 * Creates an {@link Property} object, based on the given values.
	 * </p>
	 * 
	 * @param name
	 *            is the key of the {@link Property} object.
	 * @param value
	 *            is the value corresponding to the key.
	 * @param comment
	 *            is a comment.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <code>key</code> is null.
	 * @throws IllegalArgumentException
	 *             if the given <code>value</code> is null.
	 * 
	 */
	public Property(PropertyName name, String value, String comment) {
		this();
		setName(name);
		setValue(value);
		setComment(comment);
	}

	/**
	 * <p>
	 * Create a {@link Property} object based on the the given <tt>String</tt>.
	 * </p>
	 * 
	 * <p>
	 * <i> The given <tt>String</tt> must be composed of a Name part, followed
	 * by the equal character ('='), followed by the Value part. <BR/>
	 * More formally : <BR/>
	 * * The given <tt>String</tt> must satisfied the following pattern :
	 * <code>"^\\w+([.]\\w+)*=.*$"</code>. <BR/>
	 * * It implies that space character (<code>' '</code>) are forbidden in the
	 * Name part. <BR/>
	 * * It implies that comma character (<code>','</code>) are forbidden in the
	 * Name part. <BR/>
	 * * It implies the Name part cannot be an empty <tt>String</tt>. <BR/>
	 * * It implies the given <tt>String</tt> cannot an empty <tt>String</tt>. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param v
	 *            is the given <tt>String</tt>.
	 * 
	 * @return a {@link Property} object, whose 'name' is equal to the Name part
	 *         of the given <tt>String</tt> and whose 'value' is equal to the
	 *         Value part of the given <tt>String</tt>.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given <tt>String</tt> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*=.*$".
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is null.
	 * 
	 */
	public Property(String v) throws IllegalPropertyException {
		this();
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String.");
		}
		if (v.trim().length() == 0) {
			throw new IllegalPropertyException(Messages.bind(
					Messages.PropertyEx_EMPTY, v));
		} else if (!v.matches("^" + PATTERN + "$")) {
			throw new IllegalPropertyException(Messages.bind(
					Messages.PropertyEx_INVALID, v, PATTERN));
		}
		int split = v.indexOf('=');
		setName(v.substring(0, split));
		setValue(v.substring(split + 1));
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	public PropertyName getName() {
		return _name;
	}

	@Attribute(name = NAME_ATTR, mandatory = true)
	public PropertyName setName(PropertyName n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ PropertyName.class.getCanonicalName() + ".");
		}
		PropertyName previous = getName();
		_name = n;
		return previous;
	}

	public PropertyName setName(String n) throws IllegalPropertyException {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a PropertyName).");
		}
		try {
			return setName(PropertyName.parseString(n));
		} catch (IllegalPropertyNameException Ex) {
			throw new IllegalPropertyException(Ex);
		}
	}

	public String getValue() {
		return _value;
	}

	@Attribute(name = VALUE_ATTR, mandatory = true)
	public String setValue(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a PropertyValue).");
		}
		String previous = getValue();
		_value = v;
		return previous;
	}

	public String getComment() {
		return _comment;
	}

	@Attribute(name = COMMENT_ATTR)
	public String setComment(String v) {
		String previous = getComment();
		_comment = v;
		return previous;
	}

}