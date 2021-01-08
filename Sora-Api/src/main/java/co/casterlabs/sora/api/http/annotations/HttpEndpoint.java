package co.casterlabs.sora.api.http.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.http.HttpMethod;
import lombok.NonNull;

/**
 * Tag methods with this annotation to listen for http requests.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpEndpoint {

    /**
     * Allowed methods.
     *
     * @return a list of allowed methods
     */
    @Nullable
    HttpMethod[] allowedMethods() default {
            HttpMethod.GET,
            HttpMethod.HEAD,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.PATCH
    };

    /**
     * Uri.
     *
     * @return the uri
     */
    @NonNull
    String uri();

}
