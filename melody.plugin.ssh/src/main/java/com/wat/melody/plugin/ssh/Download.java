package com.wat.melody.plugin.ssh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.ISshSession;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.RemoteResourcesSpecification;
import com.wat.melody.common.ssh.filesfinder.ResourcesSpecification;
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

	private List<ResourcesSpecification> _resourcesSpecifications;
	private int _maxPar;

	public Download() {
		super();
		setResourcesSpecifications(new ArrayList<ResourcesSpecification>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
	}

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		ISshSession session = null;
		try {
			session = openSession();
			session.download(getResourcesSpecifications(), getMaxPar());
		} catch (SshSessionException Ex) {
			throw new SshException(Ex);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
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
		ResourcesSpecification rrs = new RemoteResourcesSpecification(basedir);
		getResourcesSpecifications().add(rrs);
		return rrs;
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