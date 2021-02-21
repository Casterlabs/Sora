package co.casterlabs.sora.networking.nano;

import java.util.List;
import java.util.Map;

import co.casterlabs.sora.api.websockets.WebsocketSession;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import lombok.Getter;
import lombok.NonNull;

public class NanoWebsocketSessionWrapper extends WebsocketSession {
    private @Getter IHTTPSession nanoSession;

    public NanoWebsocketSessionWrapper(@NonNull IHTTPSession nanoSession) {
        this.nanoSession = nanoSession;
    }

    // Headers
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

    // Misc
    @Override
    public @NonNull String getRemoteIpAddress() {
        return this.nanoSession.getRemoteIpAddress();
    }

}
