package com.example.service;

import com.example.configuration.BotConfiguration;
import com.example.dto.FileDto;
import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.exec.spi.JdbcOperationQueryInsert;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final ButtonService buttonService;
    private final UserRepository userRepository;


    private static final String FILE_ID = "BQACAgIAAxkBAAIBOmcKfNuZSAqORoUdSqtf4guZL_JsAALcXAACPKpISGzRs0Ung6Y5NgQ";
    private static final String VIDEO_MSG_FILE_ID = "DQACAgIAAxkBAAID2We4xq_WfP4BYlZLNL3qG5UiuVygAAJ5cwACHumwSdX8r3SvtcpINgQ";
    private static final String VIDEO_FILE_ID = "BAACAgIAAxkBAAID3We4x19RiFCjtKt0pLI72uSAkw41AAJ3YQACZz64SXRC0dyvCUQvNgQ";

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
        SendMessage sendMessage = getSendMessage(user);
        Message message1 = update.getMessage();


        if (user.getIsAdmin()) {

            if (ButtonConst.SHARE_ADS.equals(text)) {
                sendMessage.setText("Forward Message yuboring\n" +
                        "⚠️Xabar matnida xatoliklar yo'qligiga etibor qiling, Yuborishni bekor qilish uchun \"" + ButtonConst.BACK_MAIN_MENU + "\" ni bosing");
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.BACK_MAIN_MENU));
                execute(sendMessage);
                user.setStep("SHARE_ADS");
                userRepository.save(user);
                return;
            }

            if ("SHARE_ADS".equals(user.getStep()) && !ButtonConst.BACK_MAIN_MENU.equals(text)) {
                if (update.hasMessage()) {
                    for (User client : userRepository.findAll()) {
                        sendMessageToUser(client.getChatId(), update.getMessage());
                    }
                } else {
                    sendMessage.setText("Reklama kontentida xatolik bor");
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_ADS));
                    execute(sendMessage);
                }
                user.setStep(Steps.NEW_USER);
                userRepository.save(user);
            }

            if (text.equals(ButtonConst.DOWNLOAD)) {
                applicantsButtonClick(user);
            }

            if (Steps.NEW_USER.equals(user.getStep()) || ButtonConst.BACK_MAIN_MENU.equals(text)) {
                sendMessage.setText("Admin Panel.....");
                sendMessage.setReplyMarkup(buttonService.adminMenu());
                execute(sendMessage);
            }

            return;
        }

        if (!user.getHasFullName()) {
            if (Steps.NEW_USER.equals(user.getStep())) {
                SendVideo sendVideo = new SendVideo();

                sendVideo.setVideo(new InputFile(VIDEO_MSG_FILE_ID));
                sendVideo.setChatId(user.getChatId());
                execute(sendVideo);


                scheduler.schedule(() -> {
                    sendVideo.setVideo(new InputFile(VIDEO_FILE_ID));
                    sendVideo.setParseMode("MarkdownV2");

                    sendVideo.setCaption("""
                            ‼️ *Ko\\'rish muhim* ‼️

                            Bu videoda:

                            \\- _Kerakli instrumentlarni qayerdan olish mumkinligi_
                            
                            \\- _Tadbirkorlar uchun qanday foydalar bera olishimiz_
                            
                            \\- _O\\'zimiz haqimizda to\\'liq gapirib berganmiz_
                            """);
                    try {
                        execute(sendVideo);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }, 60, TimeUnit.SECONDS);

                scheduler.schedule(() -> {
                    try {
                        sendMessage.setText("Botdan to'laqonli foydalanish va Bepul darsliklarni qo'lga kiritish uchun ro'yxatdan o'ting\uD83D\uDC47");
                        execute(sendMessage);

                        sendMessage.setText(MessageConst.FULL_NAME_MSG);
                        execute(sendMessage);
                        user.setStep(Steps.ASK_FULL_NAME);
                        userRepository.save(user);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }, 180, TimeUnit.SECONDS);
                return;
            }

            if (Steps.ASK_FULL_NAME.equals(user.getStep())) {
                if ("error_text".equals(text)) {
                    sendMessage.setText(MessageConst.FULL_NAME_MSG);
                    execute(sendMessage);
                    return;
                } else {
                    user.setFullName(text);
                    user.setHasFullName(true);
                    user.setStep(Steps.PHONE_NUMBER);
                    userRepository.save(user);
                }
            }
        }

        if (!user.getHasPhoneNumber()) {
            if (Steps.PHONE_NUMBER.equals(user.getStep())) {
                sendMessage.setText(MessageConst.PHONE_NUMBER_MSG);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT));
                execute(sendMessage);

                user.setStep(Steps.PHONE_NUMBER_ASK);
                userRepository.save(user);
                return;
            }

            if (Steps.PHONE_NUMBER_ASK.equals(user.getStep())) {
                if (!update.hasMessage() || !update.getMessage().hasContact()) {
                    sendMessage.setText(MessageConst.PHONE_NUMBER_MSG);
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_CONTACT));
                    execute(sendMessage);
                    return;
                } else {
                    user.setPhoneNumber(update.getMessage().getContact().getPhoneNumber());
                    user.setHasPhoneNumber(true);
                    user.setStep(Steps.BUSINESS_TYPE);
                    userRepository.save(user);
                }
            }

        }


        if (!user.getHasEmploymentActivity()) {
            if (Steps.BUSINESS_TYPE.equals(user.getStep())) {
                sendMessage.setText(MessageConst.EMPLOYMENT_ACTIVITY_MSG);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);

                user.setStep(Steps.BUSINESS_TYPE_ASK);
                userRepository.save(user);
                return;
            }

            if (Steps.BUSINESS_TYPE_ASK.equals(user.getStep())) {
                if ("error_text".equals(text)) {
                    sendMessage.setText(MessageConst.EMPLOYMENT_ACTIVITY_MSG);
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                    execute(sendMessage);
                    return;
                } else {
                    user.setEmploymentActivity(text);
                    user.setHasEmploymentActivity(true);
                    user.setStep(Steps.EMPLOYEE_COUNT);
                    userRepository.save(user);
                }
            }
        }

        sendMessage.setText("""
                Ro'yxatdan muvaffaqiyatli o'tdingiz!

                Quyidagi havola orqali va'da qilingan darsliklarni olishingiz mumkin\uD83D\uDC47""");
        sendMessage.setReplyMarkup(buttonService.guideList1());
        execute(sendMessage);
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


    private void sendMessageToUser(String recipientChatId, Message message) {
        try {
            if (message.hasText()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(recipientChatId);
                sendMessage.setText(message.getText());
                execute(sendMessage);
            } else if (message.hasPhoto()) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(recipientChatId);
                sendPhoto.setPhoto(new InputFile(message.getPhoto().get(0).getFileId())); // Gets the first photo
                if (message.getCaption() != null) {
                    sendPhoto.setCaption(message.getCaption());
                }
                execute(sendPhoto);
            } else if (message.hasVideo()) {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(recipientChatId);
                sendVideo.setVideo(new InputFile(message.getVideo().getFileId()));
                if (message.getCaption() != null) {
                    sendVideo.setCaption(message.getCaption());
                }
                execute(sendVideo);
            } else if (message.hasDocument()) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(recipientChatId);
                sendDocument.setDocument(new InputFile(message.getDocument().getFileId()));
                if (message.getCaption() != null) {
                    sendDocument.setCaption(message.getCaption());
                }
                execute(sendDocument);
            } else if (message.hasAudio()) {
                SendAudio sendAudio = new SendAudio();
                sendAudio.setChatId(recipientChatId);
                sendAudio.setAudio(new InputFile(message.getAudio().getFileId()));
                if (message.getCaption() != null) {
                    sendAudio.setCaption(message.getCaption());
                }
                execute(sendAudio);
            } else if (message.hasAnimation()) {
                SendAnimation sendAnimation = new SendAnimation();
                sendAnimation.setChatId(recipientChatId);
                sendAnimation.setAnimation(new InputFile(message.getAnimation().getFileId()));
                if (message.getCaption() != null) {
                    sendAnimation.setCaption(message.getCaption());
                }
                execute(sendAnimation);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle any sending issues here
        }
    }


    private void applicantsButtonClick(User user) {
        SendMessage sendMessage = getSendMessage(user);
        try {
            exportUsersDataToExcel(userService.applicantsExcel(), sendMessage, user);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void exportUsersDataToExcel(java.io.File file, SendMessage sendMessage, User user) throws TelegramApiException {
        if (file == null) {
            sendMessage.setText("Fayl generatsiya qilishda xatolik!");
            execute(sendMessage);
            return;
        }
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(user.getChatId());
        sendDocument.setDocument(new InputFile(file));
        execute(sendDocument);
    }

}
