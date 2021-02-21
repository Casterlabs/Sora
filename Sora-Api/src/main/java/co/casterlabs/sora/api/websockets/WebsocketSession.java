package co.casterlabs.sora.api.websockets;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public abstract class WebsocketSession {

    // Request headers
    public abstract @NonNull Map<String, String> getHeaders();

    public @Nullable String getHeader(@NonNull String header) {
        return this.getHeaders().get(header);
    }

    // URI
    public abstract String getUri();

    public abstract @NonNull Map<String, List<String>> getAllQueryParameters();

    public abstract @NonNull Map<String, String> getQueryParameters();

    public abstract @NonNull String getQueryString();

    // Misc

    public abstract @NonNull String getRemoteIpAddress();

}
