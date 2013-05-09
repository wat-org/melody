package com.wat.melody.common.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.wat.melody.common.ex.Util;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.exception.IllegalPropertiesSetFileFormatException;
import com.wat.melody.common.properties.exception.IllegalPropertyException;

/**
 * <p>
 * <code>PropertiesSet</code> holds Properties (e.g. a list of key/value
 * elements).
 * </p>
 * <p>
 * Each key and its corresponding value is a <code>String</code>.
 * </p>
 * <p>
 * Each value can contains a <b>variable part</b> of the form
 * <code>${var}</code>, where <code>var</code> refers to another property's key.
 * The <b>variable part</b> resolution process is called <b>expansion</b>.
 * 
 * <p>
 * Properties hold by a <code>PropertiesSet</code> object can be initialized
 * from a file using the method {@link #load(String)}.
 * </p>
 * <p>
 * A property hold by a <code>PropertiesSet</code> object can be <b>set</b>
 * using the method {@link #put(String, String)}.
 * </p>
 * <p>
 * A property hold by a <code>PropertiesSet</code> object can be <b>get</b>
 * using the method {@link #get(String)}. The variable part of the Configuration
 * Directive's value will be expanded.
 * </p>
 * 
 * <p>
 * <i> This class is thread-safe, meaning that multiple threads can share the
 * same <code>PropertiesSet</code> object without the need for external
 * synchronization. </i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class PropertiesSet {

	public static final String COMMENT_PATTERN = "^\\s*#.*$";
	public static final String EMPTY_STRING_PATTERN = "^\\s*$";

	private Map<String, Property> moProperties;
	private String msFilePath;

	/**
	 * <p>
	 * Creates an empty <code>PropertiesSet</code>.
	 * </p>
	 */
	public PropertiesSet() {
		initProperties();
		initFilePath();
	}

	/**
	 * <p>
	 * Creates a new <code>PropertiesSet</code> which holds all properties
	 * defined in the file pointed by the given path (see {@link #load(String)}
	 * ).
	 * </p>
	 * 
	 * @param sFilePath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalFileException
	 *             if the given path doesn't points to a valid file (non
	 *             existing, or non readable, or a directory, ...).
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a line of the file points by the given path is neither an
	 *             empty <code>String</code>, nor a comment, nor a Property
	 *             String.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a Property's name is declared twice.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if an unrecognized character is escaped.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a variable part is not properly formatted (no '{'
	 *             immediatly after a '$', or no '}' after a '${', ...).
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if, during the expansion process, a variable part refers to
	 *             an unknown Property's name.
	 * @throws IllegalPropertiesSetFileFormatException
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
			IllegalPropertiesSetFileFormatException, IOException {
		this();
		load(sFilePath);
	}

	private void initProperties() {
		moProperties = new Hashtable<String, Property>();
	}

	private void initFilePath() {
		msFilePath = null;
	}

	private Map<String, Property> getProperties() {
		return moProperties;
	}

	private String setFilePath(String sFilePath) {
		if (sFilePath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}
		return msFilePath = sFilePath;
	}

	/**
	 * <p>
	 * Get the path of the file which was used to load (via {@link load(String)}
	 * ) this object.
	 * </p>
	 * 
	 * @return A <code>String</code> which represents the path of the file which
	 *         was used to load (via {@link load(String)}) this object.
	 * 
	 */
	public String getFilePath() {
		return msFilePath;
	}

	/**
	 * <p>
	 * Load all Configuration Directives defined in the file pointed by the
	 * given path.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with {@link #get(String)},
	 * {@link #put(String, String)} and {@link #containsKey(String)} methods. <BR/>
	 * * The file pointed by the given path is in a simple line-oriented format.
	 * <BR/>
	 * * Every line which first non space character is a dash ('<code>#</code>')
	 * is considered as a comment. <BR/>
	 * * Every line which contains only space character is considered as an
	 * empty line. <BR/>
	 * * Every line which contains a character sequence which matches the
	 * pattern '^\\w+([.]\\w+)*=.*$' is considered as a Property String (as
	 * specified in {@link Property#Property(String)}). <BR/>
	 * * Every line which is neither an empty line, nor a comment, nor a
	 * Property String will raise an error. <BR/>
	 * * In the whole file, a Property's name cannot be declared twice. <BR/>
	 * <BR/>
	 * * A Property's value can contains a <b>variable part</b> of the form
	 * <code>${var}</code>, where <code>var</code> refers to another Property's
	 * Name. <BR/>
	 * * This method will expand all variable parts found in each Property's
	 * value. <BR/>
	 * * If a variable part refers to a non existing Property's name, a
	 * {@link IllegalPropertiesSetFileFormatException} will be raised. <BR/>
	 * * If a circular reference is detected, a
	 * {@link IllegalPropertiesSetFileFormatException} will be raised. <BR/>
	 * * Characters in a Property's value can be escaped. The escape character
	 * is the backslash ('<code>\</code>'). <BR/>
	 * * If leading backslash is found just before a <code>$</code>, the
	 * trailing variable part will not be expanded. <BR/>
	 * </i>
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
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a line of the file points by the given path is neither an
	 *             empty <code>String</code>, nor a comment, nor a Property
	 *             String.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a Property's name is declared twice.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if an unrecognized character is escaped.
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if a variable part is not properly formatted (no '{'
	 *             Immediately after a '$', or no '}' after a '${', ...).
	 * @throws IllegalPropertiesSetFileFormatException
	 *             if, during the expansion process, a variable part refers to
	 *             an unknown Property's name.
	 * @throws IllegalPropertiesSetFileFormatException
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
			IllegalFileException, IllegalPropertiesSetFileFormatException {
		// remove all elements
		getProperties().clear();

		// Validate input parameters
		FS.validateFileExists(sFilePath);

		// Add a property 'UUID', which have a unique value
		final String UUID_COMMENT = "# 'UUID' is a special configuration "
				+ "directive automatically added by Melody." + Util.NEW_LINE
				+ "# 'UUID' can be used in Configuration Directive's value "
				+ "to generate unique value." + Util.NEW_LINE
				+ "# 'UUID' is used in the 'workingFolderPath' directive "
				+ "to generate unique working folder, so that multiple "
				+ "simultaneous execution of Melody have their own working "
				+ "folder.";
		try {
			put(new Property("UUID", java.util.UUID.randomUUID().toString(),
					UUID_COMMENT));
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
			throws IllegalPropertiesSetFileFormatException, IOException {
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
			throws IllegalPropertiesSetFileFormatException, IOException {
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
					comment += Util.NEW_LINE + line;
				}
			} else {
				try {
					p = Property.parseProperty(line);
				} catch (IllegalPropertyException Ex) {
					throw new IllegalPropertiesSetFileFormatException(
							Messages.bind(
									Messages.PropertiesSetEx_MALFORMED_LINE,
									line), Ex);
				}
				if (containsKey(p.getName().getValue())) {
					throw new IllegalPropertiesSetFileFormatException(
							Messages.bind(
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
			throws IllegalPropertiesSetFileFormatException {
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
			} catch (IllegalPropertiesSetFileFormatException Ex) {
				throw new IllegalPropertiesSetFileFormatException(
						Messages.bind(
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
			throws IllegalPropertiesSetFileFormatException {
		int nBB = v.indexOf('\\');
		int nBegin = v.indexOf('$');
		boolean escaped = false;
		if (nBB != -1 && (nBegin == -1 || (nBegin != -1 && nBB < nBegin))) {
			if (nBB + 1 >= v.length()) {
				throw new IllegalPropertiesSetFileFormatException(
						Messages.bind(
								Messages.PropertiesSetEx_INVALID_ESCAPE_SEQUENCE,
								v, nBB));
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
					throw new IllegalPropertiesSetFileFormatException(
							Messages.bind(
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
			throw new IllegalPropertiesSetFileFormatException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_FOUND, v,
					nBegin));
		}
		// If the $ is found but the next character is not a '{'
		// => raise an error
		if (v.charAt(nBegin + 1) != '{') {
			throw new IllegalPropertiesSetFileFormatException(Messages.bind(
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
			throw new IllegalPropertiesSetFileFormatException(Messages.bind(
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
			throws IllegalPropertiesSetFileFormatException {
		String escaped = escapeAndExpand(v, circle);
		if (!containsKey(escaped)) {
			throw new IllegalPropertiesSetFileFormatException(Messages.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_UNDEFINED, v));
		} else if (circle.contains(escaped)) {
			throw new IllegalPropertiesSetFileFormatException(Messages.bind(
					Messages.PropertiesSetEx_CIRCULAR_REFERENCE,
					printCircularReferences(circle)));
		}
		circle.add(escaped);
		String expanded = escapeAndExpand(get(escaped), circle);
		circle.remove(escaped);
		return expanded;
	}

	private String printCircularReferences(List<String> circularRefStack) {
		String str = "";
		for (String property : circularRefStack) {
			str += Util.NEW_LINE + "  Configuration Directive '" + property
					+ "' depends of '" + get(property) + "'";
		}
		return str;
	}

	/**
	 * <p>
	 * Tests if the requested key is declared is the properties holds by this
	 * object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #get(String)},
	 * {@link #put(String, String)} and {@link #load(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param key
	 *            is the requested key to test.
	 * 
	 * @return <code>true</code> if and only if the requested key is declared is
	 *         the properties holds by this object; <code>false</code>
	 *         otherwise.
	 * 
	 * @throws NullPointerException
	 *             if the requested key is <code>null</code>.
	 * 
	 */
	public synchronized boolean containsKey(String key) {
		return getProperties().containsKey(key);
	}

	/**
	 * <p>
	 * Put the key and its corresponding value in the properties hold by this
	 * object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #get(String)},
	 * {@link #load(String)} and {@link #containsKey(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param key
	 *            is key to be placed into the properties holds by this object.
	 * @param value
	 *            is its corresponding value.
	 * 
	 * @return the previous value of the specified key, or <code>null</code> if
	 *         it did not have one.
	 * 
	 */
	public synchronized Property put(Property p) {
		return getProperties().put(p.getName().getValue(), p);
	}

	/**
	 * <p>
	 * Get the value corresponding to the given key.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #put(String, String)},
	 * {@link #load(String)} and {@link #containsKey(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param key
	 *            is the requested key.
	 * 
	 * @return the value of the specified key, or <code>null</code> if it did
	 *         not have one.
	 * 
	 */
	public synchronized String get(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().get(key).getValue();
	}

	/**
	 * <p>
	 * Get the {@link Property} corresponding to the given key.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #put(String, String)},
	 * {@link #load(String)} and {@link #containsKey(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param key
	 *            is the requested key.
	 * 
	 * @return the {@link Property} of the specified key, or <code>null</code>
	 *         if it did not have one.
	 * 
	 */
	public synchronized Property getProperty(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().get(key);
	}

	/**
	 * <p>
	 * Remove the Property which correspond to the given key.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #put(String, String)},
	 * {@link #load(String)} and {@link #containsKey(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param key
	 *            is the key to remove.
	 * 
	 * @return the previous value of the specified key, or <code>null</code> if
	 *         it did not have one.
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
	 * Put all the content of the given PropertiesSet into this object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Provided for parallelism with the {@link #put(String, String)},
	 * {@link #load(String)} and {@link #containsKey(String)} methods. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param ps
	 *            is the source.
	 * 
	 */
	public synchronized void putAll(PropertiesSet ps) {
		for (Property p : ps.getProperties().values()) {
			put(p);
		}
	}

	/**
	 * <p>
	 * Duplicate this <code>PropertySet</code>.
	 * </p>
	 * 
	 * <p>
	 * <i> * Key and values are not duplicated. <BR/>
	 * * The duplicated PropertySet can be modified (add, remove key, ...)
	 * without impact one the original one. <BR/>
	 * * Thread-safe. <BR/>
	 * </i>
	 * </p>
	 * 
	 * @return A <code>PropertySet</code>, which is the copy of this object.
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
