package co.casterlabs.sora.plugins.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.sora.PreProcessorReflectionUtil;
import co.casterlabs.sora.SoraUtil;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.plugins.SoraPlugins;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class HttpEndpointWrapper {
    private HttpEndpoint annotation;
    private HttpProvider provider;
    private Method method;

    private URIParameterMeta uriMeta;

    private SoraPlugins sora;

    private HttpEndpointWrapper(HttpEndpoint annotation, HttpProvider provider, Method method, SoraPlugins sora) {
        this.annotation = annotation;
        this.provider = provider;
        this.method = method;
        this.sora = sora;

        this.method.setAccessible(true);

        this.uriMeta = new URIParameterMeta(this.annotation.uri());
    }

    public @Nullable HttpResponse serve(@NonNull HttpSession session) {
        if (SoraUtil.arrayContains(this.annotation.allowedMethods(), session.getMethod()) &&
            session.getUri().matches(this.uriMeta.getUriRegex())) {

            try {
                SoraHttpSession soraSession = new SoraHttpSession(session, this.uriMeta.decode(session.getUri()));

                String preprocessorId = this.annotation.preprocessor();
                if (!preprocessorId.isEmpty()) {
                    HttpPreProcessor<?> preprocessor = this.sora.getHttpPreProcessor(preprocessorId);

                    if (preprocessor == null) {
                        FastLogger.logStatic(LogLevel.WARNING, "Could not find an http preprocessor with an id of %s", preprocessorId);
                    } else {
                        try {
                            Object preprocessorData = this.annotation.preprocessorData().newInstance();
                            HttpResponse preprocessedResponse = PreProcessorReflectionUtil.invokeHttpPreProcessor(preprocessor, preprocessorData, soraSession);

                            if (preprocessedResponse != null) {
                                return preprocessedResponse;
                            }
                        } catch (Throwable t) {
                            FastLogger.logStatic(LogLevel.SEVERE, "An error occured whilst preprocessing (%s)\n%s", preprocessorId, t);
                            return null;
                        }
                    }
                }

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

    public static List<HttpEndpointWrapper> wrap(@NonNull HttpProvider provider, @NonNull SoraPlugins sora) {
        List<HttpEndpointWrapper> wrappers = new ArrayList<>();

        for (Method method : provider.getClass().getMethods()) {
            if (isListenerMethod(method)) {
                HttpEndpoint annotation = method.getAnnotation(HttpEndpoint.class);

                wrappers.add(new HttpEndpointWrapper(annotation, provider, method, sora));
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
