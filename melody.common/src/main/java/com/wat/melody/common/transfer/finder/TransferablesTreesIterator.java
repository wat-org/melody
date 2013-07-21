package com.wat.melody.common.transfer.finder;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferablesTreesIterator implements Iterable<TransferablesTree>,
		Iterator<TransferablesTree> {

	private TransferablesTree _tree;
	private Iterator<Path> _entriesIt;
	private TransferablesTreesIterator _subEntriesIt;

	public TransferablesTreesIterator(TransferablesTree tree) {
		if (tree == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferablesTree.class.getCanonicalName() + ".");
		}
		_tree = tree;
		restart();
	}

	@Override
	public Iterator<TransferablesTree> iterator() {
		return this;
	}

	public synchronized void restart() {
		_entriesIt = _tree.getDirectoriesKeySet().iterator();
		_subEntriesIt = null;
	}

	@Override
	public synchronized boolean hasNext() {
		if (_subEntriesIt != null && !_subEntriesIt.hasNext()) {
			_subEntriesIt = null;
		}
		return (_entriesIt.hasNext() && _subEntriesIt == null)
				|| (_subEntriesIt != null && _subEntriesIt.hasNext());
	}

	@Override
	public synchronized TransferablesTree next() {
		if (hasNext()) {
			if (_entriesIt.hasNext() && _subEntriesIt == null) {
				TransferablesTree sub = _tree.getDirectory(_entriesIt.next());
				_subEntriesIt = sub.getAllTransferablesTrees();
				return sub;
			}
			if (_subEntriesIt != null && _subEntriesIt.hasNext()) {
				return _subEntriesIt.next();
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public synchronized void remove() {
		throw new UnsupportedOperationException();
	}

}