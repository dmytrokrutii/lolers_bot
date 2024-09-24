package org.lolers.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import org.lolers.command.*;
import org.lolers.event.UpdateReceivedEventHandler;
import org.lolers.infrastructure.Bot;
import org.lolers.infrastructure.EventBus;
import org.lolers.infrastructure.LolersApi;
import org.lolers.infrastructure.listener.PostConstructTypeListener;
import org.lolers.service.CleanerService;
import org.lolers.service.MessageService;
import org.lolers.service.MuteService;
import org.lolers.service.PollService;
import org.lolers.service.impl.CleanServiceImpl;
import org.lolers.service.impl.MessageServiceImpl;
import org.lolers.service.impl.MuteServiceImpl;
import org.lolers.service.impl.PollServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppModule.class.getName());

    @Override
    protected void configure() {
        LOGGER.info("configure:: Starting Guice");

        bindListener(Matchers.any(), new PostConstructTypeListener());

        var commandBinder = Multibinder.newSetBinder(binder(), Command.class);
        commandBinder.addBinding().to(MuteCommand.class);
        commandBinder.addBinding().to(TagCommand.class);
        commandBinder.addBinding().to(UnmuteCommand.class);
        commandBinder.addBinding().to(HelpCommand.class);

        bind(UpdateReceivedEventHandler.class);
        bind(EventBus.class);
        bind(CommandsConfig.class);
        bind(UpdateReceivedEventHandler.class);
        bind(CommandInvoker.class);
        bind(MuteService.class).to(MuteServiceImpl.class);
        bind(MessageService.class).to(MessageServiceImpl.class);
        bind(CleanerService.class).to(CleanServiceImpl.class);
        bind(PollService.class).to(PollServiceImpl.class);
        bind(Bot.class);
        bind(LolersApi.class);
    }
}
