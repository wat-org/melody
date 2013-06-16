package com.wat.melody.api;

import java.lang.Thread.State;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface MelodyThread {

	public long getId();

	public String getName();

	public int getPriority();

	public State getState();

	public ThreadGroup getThreadGroup();

	public void start();

	public void join() throws InterruptedException;

	public void join(long millis) throws InterruptedException;

	public void join(long millis, int nanos) throws InterruptedException;

	public MelodyThread createNewMelodyThread();

	public MelodyThread createNewMelodyThread(Runnable runnable);

	public MelodyThread createNewMelodyThread(String name);

	public MelodyThread createNewMelodyThread(Runnable runnable, String name);

	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable);

	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			String name);

	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable, String name);

	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable, String name, long stackSize);

	public ITaskContext getContext();

	public void pushContext(ITaskContext taskContext);

	public void popContext();

}