package co.casterlabs.sora;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import co.casterlabs.sora.networking.nano.NanoServer;
import lombok.SneakyThrows;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
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

    public static void main(String[] args) throws IOException, InterruptedException {
        new CommandLine(new SoraLauncher()).execute(args);
    }

    @SneakyThrows
    @Override
    public void run() {
        if (this.debug) {
            FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        }

        SoraFramework framework = new SoraFramework(new NanoServer(this.bindAddress, this.port));

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
