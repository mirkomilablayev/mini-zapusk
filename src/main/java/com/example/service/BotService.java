package com.example.service;

import com.example.configuration.BotConfiguration;
import com.example.dto.FileDto;
import com.example.dto.GetPaymentResult;
import com.example.dto.GetVerifyCodeResponse;
import com.example.entity.User;
import com.example.util.ButtonConst;
import com.example.util.ContentType;
import com.example.util.Steps;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final ButtonService buttonService;

    private static final String FILE_ID = "BQACAgIAAxkBAAIBOmcKfNuZSAqORoUdSqtf4guZL_JsAALcXAACPKpISGzRs0Ung6Y5NgQ";


    @Override
    public String getBotUsername() {
        return this.botConfiguration.getUsername();
    }

    @Override
    public String getBotToken() {
        return this.botConfiguration.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        User user = userService.createUser(update);
        String message = getMessage(update);
        String userStep = user.getStep();

        if (user.getHasPhoneNumber() && user.getHasFullName() && user.getHasEmploymentActivity() && user.getHasNumberOfEmployees()) {
            if (!handleMessage(user, message) && !handleUserStep(update, user, userStep)) undefined(user);
        } else fillUserForms(update, user);
    }

    private void fillUserForms(Update update, User user) {
        Map<String, BiConsumer<Update, User>> actions = new HashMap<>();
        actions.put(Steps.ASK_CONTACT, this::askContact);
        actions.put(Steps.ASK_FULL_NAME, this::askFullName);
        actions.put(Steps.ASK_EMPLOYMENT_ACTIVITY, this::askEmploymentActivity);
        actions.put(Steps.ASK_NUMBER_OF_EMPLOYEES, this::askNumberOfEmployees);


        if (!user.getHasPhoneNumber()) {
            handleUserStep(user, Steps.ASK_CONTACT, update, actions);
        } else if (!user.getHasFullName()) {
            handleUserStep(user, Steps.ASK_FULL_NAME, update, actions);
        } else if (!user.getHasEmploymentActivity()) {
            handleUserStep(user, Steps.ASK_EMPLOYMENT_ACTIVITY, update, actions);
        } else if (!user.getHasNumberOfEmployees()) {
            handleUserStep(user, Steps.ASK_NUMBER_OF_EMPLOYEES, update, actions);
        }
    }

    private void handleUserStep(User user, String step, Update update, Map<String, BiConsumer<Update, User>> actions) {
        if (step.equals(user.getStep())) {
            actions.get(step).accept(update, user);
        } else {
            switch (step) {
                case Steps.ASK_CONTACT -> pleaseShareContact(user);
                case Steps.ASK_FULL_NAME -> pleaseShareFullName(user);
                case Steps.ASK_EMPLOYMENT_ACTIVITY -> pleaseShareEmploymentActivity(user);
                case Steps.ASK_NUMBER_OF_EMPLOYEES -> pleaseShareNumberOfEmployees(user);
            }
        }
    }


    private static String getMessage(Update update) {
        String message = "";
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasText()) {
                message = updateMessage.getText();
            }
        }
        return message;
    }


    public boolean handleMessage(User user, String message) {
        switch (message) {
            case "/start", ButtonConst.BACK_MAIN_MENU -> {
                resetUserState(user);
                start(user);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void resetUserState(User user) {
        user.setCardExp(null);
        user.setCardNumber(null);
        user.setTransactionId(0L);
        user.setTranIdCreatedTime(null);
        user.setStep(Steps.REGISTERED);
        userService.updateUser(user);
    }


    public boolean handleUserStep(Update update, User user, String userStep) {
        switch (userStep) {
            case Steps.REGISTERED, Steps.ASK_TO_JOIN_CHANNEL -> start(user);
            case Steps.ASK_CARD_NUMBER -> getCardNumberAskExpireDate(user, update);
            case Steps.ASK_CARD_EXP -> getExpireDateAndAskVerificationCode(user, update);
            case Steps.VERIFY_AND_PAY -> getVerificationCodeAndPay(user, update);
            default -> {
                return false;
            }
        }
        return true;
    }


    public void start(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setChatId(user.getChatId());

        if (userService.isSubscribed(botConfiguration.getChannelUsername(), botConfiguration.getToken(), user.getChatId())) {
            if (!user.getPremium()) {
                promptForCardDetails(user, sendMessage);
                return;
            }
        } else {
            promptForChannelSubscription(user, sendMessage);
            return;
        }

        sendGiftDocument(user);
    }

    private void promptForCardDetails(User user, SendMessage sendMessage) {
        sendMessage.setText("\uD83D\uDCB3 Karta raqamingizni kiriting");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_CARD_NUMBER);
        userService.updateUser(user);
    }

    private void promptForChannelSubscription(User user, SendMessage sendMessage) {
        sendMessage.setText("Majburiy kanalga obuna bo'ling \uD83D\uDC47");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.NEXT));
        sendMessageExecutor(sendMessage);

        sendMessage.setText("Kanalga obuna qilish uchun undovchi matn");
        sendMessage.setReplyMarkup(buttonService.enterChannel(botConfiguration.getChannelUsername()));
        sendMessageExecutor(sendMessage);

        user.setStep(Steps.ASK_TO_JOIN_CHANNEL);
        userService.updateUser(user);
    }

    private void sendGiftDocument(User user) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(user.getChatId());
        sendDocument.setDocument(new InputFile(FILE_ID));
        sendDocument.setCaption("Siz uchun sovg'a");
        sendDocument.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendDocumentExecutor(sendDocument);
    }


    public void getCardNumberAskExpireDate(User user, Update update) {
        String message = getMessage(update).replace(" ", "");
        if (message.length() != 16) {
            start(user);
            return;
        }

        user.setCardNumber(message);
        user.setStep(Steps.ASK_CARD_EXP);
        userService.updateUser(user);

        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimos kartaning amal qilish muddatini kiriting: namuna: 08/28");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.BACK_MAIN_MENU));
        sendMessageExecutor(sendMessage);
    }

    public void getExpireDateAndAskVerificationCode(User user, Update update) {
        String message = getMessage(update).replace(" ", "").replace("/", "");

        if (message.length() != 4) {
            start(user);
            return;
        }

        user.setCardExp(message);
        SendMessage sendMessage = getSendMessage(user);

        GetVerifyCodeResponse verifyCodeResponse = userService.getVerifyCode(user);
        if (!verifyCodeResponse.isSuccess()) {
            sendAndRestart(sendMessage, verifyCodeResponse.getMessage(), user);
            return;
        }

        updateUserForVerification(user, verifyCodeResponse);

        sendMessage.setText("Tastiqlash kodi " + verifyCodeResponse.getItem().getPhone() + "ga yuborildi, Tastiqlash kodini kiriting..");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.BACK_MAIN_MENU));
        sendMessageExecutor(sendMessage);
    }


    private void updateUserForVerification(User user, GetVerifyCodeResponse response) {
        user.setTransactionId(response.getItem().getTransactionId());
        user.setTranIdCreatedTime(LocalDateTime.now());
        user.setStep(Steps.VERIFY_AND_PAY);
        userService.updateUser(user);
    }


    public void getVerificationCodeAndPay(User user, Update update) {
        String message = getMessage(update).replace(" ", "");
        SendMessage sendMessage = getSendMessage(user);

        if (!isValidVerificationCode(message)) {
            sendAndRestart(sendMessage, "Tastiqlash kodi 6 xonali son bo'lishi kerak!", user);
            return;
        }

        GetPaymentResult verify = userService.verify(message, user);
        if (!verify.isSuccess()) {
            sendAndRestart(sendMessage, verify.getMessage(), user);
            return;
        }

        completePayment(user);
        sendMessage.setText("To'lov muvaffaqiyatli amalga oshirildi ✅");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendMessageExecutor(sendMessage);
        start(user);
    }

    private boolean isValidVerificationCode(String message) {
        return message.length() == 6;
    }

    private void sendAndRestart(SendMessage sendMessage, String text, User user) {
        sendMessage.setText(text);
        sendMessageExecutor(sendMessage);
        start(user);
    }

    private void completePayment(User user) {
        user.setCardExp(null);
        user.setCardNumber(null);
        user.setTransactionId(0L);
        user.setTranIdCreatedTime(null);
        user.setStep(Steps.REGISTERED);
        user.setPremium(true);
        userService.updateUser(user);
    }

    private void pleaseShareContact(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimos Kontaktingizni yuboring!");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_CONTACT);
        userService.updateUser(user);
    }

    public void askContact(Update update, User user) {
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasContact()) {
                Contact contact = updateMessage.getContact();
                user.setPhoneNumber(contact.getPhoneNumber());
                user.setHasPhoneNumber(true);
                user.setStep(Steps.REGISTERED);
                userService.updateUser(user);
                pleaseShareFullName(user);
                return;
            }
        }
        pleaseShareContact(user);
    }


    private void pleaseShareFullName(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimos To'liq ism-familiyangizni kiriting");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_FULL_NAME);
        userService.updateUser(user);
    }

    public void askFullName(Update update, User user) {
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasText()) {
                String text = updateMessage.getText();
                user.setFullName(text);
                user.setHasFullName(true);
                user.setStep(Steps.REGISTERED);
                userService.updateUser(user);
                pleaseShareEmploymentActivity(user);
                return;
            }
        }
        pleaseShareFullName(user);
    }

    private void pleaseShareEmploymentActivity(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Mehnat faoliyatingiz haqida");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_EMPLOYMENT_ACTIVITY);
        userService.updateUser(user);
    }

    public void askEmploymentActivity(Update update, User user) {
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasText()) {
                String text = updateMessage.getText();
                user.setEmploymentActivity(text);
                user.setHasEmploymentActivity(true);
                user.setStep(Steps.REGISTERED);
                userService.updateUser(user);
                pleaseShareNumberOfEmployees(user);
                return;
            }
        }
        pleaseShareEmploymentActivity(user);
    }

    private void pleaseShareNumberOfEmployees(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimos Hodimlaringiz sonini kiriting!");
        sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_NUMBER_OF_EMPLOYEES);
        userService.updateUser(user);
    }

    public void askNumberOfEmployees(Update update, User user) {
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasText()) {
                String text = updateMessage.getText();
                user.setNumberOfEmployees(text);
                user.setHasNumberOfEmployees(true);
                user.setStep(Steps.REGISTERED);
                userService.updateUser(user);
                start(user);
                return;
            }
        }
        pleaseShareNumberOfEmployees(user);
    }


    private void undefined(User user) {
        SendMessage sendMessage = getSendMessage(user);
        if (user.getHasPhoneNumber()) {
            sendMessage.setText("Xato buyruq kiritildi ❌");
        } else {
            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT));
            sendMessage.setText("Iltimos Telefon raqamingizni yuboring...");
        }
        sendMessageExecutor(sendMessage);
    }

    private void sendMessageExecutor(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendDocumentExecutor(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private SendMessage getSendMessage(User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        return sendMessage;
    }

    public FileDto getFileId(Update update) {
        FileDto fileDto = new FileDto();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                PhotoSize largestPhoto = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                fileDto.setFileId(largestPhoto != null ? largestPhoto.getFileId() : null);
                fileDto.setContentType(largestPhoto != null ? ContentType.PICTURE : ContentType.NONE);
                return fileDto;
            } else if (message.hasVideo()) {
                Video video = message.getVideo();
                fileDto.setFileId(video.getFileId());
                fileDto.setContentType(ContentType.VIDEO);
                return fileDto;
            } else if (message.hasDocument()) {
                Document document = message.getDocument();
                fileDto.setFileId(document.getFileId());
                fileDto.setContentType(ContentType.DOCUMENT);
                return fileDto;
            } else {
                return new FileDto(ContentType.NONE);
            }
        }
        return new FileDto(ContentType.NONE);
    }



}
