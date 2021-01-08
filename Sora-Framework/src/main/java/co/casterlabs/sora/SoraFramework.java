package co.casterlabs.sora;

import co.casterlabs.sora.networking.Server;
import co.casterlabs.sora.plugins.SoraPlugins;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
public class SoraFramework {
    public static final FastLogger LOGGER = new FastLogger();

    private static @Getter SoraFramework instance;

    private SoraPlugins sora = new SoraPlugins();
    private Server server;

    public SoraFramework(Server server) {
        if (instance == null) {
            this.server = server;

            instance = this;
        } else {
            throw new IllegalStateException("SoraFramework has already been initialized, use SoraFramework.getInstance() to get the current instance.");
        }
    }

}
