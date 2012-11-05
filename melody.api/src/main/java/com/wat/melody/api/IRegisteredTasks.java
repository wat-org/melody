package com.wat.melody.api;

public interface IRegisteredTasks {

	public Class<ITask> registerTaskClass(Class<ITask> c);

	public Class<ITask> getRegisteredTaskClass(String taskName);

	public boolean containsRegisteredTaskClass(String taskName);

}
