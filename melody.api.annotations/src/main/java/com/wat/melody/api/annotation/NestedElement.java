package com.wat.melody.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A Melody Task can have Nested Elements.
 * </p>
 * 
 * <p>
 * In order to declare the setter method of a Nested Element, you must use the
 * {@link NestedElement} annotation.
 * </p>
 * 
 * <p>
 * {@link #name()} allow to define the name of the Nested Element.
 * {@link #mandatory()} allow to specify if the Attribute is mandatory or not.
 * {@link #type()} allow to specify the type of the Nested Element.
 * </p>
 * 
 * <p>
 * Two type of Nested Elements exits :
 * <ul>
 * <li>Add Nested Element, which can be declared by setting the {@link #type()}
 * to {@link Type#ADD} ;</li>
 * <li>Create Nested Element, which can be declared by setting the
 * {@link #type()} to {@link Type#CREATE} ;</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A Add Nested Element must annotate a public 1-arg method. This method's 1-arg
 * will be created (using it public 0-arg constructor), then it will be fulfill
 * with the Nested Element's content (using its Attributes and Nested Element's
 * setter method), and then it will be pass the the method.
 * </p>
 * 
 * <p>
 * A Create Nested Element must annotate a public 0-arg method which return an
 * object. This method will be called, and the returned object will be fulfill
 * with the Nested Element's content (using its Attributes and Nested Element's
 * setter method).
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NestedElement {

	/**
	 * <p>
	 * Specifies the name of the nested element.
	 * </p>
	 */
	String name();

	/**
	 * <p>
	 * If <tt>true</tt> and if the nested element is not declared, an error will
	 * be generated.
	 * </p>
	 */
	boolean mandatory() default false;

	/**
	 * <p>
	 * Specifies the type of the nested element.
	 * </p>
	 */
	Type type() default Type.ADD;

	public static enum Type {
		ADD, CREATE
	};

	/**
	 * <p>
	 * Specifies the description of the attribute.
	 * </p>
	 */
	String description() default "";

}