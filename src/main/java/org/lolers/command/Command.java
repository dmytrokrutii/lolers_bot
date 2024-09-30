package org.lolers.command;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    //language=HTML
    String FAILED_COMMAND_MESSAGE = "<b>❌ Викнила помилка при обробці команди</b>";

    void execute(Update update);

    String getInvoker();
}
