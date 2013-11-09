package com.wat.melody.plugin.cifs.common;

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
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.cifs.common.exception.CifsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Transfer extends AbstractCifsOperation implements
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

	private List<ResourcesSpecification> _resourcesSpecifications = new ArrayList<ResourcesSpecification>();
	private int _maxPar = 10;

	private ITaskContext _taskContext;

	public Transfer() {
		super();
	}

	@Override
	public void validate() throws CifsException {
		super.validate();

		// keep the current context, necessary for templating operations
		setContext(Melody.getContext());
	}

	@Override
	public void doProcessing() throws CifsException, InterruptedException {
		try {
			doTransfer(getLocation(), getDomain(), getUserName(), getPassword());
		} catch (TransferException Ex) {
			throw new CifsException(Ex);
		}
	}

	public abstract void doTransfer(String location, String domain,
			String username, String password) throws TransferException,
			InterruptedException;

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
					.getProcessorManager().getWorkingFolderPath()));
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

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.CREATE)
	public ResourcesSpecification createResourcesSpecification() {
		File basedir = Melody.getContext().getProcessorManager()
				.getSequenceDescriptor().getBaseDir();
		ResourcesSpecification rss = newResourcesSpecification(basedir);
		getResourcesSpecifications().add(rss);
		return rss;
	}

	public abstract ResourcesSpecification newResourcesSpecification(
			File basedir);

	public int getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int maxPar) throws CifsException {
		if (maxPar < 1 || maxPar > 10) {
			throw new CifsException(Msg.bind(
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