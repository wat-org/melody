package com.wat.melody.core.internal;

import java.io.IOException;
import java.nio.file.Path;

import org.w3c.dom.Element;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.properties.PropertySet;
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

	private Element _relatedElement;
	private PropertySet _propertiesSet;
	private ProcessorManager _processorManager;

	public TaskContext(Element n, PropertySet ps, ProcessorManager p) {
		setProcessorManager(p);
		setProperties(ps);
		setRelatedElement(n);
	}

	@Override
	public IProcessorManager getProcessorManager() {
		return _processorManager;
	}

	private ProcessorManager setProcessorManager(ProcessorManager pm) {
		if (pm == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProcessorManager.class.getCanonicalName() + ".");
		}
		ProcessorManager previous = _processorManager;
		_processorManager = pm;
		return previous;
	}

	@Override
	public PropertySet getProperties() {
		return _propertiesSet;
	}

	private PropertySet setProperties(PropertySet ps) {
		if (ps == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + PropertySet.class.getCanonicalName()
					+ ".");
		}
		PropertySet previous = getProperties();
		_propertiesSet = ps;
		return previous;
	}

	@Override
	public Element getRelatedElement() {
		return _relatedElement;
	}

	private Element setRelatedElement(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getRelatedElement();
		_relatedElement = n;
		return previous;
	}

	@Override
	public void handleProcessorStateUpdates() throws InterruptedException {
		_processorManager.handleProcessorStateUpdates();
	}

	@Override
	public String expand(String sToExpand) throws ExpressionSyntaxException {
		return XPathExpander.expand(sToExpand, _processorManager
				.getResourcesDescriptor().getDocument().getFirstChild(),
				getProperties());
	}

	@Override
	public String expand(Path fileToExpand) throws ExpressionSyntaxException,
			IOException, IllegalFileException {
		return XPathExpander.expand(fileToExpand, _processorManager
				.getResourcesDescriptor().getDocument().getFirstChild(),
				getProperties());
	}

	@Override
	public void processTask(Element n) throws TaskException,
			InterruptedException {
		processTask(n, getProperties());
	}

	@Override
	public void processTask(Element n, PropertySet ps) throws TaskException,
			InterruptedException {
		_processorManager.createAndProcessTask(n, ps);
	}

	@Override
	public IProcessorManager createSubProcessorManager() {
		return _processorManager.createSubProcessorManager(getProperties());
	}

}