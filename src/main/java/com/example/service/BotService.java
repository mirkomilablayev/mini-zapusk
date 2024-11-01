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

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final ButtonService buttonService;
    private final UserRepository userRepository;


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
        SendMessage sendMessage = getSendMessage(user);


        if ("1813855034".equals(user.getChatId())) {

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
                    user.setStep(Steps.NEW_USER);
                    userRepository.save(user);
                    sendMessage.setText("Reklama kontentida xatolik bor");
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_ADS));
                    execute(sendMessage);
                }
            }

            if (Steps.NEW_USER.equals(user.getStep()) || ButtonConst.BACK_MAIN_MENU.equals(text)) {
                sendMessage.setText("Admin Panel.....");
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.SHARE_ADS));
                execute(sendMessage);
            }

            return;
        }

        if (!user.getHasFullName()) {
            if (Steps.NEW_USER.equals(user.getStep())) {
                sendMessage.setText(MessageConst.START_MESSAGE);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);
                sendMessage.setText(MessageConst.FULL_NAME_MSG);
                execute(sendMessage);
                user.setStep(Steps.ASK_FULL_NAME);
                userRepository.save(user);
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
                    user.setStatus(UserStatus.NOT_JOINED);
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

        if (!user.getHasNumberOfEmployees()) {
            if (Steps.EMPLOYEE_COUNT.equals(user.getStep())) {
                sendMessage.setText(MessageConst.EMPLOYEE_COUNT);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);

                user.setStep(Steps.EMPLOYEE_COUNT_ASK);
                userRepository.save(user);
                return;
            }

            if (Steps.EMPLOYEE_COUNT_ASK.equals(user.getStep())) {
                if ("error_text".equals(text)) {
                    sendMessage.setText(MessageConst.EMPLOYEE_COUNT);
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                    execute(sendMessage);
                    return;
                } else {
                    user.setNumberOfEmployees(text);
                    user.setHasNumberOfEmployees(true);
                    user.setStep(Steps.EMPLOYEE_COUNT);
                    userRepository.save(user);

                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.GET_FILE));
                    sendMessage.setText("Siz gaydlarni olishingiz mumkin!");
                    execute(sendMessage);
                    return;
                }
            }
        }

        if (!userService.isSubscribed(botConfiguration.getChannelUsername(), botConfiguration.getToken(), user.getChatId())) {
            if (!Steps.JOIN_CHANNEL.equals(user.getStep())) {
                user.setStep(Steps.JOIN_CHANNEL);
                userRepository.save(user);
            }

            SendMessage message = SendMessage.builder().chatId(user.getChatId()).text(MessageConst.JOIN_CHANNEL_MESSAGE).replyMarkup(buttonService.enterChannel(botConfiguration.getChannelUsername())).build();
            execute(message);
            return;
        } else {
            if (!UserStatus.JOINED_CHANNEL.equals(user.getStatus())) {
                user.setStatus(UserStatus.JOINED_CHANNEL);
                userRepository.save(user);
            }
        }

        if (ButtonConst.GET_FILE.equals(text)) {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(user.getChatId());
            sendDocument.setDocument(new InputFile(FILE_ID));
            execute(sendDocument);
        } else {
            sendMessage.setText("Noto'g'ri buyruq kiritildi \uD83D\uDE33");
            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.GET_FILE));
            execute(sendMessage);
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


    // Initial delay and every 12 hours in milliseconds
    @Scheduled(initialDelay = 12 * 60 * 60 * 1000, fixedRate = 12 * 60 * 60 * 1000)
    public void pushNotification() throws Exception {
        for (User user : userRepository.findAllByStatus(UserStatus.STARTER)) {
            SendMessage sendMessage = getSendMessage(user);
            sendMessage.setText("""
                    Salom! Siz bilan Cubic Assistant!
                    
                    Biz bilan birinchi qadamni tashlaganingizni ko‘rdik, lekin jarayonni yakunlashni unutib qo‘ydingiz! \uD83D\uDE0A
                    
                    Xalqaro standartlar asosida marketing bo‘limini yaratish bo‘yicha PRAKTIKUMda ishtirok etish uchun ism va telefon raqamingizni qoldirishingiz kerak. Unutmang bu sizga biznesdagi sotuvlaringiz o'sishiga va marketing strategiyangizni yangi darajaga olib chiqishda yordam beradi! \uD83D\uDE80
                    
                    Nega Praktikumda ishtirok etishingiz kerak?
                    Xalqaro darajada marketing bo‘limini tashkil qilishni o‘rganasiz.
                    Bozorning o‘sish imkoniyatlarini to‘liq anglab yetasiz.
                    Marketing jamoangizni qanday samarali boshqarishni bilib olasiz.
                    
                    
                    \uD83D\uDC49 Jarayonni yakunlab, Praktikumda ishtirok etish uchun hoziroq ism va telefon raqamingizni qoldiring va o‘z biznesingizni yangi darajaga ko‘taring!""");
            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
            execute(sendMessage);
        }


        for (User user : userRepository.findAllByStatus(UserStatus.NOT_JOINED)) {
            SendMessage sendMessage = getSendMessage(user);
            sendMessage.setText("Salom! Siz bilan Cubic Assistant!\n" + "\n" + user.getFullName() + " Workshopga qo'shilishga juda yaqin turibsiz! \uD83D\uDE0A\n" + "\n" + "Siz allaqachon ma'lumotlaringizni qoldirdingiz, endi faqat shartni bajarib, xalqaro darajadagi marketing bo‘limini yaratish bo‘yicha workshopga kirsangiz bo‘ldi. \uD83D\uDE80\n" + "\n");
            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
            execute(sendMessage);
        }
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

}
