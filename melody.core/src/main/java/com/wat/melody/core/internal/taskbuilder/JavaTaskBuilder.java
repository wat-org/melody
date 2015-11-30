package com.wat.melody.core.internal.taskbuilder;

import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskBuilder;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.common.properties.PropertySet;

/**
 * <p>
 * Instantiate an {@link ITask} based on its java canonical class name.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class JavaTaskBuilder implements ITaskBuilder {

	private Class<? extends ITask> _taskClass;
	private ICondition _condition = null;

	public JavaTaskBuilder(Class<? extends ITask> t) {
		setTaskClass(t);
	}

	private ICondition getCondition() {
		return _condition;
	}

	private ICondition setCondition(ICondition c) {
		if (c == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + ICondition.class.getCanonicalName()
					+ ".");
		}
		ICondition previous = getCondition();
		_condition = c;
		return previous;
	}

	@Override
	public String getTaskName() {
		// the task name is either the simple name of the Java class (low case)
		String taskName = getTaskClass().getSimpleName().toLowerCase();
		Task a = getTaskClass().getAnnotation(Task.class);
		if (a != null) {
			// either the name defined in the Task annotation (low case too)
			taskName = a.name().toLowerCase();
		}
		return taskName;
	}

	@Override
	public Class<? extends ITask> getTaskClass() {
		return _taskClass;
	}

	private Class<? extends ITask> setTaskClass(Class<? extends ITask> t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not Accepted. "
					+ "Must be a valid " + Class.class.getCanonicalName() + "<"
					+ ITask.class.getCanonicalName() + ">.");
		}
		Class<? extends ITask> previous = getTaskClass();
		_taskClass = t;
		computeCondition();
		return previous;
	}

	/**
	 * All conditions are described via java annotation (see {@link Conditions},
	 * {@link Condition}, {@link Match}).
	 */
	private void computeCondition() {
		Conditions acs = getTaskClass().getAnnotation(Conditions.class);
		// if no 'Conditions' annotation can be found,
		// or 'Conditions' annotation is 0 size,
		// build an always true condition
		if (acs == null || acs.value().length == 0) {
			setCondition(new TrueCondition());
			return;
		}

		AnyCondition c = new AnyCondition();
		setCondition(c);
		for (Condition ap : acs.value()) {
			AllCondition ac = new AllCondition();
			c.add(ac);
			for (Match am : ap.value()) {
				ac.add(new MatchCondition(am.expression(), am.value()));
			}
		}
	}

	@Override
	public boolean isEligible(Element elmt, PropertySet ps) {
		return getCondition().isEligible(elmt, ps);
	}

	@Override
	public void markEligibleElements(Element elmt, PropertySet ps) {
		getCondition().markEligibleElements(elmt, ps);
	}

	@Override
	public ITask build() {
		ITask t = null;
		Class<? extends ITask> c = getTaskClass();
		try {
			t = c.getConstructor().newInstance();
		} catch (NoSuchMethodException | InstantiationException
				| IllegalAccessException | ClassCastException Ex) {
			throw new RuntimeException("Unexpected error while creating a "
					+ "Task '" + c.getCanonicalName() + "' by reflection. "
					+ "Because a public no-argument constructor exists "
					+ "(see 'registerTasks'), such error cannot happened. "
					+ "Source code has certainly been modified and a "
					+ "bug have been introduced.", Ex);
		} catch (InvocationTargetException Ex) {
			throw new RuntimeException("Task '" + c.getCanonicalName()
					+ "' creation fails, generating the error below. ",
					Ex.getCause());
		}
		return t;
	}

}