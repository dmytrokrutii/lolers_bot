package org.lolers.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import org.lolers.command.*;
import org.lolers.event.UpdateReceivedEventHandler;
import org.lolers.infrastructure.Bot;
import org.lolers.infrastructure.EventBus;
import org.lolers.infrastructure.LolersApi;
import org.lolers.infrastructure.Mapper;
import org.lolers.infrastructure.listener.PostConstructTypeListener;
import org.lolers.service.CleanerService;
import org.lolers.service.MessageService;
import org.lolers.service.MuteService;
import org.lolers.service.PollService;
import org.lolers.service.impl.CleanServiceImpl;
import org.lolers.service.impl.MessageServiceImpl;
import org.lolers.service.impl.MuteServiceImpl;
import org.lolers.service.impl.PollServiceImpl;
import org.lolers.storage.MutedUserStorage;
import org.lolers.storage.PollStorage;
import org.lolers.storage.UserStorage;
import org.lolers.storage.entity.Rating;
import org.lolers.storage.entity.User;
import org.lolers.storage.repository.RatingRepository;
import org.lolers.storage.repository.SupabaseClient;
import org.lolers.storage.repository.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class AppModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppModule.class.getName());

    @Override
    protected void configure() {
        LOGGER.info("configure:: Starting Guice");

        //commands
        var commandBinder = Multibinder.newSetBinder(binder(), Command.class);
        commandBinder.addBinding().to(MuteCommand.class);
        commandBinder.addBinding().to(TagCommand.class);
        commandBinder.addBinding().to(UnmuteCommand.class);
        commandBinder.addBinding().to(HelpCommand.class);
        commandBinder.addBinding().to(RandomCommand.class);

        //domain
        bind(UpdateReceivedEventHandler.class);
        bind(EventBus.class);
        bind(UpdateReceivedEventHandler.class);
        bind(MuteService.class).to(MuteServiceImpl.class);
        bind(MessageService.class).to(MessageServiceImpl.class);
        bind(CleanerService.class).to(CleanServiceImpl.class);
        bind(PollService.class).to(PollServiceImpl.class);
        bind(Bot.class);
        bind(LolersApi.class);
        bind(Mapper.class);

        //configs
        bindListener(Matchers.any(), new PostConstructTypeListener());

        //Storage
        bind(MutedUserStorage.class);
        bind(PollStorage.class);
        bind(RatingRepository.class);

        //API DB clients
        bind(new TypeLiteral<SupabaseClient<Rating>>() {
        });
    }

    @Provides
    public CommandInvoker provideCommandInvoker(MuteCommand muteCommand, UnmuteCommand unmuteCommand,
                                                TagCommand tagCommand, HelpCommand helpCommand, RandomCommand randomCommand) {
        var commands = List.of(muteCommand, unmuteCommand, tagCommand, helpCommand, randomCommand);
        var log = commands.stream()
                .map(Command::getInvoker)
                .collect(Collectors.joining(" "));
        LOGGER.info("provideCommandInvoker:: Register commands: {}", log);
        var invoker = new CommandInvoker();
        invoker.register(commands);
        return invoker;
    }

    @Provides
    public UserStorage provideUserStorage(SupabaseClient<User> client) {
        var users = client.get(Table.USERS, User[].class);
        LOGGER.info("provideUserStorage:: loaded {} users", users.size());
        return new UserStorage(users);
    }

    @Provides
    @Named("db.url")
    public String provideDbUrl() {
        return System.getenv("DB_URL"); // Get from environment variable
    }

    @Provides
    @Named("db.key")
    public String provideDbPassword() {
        return System.getenv("DB_KEY");
    }

    @Provides
    @Named("bot.token")
    public String provideBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Provides
    @Named("bot.username")
    public String provideBotName() {
        return System.getenv("BOT_USERNAME");
    }
}
