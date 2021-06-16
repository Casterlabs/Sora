package co.casterlabs.sora.plugins.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.sora.SoraUtil;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.NonNull;

public class HttpEndpointWrapper {
    private HttpEndpoint annotation;
    private HttpProvider provider;
    private Method method;

    private URIParameterMeta uriMeta;

    private HttpEndpointWrapper(HttpEndpoint annotation, HttpProvider provider, Method method) {
        this.annotation = annotation;
        this.provider = provider;
        this.method = method;

        this.method.setAccessible(true);

        this.uriMeta = new URIParameterMeta(this.annotation.uri());
    }

    public @Nullable HttpResponse serve(@NonNull HttpSession session) {
        if (SoraUtil.arrayContains(this.annotation.allowedMethods(), session.getMethod()) &&
            session.getUri().matches(this.uriMeta.getUriRegex())) {

            try {
                SoraHttpSession soraSession = new SoraHttpSession(session, this.uriMeta.decode(session.getUri()));

                HttpResponse response = (HttpResponse) this.method.invoke(this.provider, soraSession);

                if (response == null) {
                    return HttpResponse.NO_RESPONSE;
                } else {
                    return response;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();

                return HttpResponse.INTERNAL_ERROR;
            }
        }

        return null;
    }

    public static List<HttpEndpointWrapper> wrap(@NonNull HttpProvider provider) {
        List<HttpEndpointWrapper> wrappers = new ArrayList<>();

        for (Method method : provider.getClass().getMethods()) {
            if (isListenerMethod(method)) {
                HttpEndpoint annotation = method.getAnnotation(HttpEndpoint.class);

                wrappers.add(new HttpEndpointWrapper(annotation, provider, method));
            }
        }

        return wrappers;
    }

    private static boolean isListenerMethod(@NonNull Method method) {
        return method.isAnnotationPresent(HttpEndpoint.class) &&
            (method.getParameterCount() == 1) &&
            method.getParameters()[0].getType().isAssignableFrom(SoraHttpSession.class);
    }

}
