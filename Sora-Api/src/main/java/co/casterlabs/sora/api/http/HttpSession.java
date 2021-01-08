package co.casterlabs.sora.api.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public abstract class HttpSession {
    // https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_request_header
    public static final String[] SAFELISTED_HEADERS = {
            "Accept",
            "Accept-Language",
            "Content-Language",
            "Content-Type"
    };

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

    // Request body
    public abstract boolean hasBody();

    public @Nullable String getRequestBody() throws IOException {
        if (this.hasBody()) {
            return new String(this.getRequestBodyBytes(), StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public abstract @Nullable byte[] getRequestBodyBytes() throws IOException;

    public abstract @NonNull Map<String, String> parseFormBody() throws IOException;

    // Misc
    public abstract @NonNull HttpMethod getMethod();

    public abstract @NonNull String getRemoteIpAddress();

}
