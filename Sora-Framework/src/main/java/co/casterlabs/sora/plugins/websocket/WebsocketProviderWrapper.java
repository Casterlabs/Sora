package co.casterlabs.sora.plugins.websocket;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.plugins.SoraPlugins;
import co.casterlabs.sora.plugins.websocket.WebocketEndpointWrapper.WebsocketListenerPluginPair;
import lombok.NonNull;

public class WebsocketProviderWrapper {
    private List<WebocketEndpointWrapper> endpoints;
    private WebsocketProvider provider;
    private SoraPlugin plugin;

    public WebsocketProviderWrapper(SoraPlugin plugin, WebsocketProvider provider, SoraPlugins sora) {
        this.provider = provider;
        this.plugin = plugin;

        this.endpoints = WebocketEndpointWrapper.wrap(this.plugin, this.provider, sora);
    }

    public @Nullable WebsocketListenerPluginPair serve(@NonNull WebsocketSession session) {
        for (WebocketEndpointWrapper endpoint : this.endpoints) {
            WebsocketListenerPluginPair pair = endpoint.serve(session);

            if (pair != null) {
                return pair;
            }
        }

        return null;
    }

}
