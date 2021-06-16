package co.casterlabs.sora.plugins.websocket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebocketEndpointWrapper {
    /*private static final String[] SAFELISTED_HEADERS = { // https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_request_header
            "Accept",
            "Accept-Language",
            "Content-Language",
            "Content-Type"
    };*/

    private WebsocketEndpoint annotation;
    private WebsocketProvider provider;
    private SoraPlugin plugin;
    private Method method;

    public @Nullable WebsocketListenerPluginPair serve(@NonNull WebsocketSession session) {
        boolean uriMatches = session.getUri().matches(this.annotation.uri());

        if (uriMatches) {
            try {
                WebsocketListener listener = (WebsocketListener) this.method.invoke(this.provider, session);

                return new WebsocketListenerPluginPair(this.plugin, listener);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();

                return null;
            }
        }

        return null;
    }

    public static List<WebocketEndpointWrapper> wrap(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider provider) {
        List<WebocketEndpointWrapper> wrappers = new ArrayList<>();

        for (Method method : provider.getClass().getMethods()) {
            if (isListenerMethod(method)) {
                WebsocketEndpoint annotation = method.getAnnotation(WebsocketEndpoint.class);

                wrappers.add(new WebocketEndpointWrapper(annotation, provider, plugin, method));
            }
        }

        return wrappers;
    }

    private static boolean isListenerMethod(@NonNull Method method) {
        return method.isAnnotationPresent(WebsocketEndpoint.class) &&
            (method.getParameterCount() == 1) &&
            method.getParameters()[0].getType().isAssignableFrom(WebsocketSession.class);
    }

    @Getter
    @AllArgsConstructor
    public static class WebsocketListenerPluginPair {
        private SoraPlugin plugin;
        private WebsocketListener listener;

    }

}
