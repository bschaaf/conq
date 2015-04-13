package com.atex.plugins.js.composer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.Status;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.mapping.ContentComposer;
import com.atex.onecms.content.mapping.Context;
import com.atex.onecms.content.mapping.Request;

public class ScriptableComposer implements ContentComposer<Object, Object, Object> {

    private static final Logger LOGGER = Logger.getLogger(ScriptableComposer.class.getName());

    @Override
    public ContentResult<Object> compose(final ContentResult<Object> source,
                                         final String variant,
                                         final Request request,
                                         final Context<Object> context) {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();
        bindings.put("variant", variant);
        bindings.put("request", request);
        bindings.put("context", context);
        bindings.put("source", source);
        ContentResult<ComposerScript> script = getScript(source,
                variant,
                context.getContentManager(),
                request.getSubject());
        if (script == null || !script.getStatus().isOk()) {
            if (script != null) {
                LOGGER.log(Level.WARNING, String.format("Error reading JS mapping for variant %s from type %s",
                        variant,
                        source.getContent().getContentDataType()));
            }
            return new ContentResult<>(source.getContentId(), Status.NOT_FOUND_IN_VARIANT);
        }
        try {
            @SuppressWarnings("unchecked")
            ContentResult result =
                    (ContentResult) engine.eval(script.getContent().getContentData().getJavaScript(), bindings);
            return result;
        } catch (ScriptException | ClassCastException e) {
            LOGGER.log(Level.WARNING, String.format("Failed to execute JS for variant %s from type %s",
                    variant,
                    source.getContent().getContentDataType()), e);
            return new ContentResult<>(source.getContentId(), Status.NOT_FOUND_IN_VARIANT);
        }
    }

    private ContentResult<ComposerScript> getScript(final ContentResult<Object> source,
                                                    final String variant,
                                                    final ContentManager cm,
                                                    final Subject subject) {
        ContentVersionId scriptId = getScriptId(source, variant, cm, subject);
        if (scriptId == null) {
            return null;
        }
        return cm.get(scriptId,
                null,
                ComposerScript.class,
                Collections.<String, Object>emptyMap(),
                subject);
    }

    private ContentVersionId getScriptId(final ContentResult<Object> source,
                                         final String variant,
                                         final ContentManager cm,
                                         final Subject subject) {
        String extId = String.format("js.composer/%s/%s",
                variant,
                source.getContent().getContentDataType());
        return cm.resolve(extId, subject);
    }
}
