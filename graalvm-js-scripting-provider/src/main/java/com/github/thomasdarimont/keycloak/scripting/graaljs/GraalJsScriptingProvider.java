package com.github.thomasdarimont.keycloak.scripting.graaljs;

import org.keycloak.models.ScriptModel;
import org.keycloak.scripting.InvocableScriptAdapter;
import org.keycloak.scripting.Script;
import org.keycloak.scripting.ScriptBindingsConfigurer;
import org.keycloak.scripting.ScriptCompilationException;
import org.keycloak.scripting.ScriptingProvider;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class GraalJsScriptingProvider implements ScriptingProvider {

    public static final String GRAAL_JS_SCRIPT_ENGINE_NAME = "graal.js";

    private final ScriptEngineManager scriptEngineManager;

    GraalJsScriptingProvider(ScriptEngineManager scriptEngineManager) {
        if (scriptEngineManager == null) {
            throw new IllegalStateException("scriptEngineManager must not be null!");
        }

        this.scriptEngineManager = scriptEngineManager;
    }

    /**
     * Wraps the provided {@link ScriptModel} in a {@link javax.script.Invocable} instance with bindings configured through the {@link ScriptBindingsConfigurer}.
     *
     * @param scriptModel        must not be {@literal null}
     * @param bindingsConfigurer must not be {@literal null}
     */
    @Override
    public InvocableScriptAdapter prepareInvocableScript(ScriptModel scriptModel, ScriptBindingsConfigurer bindingsConfigurer) {
        final AbstractEvaluatableScriptAdapter evaluatable = prepareEvaluatableScript(scriptModel);
        return evaluatable.prepareInvokableScript(bindingsConfigurer);
    }

    /**
     * Wraps the provided {@link ScriptModel} in a {@link javax.script.Invocable} instance with bindings configured through the {@link ScriptBindingsConfigurer}.
     *
     * @param scriptModel must not be {@literal null}
     */
    @Override
    public AbstractEvaluatableScriptAdapter prepareEvaluatableScript(ScriptModel scriptModel) {
        if (scriptModel == null) {
            throw new IllegalArgumentException("script must not be null");
        }

        if (scriptModel.getCode() == null || scriptModel.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("script must not be null or empty");
        }

        ScriptEngine engine = createPreparedScriptEngine(scriptModel);

        if (engine instanceof Compilable) {
            return new CompiledEvaluatableScriptAdapter(scriptModel, tryCompile(scriptModel, (Compilable) engine));
        }

        return new UncompiledEvaluatableScriptAdapter(scriptModel, engine);
    }

    private CompiledScript tryCompile(ScriptModel scriptModel, Compilable engine) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return engine.compile(scriptModel.getCode());
        } catch (ScriptException e) {
            throw new ScriptCompilationException(scriptModel, e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public ScriptModel createScript(String realmId, String mimeType, String scriptName, String scriptCode, String scriptDescription) {
        return new Script(null /* scriptId */, realmId, scriptName, mimeType, scriptCode, scriptDescription);
    }

    @Override
    public void close() {
        //NOOP
    }

    /**
     * Looks-up a {@link ScriptEngine} with prepared {@link Bindings} for the given {@link ScriptModel Script}.
     */
    private ScriptEngine createPreparedScriptEngine(ScriptModel script) {
        ScriptEngine scriptEngine = lookupScriptEngineFor(script);

        if (scriptEngine == null) {
            throw new IllegalStateException("Could not find ScriptEngine for script: " + script);
        }

        return scriptEngine;
    }

    /**
     * Looks-up a {@link ScriptEngine} based on the MIME-type provided by the given {@link Script}.
     */
    private ScriptEngine lookupScriptEngineFor(ScriptModel script) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            if (ScriptModel.TEXT_JAVASCRIPT.equals(script.getMimeType())) {
                return scriptEngineManager.getEngineByName(GRAAL_JS_SCRIPT_ENGINE_NAME);
            }
            return scriptEngineManager.getEngineByMimeType(script.getMimeType());
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
