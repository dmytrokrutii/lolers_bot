package org.lolers.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.lolers.service.MessageService;
import org.telegram.telegrambots.meta.api.objects.Update;

@Singleton
public class HelpCommand implements Command {
    private final Provider<MessageService> provider;
    private static final String INFO = """
            <b> Доступні наступні команди: </b>
            <code>/all</code> - тегнути всіх
            <code>/all запитання</code> - тегнути всіх та створити опитування з запитанням
            <code>/mute @user time</code> - створити голосування за мут користувача терміном від 1 до 60 хвилин
            <code>/unmute @user</code> - розмутити користувача
            """;

    @Inject
    public HelpCommand(Provider<MessageService> provider) {
        this.provider = provider;
    }

    @Override
    public void execute(Update update) {
        provider.get().replyOnMessage(update.getMessage().getChatId(), update.getMessage().getMessageId(), INFO, true);
    }
}
