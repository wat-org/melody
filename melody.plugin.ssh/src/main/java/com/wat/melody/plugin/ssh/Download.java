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

	private List<ResourceMatcher> _resourceMatcherList;
	private int _maxPar;

	public Download() {
		super();
		setResourceMatcherList(new ArrayList<ResourceMatcher>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (ResourceMatcher rm : getResourceMatcherList()) {
			validateResourceElement(rm, RESOURCES_NE);
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
			session.download(getResourceMatcherList(), getMaxPar());
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	public List<ResourceMatcher> getResourceMatcherList() {
		return _resourceMatcherList;
	}

	public List<ResourceMatcher> setResourceMatcherList(
			List<ResourceMatcher> resources) {
		if (resources == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourceMatcher.class.getCanonicalName() + ">.");
		}
		List<ResourceMatcher> previous = getResourceMatcherList();
		_resourceMatcherList = resources;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.ADD)
	public void addResources(ResourceMatcher resourceMatcher) {
		getResourceMatcherList().add(resourceMatcher);
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