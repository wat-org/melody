package com.wat.melody.plugin.ssh.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Transfer extends AbstractSshManagedOperation implements
		TemplatingHandler {

	/**
	 * Task's attribute, which specifies the maximum number of concurrent
	 * download.
	 */
	public static final String MAXPAR_ATTR = "max-par";

	/**
	 * Task's nested element, which specifies the resources to download.
	 */
	public static final String RESOURCES_NE = "resources";

	private List<ResourcesSpecification> _resourcesSpecifications;
	private int _maxPar;

	private ITaskContext _taskContext;

	public Transfer() {
		super();
		setResourcesSpecifications(new ArrayList<ResourcesSpecification>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
		setContext(Melody.getContext());
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			doTransfer(session);
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	public abstract void doTransfer(ISshSession session)
			throws SshSessionException, InterruptedException;

	@Override
	public Path doTemplate(Path r) throws TemplatingException {
		String fileContent = null;
		try {
			fileContent = getContext().expand(r);
		} catch (IllegalFileException Ex) {
			throw new RuntimeException("Unexpected error while "
					+ "templating the file " + r + "."
					+ "Source code has certainly been modified "
					+ "and a bug have been introduced.", Ex);
		} catch (IOException Ex) {
			throw new TemplatingException(Msg.bind(
					Messages.TransferEx_READ_IO_ERROR, r), Ex);
		} catch (ExpressionSyntaxException Ex) {
			throw new TemplatingException(Ex);
		}
		Path template = null;
		try {
			Files.createDirectories(Paths.get(getContext()
					.getProcessorManager().getWorkingFolderPath().toString()));
			template = Files.createTempFile(Paths.get(getContext()
					.getProcessorManager().getWorkingFolderPath()),
					"transfer.", ".ted");
			Files.write(template, fileContent.getBytes());
		} catch (IOException Ex) {
			throw new TemplatingException(Msg.bind(
					Messages.TransferEx_WRITE_IO_ERROR, template), Ex);
		}
		return template;
	}

	public List<ResourcesSpecification> getResourcesSpecifications() {
		return _resourcesSpecifications;
	}

	public List<ResourcesSpecification> setResourcesSpecifications(
			List<ResourcesSpecification> rrss) {
		if (rrss == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourcesSpecification.class.getCanonicalName() + ">.");
		}
		List<ResourcesSpecification> previous = getResourcesSpecifications();
		_resourcesSpecifications = rrss;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.CREATE)
	public ResourcesSpecification createResourcesSpecification() {
		File basedir = Melody.getContext().getProcessorManager()
				.getSequenceDescriptor().getBaseDir();
		ResourcesSpecification rrs = newResourcesSpecification(basedir);
		getResourcesSpecifications().add(rrs);
		return rrs;
	}

	public abstract ResourcesSpecification newResourcesSpecification(
			File basedir);

	public int getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int maxPar) throws SshException {
		if (maxPar < 1 || maxPar > 10) {
			throw new SshException(Msg.bind(
					Messages.TransferEx_INVALID_MAXPAR_ATTR, maxPar));
		}
		int previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

	private ITaskContext getContext() {
		return _taskContext;
	}

	private ITaskContext setContext(ITaskContext tc) {
		ITaskContext previous = getContext();
		_taskContext = tc;
		return previous;
	}

}