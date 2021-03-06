package com.wat.melody.common.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.properties.exception.IllegalPropertiesSetException;
import com.wat.melody.common.properties.exception.IllegalPropertyException;
import com.wat.melody.common.systool.SysTool;

/**
 * <p>
 * {@link PropertySet} holds properties (e.g. a list of key/value elements).
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
 * Properties hold by a {@link PropertySet} object can be initialized from a
 * file using the method {@link #load(String)}.
 * </p>
 * <p>
 * A property hold by a {@link PropertySet} object can be <b>set</b> using the
 * method {@link #put(String, String)}.
 * </p>
 * <p>
 * A property hold by a {@link PropertySet} object can be <b>get</b> using the
 * method {@link #get(String)}. The variable part of the property's value will
 * be expanded.
 * </p>
 * 
 * <p>
 * <i>This class is thread-safe, meaning that multiple threads can share the
 * same {@link PropertySet} object without the need for external
 * synchronization.</i>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class PropertySet {

	public static final String COMMENT_PATTERN = "^\\s*#.*$";
	public static final String EMPTY_STRING_PATTERN = "^\\s*$";
	public static final String INCLUDE_PATTERN = "^\\s*include\\s+.*$";

	private Map<String, Property> _properties = new LinkedHashMap<String, Property>();
	private String _sourceFile = null;

	/**
	 * <p>
	 * Creates an empty {@link PropertySet}.
	 * </p>
	 */
	public PropertySet() {
	}

	/**
	 * <p>
	 * Creates a new {@link PropertySet} which holds all properties defined in
	 * the file pointed by the given path (see {@link #load(String)}).
	 * </p>
	 * 
	 * @param filePath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non existing
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             file ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the file points by the
	 *             given path.
	 * @throws IllegalPropertiesSetException
	 *             <ul>
	 *             <li>if a line of the file points by the given path is neither
	 *             an empty <tt>String</tt>, nor a comment, nor a Property
	 *             String ;</li>
	 *             <li>if a Property's name is declared twice ;</li>
	 *             <li>if an unrecognized character is escaped ;</li>
	 *             <li>if a variable part is not properly formatted (no '{'
	 *             immediately after a '$', or no '}' after a '${', ...) ;</li>
	 *             <li>if, during the expansion process, a variable part refers
	 *             to an unknown Property's name ;</li>
	 *             <li>if, during the expansion process, a circular reference is
	 *             detected ;</li>
	 *             </ul>
	 */
	public PropertySet(String filePath) throws IllegalFileException,
			IllegalPropertiesSetException, IOException {
		this();
		load(filePath);
	}

	private Map<String, Property> getProperties() {
		return _properties;
	}

	private String setFilePath(String filePath) {
		if (filePath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a File Path).");
		}
		String previous = getSourceFile();
		_sourceFile = filePath;
		return previous;
	}

	/**
	 * @return the path of the file which was used to load (via
	 *         {@link #load(String)}) this object.
	 */
	public String getSourceFile() {
		return _sourceFile;
	}

	/**
	 * <p>
	 * Load all properties defined in the file pointed by the given path.
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
	 * pattern '^\\w+([.]\\w+)*=.*$' is considered as a property (as specified
	 * in {@link Property#Property(String)}) ;</li>
	 * <li>Every line which is neither an empty line, nor a comment, nor a
	 * property will raise an error ;</li>
	 * <li>In the whole file, a property's name cannot be declared twice ;</li>
	 * </ul>
	 * A property's value can contains a <b>variable part</b> of the form
	 * <tt>${var}</tt>, where <tt>var</tt> refers to another property's name :
	 * <ul>
	 * <li>This method will expand all variable parts found in each property's
	 * value ;</li>
	 * <li>If a variable part refers to a non existing property's name, a
	 * {@link IllegalPropertiesSetException} will be raised ;</li>
	 * <li>If a circular reference is detected, a
	 * {@link IllegalPropertiesSetException} will be raised ;</li>
	 * <li>Characters in a property's value can be escaped. The escape character
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
	 * @param filePath
	 *            is the path of the file to load.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given path is <tt>null</tt>.
	 * @throws IllegalFileException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> points to a directory ;</li>
	 *             <li>if the given <tt>String</tt> points to a non existing
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non readable
	 *             file ;</li>
	 *             <li>if the given <tt>String</tt> points to a non writable
	 *             file ;</li>
	 *             </ul>
	 * @throws IOException
	 *             if an IO error occurred while reading the file points by the
	 *             given path.
	 * @throws IllegalPropertiesSetException
	 *             <ul>
	 *             <li>if a line of the file points by the given path is neither
	 *             an empty <tt>String</tt>, nor a comment, nor a Property
	 *             String ;</li>
	 *             <li>if a property's name is declared twice ;</li>
	 *             <li>if an unrecognized character is escaped ;</li>
	 *             <li>if a variable part is not properly formatted (no '{'
	 *             immediately after a '$', or no '}' after a '${', ...) ;</li>
	 *             <li>if, during the expansion process, a variable part refers
	 *             to an unknown property's name ;</li>
	 *             <li>if, during the expansion process, a circular reference is
	 *             detected ;</li>
	 *             </ul>
	 */
	public synchronized String load(String filePath) throws IOException,
			IllegalFileException, IllegalPropertiesSetException {
		String previous = setFilePath(filePath);
		// remove all elements
		getProperties().clear();

		// Validate input parameters
		FS.validateFileExists(filePath);

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
		parseFile(filePath);

		// Set the file path
		return previous;
	}

	private void parseFile(String filePath)
			throws IllegalPropertiesSetException, IOException {
		List<String> circle = new ArrayList<String>();
		circle.add(new File(filePath).getCanonicalPath());
		parseFile(filePath, circle);
	}

	private void parseFile(String filePath, List<String> circle)
			throws IllegalPropertiesSetException, IOException {
		File f = new File(filePath);

		// laod the given file
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			parseFile(filePath, br, circle);
		} catch (FileNotFoundException Ex) {
			throw new RuntimeException("Unexpected error occurred while "
					+ "creating an input stream for file '" + filePath + "'. "
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

	private void parseFile(String f, BufferedReader br, List<String> circle)
			throws IllegalPropertiesSetException, IOException {
		String line = null;
		int pos = 0;
		String read = null;
		Property p;
		String comment = null;
		while ((read = br.readLine()) != null) {
			pos++;
			line = read;
			// reassemble multi-lines
			while (line.length() > 0 && line.charAt(line.length() - 1) == '\\'
					&& (read = br.readLine()) != null) {
				pos++;
				line = line.substring(0, line.length() - 1) + read;
			}
			// expand variable
			line = escapeAndExpand(f, pos, line);
			if (line.matches(COMMENT_PATTERN)
					|| line.matches(EMPTY_STRING_PATTERN)) {
				if (comment == null) {
					comment = line;
				} else {
					comment += SysTool.NEW_LINE + line;
				}
			} else if (line.matches(INCLUDE_PATTERN)) {
				String toLoad = line.replaceFirst("\\s*include\\s+", "");
				toLoad = new File(toLoad).getCanonicalPath();
				// include directive can leads in circular ref
				if (circle.contains(toLoad)) {
					throw new IllegalPropertiesSetException(f, pos, Msg.bind(
							Messages.PropertiesSetEx_CIRCULAR_REF, toLoad));
				}
				circle.add(toLoad);
				// load included file
				try {
					FS.validateFileExists(toLoad);
					parseFile(toLoad, circle);
				} catch (IllegalFileException | IllegalPropertiesSetException Ex) {
					throw new IllegalPropertiesSetException(f, pos, Msg.bind(
							Messages.PropertiesSetEx_INCLUDE_FAILED, toLoad),
							Ex);
				}
			} else {
				try {
					p = Property.parseProperty(line);
				} catch (IllegalPropertyException Ex) {
					throw new IllegalPropertiesSetException(f, pos, Msg.bind(
							Messages.PropertiesSetEx_MALFORMED_LINE, line), Ex);
				}
				if (containsKey(p.getName().getValue())) {
					throw new IllegalPropertiesSetException(f, pos, Msg.bind(
							Messages.PropertiesSetEx_MULTIPLE_KEY, p.getName()));
				}
				p.setComment(comment);
				comment = null;
				put(p);
			}
		}
	}

	private String escapeAndExpand(String f, int p, String v)
			throws IllegalPropertiesSetException {
		int nBB = v.indexOf('\\');
		int nBegin = v.indexOf('$');
		if (nBB != -1 && (nBegin == -1 || (nBegin != -1 && nBB < nBegin))) {
			if (nBB + 1 >= v.length()) {
				throw new IllegalPropertiesSetException(f, p, Msg.bind(
						Messages.PropertiesSetEx_INVALID_ESCAPE_SEQUENCE, v,
						nBB));
			} else {
				switch (v.charAt(nBB + 1)) {
				case 'n':
					return v.substring(0, nBB) + '\n'
							+ escapeAndExpand(f, p, v.substring(nBB + 2));
				case 'r':
					return v.substring(0, nBB) + '\r'
							+ escapeAndExpand(f, p, v.substring(nBB + 2));
				case 't':
					return v.substring(0, nBB) + '\t'
							+ escapeAndExpand(f, p, v.substring(nBB + 2));
				case '\\':
					return v.substring(0, nBB) + '\\'
							+ escapeAndExpand(f, p, v.substring(nBB + 2));
				case '$':
					return v.substring(0, nBB) + '$'
							+ escapeAndExpand(f, p, v.substring(nBB + 2));
				default:
					throw new IllegalPropertiesSetException(f, p, Msg.bind(
							Messages.PropertiesSetEx_INVALID_ESCAPE_SEQUENCE,
							v, nBB));
				}
			}
		}
		// If no '$' can be found => nothing to do
		if (nBegin == -1)
			return v;
		// If the '$' is found at the last position => raise an error
		if (nBegin + 1 >= v.length()) {
			throw new IllegalPropertiesSetException(f, p, Msg.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_FOUND, v,
					nBegin));
		}
		// If the $ is found but the next character is not a '{'
		// => raise an error
		if (v.charAt(nBegin + 1) != '{') {
			throw new IllegalPropertiesSetException(f, p, Msg.bind(
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
		// If the '}' can not be found => raise an error
		if (nEnd == -1) {
			throw new IllegalPropertiesSetException(f, p, Msg.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_NOT_CLOSED, v,
					nBegin + 1));
		}
		// resolve the expression
		return v.substring(0, nBegin)
				+ expand(f, p, v.substring(nBegin + 2, nEnd))
				+ escapeAndExpand(f, p, v.substring(nEnd + 1));
	}

	private String expand(String f, int p, String v)
			throws IllegalPropertiesSetException {
		String escaped = escapeAndExpand(f, p, v);
		if (!containsKey(escaped)) {
			throw new IllegalPropertiesSetException(f, p, Msg.bind(
					Messages.PropertiesSetEx_VARIABLE_SEQUENCE_UNDEFINED, v));
		}
		return escapeAndExpand(f, p, get(escaped));
	}

	@Override
	public int hashCode() {
		return getProperties().hashCode() + getSourceFile().hashCode();
	}

	@Override
	public String toString() {
		Iterator<Property> i = getProperties().values().iterator();
		if (!i.hasNext())
			return "{}";

		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for (;;) {
			Property value = i.next();
			sb.append(value);
			if (!i.hasNext())
				return sb.append(" }").toString();
			sb.append(',').append(' ');
		}
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PropertySet) {
			PropertySet ps = (PropertySet) anObject;
			return getProperties().equals(ps.getProperties())
					&& getSourceFile().equals(ps.getSourceFile());
		}
		return false;
	}

	/**
	 * <p>
	 * Remove all properties holds by this objects.
	 * </p>
	 */
	public synchronized void clear() {
		getProperties().clear();
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
	 * @throws NullPointerException
	 *             if the given property is <tt>null</tt>.
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
	 * @throws NullPointerException
	 *             if the requested key is <tt>null</tt>.
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
	 * @throws NullPointerException
	 *             if the requested key is <tt>null</tt>.
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
	 * @throws NullPointerException
	 *             if the requested key is <tt>null</tt>.
	 */
	public synchronized String remove(String key) {
		if (!containsKey(key)) {
			return null;
		}
		return getProperties().remove(key).getValue();
	}

	/**
	 * <p>
	 * Put all {@link Property} hold by the given {@link PropertySet} into this
	 * object.
	 * </p>
	 * 
	 * @param ps
	 *            is a {@link PropertySet} to put into this object. Can be
	 *            <tt>null</tt>. When <tt>null</tt>, this method return
	 *            immediately.
	 */
	public synchronized void putAll(PropertySet ps) {
		if (ps == null) {
			return;
		}
		for (Property p : ps.getProperties().values()) {
			put(p);
		}
	}

	/**
	 * @return A shallow copy of this object's (element themselves are not
	 *         copied. If the returned {@link PropertySet} is modified, this
	 *         object will not be modified.
	 */
	@Override
	public synchronized PropertySet clone() {
		PropertySet copy = new PropertySet();
		for (Property p : getProperties().values()) {
			copy.put(p);
		}
		return copy;
	}

}