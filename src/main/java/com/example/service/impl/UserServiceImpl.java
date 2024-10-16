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
import com.example.util.Message;
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
        return SendMessage.builder().text(Message.START_MESSAGE).chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT)).build();
    }

    @Override
    public SendMessage saveContactAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();
            user.setPhoneNumber(contact.getPhoneNumber());
            user.setStep(UserStep.ASK_FULL_NAME);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId()).text(Message.FULL_NAME_MSG).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            return SendMessage.builder().text(Message.START_MESSAGE).chatId(user.getChatId()).replyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT)).build();
        }
    }

    @Override
    public SendMessage saveFullNameAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String fullName = update.getMessage().getText();
            user.setFullName(fullName);
            user.setStep(UserStep.ASK_EMPLOYMENT_ACTIVITY);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.EMPLOYMENT_ACTIVITY_MSG)
                    .build();
        } else {
            return SendMessage.builder().chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.FULL_NAME_MSG)
                    .build();
        }
    }

    @Override
    public SendMessage saveEmploymentActivityAndNext(User user, Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String employmentActivity = update.getMessage().getText();
            user.setEmploymentActivity(employmentActivity);
            user.setStep(UserStep.ASK_EMPLOYEE_COUNT);
            userRepository.save(user);

            return SendMessage.builder().chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.EMPLOYEE_COUNT).build();
        } else {
            return SendMessage.builder().chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.EMPLOYMENT_ACTIVITY_MSG).build();
        }
    }

    @Override
    public SendMessage saveEmployeeCountAndNext(User user, Update update, Boolean isSubscribed, String channelUsername) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String employeeCount = update.getMessage().getText();
            user.setNumberOfEmployees(employeeCount);
            user.setDone(true);
            if (isSubscribed) {
                user.setStep(UserStep.ASK_CARD_NUMBER);
                userRepository.save(user);
                return SendMessage.builder().chatId(user.getChatId())
                        .text(Message.CARD_NUMBER_MESSAGE)
                        .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                        .build();
            } else {
                user.setStep(UserStep.SHOW_CHANNEL);
                userRepository.save(user);
                return SendMessage.builder().chatId(user.getChatId())
                        .text(Message.JOIN_CHANNEL_MESSAGE)
                        .replyMarkup(buttonService.enterChannel(channelUsername))
                        .build();
            }
        } else {
            return SendMessage.builder().chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.EMPLOYEE_COUNT)
                    .build();
        }
    }

    @Override
    public SendMessage showChannelAndNext(User user, Update update, String channelUsername, Boolean isSubscribed) {
        if (isSubscribed) {
            user.setStep(UserStep.ASK_CARD_NUMBER);
            userRepository.save(user);
            return SendMessage.builder().chatId(user.getChatId()).text(Message.CARD_NUMBER_MESSAGE).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            user.setStep(UserStep.SHOW_CHANNEL);
            userRepository.save(user);
            return SendMessage.builder()
                    .chatId(user.getChatId())
                    .text(Message.JOIN_CHANNEL_MESSAGE)
                    .replyMarkup(buttonService.enterChannel(channelUsername))
                    .build();
        }
    }

    @Override
    public SendMessage saveCardNumberAndNext(User user, Update update, boolean subscribed, String channelUsername) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String cardNumber = update.getMessage().getText();
            cardNumber = cardNumber.replace(" ", "");
            user.setCardNumber(cardNumber);
            user.setStep(UserStep.ASK_CARD_EXPIRY_DATE);
            userRepository.save(user);

            return SendMessage.builder()
                    .chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.CARD_EXPIRE_MSG).build();
        } else {
            return SendMessage.builder()
                    .chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.CARD_NUMBER_MESSAGE).build();
        }
    }

    @Override
    public SendMessage saveCardExpAndNext(User user, Update update, boolean subscribed, String channelUsername) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String cardExpire = update.getMessage().getText();
            cardExpire = cardExpire.replace("/", "");
            String error;
            try {
                CommonResponse<CardCreateResponse> commonResponse = billingService.getVerifyCode(new CardCreateRequest(user.getCardNumber(), cardExpire, 9_000_00L, user.getChatId()));
                if (commonResponse.getSuccess()) {
                    user.setCardExp(cardExpire);
                    user.setStep(UserStep.ASK_VERIFICATION_CODE);
                    user.setTransactionId(commonResponse.getItem().getTransactionId());
                    userRepository.save(user);
                    return SendMessage.builder()
                            .chatId(user.getChatId())
                            .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                            .text("Tasdiqlash kodi " + commonResponse.getItem().getPhone() + " ga yuborildi. Tasdiqlash kodini kiriting")
                            .build();
                }
                error = commonResponse.getMessage();
            } catch (Exception e) {
                error = e.getMessage();
            }

            user.setStep(UserStep.ASK_CARD_NUMBER);
            userRepository.save(user);
            return SendMessage.builder().chatId(user.getChatId()).text(error + "\n\n" + Message.CARD_NUMBER_MESSAGE).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
        } else {
            return SendMessage.builder()
                    .chatId(user.getChatId())
                    .replyMarkup(buttonService.createButton(ButtonConst.EMPTY))
                    .text(Message.CARD_NUMBER_MESSAGE).build();
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
                    return SendMessage.builder().chatId(user.getChatId())
                            .text("Successfully paid, you will send gift file")
                            .replyMarkup(buttonService.createButton(ButtonConst.GET_FILE))
                            .build();
                }
                error = commonResponse.getMessage();
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        user.setStep(UserStep.ASK_CARD_NUMBER);
        userRepository.save(user);
        return SendMessage.builder().chatId(user.getChatId()).text(error + "\n\n" + Message.CARD_NUMBER_MESSAGE).replyMarkup(buttonService.createButton(ButtonConst.EMPTY)).build();
    }

    @Override
    public SendDocument sendGiftFile(User user, Update update) {
        return SendDocument.builder()
                .caption("GAYD")
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
