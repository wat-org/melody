package com.wat.melody.cloud.protectedarea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DefaultProtectedAreaController extends
		BaseProtectedAreaController {

	private static Logger log = LoggerFactory
			.getLogger(DefaultProtectedAreaController.class);

	private String _protectedAreaId;

	public DefaultProtectedAreaController() {
		super();
	}

	/**
	 * @return the protected area id. Can be <tt>null</tt>, when the protected
	 *         area is not created.
	 */
	@Override
	public String getProtectedAreaId() {
		return _protectedAreaId;
	}

	/**
	 * @param protectedAreaId
	 *            is the protected area id to assign. Can be <tt>null</tt>, when
	 *            the protected area is not created.
	 * 
	 * @return the previous value.
	 */
	protected String setSecurityGroupId(String protectedAreaId) {
		String previous = getProtectedAreaId();
		_protectedAreaId = protectedAreaId;
		return previous;
	}

	@Override
	public boolean isProtectedAreaDefined() {
		return getProtectedAreaId() != null;
	}

	@Override
	public void ensureProtectedAreaIsCreated(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException {
		if (protectedAreaExists()) {
			log.warn(Msg.bind(Messages.CreateMsg_EXISTS, getProtectedAreaId()));
		} else {
			String protectedAreaId = createProtectedArea(name, description);
			setSecurityGroupId(protectedAreaId);
		}
		fireProtectedAreaCreated();
	}

	public abstract String createProtectedArea(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException;

	@Override
	public void ensureProtectedAreaIsDestroyed() throws ProtectedAreaException,
			InterruptedException {
		if (!isProtectedAreaDefined()) {
			log.warn(Messages.DestroyMsg_ID_NOT_DEFINED);
		} else if (!protectedAreaExists()) {
			log.warn(Msg.bind(Messages.DestroyMsg_NOT_EXISTS,
					getProtectedAreaId()));
		} else {
			destroyProtectedArea();
			fireProtectedAreaDestroyed();
			setSecurityGroupId(null);
		}
	}

	public abstract void destroyProtectedArea() throws ProtectedAreaException,
			InterruptedException;

	@Override
	public void ensureProtectedAreaContentIsUpToDate(FireWallRulesPerDevice list)
			throws ProtectedAreaException, InterruptedException {
		if (!isProtectedAreaDefined()) {
			log.warn(Messages.UpdateContentMsg_ID_NOT_DEFINED);
		} else if (!protectedAreaExists()) {
			fireProtectedAreaDestroyed();
			String sProtectedAreaId = setSecurityGroupId(null);
			throw new ProtectedAreaException(Msg.bind(
					Messages.UpdateContentEx_ID_INVALID, sProtectedAreaId));
		} else {
			// will not retrieve the fwrules associated to a network device
			updateProtectedAreaContent(list.getFireWallRules());
		}
	}

	public void updateProtectedAreaContent(FireWallRules expected)
			throws ProtectedAreaException, InterruptedException {
		FireWallRules current = getProtectedAreaFireWallRules();
		FireWallRules toAdd = current.delta(expected);
		FireWallRules toRemove = expected.delta(current);

		log.info(Msg.bind(Messages.UpdateContentMsg_FWRULES_RESUME,
				getProtectedAreaId(), current, expected, toAdd, toRemove));

		revokeProtectedAreaFireWallRules(toRemove);
		authorizeProtectedAreaFireWallRules(toAdd);
	}

	public abstract void revokeProtectedAreaFireWallRules(FireWallRules toRevoke)
			throws ProtectedAreaException, InterruptedException;

	public abstract void authorizeProtectedAreaFireWallRules(
			FireWallRules toAutorize) throws ProtectedAreaException,
			InterruptedException;

}