package com.example.service.impl;

import com.example.entity.User;
import com.example.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    @Override
    public SendMessage adminMainMenu(User user, Update update) {
        return null;
    }

    @Override
    public SendMessage askForAds(User user, Update update) {
        return null;
    }

    @Override
    public SendMessage showAdsToCheck(User user, Update update) {
        return null;
    }

    @Override
    public SendMessage sendAds(User user, Update update) {
        return null;
    }
}
