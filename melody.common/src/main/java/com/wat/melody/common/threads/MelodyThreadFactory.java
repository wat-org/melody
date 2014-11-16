package com.wat.melody.common.threads;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface MelodyThreadFactory {

	public Thread newThread(ThreadGroup tg, Runnable r, String name);

}