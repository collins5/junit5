/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.jupiter.engine.Constants.DEFAULT_CONDITIONAL_SCRIPT_ENGINE_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_CONDITIONAL_SCRIPT_ENGINE_PROPERTY_VALUE;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.Conditional;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} that supports the {@link Conditional @Conditional} annotation.
 *
 * @since 5.1
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
class ConditionalExecutionCondition implements ExecutionCondition {

	private static final Logger logger = LoggerFactory.getLogger(ConditionalExecutionCondition.class);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		Optional<Conditional> annotation = findAnnotation(element, Conditional.class);
		if (!annotation.isPresent()) {
			return enabled("@Conditional is not present");
		}

		Conditional conditional = annotation.get();
		Preconditions.notEmpty(conditional.value(), "@Conditional#value() array must not be empty");

		// Find script engine
		String engine = conditional.engine();
		if (engine.isEmpty()) {
			engine = context.getConfigurationParameter(DEFAULT_CONDITIONAL_SCRIPT_ENGINE_PROPERTY_NAME) //
					.orElse(DEFAULT_CONDITIONAL_SCRIPT_ENGINE_PROPERTY_VALUE);
		}
		ScriptEngine scriptEngine = findScriptEngine(engine);
		logger.config(() -> "Found script engine: " + scriptEngine);

		// Prepare bindings
		Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		if (conditional.bindExtensionContext()) {
			bindings.put("junit$context", context);
		}
		if (conditional.bindSystemProperties()) {
			for (Map.Entry<Object, Object> it : System.getProperties().entrySet()) {
				String key = "system$" + it.getKey().toString().replace('.', '_');
				bindings.put(key, it.getValue());
			}
		}
		logger.config(() -> "Bindings: " + bindings);

		// Build actual script text from annotation properties
		String script = createScript(conditional, scriptEngine.getFactory().getLanguageName());
		logger.config(() -> "Script to evaluate: " + script);

		return evaluate(conditional, scriptEngine, script);
	}

	private ConditionEvaluationResult evaluate(Conditional conditional, ScriptEngine scriptEngine, String script) {
		Object result;
		try {
			result = scriptEngine.eval(script);
		}
		catch (ScriptException e) {
			logger.config(e, () -> "Evaluation of @Conditional script failed, disabling execution");
			return disabled(e.getMessage());
		}

		// Trivial case: script returned a custom ConditionEvaluationResult instance.
		if (result instanceof ConditionEvaluationResult) {
			return (ConditionEvaluationResult) result;
		}

		// Parse result for "true" (ignoring case) and prepare reason message.
		boolean ok = Boolean.parseBoolean(String.valueOf(result));
		String reason = conditional.reason();
		if (reason.isEmpty()) {
			reason = String.format("Script `%s` evaluated to: %s", script, result);
		}
		return ok ? enabled(reason) : disabled(reason);
	}

	ScriptEngine findScriptEngine(String string) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine scriptEngine = manager.getEngineByName(string);
		if (scriptEngine == null) {
			scriptEngine = manager.getEngineByExtension(string);
		}
		if (scriptEngine == null) {
			scriptEngine = manager.getEngineByMimeType(string);
		}
		Preconditions.notNull(scriptEngine, "Script engine not found: " + string);
		return scriptEngine;
	}

	String createScript(Conditional conditional, String language) {
		// trivial case: no imports, single script line
		if (conditional.imports().length == 0 && conditional.value().length == 1) {
			return conditional.value()[0];
		}

		switch (language) {
			case "ECMAScript":
				return createJavaScript(conditional);
			case "Groovy":
				return createGroovyScript(conditional);
			default:
				return joinLines(conditional.delimiter(), Arrays.asList(conditional.value()));
		}
	}

	private String createJavaScript(Conditional conditional) {
		boolean injectJavaImporterStatements = conditional.imports().length > 0;
		List<String> lines = new ArrayList<>();
		if (injectJavaImporterStatements) {
			String imports = String.join(", ", conditional.imports());
			lines.add("var javaImporter = new JavaImporter(" + imports + ")");
			lines.add("with (javaImporter) {");
		}
		for (String line : conditional.value()) {
			if (injectJavaImporterStatements) {
				line = "  " + line;
			}
			lines.add(line);
		}
		if (injectJavaImporterStatements) {
			lines.add("}");
			lines.add("");
		}
		return joinLines(conditional.delimiter(), lines);
	}

	private String createGroovyScript(Conditional conditional) {
		List<String> lines = new ArrayList<>();
		for (String importLine : conditional.imports()) {
			importLine = "import " + importLine;
			lines.add(importLine);
		}
		if (!lines.isEmpty()) {
			lines.add("");
		}
		lines.addAll(Arrays.asList(conditional.value()));
		return joinLines(conditional.delimiter(), lines);
	}

	private String joinLines(String delimiter, Iterable<? extends CharSequence> elements) {
		if (delimiter.isEmpty()) {
			delimiter = System.lineSeparator();
		}
		return String.join(delimiter, elements);
	}

}
