package com.wat.melody.plugin.ssh;

import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.types.ResourceMatcher;
import com.wat.melody.common.ssh.types.Resources;
import com.wat.melody.plugin.ssh.common.AbstractSshManagedOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Download extends AbstractSshManagedOperation {

	/**
	 * Task's name
	 */
	public static final String DOWNLOAD = "download";

	/**
	 * Task's attribute, which specifies the maximum number of concurrent
	 * download.
	 */
	public static final String MAXPAR_ATTR = "max-par";

	/**
	 * Task's nested element, which specifies the resources to download.
	 */
	public static final String RESOURCES_NE = "resources";

	private List<Resources> _resourcesList;
	private int _maxPar;

	public Download() {
		super();
		setResourcesList(new ArrayList<Resources>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (Resources resources : getResourcesList()) {
			// validate each inner include/exclude
			validateResourceElementLsit(resources.getIncludes(),
					Resources.INCLUDE_NE);
			validateResourceElementLsit(resources.getExcludes(),
					Resources.EXCLUDE_NE);
			// validate itself
			validateResourceElement(resources, RESOURCES_NE);
		}
	}

	private void validateResourceElementLsit(
			List<ResourceMatcher> aResourceMatcher, String which) {
		for (ResourceMatcher r : aResourceMatcher) {
			validateResourceElement(r, which);
		}
	}

	private void validateResourceElement(ResourceMatcher r, String which) {
		if (r.getLocalBaseDir() == null) {
			r.setLocalBaseDir(Melody.getContext().getProcessorManager()
					.getSequenceDescriptor().getBaseDir());
		}
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			session.download(getResourcesList(), getMaxPar());
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
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
					Messages.DownloadEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		_maxPar = iMaxPar;
		return previous;
	}

}