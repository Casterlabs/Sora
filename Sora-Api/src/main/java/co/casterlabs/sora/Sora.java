package co.casterlabs.sora;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.websockets.WebsocketPreProcessor;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import lombok.NonNull;

public interface Sora {

    public void addHttpProvider(@NonNull SoraPlugin plugin, @NonNull HttpProvider httpProvider);

    public void addWebsocketProvider(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider websocketProvider);

    public void registerHttpPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull HttpPreProcessor<?> httpPreProcessor);

    public void registerWebsocketPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull WebsocketPreProcessor<?> websocketPreProcessor);

    public @Nullable SoraPlugin getPluginById(@NonNull String id);

    /**
     * @deprecated This is only to be used internally, using it yourself will get
     *             you nowhere.
     */
    @Deprecated
    public ClassLoader getGlobalClassLoader();

}
