package co.casterlabs.sora.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.server.HttpListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.plugins.http.HttpProviderWrapper;
import co.casterlabs.sora.plugins.websocket.WebsocketProviderWrapper;
import lombok.NonNull;

public class SoraPlugins implements Sora, HttpListener {
    private Map<String, SoraPlugin> plugins = new HashMap<>();

    private Map<String, List<WebsocketProviderWrapper>> pluginWebsocketWrappers = new ConcurrentHashMap<>();
    private Map<String, List<HttpProviderWrapper>> pluginHttpWrappers = new ConcurrentHashMap<>();

    // We maintain these two based off of the maps,
    // these are here for performance reasons.
    private List<WebsocketProviderWrapper> websocketWrappers = new ArrayList<>();
    private List<HttpProviderWrapper> httpWrappers = new ArrayList<>();

    @Override
    public void addHttpProvider(@NonNull SoraPlugin plugin, @NonNull HttpProvider httpProvider) {
        HttpProviderWrapper wrapper = new HttpProviderWrapper(httpProvider);

        this.pluginHttpWrappers.get(plugin.getId()).add(wrapper);
        this.httpWrappers.add(wrapper);
    }

    @Override
    public void addWebsocketProvider(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider websocketProvider) {
        WebsocketProviderWrapper wrapper = new WebsocketProviderWrapper(websocketProvider);

        this.pluginWebsocketWrappers.get(plugin.getId()).add(wrapper);
        this.websocketWrappers.add(wrapper);
    }

    public List<SoraPlugin> getPlugins() {
        return new ArrayList<>(this.plugins.values());
    }

    @Override
    public @Nullable SoraPlugin getPluginById(@NonNull String id) {
        return this.plugins.get(id);
    }

    public void loadPluginFile(File file) throws IOException {
        List<SoraPlugin> toLoad = PluginLoader.loadFile(this, file);

        for (SoraPlugin plugin : toLoad) {
            this.register(plugin);
        }
    }

    public void register(@NonNull SoraPlugin plugin) {
        String id = plugin.getId();

        if (this.plugins.containsKey(id)) {
            throw new IllegalStateException(id + " is already registered.");
        } else {
            this.plugins.put(id, plugin);

            this.pluginWebsocketWrappers.put(id, new ArrayList<>());
            this.pluginHttpWrappers.put(id, new ArrayList<>());

            SoraFramework.LOGGER.info("Loaded plugin %s:%s (%s)", plugin.getName(), plugin.getAuthor(), id);

            plugin.onInit(this);
        }
    }

    public void unregister(@NonNull String id) {
        if (this.plugins.containsKey(id)) {
            List<WebsocketProviderWrapper> websocketProviders = this.pluginWebsocketWrappers.remove(id);
            List<HttpProviderWrapper> httpProviders = this.pluginHttpWrappers.remove(id);

            this.websocketWrappers.removeAll(websocketProviders);
            this.httpWrappers.removeAll(httpProviders);

            SoraPlugin plugin = this.plugins.remove(id);
            URLClassLoader loader = plugin.getClassLoader();

            try {
                plugin.onClose();
                loader.close();
            } catch (Throwable ignored) {}

            // Important for the GC sweep to remove the class loader.
            websocketProviders = null;
            httpProviders = null;
            plugin = null;
            loader = null;

            System.gc();
        } else {
            throw new IllegalStateException(id + " is not registered.");
        }
    }

    public void unregisterAll() {
        for (String id : this.plugins.keySet().toArray(new String[0])) {
            this.unregister(id);
        }
    }

    @Override
    public @Nullable HttpResponse serveSession(@NonNull String host, @NonNull HttpSession session, boolean secure) {
        HttpProviderWrapper[] wrappers = this.httpWrappers.toArray(new HttpProviderWrapper[0]);

        for (HttpProviderWrapper wrapper : wrappers) {
            HttpResponse response = wrapper.serve(session);

            if (response != null) {
                return response;
            }
        }

        // Try to return a default response.
        for (HttpProviderWrapper wrapper : wrappers) {
            HttpResponse response = wrapper.onNoProvider(session);

            if (response != null) {
                return response;
            }
        }

        return null;
    }

    @Override
    public @Nullable WebsocketListener serveWebsocketSession(@NonNull String host, @NonNull WebsocketSession session, boolean secure) {
        for (WebsocketProviderWrapper wrapper : this.websocketWrappers.toArray(new WebsocketProviderWrapper[0])) {
            WebsocketListener listener = wrapper.serve(session);

            if (listener != null) {
                return listener;
            }
        }

        return null;
    }

}
