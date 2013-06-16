package com.wat.melody.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A Melody Task can have Attributes.
 * </p>
 * 
 * <p>
 * In order to declare the setter method of an Attribute, you must use the
 * {@link Attribute} annotation. This annotation must annotate a public 1-arg
 * method.
 * </p>
 * 
 * <p>
 * {@link #name()} allow to define the name of the Attribute.
 * {@link #mandatory()} allow to specify if the Attribute is mandatory or not.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
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
	 * If <tt>true</tt> and if the attribute is not declared, an error will be
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