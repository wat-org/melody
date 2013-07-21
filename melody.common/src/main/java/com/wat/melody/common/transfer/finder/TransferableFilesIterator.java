package com.wat.melody.common.transfer.finder;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.wat.melody.common.transfer.Transferable;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferableFilesIterator implements Iterable<Transferable>,
		Iterator<Transferable> {

	private TransferablesTree _tree;
	private TransferablesTreesIterator _it;
	private TransferablesTree _currentTree;
	private int _currentFile;

	public TransferableFilesIterator(TransferablesTree tree) {
		if (tree == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TransferablesTree.class.getCanonicalName() + ".");
		}
		_tree = tree;
		restart();
	}

	@Override
	public Iterator<Transferable> iterator() {
		return this;
	}

	public synchronized void restart() {
		_it = _tree.getAllTransferablesTrees();
		if (_it.hasNext()) {
			_currentTree = _it.next();
			_currentFile = 0;
		} else {
			_currentTree = null;
			_currentFile = 0;
		}
	}

	@Override
	public synchronized boolean hasNext() {
		while (_currentTree != null
				&& _currentFile >= _currentTree.getFilesCount()) {
			if (_it.hasNext()) {
				_currentTree = _it.next();
				_currentFile = 0;
			} else {
				_currentTree = null;
			}
		}
		return _currentTree != null;
	}

	@Override
	public synchronized Transferable next() {
		if (hasNext()) {
			return _currentTree.getFile(_currentFile++);
		}
		throw new NoSuchElementException();
	}

	@Override
	public synchronized void remove() {
		throw new UnsupportedOperationException();
	}

}