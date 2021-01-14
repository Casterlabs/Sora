package co.casterlabs.sora.networking.nano;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.http.HttpMethod;
import co.casterlabs.sora.api.http.HttpSession;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.ResponseException;
import lombok.Getter;
import lombok.NonNull;

public class NanoHttpSessionWrapper extends HttpSession {
    private @Getter IHTTPSession nanoSession;

    private byte[] body;

    public NanoHttpSessionWrapper(@NonNull IHTTPSession nanoSession) {
        this.nanoSession = nanoSession;
    }

    // Request headers

    @Override
    public @NonNull Map<String, String> getHeaders() {
        return this.nanoSession.getHeaders();
    }

    // URI
    @Override
    public String getUri() {
        return this.nanoSession.getUri();
    }

    @Override
    public @NonNull Map<String, List<String>> getAllQueryParameters() {
        return this.nanoSession.getParameters();
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NonNull Map<String, String> getQueryParameters() {
        return this.nanoSession.getParms();
    }

    @Override
    public @NonNull String getQueryString() {
        if (this.nanoSession.getQueryParameterString() == null) {
            return "";
        } else {
            return "?" + this.nanoSession.getQueryParameterString();
        }
    }

    // Request body
    @Override
    public boolean hasBody() {
        return this.getHeader("content-length") != null;
    }

    @Override
    public @Nullable byte[] getRequestBodyBytes() throws IOException {
        if (this.body == null) {
            if (this.hasBody()) {
                int contentLength = Integer.parseInt(this.getHeader("content-length"));
                this.body = new byte[contentLength];

                this.nanoSession.getInputStream().read(this.body, 0, contentLength);

                return this.body;
            } else {
                throw new IOException("No body was sent");
            }
        } else {
            return this.body;
        }
    }

    @Override
    public @NonNull Map<String, String> parseFormBody() throws IOException {
        Map<String, String> files = new HashMap<>();

        try {
            this.nanoSession.parseBody(files);
        } catch (ResponseException e) {
            throw new IOException(e);
        }

        return files;
    }

    // Misc
    @Override
    public @NonNull HttpMethod getMethod() {
        return HttpMethod.valueOf(this.nanoSession.getMethod().name());
    }

    @Override
    public @NonNull String getRemoteIpAddress() {
        return this.nanoSession.getRemoteIpAddress();
    }

}
