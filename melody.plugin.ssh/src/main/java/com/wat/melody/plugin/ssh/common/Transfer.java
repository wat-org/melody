package com.wat.melody.plugin.ssh.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

	private List<ResourcesSpecification> _resourcesSpecifications = new ArrayList<ResourcesSpecification>();
	private int _maxPar = 10;

	private ITaskContext _taskContext;

	public Transfer() {
		super();
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		// keep the current context, necessary for templating operations
		setContext(Melody.getContext());
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

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
	public Path doTemplate(Path template, Path destination)
			throws TemplatingException {
		try {
			return getContext().expand(template, destination);
		} catch (IllegalFileException Ex) {
			throw new TemplatingException(Ex);
		} catch (IOException Ex) {
			throw new TemplatingException(Ex);
		} catch (ExpressionSyntaxException Ex) {
			throw new TemplatingException(Ex);
		}
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