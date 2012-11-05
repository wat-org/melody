package com.wat.melody.common.utils;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.utils.exception.IllegalPropertyException;
import com.wat.melody.common.utils.exception.IllegalPropertyNameException;

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
	 * Convert the given <code>String</code> to a <code>Property</code> object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Yields exactly the same result as {@link Property#Property(String)}
	 * . <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param v
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>Property</code> object, whose 'name' is equal to the Name
	 *         part of the given <code>String</code> and whose 'value' is equal
	 *         to the Value part of the given <code>String</code>.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given <code>String</code> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*=.*$".
	 * @throws IllegalArgumentException
	 *             if the given <code>String</code> is null.
	 * 
	 */
	public static Property parseProperty(String v)
			throws IllegalPropertyException {
		return new Property(v);
	}

	private PropertyName msName;
	private String msValue;
	private String msComment;

	/**
	 * <p>
	 * Creates an empty <code>Property</code>.
	 * </p>
	 */
	public Property() {
		initName();
		initValue();
		initComment();
	}

	/**
	 * <p>
	 * Creates an <code>Property</code> object, based on the given values.
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
	 *            is the name of the <code>Property</code> object.
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
	 * Creates an <code>Property</code> object, based on the given values.
	 * </p>
	 * 
	 * @param name
	 *            is the key of the <code>Property</code> object.
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
		this();
		setName(name);
		setValue(value);
		setComment(comment);
	}

	/**
	 * <p>
	 * Creates an <code>Property</code> object, based on the given values.
	 * </p>
	 * 
	 * @param name
	 *            is the key of the <code>Property</code> object.
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
	public Property(PropertyName name, String value, String comment) {
		this();
		setName(name);
		setValue(value);
		setComment(comment);
	}

	/**
	 * <p>
	 * Create a <code>Property</code> object based on the the given
	 * <code>String</code>.
	 * </p>
	 * 
	 * <p>
	 * <i> The given <code>String</code> must be composed of a Name part,
	 * followed by the equal character ('='), followed by the Value part. <BR/>
	 * More formally : <BR/>
	 * * The given <code>String</code> must satisfied the following pattern :
	 * <code>"^\\w+([.]\\w+)*=.*$"</code>. <BR/>
	 * * It implies that space character (<code>' '</code>) are forbidden in the
	 * Name part. <BR/>
	 * * It implies that comma character (<code>','</code>) are forbidden in the
	 * Name part. <BR/>
	 * * It implies the Name part cannot be an empty <code>String</code>. <BR/>
	 * * It implies the given <code>String</code> cannot an empty
	 * <code>String</code>. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param v
	 *            is the given <code>String</code>.
	 * 
	 * @return a <code>Property</code> object, whose 'name' is equal to the Name
	 *         part of the given <code>String</code> and whose 'value' is equal
	 *         to the Value part of the given <code>String</code>.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given <code>String</code> doesn't respect the pattern
	 *             "^\\w+([.]\\w+)*=.*$".
	 * @throws IllegalArgumentException
	 *             if the given <code>String</code> is null.
	 * 
	 */
	public Property(String v) throws IllegalPropertyException {
		this();
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Property.class.getCanonicalName() + ").");
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

	private void initName() {
		msName = null;
	}

	private void initValue() {
		msValue = null;
	}

	private void initComment() {
		msComment = null;
	}

	public PropertyName getName() {
		return msName;
	}

	@Attribute(name = NAME_ATTR, mandatory = true)
	public PropertyName setName(PropertyName n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertyName.");
		}
		PropertyName previous = getName();
		msName = n;
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
		return msValue;
	}

	@Attribute(name = VALUE_ATTR, mandatory = true)
	public String setValue(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a PropertyValue).");
		}
		String previous = getValue();
		msValue = v;
		return previous;
	}

	public String getComment() {
		return msComment;
	}

	@Attribute(name = COMMENT_ATTR)
	public String setComment(String v) {
		String previous = getComment();
		msComment = v;
		return previous;
	}

}