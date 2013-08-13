package com.wat.melody.core.nativeplugin.synchronize;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.core.nativeplugin.synchronize.exception.IllegalLockIdException;
import com.wat.melody.core.nativeplugin.synchronize.types.LockId;
import com.wat.melody.core.nativeplugin.synchronize.types.LockScope;
import com.wat.melody.core.nativeplugin.synchronize.types.MaxPar;
import com.wat.melody.core.nativeplugin.synchronize.types.Semaphore;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class LockManager {

	private static Logger log = LoggerFactory.getLogger(LockManager.class);

	private static Map<LockId, Semaphore> lockTableStates = new HashMap<LockId, Semaphore>();

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

		LockId semaphoreId = getSemaphoreId(scope, lockId);
		Semaphore semaphore = getSemaphore(semaphoreId);
		synchronized (semaphore) {
			while (semaphore.getRunningJobsCount() >= maxPar.getValue()) {
				log.trace(Msg
						.bind(Messages.LockMgmtMsg_BEGIN_WAIT, semaphoreId));
				try {
					semaphore.wait();
				} catch (InterruptedException Ex) {
					throw new MelodyInterruptedException("wait interrupted", Ex);
				}
				log.trace(Messages.LockMgmtMsg_END_WAIT);
			}
			semaphore.increaseRunningJobsCount();
		}

		try {
			log.trace(Messages.LockMgmtMsg_BEGIN_JOB);
			cb.doRun();
		} finally {
			log.trace(Msg.bind(Messages.LockMgmtMsg_END_JOB, semaphoreId));
			synchronized (semaphore) {
				semaphore.decreaseRunningJobsCount();
				semaphore.notify();
			}
		}
	}

	private static LockId getSemaphoreId(LockScope scope, LockId lockId) {
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

	private static synchronized Semaphore getSemaphore(LockId realLockId) {
		Semaphore currentlyRunning = lockTableStates.get(realLockId);
		if (currentlyRunning == null) {
			currentlyRunning = new Semaphore();
			lockTableStates.put(realLockId, currentlyRunning);
		}
		return currentlyRunning;
	}

}