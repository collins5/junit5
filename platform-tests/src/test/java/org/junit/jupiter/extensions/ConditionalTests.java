/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Conditional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * JavaScript-based execution condition evaluation tests.
 *
 * @since 1.1
 */
@Conditional("true")
class ConditionalTests {

	@Test
	@Conditional("true")
	void justTrue() {
	}

	@Test
	@Conditional("false")
	void justFalse() {
		fail("test must not be executed");
	}

	@Test
	@Conditional("1 == 2")
	void oneEqualsTwo() {
		fail("test must not be executed");
	}

	@Test
	@Conditional("org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled('Go!')")
	void customResultEnabled() {
	}

	@Test
	@Conditional("org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled('No go.')")
	void customResultDisabled() {
		fail("test must not be executed");
	}

	@Test
	@Conditional("java.lang.Boolean.getBoolean('is-not-set')")
	void getBoolean() {
		fail("test must not be executed");
	}

	@Test
	@Conditional(imports = "java.nio.file", value = "Files.exists(Files.createTempFile('temp-', '.txt'))")
	void filesExists() {
	}

	@Test
	@Conditional(value = "junit$context.publishReportEntry('foo', 'bar')", reason = "no result, no execution")
	void publishReportEntry() {
		fail("test must not be executed");
	}

	@Test
	@Conditional(value = "syntactically, some thing is not right")
	void syntaxFailure() {
		fail("test must not be executed");
	}

	@Test
	@Conditional("java.lang.System.getProperty('os.name').toLowerCase().contains('win')")
	void win() {
		assertTrue(System.getProperty("os.name").toLowerCase().contains("win"));
	}

	@Test
	@Conditional("/64/.test(system$os_arch)")
	void osArch() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test
	@Conditional(engine = "groovy", value = { "System.properties['jsr'] = '233'", "'233' == System.properties['jsr']" })
	void groovy() {
		assertEquals("233", System.getProperty("jsr"));
	}

	@Test
	@Conditional(engine = "groovy", imports = "java.nio.file.*", value = "Files.exists(Paths.get('foo', 'bar'))")
	void groovyImports() {
		fail("test must not be executed");
	}

	@Test
	@Conditional("true")
	@Disabled
	void enabledAndDisabled() {
		fail("test must not be executed");
	}

	@Test
	@Disabled
	@Conditional("true")
	void disabledAndEnabled() {
		fail("test must not be executed");
	}

}
