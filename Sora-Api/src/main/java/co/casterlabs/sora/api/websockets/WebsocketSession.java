package co.casterlabs.sora.api.websockets;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

public abstract class WebsocketSession {

    // URI
    public abstract String getUri();

    public abstract @NonNull Map<String, List<String>> getAllQueryParameters();

    public abstract @NonNull Map<String, String> getQueryParameters();

    public abstract @NonNull String getQueryString();

    // Misc

    public abstract @NonNull String getRemoteIpAddress();

}
