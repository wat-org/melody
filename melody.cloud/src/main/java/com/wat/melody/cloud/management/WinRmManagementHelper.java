package com.wat.melody.cloud.management;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.management.exception.ManagementException;

public class WinRmManagementHelper implements ManagementHelper {

	private ManagementInfos moManagementInfos;
	private ITaskContext moContext;

	public ManagementInfos getManagementInfos() {
		return moManagementInfos;
	}

	@Override
	public void setManagementInfos(ManagementInfos mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ManagementInfos.class.getCanonicalName() + ".");
		}
		moManagementInfos = mi;
	}

	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext mi) {
		if (mi == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ITaskContext.class.getCanonicalName() + ".");
		}
		moContext = mi;
	}

	@Override
	public void enableManagement(long timeout) throws ManagementException,
			InterruptedException {
		throw new ManagementException(Messages.MgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

	@Override
	public void disableManagement() throws ManagementException {
		throw new ManagementException(Messages.MgmtEx_WINRM_MGMT_NOT_SUPPORTED);
	}

}
