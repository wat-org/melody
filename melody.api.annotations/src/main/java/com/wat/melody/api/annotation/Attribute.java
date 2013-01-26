package com.wat.melody.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Attribute {

	/**
	 * <p>
	 * Specifies the name of the attribute.
	 * </p>
	 */
	String name();

	/**
	 * <p>
	 * If <code>true</code> and if the attribute is not declared, an error will
	 * be generated.
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
