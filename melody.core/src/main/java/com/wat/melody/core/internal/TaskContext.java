package com.wat.melody.core.internal;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Node;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.utils.PropertiesSet;
import com.wat.melody.xpathextensions.XPathExpander;

/**
 * <p>
 * Contains all contextual method and data a Task will need.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskContext implements ITaskContext {

	private Node moNode;
	private PropertiesSet moProperties;
	private ProcessorManager moProcessorManager;

	public TaskContext(Node n, PropertiesSet ps, ProcessorManager p) {
		setProcessorManager(p);
		setProperties(ps);
		setNode(n);
	}

	@Override
	public IProcessorManager getProcessorManager() {
		return moProcessorManager;
	}

	private ProcessorManager setProcessorManager(ProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ProcessorManager.");
		}
		ProcessorManager previous = moProcessorManager;
		moProcessorManager = pm;
		return previous;
	}

	@Override
	public PropertiesSet getProperties() {
		return moProperties;
	}

	private PropertiesSet setProperties(PropertiesSet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid PropertiesSet.");
		}
		PropertiesSet previous = getProperties();
		moProperties = ps;
		return previous;
	}

	@Override
	public Node getNode() {
		return moNode;
	}

	private Node setNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node.");
		}
		Node previous = getNode();
		moNode = n;
		return previous;
	}

	@Override
	public void handleProcessorStateUpdates() throws InterruptedException {
		moProcessorManager.handleProcessorStateUpdates();
	}

	@Override
	public String expand(String sToExpand) throws ExpressionSyntaxException {
		return XPathExpander.expand(sToExpand, moProcessorManager
				.getResourcesDescriptor().getDocument().getFirstChild(),
				getProperties());
	}

	@Override
	public String expand(Path fileToExpand) throws ExpressionSyntaxException,
			IOException {
		return XPathExpander.expand(fileToExpand, moProcessorManager
				.getResourcesDescriptor().getDocument().getFirstChild(),
				getProperties());
	}

	@Override
	public void processTask(Node n) throws TaskException, InterruptedException {
		processTask(n, getProperties());
	}

	@Override
	public void processTask(Node n, PropertiesSet ps) throws TaskException,
			InterruptedException {
		moProcessorManager.processTask(moProcessorManager.newTask(n, ps));
	}

	@Override
	public IProcessorManager createSubProcessorManager() {
		return moProcessorManager.createSubProcessorManager(getProperties());
	}

}