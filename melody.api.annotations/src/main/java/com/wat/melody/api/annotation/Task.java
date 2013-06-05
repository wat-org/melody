package com.wat.melody.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
