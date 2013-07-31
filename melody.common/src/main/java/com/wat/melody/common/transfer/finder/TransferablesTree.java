package com.wat.melody.common.transfer.finder;

import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.TransferableFake;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferablesTree {

	public static TransferablesTree build(List<Transferable> resources) {
		TransferablesTree root = new TransferablesTree(null);
		for (Transferable r : resources) {
			includeTransferable(root, r);
		}
		return root;
	}

	private static TransferablesTree includeParentTree(TransferablesTree root,
			Transferable r) {
		TransferablesTree rt = root;
		// get the parent directory
		Path parent = r.getDestinationPath().getParent();
		if (parent == null) {
			return rt;
		}
		// insert / if parent directory is absolute
		Path cur = parent.getRoot();
		if (cur != null) {
			TransferablesTree crt = rt.getDirectory(cur);
			if (crt == null) {
				crt = rt.putDirectory(cur,
						new TransferableFake(cur, r.getResourceSpecification()));
			}
			rt = crt;
		}
		// insert each parent directory
		for (Path elmt : parent) {
			if (cur == null) {
				cur = elmt;
			} else {
				cur = cur.resolve(elmt);
			}
			TransferablesTree crt = rt.getDirectory(elmt);
			if (crt == null) {
				crt = rt.putDirectory(elmt,
						new TransferableFake(cur, r.getResourceSpecification()));
			}
			rt = crt;
		}
		// return the parent directory's Tree
		return rt;
	}

	private static void includeTransferable(TransferablesTree root,
			Transferable r) {
		TransferablesTree ft = includeParentTree(root, r);

		if (r.isDirectory()) {
			ft.putDirectory(r.getDestinationPath().getFileName(), r);
		} else {
			ft.putFile(r);
		}
	}

	private Transferable _t;

	private Map<Path, TransferablesTree> _innerdir;
	private List<Transferable> _innerfile;

	/**
	 * @param rs
	 *            Can be <tt>null</tt>, if it is the root Element of the
	 *            structure.
	 */
	public TransferablesTree(Transferable t) {
		_t = t;
		_innerdir = new HashMap<Path, TransferablesTree>();
		_innerfile = new ArrayList<Transferable>();
	}

	public Transferable getTransferable() {
		return _t;
	}

	public int getFilesCount() {
		return _innerfile.size();
	}

	public Transferable getFile(int i) {
		return _innerfile.get(i);
	}

	public void putFile(Transferable t) {
		_innerfile.add(t);
	}

	public int getAllFilesCount() {
		int dirsize = 0;
		for (Path elmt : getDirectoriesKeySet()) {
			dirsize += getDirectory(elmt).getAllFilesCount();
		}
		return _innerfile.size() + dirsize;
	}

	public TransferableFilesIterator getAllFiles() {
		return new TransferableFilesIterator(this);
	}

	public int getDirectoriesCount() {
		return _innerdir.size();
	}

	public TransferablesTree getDirectory(Path elmt) {
		return _innerdir.get(elmt);
	}

	public Set<Path> getDirectoriesKeySet() {
		return _innerdir.keySet();
	}

	public TransferablesTree putDirectory(Path elmt, Transferable t) {
		if (_innerdir.containsKey(elmt)) {
			throw new RuntimeException("fuck");
		}
		TransferablesTree inserted = new TransferablesTree(t);
		_innerdir.put(elmt, inserted);
		return inserted;
	}

	public int getAllDirectoriesCount() {
		int dirsize = 0;
		for (Path elmt : getDirectoriesKeySet()) {
			dirsize += getDirectory(elmt).getAllDirectoriesCount();
		}
		return _innerdir.size() + dirsize;
	}

	public TransferableDirectoriesIterator getAllDirectories() {
		return new TransferableDirectoriesIterator(this);
	}

	protected TransferablesTreesIterator getAllTransferablesTrees() {
		return new TransferablesTreesIterator(this);
	}

	public String toString() {
		String str = "";
		for (Transferable t : _innerfile) {
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