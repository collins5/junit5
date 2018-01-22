/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @Conditional} is used to signal that the annotated test class or
 * test method is only executed if and only if the script passed via
 * the {@link #value()} property evaluates to {@code true}.
 *
 * <p>When this annotation with a script that evaluates to {@code false}
 * is applied at the class level, all test methods within that class
 * are automatically disabled as well.
 *
 * @since 5.1
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.1")
public @interface Conditional {

	/**
	 * Short name of the {@link javax.script.ScriptEngine ScriptEngine} to use.
	 *
	 * <p>An empty string is interpreted as {@code "javascript"}.
	 *
	 * @return script engine name
	 * @see javax.script.ScriptEngineManager#getEngineByName(String)
	 */
	String engine() default "";

	/**
	 * Script predicate to evaluate.
	 *
	 * @return lines of the script predicate
	 */
	String[] value();

	/**
	 * Delimiter separating script lines.
	 *
	 * @return the line separator or an empty String indicating {@link System#lineSeparator()}
	 */
	String delimiter() default "";

	/**
	 * Names to import.
	 *
	 * <p><b>JavaScript</b>
	 * <p>{@code JavaImporter} takes a variable number of arguments
	 * as Java packages, and the returned object is used in a {@code with} statement
	 * whose scope includes the specified package imports. The global JavaScript
	 * scope is not affected.
	 *
	 * <p><b>Groovy</b>
	 * Each name is inserted as {@code import name} at the top of the script.
	 *
	 * @return names to import
	 */
	String[] imports() default {};

	/**
	 * Bind {@code junit$context} to the active extension context.
	 */
	boolean bindExtensionContext() default true;

	/**
	 * Bind all {@link System#getProperties()} as {@code system$<key>} variables.
	 * <p>
	 * The dot {@code .} in each key is replaced by an underscore {@code _} character.
	 * For examples: {@code system$java_version}
	 */
	boolean bindSystemProperties() default true;

	/**
	 * Reason why the container or test should be enabled.
	 *
	 * @return the reason why the container or test should be enabled
	 */
	String reason() default "";

}
