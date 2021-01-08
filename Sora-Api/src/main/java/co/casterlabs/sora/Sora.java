package co.casterlabs.sora;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import lombok.NonNull;

public interface Sora {

    public void addHttpProvider(@NonNull SoraPlugin plugin, @NonNull HttpProvider httpProvider);

    public void addWebsocketProvider(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider websocketProvider);

    public @Nullable SoraPlugin getPluginById(@NonNull String id);

}
