package co.casterlabs.sora.api.websockets;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rhs.protocol.HttpVersion;
import co.casterlabs.rhs.session.TLSVersion;
import co.casterlabs.rhs.session.WebsocketSession;
import co.casterlabs.rhs.util.HeaderMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@AllArgsConstructor
public class SoraWebsocketSession extends WebsocketSession {
    private WebsocketSession wrap;
    private @Getter Map<String, String> uriParameters;

    private @Setter Object attachment;

    public SoraWebsocketSession(@NonNull WebsocketSession wrap, Map<String, String> uriParameters) {
        this.wrap = wrap;
        this.uriParameters = uriParameters;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttachment() {
        return (T) this.attachment;
    }

    /* ---------------- */
    /* Some internals   */
    /* ---------------- */

    @Override
    public String getRequestId() {
        return this.wrap.getRequestId();
    }

    @Override
    public FastLogger getLogger() {
        return this.wrap.getLogger();
    }

    @Override
    public boolean isProxied() {
        return this.wrap.isProxied();
    }

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
    public int getPort() {
        return this.wrap.getPort();
    }

    @Override
    public String getRawMethod() {
        return this.wrap.getRawMethod();
    }

    @Override
    public HttpVersion getVersion() {
        return this.wrap.getVersion();
    }

    @Override
    protected String getNetworkIpAddress() {
        return this.wrap.getRemoteIpAddress();
    }

    @Override
    public @Nullable TLSVersion getTLSVersion() {
        return this.wrap.getTLSVersion();
    }

}
