/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Conditional;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ConditionalTestsDemo {

	@Conditional("false")
	@Test
	void testWillBeSkipped() {
		fail("should not happen");
	}

	@Conditional("1 == 1") // Simple JavaScript expression
	@Test
	void testWillBeExecuted() {
	}

	@Conditional("Math.random() >= 0.5")
	@RepeatedTest(10)
	void testWillBeExecutedSometimes() {
	}

	@Conditional("/64/.test(system$os_arch)") // Regular expression testing bound system property.
	@Test
	void executedIfOsArchitectureContains64() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Conditional(imports = "java.nio.file", // Array of Java package names to import.
			value = "Files.exists(Files.createTempFile('temp-', '.txt'))")
	@Test
	void importJavaPackages() {
	}

	@Conditional(engine = "groovy", // Select Groovy as language.
			delimiter = "\n", // Set delimiter to Unix-style line separator.
			bindExtensionContext = false, // Not needed here.
			bindSystemProperties = false, // Not needed here, Groovy auto-imports "java.lang.*".
			value = { //
					"System.properties['jsr'] = '233'", //
					"'233' == System.properties['jsr']" }, //
			reason = "Groovy as script language, multiple lines, self-fulfilling.")
	@Test
	void groovy() {
		assertEquals("233", System.getProperty("jsr"));
	}
}
// end::user_guide[]
