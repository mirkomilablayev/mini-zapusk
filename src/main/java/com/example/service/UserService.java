package com.example.service;

import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.util.Steps;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(Update update) {
        String realId = getRealId(update);
        org.telegram.telegrambots.meta.api.objects.User tgUser = getUser(update);
        String chatId = getChatId(update);

        Optional<User> userOptional = userRepository.findByChatId(realId);
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setRealId(realId);
            user.setFullName(tgUser.getFirstName() + (tgUser.getLastName() == null ? "" : " " + tgUser.getLastName()));
            user.setStep(Steps.REGISTERED);
            return userRepository.save(user);
        }
        return userOptional.get();

    }


    public String getRealId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom().getId().toString();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom().getId().toString();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom().getId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom().getId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom().getId().toString();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom().getId().toString();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom().getId().toString();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser().getId().toString();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom().getId().toString();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom().getId().toString();
        }
        return "";
    }

    private org.telegram.telegrambots.meta.api.objects.User getUser(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom();
        }
        return new org.telegram.telegrambots.meta.api.objects.User();
    }


    public String getChatId(Update update) {
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

    public String getText(@NonNull Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText() != null) {
                return update.getMessage().getText();
            }
            return "error_text";
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getText();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getText();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getText();
        }
        return "";
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

//    public boolean isSubscribed(String channelUsername, String botToken, String chatId) {
//        final String url = "https://api.telegram.org/bot" + botToken + "/getChatMember?chat_id=@" + channelUsername + "&user_id=" + chatId;
//    }
//

    public boolean isSubscribed(String channelUsername, String botToken, String chatId) {
        try {
            // Construct the URL for the Telegram API request
            final String urlStr = "https://api.telegram.org/bot" + botToken + "/getChatMember?chat_id=" + channelUsername + "&user_id=" + chatId;

            // Open connection
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Check if the response code is 200 (OK)
            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                // Close the connections
                in.close();
                conn.disconnect();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(content.toString());

                // Check if the 'ok' field is true
                if (jsonResponse.getBoolean("ok")) {
                    // Get the 'status' field from the 'result' object
                    String status = jsonResponse.getJSONObject("result").getString("status");

                    // Return true if the user is a member or higher (creator, administrator)
                    return status.equals("member") || status.equals("administrator") || status.equals("creator");
                } else {
                    // Handle the case where 'ok' is false (error occurred)
                    return false;
                }
            } else {
                System.out.println("Error: Failed to connect, Response Code: " + conn.getResponseCode());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}