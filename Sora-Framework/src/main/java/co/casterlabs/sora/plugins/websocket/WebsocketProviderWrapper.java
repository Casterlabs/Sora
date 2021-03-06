package co.casterlabs.sora.plugins.websocket;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import lombok.NonNull;

public class WebsocketProviderWrapper {
    private List<WebocketEndpointWrapper> endpoints;
    private WebsocketProvider provider;

    public WebsocketProviderWrapper(WebsocketProvider provider) {
        this.provider = provider;

        this.endpoints = WebocketEndpointWrapper.wrap(this.provider);
    }

    public @Nullable WebsocketListener serve(@NonNull WebsocketSession session) {
        for (WebocketEndpointWrapper endpoint : this.endpoints) {
            WebsocketListener listener = endpoint.serve(session);

            if (listener != null) {
                return listener;
            }
        }

        return null;
    }

}
