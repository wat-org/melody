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
import com.wat.melody.common.ssh.impl.filefinder.RemoteResourcesSelector;
import com.wat.melody.common.ssh.types.filesfinder.ResourcesSelector;
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

	private List<ResourcesSelector> _resourcesSelectors;
	private int _maxPar;

	public Download() {
		super();
		setResourcesSelectors(new ArrayList<ResourcesSelector>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (ResourcesSelector r : getResourcesSelectors()) {
			if (r.getLocalBaseDir() == null) {
				r.setLocalBaseDir(Melody.getContext().getProcessorManager()
						.getSequenceDescriptor().getBaseDir());
			}
		}
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			session.download(getResourcesSelectors(), getMaxPar());
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	public List<ResourcesSelector> getResourcesSelectors() {
		return _resourcesSelectors;
	}

	public List<ResourcesSelector> setResourcesSelectors(
			List<ResourcesSelector> rslist) {
		if (rslist == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourcesSelector.class.getCanonicalName() + ">.");
		}
		List<ResourcesSelector> previous = getResourcesSelectors();
		_resourcesSelectors = rslist;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.ADD)
	public void addResourcesSelector(RemoteResourcesSelector rs) {
		getResourcesSelectors().add(rs);
	}

	public int getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int maxPar) throws SshException {
		if (maxPar < 1 || maxPar > 10) {
			throw new SshException(Msg.bind(
					Messages.DownloadEx_INVALID_MAXPAR_ATTR, maxPar));
		}
		int previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

}