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

	private Stack<ITaskContext> _context = new Stack<ITaskContext>();

	protected CoreThread() {
		super();
	}

	protected CoreThread(Runnable runnable) {
		super(runnable);
	}

	protected CoreThread(String name) {
		super(name);
	}

	protected CoreThread(Runnable runnable, String name) {
		super(runnable, name);
	}

	protected CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable) {
		super(ownerTreadGroup, runnable);
	}

	protected CoreThread(ThreadGroup ownerTreadGroup, String name) {
		super(ownerTreadGroup, name);
	}

	protected CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable,
			String name) {
		super(ownerTreadGroup, runnable, name);
	}

	protected CoreThread(ThreadGroup ownerTreadGroup, Runnable runnable,
			String name, long stackSize) {
		super(ownerTreadGroup, runnable, name, stackSize);
	}

	@Override
	public MelodyThread createNewMelodyThread() {
		return new CoreThread();
	}

	@Override
	public MelodyThread createNewMelodyThread(Runnable runnable) {
		return new CoreThread(runnable);
	}

	@Override
	public MelodyThread createNewMelodyThread(String name) {
		return new CoreThread(name);
	}

	@Override
	public MelodyThread createNewMelodyThread(Runnable runnable, String name) {
		return new CoreThread(runnable, name);
	}

	@Override
	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable) {
		return new CoreThread(ownerTreadGroup, runnable);
	}

	@Override
	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			String name) {
		return new CoreThread(ownerTreadGroup, name);
	}

	@Override
	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable, String name) {
		return new CoreThread(ownerTreadGroup, runnable, name);
	}

	@Override
	public MelodyThread createNewMelodyThread(ThreadGroup ownerTreadGroup,
			Runnable runnable, String name, long stackSize) {
		return new CoreThread(ownerTreadGroup, runnable, name, stackSize);
	}

	@Override
	public ITaskContext getContext() {
		return _context.peek();
	}

	@Override
	public void pushContext(ITaskContext context) {
		_context.push(context);
	}

	@Override
	public void popContext() {
		_context.pop();
	}

}
