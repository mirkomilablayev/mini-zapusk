package com.example.service;

import com.example.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserService {
    SendMessage askContact(User user);

    SendMessage saveContactAndNext(User user, Update update);

    SendMessage saveFullNameAndNext(User user, Update update);

    SendMessage saveEmploymentActivityAndNext(User user, Update update);

    SendMessage saveEmployeeCountAndNext(User user, Update update, Boolean isSubscribed, String channelUsername);

    SendMessage showChannelAndNext(User user, Update update, String channelUsername, Boolean isSubscribed);

    SendMessage saveCardNumberAndNext(User user, Update update);

    SendMessage saveCardExpAndNext(User user, Update update);

    SendMessage payAndNext(User user, Update update);

    SendDocument sendGiftFile(User user, Update update);

    boolean isSubscribed(String channelUsername, String botToken, String chatId);

    User createOrGetUser(Update update);

    String getText(Update update);
}
