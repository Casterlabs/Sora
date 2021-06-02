package co.casterlabs.sora;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import co.casterlabs.rakurai.io.http.server.HttpServerBuilder;
import co.casterlabs.rakurai.io.http.server.HttpServerImplementation;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Data
@Accessors(chain = true)
@Command(name = "sora", mixinStandardHelpOptions = true, version = "Sora", description = "Starts Sora")
public class SoraLauncher implements Runnable {

    @Option(names = {
            "-b",
            "--bind"
    }, description = "The address to bind on")
    private String bindAddress = "0.0.0.0";

    @Option(names = {
            "-p",
            "--port"
    }, description = "The port to bind to")
    private int port = 8080;

    @Option(names = {
            "-d",
            "--debug"
    }, description = "Enables debugging")
    private boolean debug = false;

    @Option(names = {
            "-s",
            "--server-implementation"
    }, description = "Sets the desired server implementation")
    private HttpServerImplementation implementation = HttpServerImplementation.NANO;

    public static void main(String[] args) throws IOException, InterruptedException {
        new CommandLine(new SoraLauncher()).execute(args);
    }

    /**
     * This is used by the command line interface to start Sora with plugins
     * ({@link #buildWithPluginLoader()}) and to listen for console input.
     */
    @SneakyThrows
    @Deprecated
    @Override
    public void run() {
        SoraFramework framework = this.buildWithPluginLoader();

        framework.startHttpServer();

        try (Scanner in = new Scanner(System.in)) {

            while (in.hasNext()) {
                SoraCommands.execute(in.nextLine());
            }
        }
    }

    public SoraFramework buildWithoutPluginLoader() throws IOException {
        if (this.debug) {
            FastLoggingFramework.setDefaultLevel(LogLevel.TRACE);
        }

        HttpServerBuilder builder;

        switch (this.implementation) {
            case UNDERTOW:
                FastLogger.logStatic("Using Undertow as the server implementation.");
                builder = HttpServerBuilder.getUndertowBuilder();
                break;

            case NANO:
            default:
                FastLogger.logStatic("Using Nano as the server implementation.");
                builder = HttpServerBuilder.getNanoBuilder();
                break;

        }

        builder.setHostname(this.bindAddress);
        builder.setPort(this.port);

        return new SoraFramework(builder);
    }

    public SoraFramework buildWithPluginLoader() throws IOException {
        FastLogger.logStatic(LogLevel.WARNING, "Due to some inefficiencies in the plugin loading system, Sora will consume more ram than is actually required.");
        FastLogger.logStatic(LogLevel.WARNING, "It is recommended you use aggressive garbage collection tunings like these:");
        FastLogger.logStatic(LogLevel.WARNING, "-XX:GCTimeRatio=19 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30\n");

        SoraFramework framework = this.buildWithoutPluginLoader();

        File pluginsDir = new File("./plugins");

        pluginsDir.mkdir();

        for (File file : pluginsDir.listFiles()) {
            if (file.isFile()) {
                framework.getSora().loadPluginFile(file);
            }
        }

        return framework;
    }

}
