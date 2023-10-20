package co.casterlabs.sora;

import java.io.IOException;

import co.casterlabs.rhs.server.HttpServer;
import co.casterlabs.rhs.server.HttpServerBuilder;
import co.casterlabs.sora.plugins.SoraPlugins;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
public class SoraFramework {
    public static final FastLogger LOGGER = new FastLogger();

    private static @Getter SoraFramework instance;

    private SoraPlugins sora = new SoraPlugins();
    private HttpServer server;

    public static long httpSessionsServed = 0;
    public static long httpSessionsFailed = 0;
    public static long websocketSessionsServed = 0;
    public static long websocketSessionsFailed = 0;

    public SoraFramework(HttpServerBuilder builder) throws IOException {
        if (instance == null) {
            this.server = builder.build(this.sora);

            instance = this;
        } else {
            throw new IllegalStateException("SoraFramework has already been initialized, use SoraFramework.getInstance() to get the current instance.");
        }
    }

    public void startHttpServer() throws IOException {
        this.server.start();

        SoraFramework.LOGGER.info("(Http) Sora bound to %d", this.server.getPort());
    }

}
