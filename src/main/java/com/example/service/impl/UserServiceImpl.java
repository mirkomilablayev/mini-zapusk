package com.example.service.impl;

import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.payme.controller.BillingService;
import com.example.payme.dto.CommonResponse;
import com.example.payme.dto.billing.CardCreateRequest;
import com.example.payme.dto.billing.CardCreateResponse;
import com.example.payme.dto.billing.VerifyRequest;
import com.example.payme.dto.billing.VerifyResponse;
import com.example.service.ButtonService;
import com.example.service.UserService;
import com.example.util.ButtonConst;
import com.example.util.UserStep;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String FILE_ID = "BQACAgIAAxkBAAIBOmcKfNuZSAqORoUdSqtf4guZL_JsAALcXAACPKpISGzRs0Ung6Y5NgQ";

    private final UserRepository userRepository;
    private final ButtonService buttonService;
    private final BillingService billingService;


    @Override
    public SendMessage askContact(User user) {
        user.setStep(UserStep.SAVE_CONTACT);
        userRepository.save(user);
        return SendMessage.builder().text("Send me your contact").chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT)).build();
    }

    @Override
    public SendMessage saveContactAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();
            user.setPhoneNumber(contact.getPhoneNumber());
            user.setStep(UserStep.ASK_FULL_NAME);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId()).text("send me your full name!").replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            return SendMessage.builder().text("Please, Send me your contact").chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT)).build();
        }
    }

    @Override
    public SendMessage saveFullNameAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String fullName = update.getMessage().getText();
            user.setFullName(fullName);
            user.setStep(UserStep.ASK_EMPLOYMENT_ACTIVITY);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("What do you do for living?").build();
        } else {
            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("please send me your full name").build();
        }
    }

    @Override
    public SendMessage saveEmploymentActivityAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String employmentActivity = update.getMessage().getText();
            user.setEmploymentActivity(employmentActivity);
            user.setStep(UserStep.ASK_EMPLOYEE_COUNT);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("How many employees do you have").build();
        } else {
            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("please, Tell me \nWhat do you do for living?\n").build();
        }
    }

    @Override
    public SendMessage saveEmployeeCountAndNext(User user, Update update, Boolean isSubscribed, String channelUsername) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String employeeCount = update.getMessage().getText();
            user.setNumberOfEmployees(employeeCount);

            if (isSubscribed) {
                user.setStep(UserStep.ASK_CARD_NUMBER);
                userRepository.save(user);
                return SendMessage.builder().chatId(user.getChatId()).text("Please Send me your card number").replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
            } else {
                user.setStep(UserStep.SHOW_CHANNEL);
                userRepository.save(user);
                return SendMessage.builder().chatId(user.getChatId()).text("join this channel").replyMarkup(buttonService.enterChannel(channelUsername)).build();
            }
        } else {
            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("please, Tell me \nHow many employees do you have?\n").build();
        }
    }

    @Override
    public SendMessage showChannelAndNext(User user, Update update, String channelUsername, Boolean isSubscribed) {
        if (isSubscribed) {
            user.setStep(UserStep.ASK_CARD_NUMBER);
            userRepository.save(user);
            return SendMessage.builder().chatId(user.getChatId()).text("Send me your card number").replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            user.setStep(UserStep.SHOW_CHANNEL);
            userRepository.save(user);
            return SendMessage.builder().chatId(user.getChatId()).text("Please join this channel").replyMarkup(buttonService.enterChannel(channelUsername)).build();
        }
    }

    @Override
    public SendMessage saveCardNumberAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String cardNumber = update.getMessage().getText();
            user.setCardNumber(cardNumber);
            user.setStep(UserStep.ASK_CARD_EXPIRY_DATE);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("Send me your card's expire date").build();
        } else {
            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("please, Send me your card number").build();
        }
    }

    @Override
    public SendMessage saveCardExpAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String cardExpire = update.getMessage().getText();
            String error;
            try {
                CommonResponse<CardCreateResponse> commonResponse = billingService.getVerifyCode(new CardCreateRequest(user.getCardNumber(), user.getCardExp(), 9_000_00L, user.getChatId()));
                if (commonResponse.getSuccess()) {
                    user.setCardExp(cardExpire);
                    user.setStep(UserStep.ASK_VERIFICATION_CODE);
                    user.setTransactionId(commonResponse.getItem().getTransactionId());
                    userRepository.save(user);
                    return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("we sent otp code to this number: " + commonResponse.getItem().getPhone() + "\nif you got verify it!").build();
                }
                error = commonResponse.getMessage();
            } catch (Exception e) {
                error = e.getMessage();
            }

            user.setStep(UserStep.ASK_CARD_NUMBER);
            userRepository.save(user);
            return SendMessage.builder().chatId(user.getChatId()).text(error + "\n\nPlease: Send me your card number").replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            return SendMessage.builder().chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).text("please, Send me your card number").build();
        }
    }

    @Override
    public SendMessage payAndNext(User user, Update update) {
        String error = "";
        if (update.hasMessage() && update.getMessage().hasText()) {
            String otp = update.getMessage().getText();
            try {
                CommonResponse<VerifyResponse> commonResponse = billingService.verify(new VerifyRequest(otp, user.getTransactionId()));
                if (commonResponse.getSuccess()) {
                    user.setPremium(Boolean.TRUE);
                    user.setStep(UserStep.PREMIUM);
                    userRepository.save(user);
                    return SendMessage.builder().chatId(user.getChatId()).text("Successfully paid, you will send gift file").replyMarkup(buttonService.createButton(ButtonConst.GET_FILE)).build();
                }
                error = commonResponse.getMessage();
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        user.setStep(UserStep.ASK_CARD_NUMBER);
        userRepository.save(user);
        return SendMessage.builder().chatId(user.getChatId()).text(error + "\n\nSend me your card number").replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
    }

    @Override
    public SendDocument sendGiftFile(User user, Update update) {
        return SendDocument.builder()
                .caption("Your gift is here")
                .chatId(user.getChatId())
                .document(new InputFile(FILE_ID))
                .build();
    }


    @Override
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

    @Override
    public User createOrGetUser(Update update) {
        String chatId = getChatId(update);
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setStep(UserStep.DEFAULT);
            return userRepository.save(user);
        }
        return userOptional.get();
    }

    @Override
    public String getText(Update update) {
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
