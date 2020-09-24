package com.github.thomasdarimont.keycloak.scripting.graaljs;

import org.keycloak.models.ScriptModel;
import org.keycloak.scripting.ScriptExecutionException;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Wraps a compiled {@link ScriptModel} so it can be evaluated.
 *
 * @author <a href="mailto:jay@anslow.me.uk">Jay Anslow</a>
 */
class CompiledEvaluatableScriptAdapter extends AbstractEvaluatableScriptAdapter {
    /**
     * Holds the {@link CompiledScript} for the {@link ScriptModel}.
     */
    private final CompiledScript compiledScript;

    CompiledEvaluatableScriptAdapter(final ScriptModel scriptModel, final CompiledScript compiledScript) {
        super(scriptModel);

        if (compiledScript == null) {
            throw new IllegalArgumentException("compiledScript must not be null");
        }

        this.compiledScript = compiledScript;
    }

    @Override
    protected ScriptEngine getEngine() {
        return compiledScript.getEngine();
    }

    @Override
    protected Object eval(final Bindings bindings) throws ScriptException {
        return compiledScript.eval(bindings);
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptExecutionException {
        try {
            return compiledScript.eval(context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
