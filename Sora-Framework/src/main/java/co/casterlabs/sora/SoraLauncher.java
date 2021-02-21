package co.casterlabs.sora;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import co.casterlabs.sora.networking.Server;
import co.casterlabs.sora.networking.nano.NanoServer;
import co.casterlabs.sora.networking.undertow.UndertowServer;
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
    private String bindAddress;

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
    private ServerImplementation implementation = ServerImplementation.NANO;

    public static void main(String[] args) throws IOException, InterruptedException {
        new CommandLine(new SoraLauncher()).execute(args);
    }

    @SneakyThrows
    @Override
    public void run() {
        if (this.debug) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        Server server;

        switch (this.implementation) {
            case UNDERTOW:
                FastLogger.logStatic("Using Undertow as the server implementation.");
                server = new UndertowServer(this.bindAddress, this.port);
                break;

            case NANO:
            default:
                FastLogger.logStatic("Using Nano as the server implementation.");
                server = new NanoServer(this.bindAddress, this.port);
                break;

        }

        SoraFramework framework = new SoraFramework(server);

        File pluginsDir = new File("./plugins");

        pluginsDir.mkdir();

        for (File file : pluginsDir.listFiles()) {
            if (file.isFile()) {
                framework.getSora().loadPluginFile(file);
            }
        }

        server.start();

        SoraFramework.LOGGER.info("(Http) Sora bound to %d", this.port);

        Scanner in = new Scanner(System.in);

        while (in.hasNext()) {
            SoraCommands.execute(in.nextLine());
        }

        in.close();
    }

    public static enum ServerImplementation {
        NANO,
        UNDERTOW;

    }

}
