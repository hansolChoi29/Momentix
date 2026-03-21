package com.example.momentix.domain.notification.slackdemo;


import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsOpenRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsOpenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

import static com.example.momentix.domain.common.exception.event.EventErrorCode.*;


@Service
public class SlackService {
// 제외대상임

    private final String botToken;

    private final Slack slack = Slack.getInstance();

    public SlackService(@Value("${slack.bot-token}") String botToken) {
        this.botToken = botToken;
    }

    public void send(String slackId, String message) throws IOException, SlackApiException {

        //줄 바꿈,스페이스, " 에러 방지
        String token = botToken.trim();


        //DM 열기
        ConversationsOpenResponse openResponse =
                slack.methods(token)
                        .conversationsOpen(
                                ConversationsOpenRequest.builder()
                                        .users(Arrays.asList(slackId))
                                        .build());

        if (!openResponse.isOk()) {
            throw new EventErrorException(DM_OPEN_FAILED);
        }

        String channelId = openResponse.getChannel().getId();

        //DM 채널에 Message 보내기
        ChatPostMessageResponse messageResponse =
                slack.methods(token)
                        .chatPostMessage(
                                ChatPostMessageRequest.builder()
                                        .channel(channelId)
                                        .text(message)
                                        .build());

        if (!messageResponse.isOk()) {
            throw new EventErrorException(MESSAGE_SEND_FAILED);
        }


    }

}
