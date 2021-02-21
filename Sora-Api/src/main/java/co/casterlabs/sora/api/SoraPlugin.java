package co.casterlabs.sora.api;

import java.net.URLClassLoader;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.Sora;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class SoraPlugin {
    private final @Getter FastLogger logger = new FastLogger(this.getName());
    private URLClassLoader classLoader;

    public abstract void onInit(Sora sora);

    public abstract void onClose();

    public abstract @Nullable String getVersion();

    public abstract @Nullable String getAuthor();

    public abstract @NonNull String getName();

    public abstract @NonNull String getId();

    public final URLClassLoader getClassLoader() {
        return this.classLoader;
    }

}
