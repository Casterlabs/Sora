package co.casterlabs.sora.api.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.collections.HeaderMap;
import co.casterlabs.rakurai.io.http.HttpMethod;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.HttpVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SoraHttpSession extends HttpSession {
    private HttpSession wrap;
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
    public boolean hasBody() {
        return this.wrap.hasBody();
    }

    @Override
    public @Nullable byte[] getRequestBodyBytes() throws IOException {
        return this.wrap.getRequestBodyBytes();
    }

    @Override
    public Map<String, String> parseFormBody() throws IOException {
        return this.wrap.parseFormBody();
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
    public HttpMethod getMethod() {
        return this.wrap.getMethod();
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
