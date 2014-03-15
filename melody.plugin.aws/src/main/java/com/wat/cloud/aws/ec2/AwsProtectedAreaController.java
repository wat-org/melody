package com.wat.cloud.aws.ec2;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.wat.melody.cloud.protectedarea.DefaultProtectedAreaController;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.ex.HiddenException;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsProtectedAreaController extends DefaultProtectedAreaController {

	private AmazonEC2 _cnx;
	private SecurityGroup _securityGroup;

	public AwsProtectedAreaController(AmazonEC2 connection,
			String securityGroupId) {
		setConnection(connection);
		setProtectedAreaId(securityGroupId);
	}

	@Override
	public boolean protectedAreaExists() {
		return AwsEc2CloudNetwork.getSecurityGroupById(getConnection(),
				getProtectedAreaId()) != null;
	}

	@Override
	public String createProtectedArea(ProtectedAreaName name, String description)
			throws ProtectedAreaException, InterruptedException {
		// In AWS, a protected area is a security group
		return AwsEc2CloudNetwork.createSecurityGroup(getConnection(),
				name.getValue(), description);
	}

	@Override
	public void destroyProtectedArea() throws ProtectedAreaException,
			InterruptedException {
		try {
			AwsEc2CloudNetwork.deleteSecurityGroup(getConnection(),
					getProtectedAreaId());
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode() == null) {
				throw Ex;
			} else if (Ex.getErrorCode().indexOf("InvalidGroup.InUse") != -1) {
				throw new ProtectedAreaException(
						Msg.bind(Messages.PADestroyEx_STILL_IN_USE,
								getProtectedAreaId()), new HiddenException(Ex));
			} else {
				throw Ex;
			}
		}
	}

	@Override
	public FireWallRules getProtectedAreaFireWallRules() {
		return AwsEc2CloudFireWall.getFireWallRules(getConnection(),
				getSecurityGroup());
	}

	@Override
	public void authorizeProtectedAreaFireWallRules(FireWallRules toAuthorize)
			throws ProtectedAreaException, InterruptedException {
		AwsEc2CloudFireWall.authorizeFireWallRules(getConnection(),
				getSecurityGroup(), toAuthorize);
	}

	@Override
	public void revokeProtectedAreaFireWallRules(FireWallRules toRevoke)
			throws ProtectedAreaException, InterruptedException {
		AwsEc2CloudFireWall.revokeFireWallRules(getConnection(),
				getSecurityGroup(), toRevoke);
	}

	public void refreshInternalDatas() {
		setSecurityGroup(AwsEc2CloudNetwork.getSecurityGroupById(
				getConnection(), getProtectedAreaId()));
	}

	public AmazonEC2 getConnection() {
		return _cnx;
	}

	private AmazonEC2 setConnection(AmazonEC2 connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		AmazonEC2 previous = getConnection();
		_cnx = connection;
		return previous;
	}

	private SecurityGroup getSecurityGroup() {
		return _securityGroup;
	}

	/**
	 * @param securityGroup
	 *            is the {@link SecurityGroup} to associate to this object.
	 * 
	 * @return the underlying {@link SecurityGroup} this object managed. Can be
	 *         <tt>null</tt>, when the AWS Security Group is not created.
	 */
	private SecurityGroup setSecurityGroup(SecurityGroup securityGroup) {
		SecurityGroup previous = getSecurityGroup();
		_securityGroup = securityGroup;
		return previous;
	}

	@Override
	public String setProtectedAreaId(String protectedAreaId) {
		String previous = super.setProtectedAreaId(protectedAreaId);
		refreshInternalDatas();
		return previous;
	}

}