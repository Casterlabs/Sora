package co.casterlabs.sora.plugins.websocket;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.websockets.WebsocketListener;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.WebsocketSession;
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
