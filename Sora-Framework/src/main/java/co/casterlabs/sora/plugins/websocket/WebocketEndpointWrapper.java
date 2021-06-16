package co.casterlabs.sora.plugins.websocket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;
import co.casterlabs.sora.plugins.http.URIParameterMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class WebocketEndpointWrapper {
    private WebsocketEndpoint annotation;
    private WebsocketProvider provider;
    private SoraPlugin plugin;
    private Method method;

    private URIParameterMeta uriMeta;

    private WebocketEndpointWrapper(WebsocketEndpoint annotation, WebsocketProvider provider, SoraPlugin plugin, Method method) {
        this.annotation = annotation;
        this.provider = provider;
        this.plugin = plugin;
        this.method = method;

        this.method.setAccessible(true);

        this.uriMeta = new URIParameterMeta(this.annotation.uri());
    }

    public @Nullable WebsocketListenerPluginPair serve(@NonNull WebsocketSession session) {
        if (session.getUri().matches(this.uriMeta.getUriRegex())) {
            try {
                SoraWebsocketSession soraSession = new SoraWebsocketSession(session, this.uriMeta.decode(session.getUri()));

                WebsocketListener listener = (WebsocketListener) this.method.invoke(this.provider, soraSession);

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
            method.getParameters()[0].getType().isAssignableFrom(SoraWebsocketSession.class);
    }

    @Getter
    @AllArgsConstructor
    public static class WebsocketListenerPluginPair {
        private SoraPlugin plugin;
        private WebsocketListener listener;

    }

}
