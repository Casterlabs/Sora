package co.casterlabs.sora;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import co.casterlabs.sora.api.SoraPlugin;
import lombok.SneakyThrows;
import xyz.e3ndr.consolidate.CommandEvent;
import xyz.e3ndr.consolidate.CommandRegistry;
import xyz.e3ndr.consolidate.Resolver;
import xyz.e3ndr.consolidate.command.Command;
import xyz.e3ndr.consolidate.command.CommandListener;
import xyz.e3ndr.consolidate.exception.ArgumentsLengthException;
import xyz.e3ndr.consolidate.exception.CommandExecutionException;
import xyz.e3ndr.consolidate.exception.CommandNameException;

public class SoraCommands implements CommandListener<Void> {
    private static CommandRegistry<Void> registry = new CommandRegistry<>();

    static {
        registry.addCommand(new SoraCommands());

        registry.addResolver(new Resolver<File>() {
            @Override
            public File resolve(String value) throws IllegalArgumentException {
                return new File(value);
            }
        }, File.class);
    }

    @Command(name = "help", description = "Shows this page.")
    public void onHelpCommand(CommandEvent<Void> event) {
        Collection<Command> commands = registry.getCommands();
        StringBuilder sb = new StringBuilder();

        sb.append("All available commands:");

        for (Command c : commands) {
            sb.append("\n\t").append(c.name()).append(": ").append(c.description());
        }

        SoraFramework.LOGGER.info(sb);
    }

    @Command(name = "stop", description = "Stops the server.")
    public void onStopCommand(CommandEvent<Void> event) throws IOException {
        SoraFramework.getInstance().getServer().stop();
        SoraFramework.LOGGER.info("Server stopped.");
    }

    @Command(name = "start", description = "Starts the server.")
    public void onStartCommand(CommandEvent<Void> event) throws IOException {
        try {
            SoraFramework.getInstance().getServer().start();
        } catch (IOException e) {
            if (e.getMessage().contains("JVM_BIND")) {
                SoraFramework.LOGGER.severe("Server already started!");
            }
        }
    }

    @Command(name = "plugins", description = "Lists all plugins loaded.")
    public void onPluginsCommand(CommandEvent<Void> event) {
        StringBuilder sb = new StringBuilder();

        sb.append("All plugins:");

        for (SoraPlugin plugin : SoraFramework.getInstance().getSora().getPlugins()) {
            sb.append("\n\t").append(plugin.getName()).append(':').append(plugin.getAuthor()).append(" (").append(plugin.getId()).append(')');
        }

        SoraFramework.LOGGER.info(sb);
    }

    @Command(name = "unload", description = "Unloads a specified plugin.", minimumArguments = 1)
    public void onUnloadCommand(CommandEvent<Void> event) {
        try {
            SoraFramework.getInstance().getSora().unregister(event.getArgs().get(0));
            SoraFramework.LOGGER.info("Unloaded plugin: %s", event.getArgs().get(0));
        } catch (IllegalStateException e) {
            SoraFramework.LOGGER.severe(e.getMessage());
        }
    }

    @Command(name = "load", description = "Loads a specified plugin file.", minimumArguments = 1)
    public void onLoadCommand(CommandEvent<Void> event) {
        try {
            File file = new File("plugins", event.getArgs().get(0));

            SoraFramework.getInstance().getSora().loadPluginFile(file);
            SoraFramework.LOGGER.info("Loaded plugin file: %s", file);
        } catch (IOException e) {
            SoraFramework.LOGGER.severe(e.getMessage());
        }
    }

    @SneakyThrows
    public static void execute(String input) {
        try {
            registry.execute(input);
        } catch (CommandNameException e) {
            SoraFramework.LOGGER.info("Unknown command: %s", input);
        } catch (ArgumentsLengthException e) {
            SoraFramework.LOGGER.info("Incorrect amount of arguments for command input: %s", input);
        } catch (CommandExecutionException e) {
            SoraFramework.LOGGER.exception(e.getCause().getCause());
        }
    }

}
