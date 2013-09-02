package com.wat.melody.api;

import java.util.EmptyStackException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Melody {

	/**
	 * <p>
	 * Get the last {@link ITaskContext} associated to the current Thread.
	 * </p>
	 * 
	 * @return the {@link ITaskContext} associated the current {@link Thread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 * @throws EmptyStackException
	 *             if no {@link ITaskContext} have been associated yet.
	 */
	public static ITaskContext getContext() throws IllegalStateException,
			EmptyStackException {
		return currentMelodyThread().getContext();
	}

	/**
	 * <p>
	 * Push the {@link ITaskContext} into the current {@link MelodyThread}.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static void pushContext(ITaskContext taskContext)
			throws IllegalStateException {
		MelodyThread t = currentMelodyThread();
		if (t != null) {
			t.pushContext(taskContext);
		}
	}

	/**
	 * <p>
	 * Pop the last {@link ITaskContext} associated the current
	 * {@link MelodyThread}.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 * @throws EmptyStackException
	 *             if no {@link ITaskContext} have been associated yet.
	 */
	public static void popContext() throws IllegalStateException,
			EmptyStackException {
		MelodyThread t = currentMelodyThread();
		if (t != null) {
			t.popContext();
		}
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread()
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread();
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(Runnable runnable)
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(runnable);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(String name)
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(name);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(Runnable runnable,
			String name) throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(runnable, name);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable)
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(ownerTreadGroup,
				runnable);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, String name)
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(ownerTreadGroup,
				name);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable, String name)
			throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(ownerTreadGroup,
				runnable, name);
	}

	/**
	 * <p>
	 * Create a new {@link MelodyThread}.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}.
	 * 
	 * @throws IllegalStateException
	 *             if the current {@link Thread} is not a {@link MelodyThread}.
	 */
	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable, String name,
			long stackSize) throws IllegalStateException {
		return currentMelodyThread().createNewMelodyThread(ownerTreadGroup,
				runnable, name, stackSize);
	}

	/**
	 * <p>
	 * Get the current {@link MelodyThread}.
	 * </p>
	 * 
	 * @return the current {@link MelodyThread}.
	 */
	private static MelodyThread currentMelodyThread()
			throws IllegalStateException {
		Thread t = Thread.currentThread();
		if (!(t instanceof MelodyThread)) {
			throw new IllegalStateException("Current thread is not a "
					+ MelodyThread.class.getCanonicalName() + ".");

		}
		return (MelodyThread) t;
	}

}