package com.wat.cloud.aws.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.wat.cloud.aws.ec2.exception.SecurityGroupInUseException;
import com.wat.melody.cloud.protectedarea.DefaultProtectedAreaController;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.cloud.protectedarea.ProtectedAreaName;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
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
			ProtectedAreaId protectedAreaId) {
		setConnection(connection);
		setProtectedAreaId(protectedAreaId);
	}

	@Override
	public boolean protectedAreaExists() {
		if (getProtectedAreaId() == null) {
			return false;
		} else {
			return AwsEc2CloudNetwork.getSecurityGroupById(getConnection(),
					getProtectedAreaId().getValue()) != null;
		}
	}

	@Override
	public ProtectedAreaId createProtectedArea(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException {
		// In AWS, a protected area is a security group
		String sgId = AwsEc2CloudNetwork.createSecurityGroup(getConnection(),
				name.getValue(), description);
		try {
			return ProtectedAreaId.parseString(sgId);
		} catch (IllegalProtectedAreaIdException Ex) {
			throw new RuntimeException("Fail to convert '" + sgId + "' into '"
					+ ProtectedAreaId.class.getCanonicalName() + "'. "
					+ "If this error happened, you should modify the "
					+ "conversion rule.", Ex);
		}
	}

	@Override
	public void destroyProtectedArea() throws ProtectedAreaException,
			InterruptedException {
		try {
			AwsEc2CloudNetwork.deleteSecurityGroup(getConnection(),
					getProtectedAreaId().getValue());
		} catch (SecurityGroupInUseException Ex) {
			throw new ProtectedAreaException(Msg.bind(
					Messages.PADestroyEx_STILL_IN_USE, getProtectedAreaId()),
					new HiddenException(Ex));
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
		if (getProtectedAreaId() == null) {
			setSecurityGroup(null);
		} else {
			setSecurityGroup(AwsEc2CloudNetwork.getSecurityGroupById(
					getConnection(), getProtectedAreaId().getValue()));
		}
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
	public ProtectedAreaId setProtectedAreaId(ProtectedAreaId protectedAreaId) {
		ProtectedAreaId previous = super.setProtectedAreaId(protectedAreaId);
		refreshInternalDatas();
		return previous;
	}

}