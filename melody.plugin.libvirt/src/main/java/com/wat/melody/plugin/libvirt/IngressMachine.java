package com.wat.melody.plugin.libvirt;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.Instance;
import com.wat.melody.api.ITask;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.firewall.FireWallManagementHelper;
import com.wat.melody.cloud.firewall.FireWallRulesHelper;
import com.wat.melody.cloud.firewall.FwRuleLoader;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.network.FwRuleDecomposed;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.common.network.Interface;
import com.wat.melody.common.network.exception.IllegalInterfaceException;
import com.wat.melody.plugin.libvirt.common.AbstractLibVirtOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.xpath.XPathHelper;

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

		// Find FwRule Nodes Selector in the RD
		String fwRulesSelector = FireWallManagementHelper
				.findFireWallRulesSelector(getTargetNode());

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			NodeList nl = XPathHelper.getHeritedContent(getTargetNode(),
					fwRulesSelector);
			FwRuleLoader fwl = new FwRuleLoader(getContext());
			setFwRules(fwl.load(nl).decompose());
		} catch (XPathExpressionException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.IngressEx_INVALID_DISK_DEVICES_SELECTOR,
					fwRulesSelector), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
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
		}

		NetworkDeviceNameList netdevs = getInstanceNetworkDevices(i);
		for (NetworkDeviceName netdev : netdevs) {
			FwRulesDecomposed currentrules = getInstanceFireWallRules(i, netdev);
			FwRulesDecomposed newrules = getFwRules(netdev);
			FwRulesDecomposed toAdd = FireWallRulesHelper
					.computeFireWallRulesToAdd(currentrules, newrules);
			FwRulesDecomposed toRemove = FireWallRulesHelper
					.computeFireWallRulesToRemove(currentrules, newrules);

			log.info(Messages.bind(Messages.IngressMsg_FWRULES_RESUME,
					new Object[] { getInstanceID(), netdev, getFwRules(),
							toAdd, toRemove, getTargetNodeLocation() }));

			revokeFireWallRules(i, netdev, toRemove);
			authorizeFireWallRules(i, netdev, toAdd);
		}

		setInstanceRelatedInfosToED(i);
	}

	private FwRulesDecomposed getFwRules(NetworkDeviceName netdev) {
		Interface inter = null;
		try {
			inter = Interface.parseString(netdev.getValue());
		} catch (IllegalInterfaceException Ex) {
			throw new RuntimeException(Ex);
		}
		FwRulesDecomposed rules = new FwRulesDecomposed();
		for (FwRuleDecomposed rule : getFwRules()) {
			if (rule.getInterface().getValue().equals(netdev.getValue())) {
				rules.add(rule);
			} else if (rule.getInterface().equals(Interface.ALL)) {
				FwRuleDecomposed r = new FwRuleDecomposed();
				r.setInterface(inter);
				r.setFromIpRange(rule.getFromIpRange());
				r.setFromPortRange(rule.getFromPortRange());
				r.setToIpRange(rule.getToIpRange());
				r.setToPortRange(rule.getToPortRange());
				r.setProtocol(rule.getProtocol());
				r.setDirection(rule.getDirection());
				r.setAccess(rule.getAccess());
				rules.add(r);
			}
		}
		return rules;
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
