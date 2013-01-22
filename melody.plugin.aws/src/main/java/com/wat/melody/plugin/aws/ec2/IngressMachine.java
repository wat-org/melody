package com.wat.melody.plugin.aws.ec2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.ex.Util;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.FwRuleDecomposed;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.plugin.aws.ec2.common.AbstractAwsOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.FwRuleLoader;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IngressMachine extends AbstractAwsOperation {

	private static Log log = LogFactory.getLog(IngressMachine.class);

	/**
	 * The 'IngressMachine' XML element
	 */
	public static final String INGRESS_MACHINE = "IngressMachine";

	/**
	 * The 'FwRulesXprSuffix' XML attribute
	 */
	public static final String FWRULES_XPR_SUFFIX_ATTR = "FwRulesXprSuffix";

	private String msFWRulesXprSuffix;
	private FwRulesDecomposed maFwRules;

	public IngressMachine() {
		super();
		initFWRulesXprSuffix();
		initFwRules();
	}

	private void initFWRulesXprSuffix() {
		msFWRulesXprSuffix = "//in//" + Common.FWRULE_NE;
	}

	private void initFwRules() {
		maFwRules = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			NodeList nl = XPathHelper.getHeritedContent(getTargetNode(),
					getFWRulesXprSuffix());
			FwRuleLoader fwl = new FwRuleLoader(getContext());
			setFwRules(fwl.load(nl).decompose().simplify());
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.IngressEx_INVALID_FWRULE_XPATH,
					getFWRulesXprSuffix()), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	public void doProcessing() throws InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.IngressMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new AwsException(
					Messages.IngressMsg_GENERIC_WARN, Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		String sgname = i.getSecurityGroups().get(0).getGroupName();
		List<IpPermission> ap = Common.describeSecurityGroupRules(getEc2(),
				sgname);
		List<IpPermission> toAuth = computeAuthorizeRemoveRules(ap);
		authorizeRules(sgname, toAuth);
		revokeRules(sgname, ap);
	}

	/**
	 * <p>
	 * Let only the Rules to revoke into the given list.
	 * </p>
	 * 
	 * @param ap
	 * 
	 * @return the Rules to auth
	 */
	private List<IpPermission> computeAuthorizeRemoveRules(List<IpPermission> ap) {
		List<IpPermission> toAdd = new ArrayList<IpPermission>();
		for (FwRuleDecomposed fwr : getFwRules()) {
			if (fwr.getAccess() == Access.DENY) {
				log.info(Messages.bind(Messages.IngressMsg_DENY_NA,
						new Object[] { fwr.toString(), INGRESS_MACHINE,
								Access.DENY, getTargetNodeLocation() }));
			} else if (!containsFwRule(ap, fwr)) {
				toAdd.add(createIpPermission(fwr));
			}
		}
		log.info(Messages.bind(Messages.IngressMsg_FWRULES_DIGEST,
				new Object[] { getAwsInstanceID(), getFwRules(), toAdd, ap,
						getTargetNodeLocation() }));
		return toAdd;
	}

	/**
	 * <p>
	 * Search the given {@link FwRuleDecomposed} in the given
	 * {@link IpPermission}'s {@link List}.<BR/>
	 * If the given {@link FwRuleDecomposed} is found, it is remove from the
	 * list.
	 * </p>
	 * 
	 * @param ap
	 *            is the {@link IpPermission}'s {@link List} to search in.
	 * @param fw
	 *            is the FwRule to search.
	 * 
	 * @return <code>true</code> if the given {@link FwRuleDecomposed} was
	 *         found, <code>false</code> otherwise.
	 */
	private boolean containsFwRule(List<IpPermission> ap, FwRuleDecomposed fw) {
		if (fw == null) {
			return false;
		}
		if (ap == null) {
			return false;
		}
		for (IpPermission sgr : ap) {
			for (String ip : sgr.getIpRanges()) {
				if (fw.equals(ip, IpRange.ALL, sgr.getFromPort(),
						sgr.getToPort(), sgr.getIpProtocol(),
						Access.ALLOW.toString())) {
					sgr.getIpRanges().remove(ip);
					if (sgr.getIpRanges().size() == 0) {
						ap.remove(sgr);
					}
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Converts the given {@link FwRuleDecomposed} to an {@link IpPermission}.
	 * </p>
	 * 
	 * @param fwr
	 *            is the {@link FwRuleDecomposed} to convert.
	 * 
	 * @return an {@link IpPermission}, which is equal to the given
	 *         {@link FwRuleDecomposed}.
	 * 
	 * @throws IllegalArgumentException
	 *             if fwr is <code>null</code>.
	 */
	private IpPermission createIpPermission(FwRuleDecomposed fwr) {
		if (fwr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ FwRuleDecomposed.class.getCanonicalName() + ".");
		}
		IpPermission sgr = new IpPermission();
		sgr.withIpProtocol(fwr.getProtocol().getValue().toLowerCase());
		sgr.withFromPort(fwr.getPortRange().getFromPort().getValue());
		sgr.withToPort(fwr.getPortRange().getToPort().getValue());
		sgr.withIpRanges(fwr.getFromIpRange().getValue());
		return sgr;
	}

	private void authorizeRules(String sgname, List<IpPermission> toAuth) {
		if (sgname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (toAuth == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ IpPermission.class.getCanonicalName() + ">" + ".");
		}
		if (toAuth.size() > 0) {
			AuthorizeSecurityGroupIngressRequest authreq = null;
			authreq = new AuthorizeSecurityGroupIngressRequest();
			authreq = authreq.withGroupName(sgname).withIpPermissions(toAuth);
			getEc2().authorizeSecurityGroupIngress(authreq);
		}
	}

	private void revokeRules(String sgname, List<IpPermission> toRev) {
		if (sgname == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (toRev == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ IpPermission.class.getCanonicalName() + ">" + ".");
		}
		if (toRev.size() > 0) {
			RevokeSecurityGroupIngressRequest revreq = null;
			revreq = new RevokeSecurityGroupIngressRequest();
			revreq = revreq.withGroupName(sgname).withIpPermissions(toRev);
			getEc2().revokeSecurityGroupIngress(revreq);
		}
	}

	public String getFWRulesXprSuffix() {
		return msFWRulesXprSuffix;
	}

	@Attribute(name = FWRULES_XPR_SUFFIX_ATTR)
	public String setFWRulesXprSuffix(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getFWRulesXprSuffix();
		msFWRulesXprSuffix = v;
		return previous;
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
