package co.casterlabs.sora.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.DropConnectionException;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpListener;
import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketFrame;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraBasicRequestPreProcessor;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpPreProcessor;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.websockets.WebsocketPreProcessor;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.plugins.http.HttpProviderWrapper;
import co.casterlabs.sora.plugins.websocket.WebocketEndpointWrapper.WebsocketListenerPluginPair;
import co.casterlabs.sora.plugins.websocket.WebsocketProviderWrapper;
import lombok.NonNull;

public class SoraPlugins implements Sora, HttpListener {
    private Map<String, SoraPlugin> plugins = new HashMap<>();

    private Map<String, List<WebsocketProviderWrapper>> pluginWebsocketWrappers = new ConcurrentHashMap<>();
    private Map<String, List<HttpProviderWrapper>> pluginHttpWrappers = new ConcurrentHashMap<>();

    private Map<String, List<String>> pluginWebsocketPreProcessors = new ConcurrentHashMap<>();
    private Map<String, List<String>> pluginHttpPreProcessors = new ConcurrentHashMap<>();

    // We maintain these based off of the maps,
    // these are here for performance reasons.
    private List<WebsocketProviderWrapper> websocketWrappers = new ArrayList<>();
    private List<HttpProviderWrapper> httpWrappers = new ArrayList<>();

    private Map<String, WebsocketPreProcessor<?>> websocketPreProcessors = new ConcurrentHashMap<>();
    private Map<String, HttpPreProcessor<?>> httpPreProcessors = new ConcurrentHashMap<>();

    public SoraPlugins() {
        this.httpPreProcessors.put(SoraBasicRequestPreProcessor.ID, SoraBasicRequestPreProcessor.INSTANCE);
    }

    @Override
    public void addHttpProvider(@NonNull SoraPlugin plugin, @NonNull HttpProvider httpProvider) {
        HttpProviderWrapper wrapper = new HttpProviderWrapper(httpProvider, this);

        this.pluginHttpWrappers.get(plugin.getId()).add(wrapper);
        this.httpWrappers.add(wrapper);
    }

    @Override
    public void addWebsocketProvider(@NonNull SoraPlugin plugin, @NonNull WebsocketProvider websocketProvider) {
        WebsocketProviderWrapper wrapper = new WebsocketProviderWrapper(plugin, websocketProvider, this);

        this.pluginWebsocketWrappers.get(plugin.getId()).add(wrapper);
        this.websocketWrappers.add(wrapper);
    }

    @Override
    public void registerHttpPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull HttpPreProcessor<?> httpPreProcessor) {
        this.pluginHttpPreProcessors.get(plugin.getId()).add(id);
        this.httpPreProcessors.put(id, httpPreProcessor);
    }

    @Override
    public void registerWebsocketPreProcessor(@NonNull SoraPlugin plugin, @NonNull String id, @NonNull WebsocketPreProcessor<?> websocketPreProcessor) {
        this.pluginWebsocketPreProcessors.get(plugin.getId()).add(id);
        this.websocketPreProcessors.put(id, websocketPreProcessor);
    }

    public @Nullable HttpPreProcessor<?> getHttpPreProcessor(@NonNull String id) {
        return this.httpPreProcessors.get(id);
    }

    public @Nullable WebsocketPreProcessor<?> getWebsocketPreProcessor(@NonNull String id) {
        return this.websocketPreProcessors.get(id);
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
            this.pluginWebsocketPreProcessors.put(id, new ArrayList<>());
            this.pluginHttpPreProcessors.put(id, new ArrayList<>());

            SoraFramework.LOGGER.info("Loaded plugin %s:%s (%s)", plugin.getName(), plugin.getAuthor(), id);

            plugin.onInit(this);
        }
    }

    public void unregister(@NonNull String id) {
        if (this.plugins.containsKey(id)) {
            // Remove all wrappers and processors.
            this.websocketWrappers.removeAll(this.pluginWebsocketWrappers.remove(id));
            this.httpWrappers.removeAll(this.pluginHttpWrappers.remove(id));

            this.pluginWebsocketPreProcessors.remove(id).forEach(this.websocketPreProcessors::remove);
            this.pluginHttpPreProcessors.remove(id).forEach(this.httpPreProcessors::remove);

            // Unload the plugin.
            SoraPlugin plugin = this.plugins.remove(id);
            URLClassLoader loader = plugin.getClassLoader();

            try {
                plugin.close();
            } catch (Throwable ignored) {}

            try {
                loader.close();
            } catch (Throwable ignored) {}

            // Important for the GC sweep to remove the class loader.
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
        try {
            HttpProviderWrapper[] wrappers = this.httpWrappers.toArray(new HttpProviderWrapper[0]);

            for (HttpProviderWrapper wrapper : wrappers) {
                HttpResponse response = wrapper.serve(session);

                if (response != null) {
                    SoraFramework.httpSessionsServed++;
                    return response;
                }
            }

            // Try to return a default response.
            {
                SoraHttpSession soraSession = new SoraHttpSession(session, Collections.emptyMap());

                for (HttpProviderWrapper wrapper : wrappers) {
                    HttpResponse response = wrapper.onNoProvider(soraSession);

                    if (response != null) {
                        SoraFramework.httpSessionsServed++;
                        return response;
                    }
                }
            }

            SoraFramework.httpSessionsFailed++;
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_IMPLEMENTED, new byte[0]);
        } catch (DropConnectionException e) {
            SoraFramework.httpSessionsFailed++;
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable WebsocketListener serveWebsocketSession(@NonNull String host, @NonNull WebsocketSession session, boolean secure) {
        try {
            for (WebsocketProviderWrapper wrapper : this.websocketWrappers.toArray(new WebsocketProviderWrapper[0])) {
                WebsocketListenerPluginPair pair = wrapper.serve(session);

                if (pair != null) {
                    WebsocketListener original = pair.getListener();
                    SoraPlugin plugin = pair.getPlugin();

                    SoraFramework.websocketSessionsServed++;
                    return new WebsocketListener() {
                        private List<Websocket> websockets;

                        @Override
                        public void onOpen(Websocket websocket) {
                            // Try to get the websocket variable to push and pop on.
                            try {
                                Field websocketsField = SoraPlugin.class.getDeclaredField("websockets");
                                websocketsField.setAccessible(true);

                                this.websockets = (List<Websocket>) websocketsField.get(plugin);
                            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            if (this.websockets != null) {
                                this.websockets.add(websocket);
                            }

                            original.onOpen(websocket);
                        }

                        @Override
                        public void onClose(Websocket websocket) {
                            if (this.websockets != null) {
                                this.websockets.remove(websocket);
                            }

                            original.onClose(websocket);
                        }

                        @Override
                        public void onFrame(Websocket websocket, WebsocketFrame frame) {
                            original.onFrame(websocket, frame);
                        }

                    };
                }
            }

            SoraFramework.websocketSessionsFailed++;
            return null;
        } catch (DropConnectionException e) {
            SoraFramework.websocketSessionsFailed++;
            throw e;
        }
    }

}
