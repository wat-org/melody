package com.wat.melody.common.transfer.finder;

import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.wat.melody.common.bool.Bool;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.TransferableFake;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferablesTree {

	private static boolean DEBUG = Bool
			.easyParseString(System.getProperty(
					"com.wat.melody.common.transfer.finder.TransferablesTree",
					"false"));

	private TransferablesTree _parent = null;
	private Transferable _t = null;

	private Map<Path, TransferablesTree> _innerdir;
	private Map<Path, Transferable> _innerfile;

	public TransferablesTree() {
		_innerdir = new HashMap<Path, TransferablesTree>();
		_innerfile = new HashMap<Path, Transferable>();
	}

	/**
	 * @param parent
	 *            Can be <tt>null</tt>, if this object is the root Element of
	 *            the tree.
	 * @param t
	 *            Can be <tt>null</tt>, if this object is the root Element of
	 *            the tree.
	 */
	private TransferablesTree(TransferablesTree parent, Transferable t) {
		this();
		if (DEBUG) {
			if (parent == null) {
				throw new IllegalArgumentException("null: Not accepted. "
						+ "Must be a valid "
						+ TransferablesTree.class.getCanonicalName() + ".");
			}
			if (t == null) {
				throw new IllegalArgumentException("null: Not accepted. "
						+ "Must be a valid "
						+ Transferable.class.getCanonicalName() + ".");
			}

			// only accept direct child
			if (parent.getTransferable() != null) {
				Path path = t.getDestinationPath();
				Path parentpath = parent.getTransferable().getDestinationPath();
				if (path.equals(parentpath)) {
					throw new IllegalArgumentException(path
							+ ": Not accepted. " + "Must be diffenret than "
							+ parentpath + ".");
				}
				if (!path.startsWith(parentpath)) {
					throw new IllegalArgumentException(path
							+ ": Not accepted. " + "Must be a child of "
							+ parentpath + ".");
				}
				if (parentpath.relativize(path).getNameCount() > 1) {
					throw new IllegalArgumentException(path
							+ ": Not accepted. " + "Must be a direct child of "
							+ parentpath + ".");
				}
			} else {
				Path path = t.getDestinationPath();
				if (path.getNameCount() > 1) {
					throw new IllegalArgumentException(path
							+ ": Not accepted. "
							+ "Must be a direct child of root.");
				}
			}
		}

		_parent = parent;
		_t = t;
	}

	public Transferable getTransferable() {
		return _t;
	}

	public TransferablesTree getParent() {
		return _parent;
	}

	public boolean contains(Transferable t) {
		Path path = t.getDestinationPath();
		Path parent = null;
		Path cetofind = null;

		if (_t != null) {
			Path treepath = _t.getDestinationPath();
			if (!path.startsWith(treepath) || path.equals(treepath)) {
				return false;
			}
			parent = treepath.relativize(path).getParent();
		} else {
			cetofind = path.getRoot();
			parent = path.getParent();
		}

		if (cetofind == null && parent != null) {
			cetofind = parent.getName(0);
		}
		if (cetofind != null) {
			TransferablesTree ct = getDirectory(cetofind);
			if (ct == null) {
				return false;
			}
			return ct.contains(t);
		} else {
			if (t.isDirectory()) {
				return _innerdir.containsKey(path.getFileName());
			} else {
				return _innerfile.containsKey(path.getFileName());
			}
		}
	}

	public void put(Transferable t) {
		Path path = t.getDestinationPath();
		Path parent = null;
		Path cetoput = null;

		if (_t != null) {
			Path treepath = _t.getDestinationPath();
			if (!path.startsWith(treepath) || path.equals(treepath)) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Not a child of " + _t);
			}
			parent = treepath.relativize(path).getParent();
		} else {
			cetoput = path.getRoot();
			parent = path.getParent();
		}

		if (cetoput == null && parent != null) {
			cetoput = parent.getName(0);
		}
		if (cetoput != null) {
			TransferablesTree ct = getDirectory(cetoput);
			if (ct == null) {
				ct = putDirectory(new TransferableFake(_t == null ? cetoput
						: _t.getDestinationPath().resolve(cetoput)));
			}
			ct.put(t);
		} else {
			if (t.isDirectory()) {
				putDirectory(t);
			} else {
				putFile(t);
			}
		}
	}

	public void remove(Transferable t) {
		Path path = t.getDestinationPath();
		Path parent = null;
		Path cetoremove = null;

		if (_t != null) {
			Path treepath = _t.getDestinationPath();
			if (!path.startsWith(treepath) || path.equals(treepath)) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Not a child of " + _t);
			}
			parent = treepath.relativize(path).getParent();
		} else {
			parent = path.getParent();
			cetoremove = path.getRoot();
		}

		if (cetoremove == null && parent != null) {
			cetoremove = parent.getName(0);
		}
		if (cetoremove != null) {
			TransferablesTree ct = getDirectory(cetoremove);
			if (ct != null) {
				ct.remove(t);
			}
		} else {
			if (t.isDirectory()) {
				removeDirectory(t);
			} else {
				removeFile(t);
			}
		}
	}

	public int countFiles() {
		return _innerfile.size();
	}

	public int countDirectories() {
		return _innerdir.size();
	}

	public int countAllFiles() {
		int dirsize = 0;
		for (Path elmt : getDirectoriesKeySet()) {
			dirsize += getDirectory(elmt).countAllFiles();
		}
		return _innerfile.size() + dirsize;
	}

	public int countAllDirectories() {
		int dirsize = 0;
		for (Path elmt : getDirectoriesKeySet()) {
			dirsize += getDirectory(elmt).countAllDirectories();
		}
		return _innerdir.size() + dirsize;
	}

	public Transferable getFile(Path elmt) {
		return _innerfile.get(elmt);
	}

	public TransferablesTree getDirectory(Path elmt) {
		return _innerdir.get(elmt);
	}

	public Set<Path> getFilesKeySet() {
		return _innerfile.keySet();
	}

	public Set<Path> getDirectoriesKeySet() {
		return _innerdir.keySet();
	}

	public TransferableFilesIterator getAllFiles() {
		return new TransferableFilesIterator(this);
	}

	public TransferableDirectoriesIterator getAllDirectories() {
		return new TransferableDirectoriesIterator(this);
	}

	protected TransferablesTreesIterator getAllTransferablesTrees() {
		return new TransferablesTreesIterator(this);
	}

	private void putFile(Transferable t) {
		Path elmt = t.getDestinationPath().getFileName();
		if (DEBUG) {
			// can only insert regular file or link on regular file
			if (t.isDirectory()) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Only accept file or link on regular file.");
			}

			// can only insert direct child

			// reject if it already exists in directories
			if (_innerdir.containsKey(elmt)) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "A directory has already ben added with this name.");
			}
		}

		// insert (replace if already in)
		_innerfile.put(elmt, t);
	}

	private TransferablesTree putDirectory(Transferable t) {
		Path elmt = t.getDestinationPath().getFileName();
		if (DEBUG) {
			// can only insert directory or link on directory
			if (!t.isDirectory()) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Only accept directory or link on directory.");
			}

			// can only insert direct child

			// reject if it already exists in files
			if (elmt != null && _innerfile.containsKey(elmt)) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "A file has already ben added with this name.");
			}
		}

		// if /
		if (elmt == null) {
			elmt = t.getDestinationPath().getRoot();
		}

		// replace t if already in
		TransferablesTree child = getDirectory(elmt);
		if (child != null) {
			child._t = t;
			return child;
		}

		// insert
		TransferablesTree inserted = new TransferablesTree(this, t);
		_innerdir.put(elmt, inserted);
		return inserted;
	}

	private void removeFile(Transferable t) {
		Path path = t.getDestinationPath();
		if (DEBUG) {
			// can only remove regular file or link on regular file
			if (t.isDirectory()) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Only accept file or link on regular file.");
			}

			// can only remove direct child
			if (_t != null) {
				Path treepath = _t.getDestinationPath();
				if (!path.startsWith(treepath) || path.equals(treepath)) {
					throw new IllegalArgumentException(t + ": Not accepted. "
							+ "Not a child of " + _t);
				}
			} else {
				if (path.getNameCount() > 1) {
					throw new IllegalArgumentException(t + ": Not accepted. "
							+ "Not a child of root");
				}
			}
		}
		// remove
		Path elmt = path.getFileName();
		_innerfile.remove(elmt);

		// if parent != null, if current is Fake and if current is empty
		// => remove current from parent's directory
		if (_parent != null && _t instanceof TransferableFake
				&& countDirectories() + countFiles() == 0) {
			_parent.removeDirectory(_t);
		}
	}

	private void removeDirectory(Transferable t) {
		Path path = t.getDestinationPath();
		if (DEBUG) {
			// can only remove directory or link on directory
			if (!t.isDirectory()) {
				throw new IllegalArgumentException(t + ": Not accepted. "
						+ "Only accept directory or link on directory.");
			}

			// can only remove direct child
			if (_t != null) {
				Path treepath = _t.getDestinationPath();
				if (!path.startsWith(treepath) || path.equals(treepath)) {
					throw new IllegalArgumentException(t + ": Not accepted. "
							+ "Not a child of " + _t);
				}
			} else {
				if (path.getNameCount() > 1) {
					throw new IllegalArgumentException(t + ": Not accepted. "
							+ "Not a child of root");
				}
			}
		}

		Path elmt = path.getFileName();
		// if /
		if (elmt == null) {
			elmt = t.getDestinationPath().getRoot();
		}

		TransferablesTree ct = _innerdir.get(elmt);
		if (ct != null && ct.countFiles() + ct.countDirectories() != 0) {
			// convert to Fake if not empty
			ct._t = new TransferableFake(elmt);
		} else {
			// remove if empty
			// will not fail if it doesn't exists
			_innerdir.remove(elmt);
		}

		// if parent != null, if current is Fake and if current is empty
		// => remove current from parent's dir
		if (_parent != null && _t instanceof TransferableFake
				&& countDirectories() + countFiles() == 0) {
			_parent.removeDirectory(_t);
		}
	}

	@Override
	public String toString() {
		String str = "";
		for (Path path : getFilesKeySet()) {
			Transferable t = getFile(path);
			String type = "file";
			if (!t.exists()) {
				type = "invalidlink";
			} else if (!t.linkShouldBeConvertedToFile()) {
				type = "filelink";
			}
			str += "\n" + type + ":" + t.getDestinationPath().getFileName();
			FileAttribute<?>[] attrs = t.getExpectedAttributes();
			if (attrs != null) {
				str += "   " + Arrays.asList(attrs);
			}
		}
		for (Path path : getDirectoriesKeySet()) {
			Transferable t = getDirectory(path).getTransferable();
			if (!t.linkShouldBeConvertedToFile()) {
				str += "\n" + "dirlink:" + path;
				FileAttribute<?>[] attrs = t.getExpectedAttributes();
				if (attrs != null) {
					str += "   " + Arrays.asList(attrs);
				}
				continue;
			}
			str += "\n" + "dir:" + path;
			FileAttribute<?>[] attrs = t.getExpectedAttributes();
			if (attrs != null) {
				str += "   " + Arrays.asList(attrs);
			}
			String content = getDirectory(path).toString();
			if (content != null && content.length() != 0) {
				str += ("\n" + content).replaceAll("\\n", "\n  ");
			}
		}
		return str.length() == 0 ? str : str.substring(1);
	}

}