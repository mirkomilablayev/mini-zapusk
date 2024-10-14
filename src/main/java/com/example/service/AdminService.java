package com.example.service;

import com.example.entity.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface AdminService {
    SendMessage adminMainMenu(User user, Update update);
    SendMessage askForAds(User user, Update update);
    SendMessage showAdsToCheck(User user, Update update);
    SendMessage sendAds(User user, Update update);
}
