package co.casterlabs.sora.api;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketCloseCode;
import co.casterlabs.sora.Sora;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class SoraPlugin {
    private final @Getter FastLogger logger = new FastLogger(this.getName());

    private @Getter List<Websocket> websockets = new LinkedList<>();
    private @Nullable URLClassLoader classLoader;

    public abstract void onInit(Sora sora);

    public abstract void onClose();

    public abstract @Nullable String getVersion();

    public abstract @Nullable String getAuthor();

    public abstract @NonNull String getName();

    public abstract @NonNull String getId();

    public final @Nullable URLClassLoader getClassLoader() {
        return this.classLoader;
    }

    public final void close() {
        for (Websocket websocket : new ArrayList<>(this.websockets)) {
            try {
                websocket.close(WebsocketCloseCode.GOING_AWAY);
            } catch (Throwable ignored) {}
        }

        this.onClose();
    }

}
