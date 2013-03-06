package com.wat.melody.plugin.libvirt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.cloud.libvirt.LibVirtInstance;
import com.wat.melody.api.ITask;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.firewall.FireWallRulesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.plugin.libvirt.common.AbstractLibVirtOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IngressMachine extends AbstractLibVirtOperation implements ITask {

	private static Log log = LogFactory.getLog(IngressMachine.class);

	/**
	 * The 'IngressMachine' XML element
	 */
	public static final String INGRESS_MACHINE = "IngressMachine";

	private FwRulesDecomposed maFwRules;

	public IngressMachine() {
		initFwRules();
	}

	private void initFwRules() {
		maFwRules = null;
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			setFwRules(new FireWallRulesLoader(getContext()).load(
					getTargetNode()).decompose());
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		LibVirtInstance i = getInstance();
		if (i == null) {
			LibVirtException Ex = new LibVirtException(Messages.bind(
					Messages.IngressMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new LibVirtException(
					Messages.IngressMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		try {
			i.updateFireWallRules(getFwRules());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.IngressEx_GENERIC_FAIL, getTargetNodeLocation()),
					Ex);
		}
	}

	private FwRulesDecomposed getFwRules() {
		return maFwRules;
	}

	private FwRulesDecomposed setFwRules(FwRulesDecomposed fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid FwRules.");
		}
		FwRulesDecomposed previous = getFwRules();
		maFwRules = fwrs;
		return previous;
	}

}
