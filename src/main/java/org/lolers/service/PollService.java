package org.lolers.service;

import org.lolers.dto.InitMutePollPayload;

public interface PollService {

    void initMutePoll(InitMutePollPayload payload);

    void initPoll(long chatId, String question);
}
