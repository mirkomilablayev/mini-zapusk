package com.example.service;

import com.example.configuration.BotConfiguration;
import com.example.dto.FileDto;
import com.example.entity.User;
import com.example.service.impl.AdminServiceImpl;
import com.example.util.ButtonConst;
import com.example.util.ContentType;
import com.example.util.UserStep;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final AdminService adminService;


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
        User user = userService.createOrGetUser(update);
        String text = userService.getText(update);

        if (ButtonConst.GET_FILE.equals(text)) {
            SendDocument sendDocument = userService.sendGiftFile(user, update);
            execute(sendDocument);
        } else if (!user.getAdmin()) {
            handleUserStep(update, user);
        } else {
            handleAdminStep(update, user);
        }
    }

    private void handleAdminStep(Update update, User user) throws Exception {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
//                if (ButtonConst.)
            }
        }
        if (UserStep.ADMIN_MAIN_MENU.equals(user.getStep())) {
            SendMessage sendMessage = adminService.adminMainMenu(user, update);
            execute(sendMessage);
        }
    }

    public void handleUserStep(Update update, User user) throws Exception {
        boolean subscribed = userService.isSubscribed(botConfiguration.getChannelUsername(), getBotToken(), user.getChatId());

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if ("check_me".equals(data) ) {
                SendMessage sendMessage = userService.showChannelAndNext(user, update, botConfiguration.getChannelUsername(), subscribed);
                execute(sendMessage);
            }
            return;
        }
        if (!subscribed && user.getDone()) {
            SendMessage sendMessage = userService.showChannelAndNext(user, update, botConfiguration.getChannelUsername(), false);
            execute(sendMessage);
            return;
        }
        if (UserStep.DEFAULT.equals(user.getStep())) {
            SendMessage sendMessage = userService.askContact(user);
            execute(sendMessage);
        } else if (UserStep.SAVE_CONTACT.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveContactAndNext(user, update);
            execute(sendMessage);
        } else if (UserStep.ASK_FULL_NAME.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveFullNameAndNext(user, update);
            execute(sendMessage);
        } else if (UserStep.ASK_EMPLOYMENT_ACTIVITY.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveEmploymentActivityAndNext(user, update);
            execute(sendMessage);
        } else if (UserStep.ASK_EMPLOYEE_COUNT.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveEmployeeCountAndNext(user, update, subscribed, botConfiguration.getChannelUsername());
            execute(sendMessage);
        } else if (UserStep.SHOW_CHANNEL.equals(user.getStep())) {
            SendMessage sendMessage = userService.showChannelAndNext(user, update, botConfiguration.getChannelUsername(), subscribed);
            execute(sendMessage);
        } else if (UserStep.ASK_CARD_NUMBER.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveCardNumberAndNext(user, update, subscribed, botConfiguration.getChannelUsername());
            execute(sendMessage);
        } else if (UserStep.ASK_CARD_EXPIRY_DATE.equals(user.getStep())) {
            SendMessage sendMessage = userService.saveCardExpAndNext(user, update, subscribed, botConfiguration.getChannelUsername());
            execute(sendMessage);
        } else if (UserStep.ASK_VERIFICATION_CODE.equals(user.getStep())) {
            SendMessage sendMessage = userService.payAndNext(user, update);
            execute(sendMessage);
        } else if (UserStep.PREMIUM.equals(user.getStep())) {
            SendDocument sendDocument = userService.sendGiftFile(user, update);
            execute(sendDocument);
        } else execute(SendMessage.builder().text("Wrong command ❌").chatId(user.getChatId()).build());
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
