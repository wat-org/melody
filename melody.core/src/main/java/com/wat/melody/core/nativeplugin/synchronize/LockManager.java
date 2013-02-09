package com.wat.melody.core.nativeplugin.synchronize;

import java.util.HashMap;
import java.util.Map;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.core.nativeplugin.synchronize.exception.IllegalLockIdException;
import com.wat.melody.core.nativeplugin.synchronize.types.LockDatas;
import com.wat.melody.core.nativeplugin.synchronize.types.LockId;
import com.wat.melody.core.nativeplugin.synchronize.types.LockScope;
import com.wat.melody.core.nativeplugin.synchronize.types.MaxPar;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LockManager {

	private static Map<LockId, LockDatas> lockTableStates = new HashMap<LockId, LockDatas>();

	/**
	 * <p>
	 * Run the given job when there is an empty place in the given semaphore.
	 * </p>
	 * <p>
	 * When multiple calls to this method referenced the same semaphore, this
	 * method guarantees that only {@link MaxPar} jobs will run simultaneously.
	 * </p>
	 * 
	 * @param cb
	 *            is the job to call when there is an empty place in the
	 *            semaphore.
	 * @param maxPar
	 *            is the size of the semaphore. 0 means there is no size limit
	 *            (fully concurrent). 1 means fully sequential. X means that X
	 *            job can run simultaneously.
	 * @param scope
	 *            is the family the semaphore belongs to.
	 *            {@link LockScope#CURRENT} means ... {@link LockScope#CALL}
	 *            means ... {@link LockScope#GLOBAL} means ...
	 * @param lockId
	 *            identifies a semaphore in the given scope.
	 * 
	 * @throws MelodyException
	 * @throws InterruptedException
	 */
	public static void run(LockCallback cb, MaxPar maxPar, LockScope scope,
			LockId lockId) throws MelodyException, InterruptedException {
		if (cb == null) {
			throw new IllegalArgumentException("null:Not accepted. "
					+ "Must be a valid "
					+ LockCallback.class.getCanonicalName() + ".");
		}
		if (maxPar != null && maxPar.equals(MaxPar.UNLIMITED)) {
			cb.doRun();
			return;
		}

		if (lockId == null) {
			lockId = LockId.DEFAULT_LOCK_ID;
		}
		if (scope == null) {
			scope = LockScope.CURRENT;
		}
		if (maxPar == null) {
			maxPar = MaxPar.SEQUENTIAL;
		}

		LockId realLockId = getRealLockId(scope, lockId);
		LockDatas lockDatas = getLockDatas(realLockId);
		while (true) {
			synchronized (lockDatas) {
				if (lockDatas.getRunningJobsCount() < maxPar.getValue()) {
					lockDatas.increaseRunningJobsCount();
					break;
				}
			}
			Thread.sleep(100);
		}
		try {
			cb.doRun();
		} finally {
			synchronized (lockDatas) {
				lockDatas.decreaseRunningJobsCount();
			}
		}
	}

	private static LockId getRealLockId(LockScope scope, LockId lockId) {
		String scopeId = null;
		switch (scope) {
		case CURRENT:
			scopeId = Thread.currentThread().getThreadGroup().getName();
			break;
		case CALL:
			int ind = Thread.currentThread().getThreadGroup().getName()
					.lastIndexOf(">call>PM-");
			if (ind != -1) {
				scopeId = Thread.currentThread().getThreadGroup().getName()
						.substring(0, ind + ">call".length());
				break;
			}
		case GLOBAL:
			scopeId = "PM-main";
			break;
		}
		try {
			return LockId.parseString(scopeId + ">" + lockId.getValue());
		} catch (IllegalLockIdException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a lock id. "
					+ "Since this default value is automatically created, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static synchronized LockDatas getLockDatas(LockId realLockId) {
		LockDatas currentlyRunning = lockTableStates.get(realLockId);
		if (currentlyRunning == null) {
			currentlyRunning = new LockDatas();
			lockTableStates.put(realLockId, currentlyRunning);
		}
		return currentlyRunning;
	}

}
