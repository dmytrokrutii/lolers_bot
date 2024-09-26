package org.lolers.infrastructure.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.lolers.command.*;

import javax.annotation.PostConstruct;

@Singleton
public class CommandsConfig {
    private final CommandInvoker invoker;
    private final MuteCommand muteCommand;
    private final TagCommand tagCommand;
    private final UnmuteCommand unmuteCommand;
    private final HelpCommand helpCommand;
    private final RandomCommand randomCommand;

    @Inject
    public CommandsConfig(CommandInvoker invoker, MuteCommand muteCommand, UnmuteCommand unmuteCommand, TagCommand tagCommand, HelpCommand helpCommand, RandomCommand randomCommand) {
        this.invoker = invoker;
        this.muteCommand = muteCommand;
        this.unmuteCommand = unmuteCommand;
        this.tagCommand = tagCommand;
        this.helpCommand = helpCommand;
        this.randomCommand = randomCommand;
    }

    @PostConstruct
    public void init() {
        invoker.register("/mute", muteCommand);
        invoker.register("/unmute", unmuteCommand);
        invoker.register("/all", tagCommand);
        invoker.register("/help", helpCommand);
        invoker.register("/r", randomCommand);
    }
}
