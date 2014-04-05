package com.wat.cloud.aws.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
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
	private FireWallRules _fireWallRules;

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
		// get cached firewall rules (without libvirt call)
		return getFireWallRules();
	}

	@Override
	public void authorizeProtectedAreaFireWallRules(FireWallRules toAuthorize)
			throws ProtectedAreaException, InterruptedException {
		AwsEc2CloudFireWall.authorizeFireWallRules(getConnection(),
				getProtectedAreaId(), toAuthorize);
		// update firewall rules, without libvirt call
		getFireWallRules().addAll(toAuthorize);
	}

	@Override
	public void revokeProtectedAreaFireWallRules(FireWallRules toRevoke)
			throws ProtectedAreaException, InterruptedException {
		AwsEc2CloudFireWall.revokeFireWallRules(getConnection(),
				getProtectedAreaId(), toRevoke);
		// update firewall rules, without libvirt call
		getFireWallRules().removeAll(toRevoke);
	}

	public void refreshInternalDatas() {
		// put firewall rules in cache
		if (getProtectedAreaId() == null) {
			setFireWallRules(null);
		} else {
			setFireWallRules(AwsEc2CloudFireWall.getFireWallRules(
					getConnection(), getProtectedAreaId()));
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

	private FireWallRules getFireWallRules() {
		return _fireWallRules;
	}

	/**
	 * @param fireWallRules
	 *            is the {@link FireWallRules} to associate to this object.
	 * 
	 * @return the underlying {@link FireWallRules} this object managed. Can be
	 *         <tt>null</tt>, when the Protected Area is not created.
	 */
	private FireWallRules setFireWallRules(FireWallRules fireWallRules) {
		FireWallRules previous = getFireWallRules();
		_fireWallRules = fireWallRules;
		return previous;
	}

	@Override
	public ProtectedAreaId setProtectedAreaId(ProtectedAreaId protectedAreaId) {
		ProtectedAreaId previous = super.setProtectedAreaId(protectedAreaId);
		refreshInternalDatas();
		return previous;
	}

}