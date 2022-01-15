package co.casterlabs.sora.api.websockets;

import java.util.List;
import java.util.Map;

import co.casterlabs.rakurai.collections.HeaderMap;
import co.casterlabs.rakurai.io.http.HttpVersion;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SoraWebsocketSession extends WebsocketSession {
    private WebsocketSession wrap;
    private @Getter Map<String, String> uriParameters;

    /* ---------------- */
    /* Wrap             */
    /* ---------------- */

    @Override
    public HeaderMap getHeaders() {
        return this.wrap.getHeaders();
    }

    @Override
    public String getUri() {
        return this.wrap.getUri();
    }

    @Override
    public Map<String, List<String>> getAllQueryParameters() {
        return this.wrap.getAllQueryParameters();
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return this.wrap.getQueryParameters();
    }

    @Override
    public String getQueryString() {
        return this.wrap.getQueryString();
    }

    @Override
    public String getHost() {
        return this.wrap.getHost();
    }

    @Override
    public int getPort() {
        return this.wrap.getPort();
    }

    @Override
    public HttpVersion getVersion() {
        return this.wrap.getVersion();
    }

    @Override
    public String getRemoteIpAddress() {
        return this.wrap.getRemoteIpAddress();
    }

}
