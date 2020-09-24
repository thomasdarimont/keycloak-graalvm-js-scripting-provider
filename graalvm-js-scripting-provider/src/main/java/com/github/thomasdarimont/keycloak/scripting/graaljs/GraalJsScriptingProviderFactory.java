package com.github.thomasdarimont.keycloak.scripting.graaljs;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scripting.ScriptingProvider;
import org.keycloak.scripting.ScriptingProviderFactory;

import javax.script.ScriptEngineManager;

@AutoService(ScriptingProviderFactory.class)
public class GraalJsScriptingProviderFactory implements ScriptingProviderFactory {

    public static final String GRAALVM_JS_POLYGLOT_JS_NASHORN_COMPAT = "polyglot.js.nashorn-compat";

    // replace existing script engine manager extension...
    static final String ID = "script-based-auth";

    private ScriptEngineManager scriptEngineManager;

    @Override
    public ScriptingProvider create(KeycloakSession session) {
        lazyInit();

        return new GraalJsScriptingProvider(scriptEngineManager);
    }

    @Override
    public void init(Config.Scope config) {
        //NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //NOOP
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public String getId() {
        return ID;
    }

    private void lazyInit() {
        if (scriptEngineManager == null) {
            synchronized (this) {
                if (scriptEngineManager == null) {

                    // users can force non compat mode via -Dpolyglot.js.nashorn-compat=false
                    if (System.getProperty(GRAALVM_JS_POLYGLOT_JS_NASHORN_COMPAT) == null) {
                        System.setProperty(GRAALVM_JS_POLYGLOT_JS_NASHORN_COMPAT, "true");
                    }

                    ClassLoader cl = Thread.currentThread().getContextClassLoader();

                    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                    try {
                        scriptEngineManager = new ScriptEngineManager();
                    } finally {
                        Thread.currentThread().setContextClassLoader(cl);
                    }
                }
            }
        }
    }

}
