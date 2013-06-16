package com.wat.melody.core.nativeplugin.synchronize.types;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Semaphore {

	private int _runningJobs;

	public Semaphore() {
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