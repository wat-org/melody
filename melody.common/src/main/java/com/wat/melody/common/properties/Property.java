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
	 * The pattern this object must satisfy.
	 */
	public static final String PATTERN = PropertyName.PATTERN + "=.*";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Property} object.
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
	 *             {@link #PATTERN}.
	 * @throws IllegalPropertyException
	 *             if the Name part of the given <tt>String</tt> is empty.
	 * @throws IllegalPropertyException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
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
	 * @param name
	 *            is the name of the {@link Property} object to create.
	 * @param value
	 *            is the value associated to the given name.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given name doesn't respect the pattern
	 *             {@link #PATTERN}.
	 * @throws IllegalPropertyException
	 *             if the given name is empty.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given value is <tt>null</tt>.
	 */
	public Property(String name, String value) throws IllegalPropertyException {
		this();
		setName(name);
		setValue(value);
	}

	/**
	 * @param name
	 *            is the name of the {@link Property} object to create.
	 * @param value
	 *            is the value associated to the given name.
	 * @param comment
	 *            is a comment associated to the given name.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given name doesn't respect the pattern
	 *             {@link #PATTERN}.
	 * @throws IllegalPropertyException
	 *             if the given name is empty.
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given value is <tt>null</tt>.
	 */
	public Property(String name, String value, String comment)
			throws IllegalPropertyException {
		this(name, value);
		setComment(comment);
	}

	/**
	 * @param name
	 *            is the name of the {@link Property} object to create.
	 * @param value
	 *            is the value associated to the given name.
	 * @param comment
	 *            is a comment associated to the given name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given name is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given value is <tt>null</tt>.
	 */
	public Property(PropertyName name, String value, String comment) {
		this();
		setName(name);
		setValue(value);
		setComment(comment);
	}

	/**
	 * <p>
	 * Create a {@link Property} object, based on the the given <tt>String</tt>.
	 * </p>
	 * 
	 * <p>
	 * The given <tt>String</tt> must be composed of a Name part, followed by
	 * the equal character ('='), followed by the Value part. More formally :
	 * <ul>
	 * <li>The given <tt>String</tt> must satisfied the pattern {@link #PATTERN}
	 * ;</li>
	 * <li>the Name part cannot contains space character (<code>' '</code>),
	 * comma character ( <code>','</code>) and equal character (<code>'='</code>
	 * ) ;</li>
	 * <li>The Name part cannot be an empty <tt>String</tt> ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param v
	 *            is the <tt>String</tt> to convert.
	 * 
	 * @return a {@link Property} object, whose 'name' is equal to the Name part
	 *         of the given <tt>String</tt> and whose 'value' is equal to the
	 *         Value part of the given <tt>String</tt>.
	 * 
	 * @throws IllegalPropertyException
	 *             if the given <tt>String</tt> doesn't respect the pattern
	 *             {@link #PATTERN}.
	 * @throws IllegalPropertyException
	 *             if the Name part of the given <tt>String</tt> is empty.
	 * @throws IllegalPropertyException
	 *             if the given <tt>String</tt> is empty.
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
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
	public int hashCode() {
		return getName().hashCode() + getValue().hashCode();
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof Property) {
			Property ps = (Property) anObject;
			return getName().equals(ps.getName())
					&& getValue().equals(ps.getValue());
		}
		return false;
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