package com.wat.cloud.libvirt;

import org.libvirt.Connect;

import com.wat.cloud.libvirt.exception.ProtectedAreaStillInUseException;
import com.wat.melody.cloud.protectedarea.DefaultProtectedAreaController;
import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
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
public class LibVirtProtectedAreaController extends
		DefaultProtectedAreaController {

	private Connect _cnx;
	private FireWallRules _fireWallRules;

	public LibVirtProtectedAreaController(Connect connection,
			ProtectedAreaId protectedAreaId) {
		setConnection(connection);
		setProtectedAreaId(protectedAreaId);
	}

	@Override
	public boolean protectedAreaExists() {
		if (getProtectedAreaId() == null) {
			return false;
		} else {
			return LibVirtCloudProtectedArea.protectedAreaExists(
					getConnection(), getProtectedAreaId());
		}
	}

	@Override
	public ProtectedAreaId createProtectedArea(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException {
		return LibVirtCloudProtectedArea.createProtectedArea(getConnection(),
				name, description);
	}

	@Override
	public void destroyProtectedArea() throws ProtectedAreaException,
			InterruptedException {
		try {
			LibVirtCloudProtectedArea.destroyProtectedArea(getConnection(),
					getProtectedAreaId());
		} catch (ProtectedAreaStillInUseException Ex) {
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
		LibVirtCloudProtectedArea.authorizeFireWallRules(getConnection(),
				getProtectedAreaId(), toAuthorize);
		// update firewall rules, without libvirt call
		getFireWallRules().addAll(toAuthorize);
	}

	@Override
	public void revokeProtectedAreaFireWallRules(FireWallRules toRevoke)
			throws ProtectedAreaException, InterruptedException {
		LibVirtCloudProtectedArea.revokeFireWallRules(getConnection(),
				getProtectedAreaId(), toRevoke);
		// update firewall rules, without libvirt call
		getFireWallRules().removeAll(toRevoke);
	}

	public void refreshInternalDatas() {
		// put firewall rules in cache
		if (getProtectedAreaId() == null) {
			setFireWallRules(null);
		} else {
			setFireWallRules(LibVirtCloudProtectedArea.getFireWallRules(
					getConnection(), getProtectedAreaId()));
		}
	}

	public Connect getConnection() {
		return _cnx;
	}

	private Connect setConnection(Connect connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getConnection();
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