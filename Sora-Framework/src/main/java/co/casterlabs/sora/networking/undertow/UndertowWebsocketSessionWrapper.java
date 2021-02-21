package co.casterlabs.sora.networking.undertow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.casterlabs.sora.api.websockets.WebsocketSession;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import lombok.NonNull;

public class UndertowWebsocketSessionWrapper extends WebsocketSession {
    private WebSocketHttpExchange exchange;
    private WebSocketChannel channel;

    private Map<String, List<String>> allQueryParameters = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();

    public UndertowWebsocketSessionWrapper(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        this.exchange = exchange;
        this.channel = channel;

        Map<String, List<String>> headers = exchange.getRequestHeaders();

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(), entry.getValue().get(0));
        }

        for (Map.Entry<String, List<String>> entry : exchange.getRequestParameters().entrySet()) {
            this.allQueryParameters.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            this.queryParameters.put(entry.getKey(), entry.getValue().get(0));
        }

        this.headers = Collections.unmodifiableMap(this.headers);
    }

    // Request headers
    @Override
    public @NonNull Map<String, String> getHeaders() {
        return this.headers;
    }

    // URI
    @Override
    public String getUri() {
        return this.exchange.getRequestURI().split("\\?")[0];
    }

    @Override
    public @NonNull Map<String, List<String>> getAllQueryParameters() {
        return this.allQueryParameters;
    }

    @Override
    public @NonNull Map<String, String> getQueryParameters() {
        return this.queryParameters;
    }

    @Override
    public @NonNull String getQueryString() {
        if (this.exchange.getQueryString() == null) {
            return "";
        } else {
            return "?" + this.exchange.getQueryString();
        }
    }

    // Misc
    @Override
    public @NonNull String getRemoteIpAddress() {
        return this.channel.getSourceAddress().getHostString();
    }

}
