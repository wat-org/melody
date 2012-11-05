package com.wat.melody.api.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface NestedElement {
	
	String name();

	boolean mandatory() default false;

	Type type() default Type.ADD;

	public static enum Type { ADD, CREATE };
	
}
