package co.casterlabs.sora;

import java.lang.reflect.Method;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketPreProcessor;

public class PreProcessorReflectionUtil {
    private static Method httpPreProcessorInvoke;
    private static Method websocketPreProcessorInvoke;

    static {
        try {
            httpPreProcessorInvoke = HttpPreProcessor.class.getMethod("preprocessHttpSession", Object.class, SoraHttpSession.class);
            websocketPreProcessorInvoke = WebsocketPreProcessor.class.getMethod("preprocessWebsocketSession", Object.class, SoraWebsocketSession.class);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

    }

    public static @Nullable HttpResponse invokeHttpPreProcessor(HttpPreProcessor<?> preprocessor, Object data, SoraHttpSession session) throws Throwable {
        return (HttpResponse) httpPreProcessorInvoke.invoke(preprocessor, data, session);
    }

    public static boolean invokeWebsocketPreProcessor(WebsocketPreProcessor<?> preprocessor, Object data, SoraWebsocketSession session) throws Throwable {
        return (boolean) websocketPreProcessorInvoke.invoke(preprocessor, data, session);
    }

}
