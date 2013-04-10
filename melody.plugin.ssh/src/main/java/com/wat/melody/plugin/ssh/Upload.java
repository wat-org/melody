package com.wat.melody.plugin.ssh;

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
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.types.ResourceMatcher;
import com.wat.melody.common.ssh.types.Resources;
import com.wat.melody.common.ssh.types.SimpleResource;
import com.wat.melody.common.ssh.types.exception.ResourceException;
import com.wat.melody.plugin.ssh.common.AbstractSshConnectionManagedOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Upload extends AbstractSshConnectionManagedOperation implements
		TemplatingHandler {

	/**
	 * The 'scp' XML element used in the Sequence Descriptor
	 */
	public static final String UPLOAD = "upload";

	/**
	 * The 'maxpar' XML Attribute
	 */
	public static final String MAXPAR_ATTR = "maxpar";

	/**
	 * The 'resources' XML Nested Element
	 */
	public static final String RESOURCES_NE = "resources";

	private List<Resources> maResourcesList;
	private int miMaxPar;

	private List<SimpleResource> maSimpleResourcesList;
	private ITaskContext moTaskContext;

	public Upload() {
		super();
		setResourcesList(new ArrayList<Resources>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
		setSimpleResourcesList(new ArrayList<SimpleResource>());
		setContext(Melody.getContext());
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (Resources resources : getResourcesList()) {
			try {
				// validate each inner include/exclude ResourceMatcher
				validateResourceElementLsit(resources.getIncludes(),
						Resources.INCLUDE_NE);
				validateResourceElementLsit(resources.getExcludes(),
						Resources.EXCLUDE_NE);
				// validate itself, if it has no include ResourceMatcher
				if (resources.getIncludes().size() == 0) {
					validateResourceElement(resources, RESOURCES_NE);
				}
				// Add all found SimpleResource to the global list
				List<SimpleResource> ar = resources.findResources();
				getSimpleResourcesList().removeAll(ar); // remove duplicated
				getSimpleResourcesList().addAll(ar);
			} catch (IOException Ex) {
				throw new RuntimeException("IO Error while finding files.", Ex);
			} catch (SshException Ex) {
				throw new SshException(Messages.bind(
						Messages.UploadEx_INVALID_NE, RESOURCES_NE), Ex);
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
			File localBaseDir = getContext().getProcessorManager()
					.getSequenceDescriptor().getBaseDir();
			try {
				r.setLocalBaseDir(localBaseDir);
			} catch (ResourceException Ex) {
				throw new RuntimeException("Unexpected error occurred while "
						+ "setting the localBaseDir to '" + localBaseDir
						+ "'. "
						+ "Source code has certainly been modified and "
						+ "a bug have been introduced. "
						+ "Or an external event made the file no more "
						+ "accessible (deleted, moved, read permission "
						+ "removed, ...).", Ex);
			}
		}
		if (r.getMatch() == null) {
			throw new SshException(
					Messages.bind(Messages.UploadEx_MISSING_ATTR,
							Resources.MATCH_ATTR, which));
		}
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			session.upload(getSimpleResourcesList(), getMaxPar(), this);
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
			throw new TemplatingException(Messages.bind(
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
			throw new TemplatingException(Messages.bind(
					Messages.SshEx_WRITE_IO_ERROR, template), Ex);
		}
		return template;
	}

	public List<Resources> getResourcesList() {
		return maResourcesList;
	}

	public List<Resources> setResourcesList(List<Resources> resources) {
		if (resources == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Resources>.");
		}
		List<Resources> previous = getResourcesList();
		maResourcesList = resources;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.ADD)
	public void addResources(Resources resources) {
		getResourcesList().add(resources);
	}

	public int getMaxPar() {
		return miMaxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int iMaxPar) throws SshException {
		if (iMaxPar < 1 || iMaxPar > 10) {
			throw new SshException(Messages.bind(
					Messages.UploadEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		miMaxPar = iMaxPar;
		return previous;
	}

	protected List<SimpleResource> getSimpleResourcesList() {
		return maSimpleResourcesList;
	}

	private List<SimpleResource> setSimpleResourcesList(List<SimpleResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<SimpleResource>.");
		}
		List<SimpleResource> previous = getSimpleResourcesList();
		maSimpleResourcesList = aft;
		return previous;
	}

	private ITaskContext getContext() {
		return moTaskContext;
	}

	private ITaskContext setContext(ITaskContext tc) {
		ITaskContext previous = getContext();
		moTaskContext = tc;
		return previous;
	}

}