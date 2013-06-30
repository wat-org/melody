package com.wat.melody.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The Name of a Melody Task is deduced from the name of its implementation
 * class.
 * </p>
 * 
 * <p>
 * In order to assign a Melody Task a more friendly Name, the {@link Task}
 * annotation can be use. This annotation must annotate a Melody Task (e.g a
 * class which implements {@link ITask}). It allows to declare an alternative
 * Name (via {@link #name()}) for that Melody Task.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Task {

	/**
	 * <p>
	 * Specifies the name of the task.
	 * </p>
	 */
	String name();

	/**
	 * <p>
	 * Specifies the description of the task.
	 * </p>
	 */
	String description() default "";

}