package com.wat.melody.common.xml;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Wrap a {@link NodeList} into an {@link Iterable} object.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class NodeCollection implements Iterable<Node>, Iterator<Node> {

	private int _index = 0;
	private NodeList _nodeList;

	public NodeCollection(NodeList nodeList) {
		if (nodeList == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + NodeList.class.getCanonicalName()
					+ ".");
		}
		_nodeList = nodeList;
	}

	public int size() {
		return _nodeList.getLength();
	}

	public Node get(int index) {
		return _nodeList.item(index);
	}

	@Override
	public Iterator<Node> iterator() {
		return this;
	}

	public void restart() {
		_index = 0;
	}

	@Override
	public boolean hasNext() {
		return (_index < _nodeList.getLength());
	}

	@Override
	public Node next() {
		if (hasNext()) {
			return _nodeList.item(_index++);
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}