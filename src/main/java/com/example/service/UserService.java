package com.example.service;

import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.util.Steps;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public boolean isSubscribed(String channelUsername, String botToken, String chatId) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.telegram.org/bot" + botToken + "/getChatMember?chat_id=" + channelUsername + "&user_id=" + chatId).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                JSONObject jsonResponse = new JSONObject(new BufferedReader(new InputStreamReader(conn.getInputStream())).lines().collect(Collectors.joining()));

                String status = jsonResponse.getJSONObject("result").getString("status");
                return jsonResponse.getBoolean("ok") && ("member".equals(status) || "administrator".equals(status) || "creator".equals(status));
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public User createOrGetUser(Update update) {
        String chatId = getChatId(update);
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setStep(Steps.NEW_USER);
            return userRepository.save(user);
        }
        return userOptional.get();
    }

    public String getText(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                return update.getMessage().getText();
            }
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getText();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getText();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getText();
        }
        return "error_text";
    }

    private String getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getChatId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getChatId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId().toString();
        } else {
            return update.getMessage().getChatId().toString();
        }
    }

}
