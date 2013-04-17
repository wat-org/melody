package com.wat.melody.core.internal;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.PropertiesSet;
import com.wat.melody.common.xpath.XPathExpander;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;

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
	private XPath moXPath;

	public TaskContext(Node n, PropertiesSet ps, ProcessorManager p) {
		setProcessorManager(p);
		setProperties(ps);
		setNode(n);
		setXPath(XPathExpander.newXPath(p.getXPathResolver()));
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

	public XPath getXPath() {
		return moXPath;
	}

	private XPath setXPath(XPath xpath) {
		XPath previous = getXPath();
		moXPath = xpath;
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
				getProperties(), getXPath());
	}

	@Override
	public String expand(Path fileToExpand) throws ExpressionSyntaxException,
			IOException, IllegalFileException {
		return XPathExpander.expand(fileToExpand, moProcessorManager
				.getResourcesDescriptor().getDocument().getFirstChild(),
				getProperties(), getXPath());
	}

	@Override
	public void processTask(Node n) throws TaskException, InterruptedException {
		processTask(n, getProperties());
	}

	@Override
	public void processTask(Node n, PropertiesSet ps) throws TaskException,
			InterruptedException {
		moProcessorManager.createAndProcessTask(n, ps);
	}

	@Override
	public IProcessorManager createSubProcessorManager() {
		return moProcessorManager.createSubProcessorManager(getProperties());
	}

}