package com.example.service;

import com.example.dto.GetPaymentResult;
import com.example.dto.GetVerifyCodeResponse;
import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.payme.controller.BillingService;
import com.example.payme.dto.CommonResponse;
import com.example.payme.dto.billing.CardCreateRequest;
import com.example.payme.dto.billing.CardCreateResponse;
import com.example.payme.dto.billing.VerifyRequest;
import com.example.payme.dto.billing.VerifyResponse;
import com.example.util.Steps;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    private final BillingService billingService;

    public User createUser(Update update) {
        String realId = getRealId(update);
        org.telegram.telegrambots.meta.api.objects.User tgUser = getUser(update);
        String chatId = getChatId(update);

        Optional<User> userOptional = userRepository.findByChatId(realId);
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
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
            final String urlStr = "https://api.telegram.org/bot" + botToken + "/getChatMember?chat_id=" + channelUsername + "&user_id=" + chatId;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                JSONObject jsonResponse = new JSONObject(content.toString());

                if (jsonResponse.getBoolean("ok")) {
                    String status = jsonResponse.getJSONObject("result").getString("status");

                    return status.equals("member") || status.equals("administrator") || status.equals("creator");
                } else {
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


    public GetVerifyCodeResponse getVerifyCode(User user) {
        try {
            CommonResponse<CardCreateResponse> verifyCode = billingService.getVerifyCode(new CardCreateRequest(user.getCardNumber(), user.getCardExp(), 900000L, user.getChatId()));
            return new GetVerifyCodeResponse(verifyCode.getSuccess(), verifyCode.getMessage(), new GetVerifyCodeResponse.Item(verifyCode.getItem().getPhone(), verifyCode.getItem().getMessage(), verifyCode.getItem().getTransactionId()));
        } catch (Exception e) {
            return new GetVerifyCodeResponse(false, e.getMessage(), null);
        }
    }


    public GetPaymentResult verify(String verificationCode, User user) {
        try {
            CommonResponse<VerifyResponse> verify = billingService.verify(new VerifyRequest(verificationCode, user.getTransactionId()));
            return new GetPaymentResult(verify.getSuccess(), verify.getMessage(), new GetPaymentResult.Item(
                    verify.getItem().getTransactionId(),
                    verify.getItem().getAmount(), verify.getItem().getStatus()
            ));
        } catch (Exception e) {
            return new GetPaymentResult(false, e.getMessage(), new GetPaymentResult.Item());
        }
    }


}