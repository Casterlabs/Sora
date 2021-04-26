package co.casterlabs.sora;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import co.casterlabs.rakurai.io.http.server.HttpServerBuilder;
import co.casterlabs.rakurai.io.http.server.HttpServerImplementation;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

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
        FastLogger.logStatic(LogLevel.WARNING, "Due to some inefficiencies in the plugin loading system, Sora will consume more ram than is actually required.");
        FastLogger.logStatic(LogLevel.WARNING, "It is recommended you use aggressive garbage collection tunings like these:");
        FastLogger.logStatic(LogLevel.WARNING, "-XX:GCTimeRatio=19 -XX:MinHeapFreeRatio=20 -XX:MaxHeapFreeRatio=30\n");

        new CommandLine(new SoraLauncher()).execute(args);
    }

    @SneakyThrows
    @Override
    public void run() {
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

        SoraFramework framework = new SoraFramework(builder);

        File pluginsDir = new File("./plugins");

        pluginsDir.mkdir();

        for (File file : pluginsDir.listFiles()) {
            if (file.isFile()) {
                framework.getSora().loadPluginFile(file);
            }
        }

        framework.getServer().start();

        SoraFramework.LOGGER.info("(Http) Sora bound to %d", this.port);

        Scanner in = new Scanner(System.in);

        while (in.hasNext()) {
            SoraCommands.execute(in.nextLine());
        }

        in.close();
    }

}
