package com.wat.melody.core.internal;

import java.util.Stack;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.MelodyThread;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CoreThread extends Thread implements MelodyThread {

	public static CoreThread currentCoreThread() {
		if (Thread.currentThread() instanceof CoreThread) {
			return (CoreThread) Thread.currentThread();
		}
		return null;
	}

	private Stack<ITaskContext> _context = new Stack<ITaskContext>();

	public CoreThread() {
		super();
	}

	public CoreThread(Runnable runnable) {
		super(runnable);
	}

	public CoreThread(String name) {
		super(name);
	}

	public CoreThread(Runnable runnable, String name) {
		super(runnable, name);
	}

	public CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable) {
		super(ownerTreadGroup, runnable);
	}

	public CoreThread(ThreadGroup ownerTreadGroup, String name) {
		super(ownerTreadGroup, name);
	}

	public CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable,
			String name) {
		super(ownerTreadGroup, runnable, name);
	}

	public CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable,
			String name, long stackSize) {
		super(ownerTreadGroup, runnable, name, stackSize);
	}

	public ITaskContext getContext() {
		return _context.peek();
	}

	/*
	 * TODO : find a way to put this into the API, cause multi-threaded Task
	 * developers will need it.
	 */
	public void pushContext(ITaskContext context) {
		_context.push(context);
	}

	public void popContext() {
		_context.pop();
	}
}
