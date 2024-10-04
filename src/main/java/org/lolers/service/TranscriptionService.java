package org.lolers.service;

public interface TranscriptionService {
    void transcribe(String field, long chatId, int messageId);
}
