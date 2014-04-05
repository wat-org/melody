package com.wat.cloud.aws.ec2;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.IcmpCode;
import com.wat.melody.common.firewall.IcmpType;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.Protocol;
import com.wat.melody.common.firewall.SimpleAbstractTcpUdpFireWallwRule;
import com.wat.melody.common.firewall.SimpleFireWallRule;
import com.wat.melody.common.firewall.SimpleIcmpFireWallRule;
import com.wat.melody.common.firewall.SimpleTcpFireWallRule;
import com.wat.melody.common.firewall.SimpleUdpFireWallRule;
import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.firewall.exception.IllegalProtocolException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.exception.IllegalIpRangeException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AwsEc2CloudFireWall {

	private static Logger log = LoggerFactory
			.getLogger(AwsEc2CloudFireWall.class);

	public static FireWallRules getFireWallRules(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev) {
		String sgid = AwsEc2CloudProtectedArea.getProtectedAreaId(ec2, i,
				netdev).getValue();
		List<IpPermission> perms = describeSecurityGroupRules(ec2, sgid);
		return convertIpPermissions(perms);
	}

	public static FireWallRules getFireWallRules(AmazonEC2 ec2,
			ProtectedAreaId paId) {
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		List<IpPermission> perms = describeSecurityGroupRules(ec2,
				paId.getValue());
		return convertIpPermissions(perms);
	}

	/**
	 * <p>
	 * Get the {@link IpPermission}s associated to the AWS Security Group which
	 * match the given identifier.
	 * </p>
	 * 
	 * @param ec2
	 * @param sSGId
	 *            is the identifier of the AWS Security Group to examine.
	 * 
	 * @return the {@link IpPermission}s associated to the AWS Security Group
	 *         which match the given identifier if such AWS Security Group
	 *         exists and if {@link IpPermission}s are associated to it, or
	 *         <tt>null</tt> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given identifier is <tt>null</tt>.
	 */
	protected static List<IpPermission> describeSecurityGroupRules(
			AmazonEC2 ec2, String sSGId) {
		SecurityGroup sg = AwsEc2CloudNetwork.getSecurityGroupById(ec2, sSGId);
		if (sg == null) {
			return null;
		} else {
			return sg.getIpPermissions();
		}
	}

	protected static FireWallRules convertIpPermissions(List<IpPermission> perms) {
		FireWallRules rules = new FireWallRules();
		if (perms == null) {
			return rules;
		}
		try {
			for (IpPermission perm : perms) {
				SimpleFireWallRule rule = null;
				Protocol proto = Protocol.parseString(perm.getIpProtocol());
				switch (proto) {
				case TCP:
					for (String ipRange : perm.getIpRanges()) {
						rule = new SimpleTcpFireWallRule(
								IpRange.parseString(ipRange), PortRange.ALL,
								IpRange.ALL,
								PortRange.parseString(perm.getFromPort() + "-"
										+ perm.getToPort()), Direction.IN,
								Access.ALLOW);
						rules.add(rule);
					}
					for (UserIdGroupPair ug : perm.getUserIdGroupPairs()) {
						rule = new SimpleTcpFireWallRule(
								ProtectedAreaId.parseString(ug.getGroupId()),
								PortRange.ALL, IpRange.ALL,
								PortRange.parseString(perm.getFromPort() + "-"
										+ perm.getToPort()), Direction.IN,
								Access.ALLOW);
						rules.add(rule);
					}
					break;
				case UDP:
					for (String ipRange : perm.getIpRanges()) {
						rule = new SimpleUdpFireWallRule(
								IpRange.parseString(ipRange), PortRange.ALL,
								IpRange.ALL,
								PortRange.parseString(perm.getFromPort() + "-"
										+ perm.getToPort()), Direction.IN,
								Access.ALLOW);
						rules.add(rule);
					}
					for (UserIdGroupPair ug : perm.getUserIdGroupPairs()) {
						rule = new SimpleUdpFireWallRule(
								ProtectedAreaId.parseString(ug.getGroupId()),
								PortRange.ALL, IpRange.ALL,
								PortRange.parseString(perm.getFromPort() + "-"
										+ perm.getToPort()), Direction.IN,
								Access.ALLOW);
						rules.add(rule);
					}
					break;
				case ICMP:
					for (String ipRange : perm.getIpRanges()) {
						rule = new SimpleIcmpFireWallRule(
								IpRange.parseString(ipRange), IpRange.ALL,
								IcmpType.parseInt(perm.getFromPort()),
								IcmpCode.parseInt(perm.getToPort()),
								Direction.IN, Access.ALLOW);
						rules.add(rule);
					}
					for (UserIdGroupPair ug : perm.getUserIdGroupPairs()) {
						rule = new SimpleIcmpFireWallRule(
								ProtectedAreaId.parseString(ug.getGroupId()),
								IpRange.ALL, IcmpType.parseInt(perm
										.getFromPort()), IcmpCode.parseInt(perm
										.getToPort()), Direction.IN,
								Access.ALLOW);
						rules.add(rule);
					}
					break;
				}
			}
		} catch (IllegalProtocolException | IllegalIpRangeException
				| IllegalPortRangeException | IllegalIcmpTypeException
				| IllegalIcmpCodeException | IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException(Ex);
		}
		return rules;
	}

	public static void revokeFireWallRules(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev, FireWallRules toRevoke) {
		if (toRevoke == null || toRevoke.size() == 0) {
			return;
		}
		String sgid = AwsEc2CloudProtectedArea.getProtectedAreaId(ec2, i,
				netdev).getValue();
		List<IpPermission> toRev = convertFwRules(toRevoke);
		// the conversion may have discard all rules
		if (toRev.size() == 0) {
			return;
		}
		RevokeSecurityGroupIngressRequest revreq = null;
		revreq = new RevokeSecurityGroupIngressRequest();
		revreq = revreq.withGroupId(sgid).withIpPermissions(toRev);
		ec2.revokeSecurityGroupIngress(revreq);
		for (SimpleFireWallRule rule : toRevoke) {
			log.info(Msg.bind(Messages.CommonMsg_REVOKE_FWRULE, i.getImageId(),
					netdev, rule));
		}
	}

	public static void revokeFireWallRules(AmazonEC2 ec2, ProtectedAreaId paId,
			FireWallRules toRevoke) {
		if (toRevoke == null || toRevoke.size() == 0) {
			return;
		}
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		String sgid = paId.getValue();
		List<IpPermission> toRev = convertFwRules(toRevoke);
		// the conversion may have discard all rules
		if (toRev.size() == 0) {
			return;
		}
		RevokeSecurityGroupIngressRequest revreq = null;
		revreq = new RevokeSecurityGroupIngressRequest();
		revreq = revreq.withGroupId(sgid).withIpPermissions(toRev);
		ec2.revokeSecurityGroupIngress(revreq);
		for (SimpleFireWallRule rule : toRevoke) {
			log.info(Msg.bind(Messages.CommonMsg_PA_REVOKE_FWRULE, sgid, rule));
		}
	}

	public static void authorizeFireWallRules(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev, FireWallRules toAuthorize) {
		if (toAuthorize == null || toAuthorize.size() == 0) {
			return;
		}
		String sgid = AwsEc2CloudProtectedArea.getProtectedAreaId(ec2, i,
				netdev).getValue();
		List<IpPermission> toAuth = convertFwRules(toAuthorize);
		// the conversion may have discard all rules
		if (toAuth.size() == 0) {
			return;
		}
		AuthorizeSecurityGroupIngressRequest authreq = null;
		authreq = new AuthorizeSecurityGroupIngressRequest();
		authreq = authreq.withGroupId(sgid).withIpPermissions(toAuth);
		ec2.authorizeSecurityGroupIngress(authreq);
		for (SimpleFireWallRule rule : toAuthorize) {
			log.info(Msg.bind(Messages.CommonMsg_AUTHORIZE_FWRULE,
					i.getImageId(), netdev, rule));
		}
	}

	public static void authorizeFireWallRules(AmazonEC2 ec2,
			ProtectedAreaId paId, FireWallRules toAuthorize) {
		if (toAuthorize == null || toAuthorize.size() == 0) {
			return;
		}
		if (paId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ProtectedAreaId.class.getCanonicalName() + ".");
		}
		String sgid = paId.getValue();
		List<IpPermission> toAuth = convertFwRules(toAuthorize);
		// the conversion may have discard all rules
		if (toAuth.size() == 0) {
			return;
		}
		AuthorizeSecurityGroupIngressRequest authreq = null;
		authreq = new AuthorizeSecurityGroupIngressRequest();
		authreq = authreq.withGroupId(sgid).withIpPermissions(toAuth);
		ec2.authorizeSecurityGroupIngress(authreq);
		for (SimpleFireWallRule rule : toAuthorize) {
			log.info(Msg.bind(Messages.CommonMsg_PA_AUTHORIZE_FWRULE, sgid,
					rule));
		}
	}

	private static List<IpPermission> convertFwRules(FireWallRules rules) {
		List<IpPermission> perms = new ArrayList<IpPermission>();
		for (SimpleFireWallRule rule : rules) {
			IpPermission perm = null;
			switch (rule.getProtocol()) {
			case TCP:
				perm = createTcpUdpPermission((SimpleTcpFireWallRule) rule);
				break;
			case UDP:
				perm = createTcpUdpPermission((SimpleUdpFireWallRule) rule);
				break;
			case ICMP:
				perm = createIcmpPermission((SimpleIcmpFireWallRule) rule);
				break;
			}
			if (perm == null) {
				continue;
			}
			perms.add(perm);
		}
		return perms;
	}

	private static IpPermission createTcpUdpPermission(
			SimpleAbstractTcpUdpFireWallwRule rule) {
		if (rule.getDirection().equals(Direction.OUT)) {
			log.info(Msg.bind(Messages.CommonMsg_SKIP_FWRULE, rule,
					Direction.OUT));
			return null;
		}
		if (rule.getAccess().equals(Access.DENY)) {
			log.info(Msg
					.bind(Messages.CommonMsg_SKIP_FWRULE, rule, Access.DENY));
			return null;
		}
		IpPermission perm = new IpPermission();
		perm.withIpProtocol(rule.getProtocol().getValue());
		if (rule.getFromAddress() instanceof IpRange) {
			perm.withIpRanges(rule.getFromAddress().getAddressAsString());
		} else if (rule.getFromAddress() instanceof ProtectedAreaId) {
			UserIdGroupPair ug = new UserIdGroupPair();
			ug.withGroupId(rule.getFromAddress().getAddressAsString());
			perm.withUserIdGroupPairs(ug);
		} else {
			throw new RuntimeException("not suported");
		}
		perm.withFromPort(rule.getToPortRange().getStartPort().getValue());
		perm.withToPort(rule.getToPortRange().getEndPort().getValue());
		return perm;
	}

	private static IpPermission createIcmpPermission(SimpleIcmpFireWallRule rule) {
		if (rule.getDirection().equals(Direction.OUT)) {
			log.info(Msg.bind(Messages.CommonMsg_SKIP_FWRULE, rule,
					Direction.OUT));
			return null;
		}
		if (rule.getAccess().equals(Access.DENY)) {
			log.info(Msg
					.bind(Messages.CommonMsg_SKIP_FWRULE, rule, Access.DENY));
			return null;
		}
		IpPermission perm = new IpPermission();
		perm.withIpProtocol(rule.getProtocol().getValue());
		if (rule.getFromAddress() instanceof IpRange) {
			perm.withIpRanges(rule.getFromAddress().getAddressAsString());
		} else if (rule.getFromAddress() instanceof ProtectedAreaId) {
			UserIdGroupPair ug = new UserIdGroupPair();
			ug.withGroupId(rule.getFromAddress().getAddressAsString());
			perm.withUserIdGroupPairs(ug);
		} else {
			throw new RuntimeException("not suported");
		}
		perm.withFromPort(rule.getType().getValue());
		perm.withToPort(rule.getCode().getValue());
		return perm;
	}

}