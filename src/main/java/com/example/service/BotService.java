package com.example.service;

import com.example.configuration.BotConfiguration;
import com.example.dto.FileDto;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final ButtonService buttonService;
    private final InlineButtonService inlineButtonService;


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


        if (!user.getIsAuthenticated()) {
            if (Steps.REGISTERED.equals(user.getStep())) {
                pleaseShareContact(user);
                return;
            }
        }


        if (!handleMessage(user, message)) {
            if (!handleUserStep(update, user, userStep)) undefined(user);
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
        return switch (message) {
            case "/start", ButtonConst.BACK_MAIN_MENU -> {
                start(user);
                yield true;
            }
            default -> false;
        };
    }


    public boolean handleUserStep(Update update, User user, String userStep) {
        return switch (userStep) {
            case Steps.ASK_CONTACT -> {
                askContact(update, user);
                yield true;
            }
            case Steps.ASK_TO_JOIN_CHANNEL -> {
                start(user);
                yield true;
            }
            case Steps.PAYMENT_ASK_CARD_NUMBER -> {
                askCardNumber(user);
                yield true;
            }
            case Steps.ASK_CARD_EXP -> {
                getCardNumberAskExpireDate(user, update);
                yield true;
            }
            default -> false;
        };
    }


    public void start(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setChatId(user.getChatId());
        boolean subscribed = userService.isSubscribed(this.botConfiguration.getChannelUsername(), this.botConfiguration.getToken(), user.getChatId());

        if (subscribed) {
            sendMessage.setText("Lid magnit sotuv matni \n\nto'lov qilish uchun undovchi matn, agar lid to'lov qilishni xoxlasa \"" + ButtonConst.CHECKING + "\" tugmasini bosishi kerak");
            sendMessage.setReplyMarkup(buttonService.createButtons(1, List.of(ButtonConst.CHECKING)));
            sendMessageExecutor(sendMessage);
            user.setStep(Steps.PAYMENT_ASK_CARD_NUMBER);
            userService.updateUser(user);
        } else {
            sendMessage.setText("Kanalga obuna qilish uchun undovchi matn\n" + this.botConfiguration.getChannelUsername());
            sendMessage.setReplyMarkup(buttonService.createButtons(1, List.of(ButtonConst.CHECKING)));
            sendMessageExecutor(sendMessage);
            user.setStep(Steps.ASK_TO_JOIN_CHANNEL);
            userService.updateUser(user);
        }
    }


    public void askCardNumber(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimoz karta raqamingizni kiriting\n9860 0401 0261 7375");
        sendMessage.setReplyMarkup(buttonService.createButtons(1, List.of(ButtonConst.BACK_MAIN_MENU)));
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_CARD_EXP);
    }

    public void getCardNumberAskExpireDate(User user, Update update) {
        String message = getMessage(update).replace(" ", "");;
        if (message.length() != 16) {
            askCardNumber(user);
            return;
        }


        user.setCardNumber(message);
        user.setStep(Steps.ASK_CARD_EXP);
        userService.updateUser(user);

        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimoz karta amal qilish muddatini kiriting\n08/28");
        sendMessage.setReplyMarkup(buttonService.createButtons(1, List.of(ButtonConst.BACK_MAIN_MENU)));
        sendMessageExecutor(sendMessage);
    }

    public void getExpireDateAndAskVerificationCode(User user, Update update) {
        String message = getMessage(update).replace(" ", "");
        if (message.length() != 5 || !message.contains("/")) {
            askCardNumber(user);
            return;
        }

        user.setCardExp(message);
        user.setStep(Steps.ASK_VERIFY_CODE);
        userService.updateUser(user);

        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimoz karta amal qilish muddatini kiriting\n08/28");
        sendMessage.setReplyMarkup(buttonService.createButtons(1, List.of(ButtonConst.BACK_MAIN_MENU)));
        sendMessageExecutor(sendMessage);
    }

    public void askContact(Update update, User user) {
        SendMessage sendMessage = getSendMessage(user);
        if (update.hasMessage()) {
            Message updateMessage = update.getMessage();
            if (updateMessage.hasContact()) {
                Contact contact = updateMessage.getContact();
                user.setPhoneNumber(contact.getPhoneNumber());
                user.setIsAuthenticated(true);
                user.setStep(Steps.REGISTERED);
                userService.updateUser(user);


                sendMessage.setText("Salomlashuv Matni");
                sendMessage.setReplyMarkup(buttonService.createEmptyKeyboard());
                sendMessageExecutor(sendMessage);

                start(user);
                return;
            }
        }
        pleaseShareContact(user);
    }


    private void pleaseShareContact(User user) {
        SendMessage sendMessage = getSendMessage(user);
        sendMessage.setText("Iltimos Kontaktingizni yuboring!");
        sendMessage.setReplyMarkup(buttonService.shareContactButton());
        sendMessageExecutor(sendMessage);
        user.setStep(Steps.ASK_CONTACT);
        userService.updateUser(user);
    }


    private void undefined(User user) {
        SendMessage sendMessage = getSendMessage(user);
        if (user.getIsAuthenticated()) {
            sendMessage.setChatId(user.getChatId());
            sendMessage.setText("Xato buyruq kiritildi ❌");
            sendMessageExecutor(sendMessage);
        } else {
            sendMessage.setReplyMarkup(buttonService.shareContactButton());
            sendMessage.setText("Iltimos Telefon raqamingizni yuboring...");
            sendMessageExecutor(sendMessage);
        }
    }

    private void sendMessageExecutor(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendLocationExecutor(SendLocation sendLocation) {
        try {
            execute(sendLocation);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendVideoExecutor(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPhotoExecutor(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteMessageExecutor(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void editMessageTextExecutor(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private static SendLocation getSendLocation(User user) {
        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(user.getChatId());
        return sendLocation;
    }

    public SendMessage getSendMessage(User user) {
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
            } else {
                return new FileDto(ContentType.NONE);
            }
        }
        return new FileDto(ContentType.NONE);
    }


}
