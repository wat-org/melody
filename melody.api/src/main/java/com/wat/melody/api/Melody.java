package com.wat.melody.api;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Melody {

	/**
	 * <p>
	 * Get the {@link ITaskContainer} associated to the current Thread.
	 * </p>
	 * 
	 * @return the {@link ITaskContainer} associated the current Thread.
	 */
	public static ITaskContext getContext() {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.getContext();
	}

	public static void pushContext(ITaskContext taskContext) {
		MelodyThread t = currentMelodyThread();
		if (t != null) {
			t.pushContext(taskContext);
		}
	}

	public static void popContext() {
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
	 * <p>
	 * Obtaining a new {@link MelodyThread} is only possible when the current
	 * {@link Thread} is a {@link MelodyThread} itself.
	 * </p>
	 * 
	 * @return a new {@link MelodyThread}, or <tt>null</tt> if the current
	 *         {@link Thread} is not a MelodyThread itself.
	 */
	public static MelodyThread createNewMelodyThread() {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread();
	}

	public static MelodyThread createNewMelodyThread(Runnable runnable) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(runnable);
	}

	public static MelodyThread createNewMelodyThread(String name) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(name);
	}

	public static MelodyThread createNewMelodyThread(Runnable runnable,
			String name) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(runnable, name);
	}

	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(ownerTreadGroup,
				runnable);
	}

	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, String name) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t
				.createNewMelodyThread(ownerTreadGroup, name);
	}

	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable, String name) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(ownerTreadGroup,
				runnable, name);
	}

	public static MelodyThread createNewMelodyThread(
			ThreadGroup ownerTreadGroup, Runnable runnable, String name,
			long stackSize) {
		MelodyThread t = currentMelodyThread();
		return t == null ? null : t.createNewMelodyThread(ownerTreadGroup,
				runnable, name, stackSize);
	}

	/**
	 * <p>
	 * Get the current {@link MelodyThread}.
	 * </p>
	 * 
	 * @return the current {@link MelodyThread}.
	 */
	private static MelodyThread currentMelodyThread() {
		Thread t = Thread.currentThread();
		return t instanceof MelodyThread ? (MelodyThread) t : null;
	}

}
