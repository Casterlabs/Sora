package co.casterlabs.sora.api.http;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rhs.server.HttpResponse;
import lombok.NonNull;

public interface HttpPreProcessor<T> {

    /**
     * @return (Optionally) A response to intercept the connection.
     */
    public @Nullable HttpResponse preprocessHttpSession(@NonNull T data, @NonNull SoraHttpSession session);

}
