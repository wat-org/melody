package com.wat.melody.common.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.exception.IllegalPropertiesSetException;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * {@link PropertiesSet} holds Properties (e.g. a list of key/value elements).
 * </p>
 * <p>
 * Each key and its corresponding value is a <tt>String</tt>.
 * </p>
 * <p>
 * Each value can contains a <b>variable part</b> of the form <tt>${var}</tt>,
 * where <tt>var</tt> refers to another property's key. The <b>variable part</b>
 * resolution process is called <b>expansion</b>.
 * 
 * <p>
 * Properties hold by a {@link PropertiesSet} object can be initialized from a
 * file using the method {@link #load(String)}.
 * </p>
 * <p>
 * A property hold by a {@link PropertiesSet} object can be <b>set</b> using the
 * method {@link #put(String, String)}.
 * </p>
 * <p>
 * A property hold by a {@link PropertiesSet} object can be <b>get</b> using the
 * method {@link #get(String)}. The variable part of the Configuration
 * Directive's value will be expanded.
 * </p>
 * 
 * <p>
 * <i> This class is thread-safe, meaning that multiple threads can share the
 * same {@link PropertiesSet} object without the need for external
 * synchronization. </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class PropertiesSet {

	public static final String COMMENT_PATTERN = "^\\s*#.*$";
	public static final String EMPTY_STRING_PATTERN = "^\\s*$";

	private Map<String, Property> _properties = new LinkedHashMap<String, Property>();
	private String _sourceFile = null;

	/**
	 * <p>
	 * Creates an empty {@link PropertiesSet}.
	 * </p>
	 */
	public PropertiesSet() {
	}

	/**
	 * <p>
	 * Creates a new {@link PropertiesSet} which holds all properties defined in
	 * the file pointed by the given path (see {@link #load(String)} ).
	 * </p>
	 * 
	 * @param sFilePath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't points to a valid file (non
	 *             existing, or non readable, or a directory, ...).
	 * @throws IllegalPropertiesSetException
	 *             if a line of the file points by the given path is neither an
	 *             empty <tt>String</tt>, nor a comment, nor a Property String.
	 * @throws IllegalPropertiesSetException
	 *             if a Property's name is declared twice.
	 * @throws IllegalPropertiesSetException
	 *             if an unrecognized character is escaped.
	 * @throws IllegalPropertiesSetException
	 *             if a variable part is not properly formatted (no '{'
	 *             Immediately after a '$', or no '}' after a '${', ...).
	 * @throws IllegalPropertiesSetException
	 *             if, during the expansion process, a variable part refers to
	 *             an unknown Property's name.
	 * @throws IllegalPropertiesSetException
	 *             if, during the expansion process, a circular reference is
	 *             detected.
	 * @throws IllegalArgumentException
	 *             if the given path is null.
	 * @throws IOException
	 *             if an IO error occurred while reading the file points by the
	 *             given path.
	 * 
	 * @see {@link #load(String)}
	 * 
	 */
	public PropertiesSet(String sFilePath) throws IllegalFileException,
			IllegalPropertiesSetException, IOException {
		this();
		load(sFilePath);
	}

	private Map<String, Property> getProperties() {
		return _properties;
	}

	private String setFilePath(String sFilePath) {
		if (sFilePath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}
		return _sourceFile = sFilePath;
	}

	/**
	 * <p>
	 * Get the path of the file which was used to load (via {@link load(String)}
	 * ) this object.
	 * </p>
	 * 
	 * @return A <tt>String</tt> which represents the path of the file which was
	 *         used to load (via {@link load(String)}) this object.
	 * 
	 */
	public String getFilePath() {
		return _sourceFile;
	}

	/**
	 * <p>
	 * Load all Configuration Directives defined in the file pointed by the
	 * given path.
	 * </p>
	 * 
	 * <p>
	 * The file pointed by the given path is in a simple line-oriented format :
	 * <ul>
	 * <li>Every line which first non space character is a dash ('<tt>#</tt>')
	 * is considered as a comment ;</li>
	 * <li>Every line which contains only space character is considered as an
	 * empty line ;</li>
	 * <li>Every line which contains a character sequence which matches the
	 * pattern '^\\w+([.]\\w+)*=.*$' is considered as a Property String (as
	 * specified in {@link Property#Property(String)}) ;</li>
	 * <li>Every line which is neither an empty line, nor a comment, nor a
	 * Property String will raise an error ;</li>
	 * <li>In the whole file, a Property's name cannot be declared twice ;</li>
	 * </ul>
	 * A Property's value can contains a <b>variable part</b> of the form
	 * <tt>${var}</tt>, where <tt>var</tt> refers to another Property's Name :
	 * <ul>
	 * <li>This method will expand all variable parts found in each Property's
	 * value ;</li>
	 * <li>If a variable part refers to a non existing Property's name, a
	 * {@link IllegalPropertiesSetException} will be raised ;</li>
	 * <li>If a circular reference is detected, a
	 * {@link IllegalPropertiesSetException} will be raised ;</li>
	 * <li>Characters in a Property's value can be escaped. The escape character
	 * is the backslash ('<tt>\</tt>') ;</li>
	 * <li>If leading backslash is found just before a <tt>$</tt>, the trailing
	 * variable part will not be expanded ;</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Sample : A file contains 2 Properties
	 * 
	 * <pre>
	 * 		property1=${var1}/sub_folder/file.ext
	 * 		var1=/home
	 * </pre>
	 * 
	 * will be expanded this way :
	 * 
	 * <pre>
	 * 		property1=/home/sub_folder/file.ext
	 * 		var1=/home
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param sFilePath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't points to a valid file (non
	 *             existing, or non readable, or a directory, ...).
	 * @throws IllegalPropertiesSetException
	 *             if a line of the file points by the given path is neither an
	 *             empty <tt>String</tt>, nor a comment, nor a Property String.
	 * @throws IllegalPropertiesSetException
	 *             if a Property's name is declared twice.
	 * @throws IllegalPropertiesSetException
	 *             if an unrecognized character is escaped.
	 * @throws IllegalPropertiesSetException
	 *             if a variable part is not properly formatted (no '{'
	 *             Immediately after a '$', or no '}' after a '${', ...).
	 * @throws IllegalPropertiesSetException
	 *             if, during the expansion process, a variable part refers to
	 *             an unknown Property's name.
	 * @throws IllegalPropertiesSetException
	 *             if, during the expansion process, a circular reference is
	 *             detected.
	 * @throws IllegalArgumentException
	 *             if the given path is null.
	 * @throws IOException
	 *             if an IO error occurred while reading the file points by the
	 *             given path.
	 * 
	 */
	public synchronized String load(String sFilePath) throws IOException,
			IllegalFileException, IllegalPropertiesSetException {
		// remove all elements
		getProperties().clear();

		// Validate input parameters
		FS.validateFileExists(sFilePath);

		// Add a property 'UUID', which have a unique value
		final String UUID_COMMENT = "# 'UUID' is a special configuration "
				+ "directive automatically added by Melody." + SysTool.NEW_LINE
				+ "# 'UUID' can be used in Configuration Directive's value "
				+ "to generate unique value." + SysTool.NEW_LINE
				+ "# 'UUID' is used in the 'workingFolderPath' directive "
				+ "to generate unique working folder, so that multiple "
				+ "simultaneous execution of Melody have their own working "
				+ "folder.";
		try {
			put(new Property("UUID", SysTool.newUUID().toString(), UUID_COMMENT));
		} catch (IllegalPropertyException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "setting the 'UUID' property. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}

		// Parse the file and load properties without treatment (no expansion,
		// no escape)
		parseFile(sFilePath);

		// Escape chars and expand each properties
		escapedAndExpandProperties();

		// Set the file path
		return setFilePath(sFilePath);
	}

	private void parseFile(String sFilePath)
			throws IllegalPropertiesSetException, IOException {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(new File(sFilePath));
			br = new BufferedReader(fr);
			parseFile(br);
		} catch (FileNotFoundException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "creating an input stream for file '" + sFilePath + "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced. "
					+ "Or an external event made the file no more accessible "
					+ "(deleted, moved, read permission removed, ...).", Ex);
		} finally {
			if (br != null) {
				br.close();
			}
			if (fr != null) {
				fr.close();
			}
		}
	}

	private void parseFile(BufferedReader br)
			throws IllegalPropertiesSetException, IOException {
		String line = null;
		String read = null;
		Property p;
		String comment = null;
		while ((read = br.readLine()) != null) {
			line = read;
			// reassemble multi-lines
			while (line.length() > 0 && line.charAt(line.length() - 1) == '\\'
					&& (read = br.readLine()) != null) {
				line = line.substring(0, line.length() - 1) + read;
			}
			if (line.matches(COMMENT_PATTERN)
					|| line.matches(EMPTY_STRING_PATTERN)) {
				if (comment == null) {
					comment = line;
				} else {
					comment += SysTool.NEW_LINE + line;
				}
			} else {
				try {
					p = Property.parseProperty(line);
				} catch (IllegalPropertyException Ex) {
					throw new IllegalPropertiesSetException(Messages.bind(
							Messages.PropertiesSetEx_MALFORMED_LINE, line), Ex);
				}
				if (containsKey(p.getName().getValue())) {
					throw new IllegalPropertiesSetException(Messages.bind(
							Messages.PropertiesSetEx_MULTIPLE_DIRECTIVE,
							p.getName()));
				}
				p.setComment(comment);
				comment = null;
				put(p);
			}
		}
	}

	private void escapedAndExpandProperties()
			throws IllegalPropertiesSetException {
		// All properties will be escaped and expanded in a temporary array
		List<Property> ps = new ArrayList<Property>();
		List<String> circle = new ArrayList<String>();
		for (Property p : getProperties().values()) {
			Property n = new Property();
			ps.add(n);
			n.setName(p.getName());
			circle.add(p.getName().getValue());
			try {
				n.setValue(escapeAndExpand(p.getValue(), circle));
			} catch (IllegalPropertiesSetException Ex) {
				throw new IllegalPropertiesSetException(Messages.bind(
						Messages.PropertiesSetEx_INVALID_PROPERTY_VALUE,
						p.getName(), p.getValue()), Ex);
			}
			circle.remove(p.getName().getValue());
		}
		// Once escaped and expanded, the temporary array become the real one,
		// and the Quick Acces is updated
		for (Property p : ps) {
			put(p);
		}
	}

	private String escapeAndExpand(String v, List<String> circle)
			throws IllegalPropertiesSetException {
		int nBB = v.indexOf('\\');
		int nBegin = v.indexOf('$');
		boolean escaped = false;
		if (nBB != -1 && (nBegin == -1 || (nBegin != -1 && nBB < nBegin))) {
			if (nBB + 1 >= v.length()) {
				throw new IllegalPropertiesSetException(Messages.bind(
						Messages.PropertiesSetEx_INVALID_ESCAPE_SEQUENCE, v,
						nBB));
			} else {
				switch (v.charAt(nBB + 1)) {
				case 'n':
					return v.substring(0, nBB) + '\n'
							+ escapeAndExpand(v.substring(nBB + 2), circle);
				case 'r':
					return v.substring(0, nBB) + '\r'
							+ escapeAndExpand(v.substring(nBB + 2), circle);
				case 't':
					return v.substring(0, nBB) + '\t'
							+ escapeAndExpand(v.substring(nBB + 2), circle);
				case '\\':
					return v.substring(0, nBB) + '\\'
							+ escapeAndExpand(v.substring(nBB + 2), circle);
				case '$':
					escaped = true;
					break;
				default:
					throw new IllegalPropertiesSetException(Messages.bind(
							Messages.PropertiesSetEx_INVALID_ESCAPE_SEQUENCE,
							v, nBB));
				}
			}
		}
		// If no '$' can be found => nothing to do
		if (nBegin == -1)
			return v;
		// If the '$' is found at the last position and was escaped
		// => remove the escape char
		if (escaped && nBegin + 1 >= v.length()) {
			return v.substring(0, nBegin - 1) + "$";
		}
		// If the '$' is found at the last position => raise an error
		if (nBegin + 1 >= v.length()) {
			throw new IllegalPropertiesSetException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_FOUND, v,
					nBegin));
		}
		// If the $ is found but the next character is not a '{'
		// => raise an error
		if (v.charAt(nBegin + 1) != '{') {
			throw new IllegalPropertiesSetException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_OPENED, v,
					nBegin + 1));
		}
		// Search the corresponding '}'
		int nEnd = v.indexOf('}', nBegin + 1), nNext = nBegin + 1;
		while ((nNext + 1) < v.length()
				&& (nNext = v.indexOf('{', nNext + 1)) != -1) {
			if (nNext < nEnd) {
				nEnd = v.indexOf('}', nEnd + 1);
			} else {
				break;
			}
		}
		// If the '}' can not be found but the $ was escaped
		// => remove the escape char
		if (escaped && nEnd == -1) {
			return v.substring(0, nBegin - 1) + v.substring(nBegin);
		}
		// If the '}' can not be found => raise an error
		if (nEnd == -1) {
			throw new IllegalPropertiesSetException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_CLOSED, v,
					nBegin + 1));
		}
		// If the expression was escaped => remove the escape char
		if (escaped) {
			return v.substring(0, nBegin - 1) + "${"
					+ escapeAndExpand(v.substring(nBegin + 2, nEnd), circle)
					+ "}" + escapeAndExpand(v.substring(nEnd + 1), circle);
		}
		// resolve the expression
		return v.substring(0, nBegin)
				+ expand(v.substring(nBegin + 2, nEnd), circle)
				+ escapeAndExpand(v.substring(nEnd + 1), circle);
	}

	private String expand(String v, List<String> circle)
			throws IllegalPropertiesSetException {
		String escaped = escapeAndExpand(v, circle);
		if (!containsKey(escaped)) {
			throw new IllegalPropertiesSetException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_UNDEFINED, v));
		} else if (circle.contains(escaped)) {
			throw new IllegalPropertiesSetException(Messages.bind(
					Messages.PropertiesSetEx_CIRCULAR_REFERENCE,
					printCircularReferences(circle)));
		}
		circle.add(escaped);
		String expanded = escapeAndExpand(get(escaped), circle);
		circle.remove(escaped);
		return expanded;
	}

	private String printCircularReferences(List<String> circularRefStack) {
		StringBuilder str = new StringBuilder("");
		for (String property : circularRefStack) {
			str.append(SysTool.NEW_LINE);
			str.append("  Configuration Directive '" + property + "'");
			str.append(" depends of '" + get(property) + "'");
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return getProperties().toString();
	}

	/**
	 * @param key
	 *            is the requested key to test.
	 * 
	 * @return <tt>true</tt> if and only if the requested key is hold by this
	 *         object; <tt>false</tt> otherwise.
	 * 
	 * @throws NullPointerException
	 *             if the requested key is <tt>null</tt>.
	 * 
	 */
	public synchronized boolean containsKey(String key) {
		return getProperties().containsKey(key);
	}

	/**
	 * @param p
	 *            is the {@link Property} to put in this object.
	 * 
	 * @return the previous value of the given {@link Property}, or
	 *         <tt>null</tt> if it did not have one.
	 * 
	 */
	public synchronized Property put(Property p) {
		return getProperties().put(p.getName().getValue(), p);
	}

	/**
	 * @param key
	 *            is the requested key.
	 * 
	 * @return the <tt>String</tt> value corresponding to the specified key, or
	 *         <tt>null</tt> if it did not have one.
	 * 
	 */
	public synchronized String get(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().get(key).getValue();
	}

	/**
	 * @param key
	 *            is the requested key.
	 * 
	 * @return the {@link Property} corresponding to the specified key, or
	 *         <tt>null</tt> if it did not have one.
	 * 
	 */
	public synchronized Property getProperty(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().get(key);
	}

	/**
	 * @param key
	 *            is the key to remove from this object.
	 * 
	 * @return the previous value of the specified key, or <tt>null</tt> if it
	 *         did not have one.
	 * 
	 */
	public synchronized String remove(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().remove(key).getValue();
	}

	/**
	 * <p>
	 * Put all {@link Property} hold by the given {@link PropertiesSet} into
	 * this object.
	 * </p>
	 * 
	 * @param ps
	 *            is a {@link PropertiesSet} into this object.
	 * 
	 */
	public synchronized void putAll(PropertiesSet ps) {
		if (ps == null) {
			return;
		}
		for (Property p : ps.getProperties().values()) {
			put(p);
		}
	}

	/**
	 * <p>
	 * Duplicate this {@link PropertiesSet}.
	 * </p>
	 * 
	 * <ul>
	 * <li>Keys and values are not duplicated ;</li>
	 * <li>The duplicated {@link PropertiesSet} can be modified (add, remove
	 * keys) without impact one this object ;</li>
	 * </ul>
	 * 
	 * @return A {@link PropertiesSet} object, which is the copy of this object.
	 * 
	 */
	public synchronized PropertiesSet copy() {
		PropertiesSet copy = new PropertiesSet();
		for (Property p : getProperties().values()) {
			copy.put(p);
		}
		return copy;
	}

}
