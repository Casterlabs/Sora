package co.casterlabs.sora;

import java.io.IOException;

import co.casterlabs.rakurai.io.http.server.HttpServer;
import co.casterlabs.rakurai.io.http.server.HttpServerBuilder;
import co.casterlabs.sora.plugins.SoraPlugins;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
public class SoraFramework {
    public static final FastLogger LOGGER = new FastLogger();

    private static @Getter SoraFramework instance;

    private SoraPlugins sora = new SoraPlugins();
    private HttpServer server;

    public SoraFramework(HttpServerBuilder builder) throws IOException {
        if (instance == null) {
            this.server = builder.build(this.sora);

            instance = this;
        } else {
            throw new IllegalStateException("SoraFramework has already been initialized, use SoraFramework.getInstance() to get the current instance.");
        }
    }

}
