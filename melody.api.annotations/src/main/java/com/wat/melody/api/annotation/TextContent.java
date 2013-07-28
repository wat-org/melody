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
@Target(ElementType.METHOD)
public @interface TextContent {

	/**
	 * <p>
	 * If <tt>true</tt> and if no text content is defined, an error will be
	 * generated.
	 * </p>
	 */
	boolean mandatory() default false;

	/**
	 * <p>
	 * Specifies the description of the attribute.
	 * </p>
	 */
	String description() default "";

}