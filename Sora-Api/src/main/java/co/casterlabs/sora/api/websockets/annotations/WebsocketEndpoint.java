package co.casterlabs.sora.api.websockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lombok.NonNull;

/**
 * Tag methods with this annotation to listen for websocket connections.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebsocketEndpoint {

    /**
     * Uri.
     *
     * @return the uri
     */
    @NonNull
    String uri();

    String preprocessor() default "";

    Class<?> preprocessorData() default Object.class;

}
