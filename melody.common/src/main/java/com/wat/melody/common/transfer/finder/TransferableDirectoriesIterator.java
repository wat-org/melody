package com.wat.melody.common.transfer.finder;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.wat.melody.common.transfer.Transferable;

public class TransferableDirectoriesIterator implements Iterable<Transferable>,
		Iterator<Transferable> {

	private TransferablesTree _tree;
	private TransferablesTreesIterator _it;

	public TransferableDirectoriesIterator(TransferablesTree tree) {
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
	}

	@Override
	public synchronized boolean hasNext() {
		return _it.hasNext();
	}

	@Override
	public synchronized Transferable next() {
		if (hasNext()) {
			return _it.next().getTransferable();
		}
		throw new NoSuchElementException();
	}

	@Override
	public synchronized void remove() {
		throw new UnsupportedOperationException();
	}

}