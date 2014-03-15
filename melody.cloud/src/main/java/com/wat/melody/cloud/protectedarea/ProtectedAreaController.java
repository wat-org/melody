package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ProtectedAreaController {

	public void addListener(ProtectedAreaControllerListener listener);

	public void removeListener(ProtectedAreaControllerListener listener);

	public boolean isProtectedAreaDefined();

	public ProtectedAreaId getProtectedAreaId();

	public boolean protectedAreaExists();

	public void ensureProtectedAreaIsCreated(ProtectedAreaName name,
			String description) throws ProtectedAreaException,
			InterruptedException;

	public void ensureProtectedAreaIsDestroyed() throws ProtectedAreaException,
			InterruptedException;

	public void ensureProtectedAreaContentIsUpToDate(FireWallRulesPerDevice list)
			throws ProtectedAreaException, InterruptedException;

	public FireWallRules getProtectedAreaFireWallRules();

}