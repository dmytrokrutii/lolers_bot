package org.lolers.command;

import com.google.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
public class CommandInvoker {
    private static final Logger LOGGER = Logger.getLogger(CommandInvoker.class.getName());
    private static final Map<String, Command> commands = new HashMap<>();

    public void register(String invoker, Command command) {
        commands.put(invoker, command);
    }

    public void execute(String invoker, Update update) {
        Optional.ofNullable(commands.get(invoker))
                .ifPresentOrElse(cmd -> cmd.execute(update), () -> LOGGER.warning("Unknown command"));
    }
}
