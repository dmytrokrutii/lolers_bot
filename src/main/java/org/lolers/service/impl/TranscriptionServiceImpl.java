package org.lolers.service.impl;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.TranscriptLanguageCode;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.lolers.service.MessageService;
import org.lolers.service.TranscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class TranscriptionServiceImpl implements TranscriptionService {
    private final static Logger LOGGER = LoggerFactory.getLogger(TranscriptionServiceImpl.class.getName());
    private final static String FAILED_TRANSCRIPTION_MESSAGE = "<b>❌ Викнила помилка при обробці команди</b>";
    private final static String TEMPLATE = "\uD83D\uDD0A Результат транскрипції: \n <i>%s</i>";

    private final AssemblyAI client;
    private final Provider<MessageService> messageService;

    @Inject
    public TranscriptionServiceImpl(Provider<MessageService> provider, @Named("assembly.key") String apiKey) {
        this.messageService = provider;
        this.client = AssemblyAI.builder()
                .apiKey(apiKey)
                .build();
    }

    @Override
    public void transcribe(String field, long chatId, int messageId) {
        LOGGER.info("transcribe:: Starting transcription for field {}", field);
        try {
            LOGGER.info("transcribe:: Getting file url...");
            var url = messageService.get().getFileUrl(field);
            getTranscription(url)
                    .map(transcription -> String.format(TEMPLATE, transcription))
                    .ifPresent(message -> messageService.get().replyOnMessage(chatId, messageId, message, true));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            messageService.get().replyOnMessage(chatId, messageId, FAILED_TRANSCRIPTION_MESSAGE, true);
        }
    }

    private Optional<String> getTranscription(String url) {
        LOGGER.info("transcribe:: Start transcription for file {}", url);
        var transcriptParams = TranscriptParams.builder()
                .audioUrl(url)
                .languageCode(TranscriptLanguageCode.UK)
                .build();

        var transcript = client.transcripts().transcribe(transcriptParams);
        if (transcript.getStatus() == TranscriptStatus.ERROR) {
            throw new RuntimeException("Transcript failed with error: " + transcript.getError().orElse(transcript.getStatus().toString()));
        }
        return transcript.getText();
    }
}
