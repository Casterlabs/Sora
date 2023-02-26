package co.casterlabs.sora;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.websockets.WebsocketPreProcessor;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import lombok.NonNull;

public interface Sora {

    /**
     * provider must extend {@link HttpProvider} or {@link WebsocketProvider} or
     * both to be properly registered.
     */
    default void addProvider(@NonNull SoraPlugin plugin, @NonNull Object provider) {
        if (provider instanceof HttpProvider) {
            this.addHttpProvider(plugin, (HttpProvider) provider);
        }
        if (provider instanceof WebsocketProvider) {
            this.addWebsocketProvider(plugin, (WebsocketProvider) provider);
        }
    }

    /**
     * preProcessor must extend {@link HttpPreProcessor} or
     * {@link WebsocketPreProcessor} or both to be properly registered.
     */
    default void registerPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull Object preProcessor) {
        if (preProcessor instanceof HttpPreProcessor) {
            this.registerHttpPreProcessor(plugin, id, (HttpPreProcessor<?>) preProcessor);
        }
        if (preProcessor instanceof WebsocketPreProcessor) {
            this.registerWebsocketPreProcessor(plugin, id, (WebsocketPreProcessor<?>) preProcessor);
        }
    }

    public @Nullable SoraPlugin getPluginById(@NonNull String id);

    /**
     * @deprecated This is only to be used internally, using it yourself will get
     *             you nowhere.
     */
    @Deprecated
    public ClassLoader getGlobalClassLoader();

    /* ---------------- */
    /* Deprecated       */
    /* ---------------- */

    /**
     * @deprecated Use {@link #addProvider(SoraPlugin, Object)}
     */
    @Deprecated
    public void addHttpProvider(@NonNull SoraPlugin plugin, @NonNull HttpProvider httpProvider);

    /**
     * @deprecated Use {@link #addProvider(SoraPlugin, Object)}
     */
    @Deprecated
    public void addWebsocketProvider(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider websocketProvider);

    /**
     * @deprecated Use {@link #registerPreProcessor(SoraPlugin, String, Object)}
     */
    @Deprecated
    public void registerHttpPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull HttpPreProcessor<?> httpPreProcessor);

    /**
     * @deprecated Use {@link #registerPreProcessor(SoraPlugin, String, Object)}
     */
    @Deprecated
    public void registerWebsocketPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull WebsocketPreProcessor<?> websocketPreProcessor);

}
