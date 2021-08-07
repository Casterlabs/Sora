package co.casterlabs.sora.api;

import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketCloseCode;
import co.casterlabs.sora.Sora;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class SoraPlugin {
    private final @Getter FastLogger logger = new FastLogger(this.getName());

    private @Nullable URLClassLoader classLoader;
    private ServiceLoader<Driver> sqlDrivers;

    private List<Websocket> websockets = Collections.synchronizedList(new LinkedList<>());

    public final List<Websocket> getWebsockets() {
        return new ArrayList<>(this.websockets);
    }

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

        // Unload the SQL Drivers.
        for (Driver driver : this.sqlDrivers) {
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException ignored) {}
        }
    }

}
