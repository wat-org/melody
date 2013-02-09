package com.wat.melody.core.nativeplugin.synchronize.types;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LockDatas {

	private int _runningJobs;

	public LockDatas() {
		_runningJobs = 0;
	}

	public int getRunningJobsCount() {
		return _runningJobs;
	}

	public synchronized void increaseRunningJobsCount() {
		_runningJobs++;
	}

	public synchronized void decreaseRunningJobsCount() {
		_runningJobs--;
	}

}
