package org.lolers.service;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface CleanerService {

     void clean(Message message);
}
