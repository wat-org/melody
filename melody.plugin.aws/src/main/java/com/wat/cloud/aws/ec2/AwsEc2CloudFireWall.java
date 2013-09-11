package com.wat.cloud.aws.ec2;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
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
		String sgname = AwsEc2CloudNetwork.getSecurityGroup(ec2, i, netdev);
		List<IpPermission> perms = describeSecurityGroupRules(ec2, sgname);
		return convertIpPermissions(perms, netdev);
	}

	/**
	 * <p>
	 * Get the {@link IpPermission}s associated to the AWS Security Group which
	 * match the given name.
	 * </p>
	 * 
	 * @param ec2
	 * @param sSGName
	 *            is the name of the AWS Security Group to examine.
	 * 
	 * @return the {@link IpPermission}s associated to the AWS Security Group
	 *         which match the given name if such AWS Security Group exists and
	 *         if {@link IpPermission}s are associated to it, or
	 *         <code>null</code> otherwise.
	 * 
	 * @throws AmazonServiceException
	 *             if the creation failed.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws IllegalArgumentException
	 *             if ec2 is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if sSGName is <code>null</code> or an empty
	 *             <code>String</code>.
	 */
	private static List<IpPermission> describeSecurityGroupRules(AmazonEC2 ec2,
			String sSGName) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (sSGName == null || sSGName.trim().length() == 0) {
			throw new IllegalArgumentException(sSGName + ": Not accepted. "
					+ "Must be a String (an AWS Security Group name).");
		}

		DescribeSecurityGroupsRequest dsgreq = null;
		dsgreq = new DescribeSecurityGroupsRequest();
		dsgreq.withGroupNames(sSGName);

		try {
			return ec2.describeSecurityGroups(dsgreq).getSecurityGroups()
					.get(0).getIpPermissions();
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.NotFound") != -1) {
				return null;
			} else {
				throw Ex;
			}
		} catch (NullPointerException | IndexOutOfBoundsException Ex) {
			return null;
		}
	}

	private static FireWallRules convertIpPermissions(List<IpPermission> perms,
			NetworkDeviceName netdev) {
		FireWallRules rules = new FireWallRules();
		try {
			for (IpPermission perm : perms) {
				SimpleFireWallRule rule = null;
				Protocol proto = Protocol.parseString(perm.getIpProtocol());
				switch (proto) {
				case TCP:
					rule = new SimpleTcpFireWallRule(IpRange.parseString(perm
							.getIpRanges().get(0)), PortRange.ALL, IpRange.ALL,
							PortRange.parseString(perm.getFromPort() + "-"
									+ perm.getToPort()), Direction.IN,
							Access.ALLOW);
					break;
				case UDP:
					rule = new SimpleUdpFireWallRule(IpRange.parseString(perm
							.getIpRanges().get(0)), PortRange.ALL, IpRange.ALL,
							PortRange.parseString(perm.getFromPort() + "-"
									+ perm.getToPort()), Direction.IN,
							Access.ALLOW);
					break;
				case ICMP:
					rule = new SimpleIcmpFireWallRule(IpRange.parseString(perm
							.getIpRanges().get(0)), IpRange.ALL,
							IcmpType.parseInt(perm.getFromPort()),
							IcmpCode.parseInt(perm.getToPort()), Direction.IN,
							Access.ALLOW);
					break;
				}
				rules.add(rule);
			}
		} catch (IllegalProtocolException | IllegalIpRangeException
				| IllegalPortRangeException | IllegalIcmpTypeException
				| IllegalIcmpCodeException Ex) {
			throw new RuntimeException(Ex);
		}
		return rules;
	}

	public static void revokeFireWallRules(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev, FireWallRules toRevoke) {
		if (toRevoke == null || toRevoke.size() == 0) {
			return;
		}
		String sgname = AwsEc2CloudNetwork.getSecurityGroup(ec2, i, netdev);
		List<IpPermission> toRev = convertFwRules(toRevoke);
		RevokeSecurityGroupIngressRequest revreq = null;
		revreq = new RevokeSecurityGroupIngressRequest();
		revreq = revreq.withGroupName(sgname).withIpPermissions(toRev);
		ec2.revokeSecurityGroupIngress(revreq);
		for (SimpleFireWallRule rule : toRevoke) {
			log.info(Msg.bind(Messages.CommonMsg_REVOKE_FWRULE, i.getImageId(),
					netdev, rule));
		}
	}

	public static void authorizeFireWallRules(AmazonEC2 ec2, Instance i,
			NetworkDeviceName netdev, FireWallRules toAuthorize) {
		if (toAuthorize == null || toAuthorize.size() == 0) {
			return;
		}
		String sgname = AwsEc2CloudNetwork.getSecurityGroup(ec2, i, netdev);
		List<IpPermission> toAuth = convertFwRules(toAuthorize);
		AuthorizeSecurityGroupIngressRequest authreq = null;
		authreq = new AuthorizeSecurityGroupIngressRequest();
		authreq = authreq.withGroupName(sgname).withIpPermissions(toAuth);
		ec2.authorizeSecurityGroupIngress(authreq);
		for (SimpleFireWallRule rule : toAuthorize) {
			log.info(Msg.bind(Messages.CommonMsg_AUTHORIZE_FWRULE,
					i.getImageId(), netdev, rule));
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
		perm.withIpRanges(rule.getFromIpRange().getValue());
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
		perm.withIpRanges(rule.getFromIpRange().getValue());
		perm.withFromPort(rule.getType().getValue());
		perm.withToPort(rule.getCode().getValue());
		return perm;
	}

}