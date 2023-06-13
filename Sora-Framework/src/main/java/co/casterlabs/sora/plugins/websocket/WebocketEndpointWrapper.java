package co.casterlabs.sora.plugins.websocket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.server.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.server.websocket.WebsocketSession;
import co.casterlabs.sora.PreProcessorReflectionUtil;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketPreProcessor;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;
import co.casterlabs.sora.plugins.SoraPlugins;
import co.casterlabs.sora.plugins.http.URIParameterMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WebocketEndpointWrapper {
    private WebsocketEndpoint annotation;
    private WebsocketProvider provider;
    private SoraPlugin plugin;
    private Method method;

    private URIParameterMeta uriMeta;

    private SoraPlugins sora;

    private WebocketEndpointWrapper(WebsocketEndpoint annotation, WebsocketProvider provider, SoraPlugin plugin, Method method, SoraPlugins sora) {
        this.annotation = annotation;
        this.provider = provider;
        this.plugin = plugin;
        this.method = method;
        this.sora = sora;

        this.method.setAccessible(true);

        this.uriMeta = new URIParameterMeta(this.annotation.uri());
    }

    public @Nullable WebsocketListenerPluginPair serve(@NonNull WebsocketSession session) {
        if (session.getUri().matches(this.uriMeta.getUriRegex())) {
            try {
                SoraWebsocketSession soraSession = new SoraWebsocketSession(session, this.uriMeta.decode(session.getUri()));

                String preprocessorId = this.annotation.preprocessor();
                if (!preprocessorId.isEmpty()) {
                    WebsocketPreProcessor<?> preprocessor = this.sora.getWebsocketPreProcessor(preprocessorId);

                    if (preprocessor == null) {
                        FastLogger.logStatic(LogLevel.WARNING, "Could not find a websocket preprocessor with an id of %s", preprocessorId);
                    } else {
                        try {
                            Object preprocessorData = this.annotation.preprocessorData().newInstance();
                            boolean shouldDrop = PreProcessorReflectionUtil.invokeWebsocketPreProcessor(preprocessor, preprocessorData, soraSession);

                            if (shouldDrop) {
                                return null;
                            }
                        } catch (Throwable t) {
                            FastLogger.logStatic(LogLevel.SEVERE, "An error occured whilst preprocessing (%s)\n%s", preprocessorId, t);
                            return null;
                        }
                    }
                }

                WebsocketListener listener = (WebsocketListener) this.method.invoke(this.provider, soraSession);

                return new WebsocketListenerPluginPair(this.plugin, listener);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();

                return null;
            }
        }

        return null;
    }

    public static List<WebocketEndpointWrapper> wrap(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider provider, @NonNull SoraPlugins sora) {
        List<WebocketEndpointWrapper> wrappers = new ArrayList<>();

        for (Method method : provider.getClass().getMethods()) {
            if (isListenerMethod(method)) {
                WebsocketEndpoint annotation = method.getAnnotation(WebsocketEndpoint.class);

                wrappers.add(new WebocketEndpointWrapper(annotation, provider, plugin, method, sora));
            }
        }

        return wrappers;
    }

    private static boolean isListenerMethod(@NonNull Method method) {
        return method.isAnnotationPresent(WebsocketEndpoint.class) &&
            (method.getParameterCount() == 1) &&
            method.getParameters()[0].getType().isAssignableFrom(SoraWebsocketSession.class);
    }

    @Getter
    @AllArgsConstructor
    public static class WebsocketListenerPluginPair {
        private SoraPlugin plugin;
        private WebsocketListener listener;

    }

}
