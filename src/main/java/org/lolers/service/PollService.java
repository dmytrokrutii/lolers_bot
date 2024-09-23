package org.lolers.service;

import org.lolers.model.InitMutePollPayload;

public interface PollService {

    void initMutePoll(InitMutePollPayload payload);

    void initGamePoll(long chatId, String question);
}
