package com.wat.melody.core.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.wat.melody.api.IProcessorManager;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.api.report.ITaskReport;
import com.wat.melody.api.report.ITaskReportItem;
import com.wat.melody.api.report.TaskReportItemType;
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
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

	private static Logger log = LoggerFactory.getLogger(TaskContext.class);

	@Override
	public void reportActivity(ITaskReport report) {
		/*
		 * TODO : implement. Should call a method in _processorManager.
		 */
		log.warn("Task reporting is not implemented yet");
	}

	@Override
	public ITaskReport createTaskReport(Set<ITaskReportItem> reportItems) {
		/*
		 * TODO : implement. Should call a method in _processorManager.
		 */
		log.warn("Task reporting is not implemented yet");
		return null;
	}

	@Override
	public ITaskReportItem createTaskReportItem(Date date,
			TaskReportItemType taskReportType, String message) {
		/*
		 * TODO : implement. Should call a method in _processorManager.
		 */
		log.warn("Task reporting is not implemented yet");
		return null;
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
	public Path expand(Path fileToExpand, Path fileToStoreRes)
			throws ExpressionSyntaxException, IllegalFileException, IOException {
		if (fileToStoreRes == null) {
			String wf = getProcessorManager().getWorkingFolderPath();
			Path wfp = Paths.get(wf);
			try {
				Files.createDirectories(wfp);
				fileToStoreRes = Files.createTempFile(wfp, "transfer.", ".ted");
			} catch (IOException Ex) {
				throw new IOException("fail to create output file", Ex);
			}
		} else {
			try {
				FS.validateFilePath(fileToStoreRes.toString());
			} catch (IllegalDirectoryException | IllegalFileException Ex) {
				throw new IllegalFileException("invalid output file", Ex);
			}
		}
		String expanded = null;
		try {
			expanded = expand(fileToExpand);
		} catch (IllegalFileException Ex) {
			throw new IllegalFileException("fail to open input file", Ex);
		} catch (IOException Ex) {
			throw new IOException("fail to read input file", Ex);
		}
		try {
			Files.write(fileToStoreRes, expanded.getBytes());
		} catch (IOException Ex) {
			throw new IOException("fail to write output file", Ex);
		}
		return fileToStoreRes;
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