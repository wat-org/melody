package com.wat.melody.plugin.ssh;

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
import com.wat.melody.common.files.FS;
import com.wat.melody.common.files.exception.IllegalDirectoryException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.types.ResourceMatcher;
import com.wat.melody.common.ssh.types.Resources;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.ssh.common.AbstractSshManagedOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Upload extends AbstractSshManagedOperation implements
		TemplatingHandler {

	/**
	 * Task's name
	 */
	public static final String UPLOAD = "upload";

	/**
	 * Task's attribute, which specifies the maximum number of concurrent
	 * upload.
	 */
	public static final String MAXPAR_ATTR = "max-par";

	/**
	 * Task's nested element, which specifies the resources to upload.
	 */
	public static final String RESOURCES_NE = "resources";

	private List<Resources> _resourcesList;
	private int _maxPar;

	private ITaskContext _taskContext;

	public Upload() {
		super();
		setResourcesList(new ArrayList<Resources>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
		setContext(Melody.getContext());
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (Resources resources : getResourcesList()) {
			try {
				// validate each inner include/exclude
				validateResourceElementLsit(resources.getIncludes(),
						Resources.INCLUDE_NE);
				validateResourceElementLsit(resources.getExcludes(),
						Resources.EXCLUDE_NE);
				// validate itself
				validateResourceElement(resources, RESOURCES_NE);
			} catch (SshException Ex) {
				throw new SshException(Msg.bind(Messages.UploadEx_INVALID_NE,
						RESOURCES_NE), Ex);
			}
		}
	}

	private void validateResourceElementLsit(
			List<ResourceMatcher> aResourceMatcher, String which)
			throws SshException {
		for (ResourceMatcher r : aResourceMatcher) {
			validateResourceElement(r, which);
		}
	}

	private void validateResourceElement(ResourceMatcher r, String which)
			throws SshException {
		if (r.getLocalBaseDir() == null) {
			r.setLocalBaseDir(Melody.getContext().getProcessorManager()
					.getSequenceDescriptor().getBaseDir());
		}
		try {
			FS.validateDirExists(r.getLocalBaseDir().toString());
		} catch (IllegalDirectoryException Ex) {
			throw new SshException(Msg.bind(
					Messages.UploadEx_INVALID_LOCALBASEDIR_ATTR,
					r.getLocalBaseDir(), ResourceMatcher.LOCAL_BASEDIR_ATTR),
					Ex);
		}
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			session.upload(getResourcesList(), getMaxPar(), this);
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

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
					Messages.SshEx_READ_IO_ERROR, r), Ex);
		} catch (ExpressionSyntaxException Ex) {
			throw new TemplatingException(Ex);
		}
		Path template = null;
		try {
			Files.createDirectories(Paths.get(getContext()
					.getProcessorManager().getWorkingFolderPath().toString()));
			template = Files.createTempFile(
					Paths.get(getContext().getProcessorManager()
							.getWorkingFolderPath().toString()), "template.",
					".txt");
			Files.write(template, fileContent.getBytes());
		} catch (IOException Ex) {
			throw new TemplatingException(Msg.bind(
					Messages.SshEx_WRITE_IO_ERROR, template), Ex);
		}
		return template;
	}

	public List<Resources> getResourcesList() {
		return _resourcesList;
	}

	public List<Resources> setResourcesList(List<Resources> resources) {
		if (resources == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Resources.class.getCanonicalName() + ">.");
		}
		List<Resources> previous = getResourcesList();
		_resourcesList = resources;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.ADD)
	public void addResources(Resources resources) {
		getResourcesList().add(resources);
	}

	public int getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int iMaxPar) throws SshException {
		if (iMaxPar < 1 || iMaxPar > 10) {
			throw new SshException(Msg.bind(
					Messages.UploadEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		_maxPar = iMaxPar;
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