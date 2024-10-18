package com.example.service;

import com.example.configuration.BotConfiguration;
import com.example.dto.FileDto;
import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.payme.controller.BillingService;
import com.example.payme.dto.CommonResponse;
import com.example.payme.dto.billing.CardCreateRequest;
import com.example.payme.dto.billing.CardCreateResponse;
import com.example.payme.dto.billing.VerifyRequest;
import com.example.payme.dto.billing.VerifyResponse;
import com.example.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final UserService userService;
    private final ButtonService buttonService;
    private final BillingService billingService;
    //    private final AdminService adminService;
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

        if (!user.getHasFullName()) {
            if (Steps.NEW_USER.equals(user.getStep())) {
                sendMessage.setText(MessageConst.START_MESSAGE);
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
                    user.setFirstPart(Boolean.TRUE);
                    user.setStep(Steps.EMPLOYEE_COUNT);
                    userRepository.save(user);
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
        }

        if (!user.getPremium()) {
            if (user.getPaymentProcessTime() == null || Duration.between(user.getPaymentProcessTime(), LocalDateTime.now()).toMinutes() > 5) {
                sendMessage.setText(MessageConst.PAYMENT_MESSAGE);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);

                sendMessage.setText(MessageConst.CARD_NUMBER_MESSAGE);
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);

                user.setPaymentProcessTime(LocalDateTime.now());
                user.setStep(Steps.ASK_CARD_NUMBER);
                userRepository.save(user);
                return;
            }

            if (Steps.ASK_CARD_NUMBER.equals(user.getStep())) {
                text = text.replace(" ", "");
                if ("error_text".equals(text) || text.length() != 16) {
                    sendMessage.setText(MessageConst.CARD_NUMBER_MESSAGE);
                } else {
                    user.setCardNumber(text);
                    user.setStep(Steps.ASK_CARD_EXPIRE);
                    userRepository.save(user);
                    sendMessage.setText(MessageConst.CARD_EXPIRE_MSG);
                }
                sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                execute(sendMessage);
                return;
            }

            if (Steps.ASK_CARD_EXPIRE.equals(user.getStep())) {
                text = text.replace(" ", "").replace("/", "");
                if ("error_text".equals(text) || text.length() != 4) {
                    sendMessage.setText(MessageConst.CARD_EXPIRE_MSG);
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                    execute(sendMessage);
                } else {
                    try {
                        CommonResponse<CardCreateResponse> commonResponse = billingService.getVerifyCode(new CardCreateRequest(user.getCardNumber(), text, 900000L, user.getChatId()));
                        if (commonResponse.getSuccess()) {
                            user.setCardNumber(text);
                            user.setStep(Steps.ASK_VERIFY_CODE);
                            user.setTransactionId(commonResponse.getItem().getTransactionId());
                            userRepository.save(user);
                            sendMessage.setText("Tastiqlash kodini kiriting!\nKod " + commonResponse.getItem().getPhone() + " ga yuborildi!");
                            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                            execute(sendMessage);
                        } else {
                            sendMessage.setText(commonResponse.getMessage());
                            execute(sendMessage);
                            sendMessage.setText(MessageConst.CARD_NUMBER_MESSAGE);
                            execute(sendMessage);
                            user.setPaymentProcessTime(null);
                            userRepository.save(user);
                        }
                    } catch (Exception e) {
                        sendMessage.setText(e.getMessage());
                        execute(sendMessage);
                        sendMessage.setText(MessageConst.CARD_NUMBER_MESSAGE);
                        execute(sendMessage);
                        user.setPaymentProcessTime(null);
                        userRepository.save(user);
                    }
                }
                return;
            }

            if (Steps.ASK_VERIFY_CODE.equals(user.getStep())) {
                text = text.replace(" ", "");
                if ("error_text".equals(text) || text.length() != 6) {
                    sendMessage.setText("Iltimos tastiqlash kodini to'g'ri kiriting!");
                    sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.EMPTY));
                    execute(sendMessage);
                } else {
                    try {
                        CommonResponse<VerifyResponse> commonResponse = billingService.verify(new VerifyRequest(text, user.getTransactionId()));
                        if (commonResponse.getSuccess()) {
                            user.setCardNumber(null);
                            user.setPaymentProcessTime(null);
                            user.setCardExp(null);
                            user.setTransactionId(null);
                            user.setPremium(true);
                            userRepository.save(user);
                            sendMessage.setText("To'lov muvaffaqiyatli amalga oshirildi ✅");
                            sendMessage.setReplyMarkup(buttonService.createButton(ButtonConst.GET_FILE));
                            execute(sendMessage);
                        } else {
                            sendMessage.setText(commonResponse.getMessage());
                            execute(sendMessage);
                            sendMessage.setText(MessageConst.CARD_NUMBER_MESSAGE);
                            execute(sendMessage);
                            user.setPaymentProcessTime(null);
                            userRepository.save(user);
                        }
                    } catch (Exception e) {
                        sendMessage.setText(e.getMessage());
                        execute(sendMessage);
                        sendMessage.setText(MessageConst.PAYMENT_MESSAGE);
                        execute(sendMessage);
                        user.setPaymentProcessTime(null);
                        userRepository.save(user);
                    }
                }
                return;
            }

            if (ButtonConst.GET_FILE.equals(text) && user.getPremium()) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(user.getChatId());
                sendDocument.setDocument(new InputFile(FILE_ID));
                execute(sendDocument);
            } else {
                sendMessage.setText("Noto'g'ri buyruq kiritildi \uD83D\uDE33");
                sendMessage.setReplyMarkup(buttonService.createButton(user.getPremium() ? ButtonConst.GET_FILE : ButtonConst.EMPTY));
                execute(sendMessage);
            }
        }
        if (ButtonConst.GET_FILE.equals(text) && user.getPremium()) {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(user.getChatId());
            sendDocument.setDocument(new InputFile(FILE_ID));
            execute(sendDocument);
        } else {
            sendMessage.setText("Noto'g'ri buyruq kiritildi \uD83D\uDE33");
            sendMessage.setReplyMarkup(buttonService.createButton(user.getPremium() ? ButtonConst.GET_FILE : ButtonConst.EMPTY));
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


    @Scheduled(initialDelay = 12 * 60 * 60 * 1000, fixedRate = 12 * 60 * 60 * 1000)
    // Initial delay and every 12 hours in milliseconds
    public void pushNotification() {
        System.out.println("Function executed at: " + System.currentTimeMillis());
    }


}
