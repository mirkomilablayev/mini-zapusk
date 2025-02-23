package com.example.service;

import com.example.util.ButtonConst;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

@Service
@AllArgsConstructor
public class ButtonService {


    public ReplyKeyboard createButton(String buttonText) {
        if (ButtonConst.EMPTY.equals(buttonText)) {
            return createKeyboardRemove();
        } else {
            return createKeyboardWithText(buttonText);
        }
    }

    public ReplyKeyboard adminMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardButton keyboardButton = new KeyboardButton(ButtonConst.SHARE_ADS);
        KeyboardButton keyboardButton2 = new KeyboardButton(ButtonConst.DOWNLOAD);

        KeyboardRow keyboardRow = new KeyboardRow(Collections.singletonList(keyboardButton));
        keyboardRowList.add(keyboardRow);
        keyboardRow = new KeyboardRow(Collections.singletonList(keyboardButton2));
        keyboardRowList.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboard createKeyboardRemove() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }

    private ReplyKeyboard createKeyboardWithText(String buttonText) {
        KeyboardButton keyboardButton = new KeyboardButton(buttonText);
        if (ButtonConst.SHARE_CONTACT.equals(buttonText)) keyboardButton.setRequestContact(true);
        KeyboardRow keyboardRow = new KeyboardRow(Collections.singletonList(keyboardButton));
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(keyboardRow)
                .build();
    }


    public ReplyKeyboard enterChannel(String channelUsername) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton channel = new InlineKeyboardButton();
        channel.setText("Kanalga o'tish ➡️");
        channel.setUrl("t.me/" + channelUsername.replace("@", ""));
//        channel.setCallbackData("chooseLanguage-uzb");
        navigationRow.add(channel);
        rowsInline.add(navigationRow);

        InlineKeyboardButton check = new InlineKeyboardButton();
        check.setText("Obuna bo'ldim ✅");
//        check.setUrl("t.me/" + channelUsername.replace("@", ""));
        check.setCallbackData("check_me");
        navigationRow = new ArrayList<>();
        navigationRow.add(check);
        rowsInline.add(navigationRow);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }


    public InlineKeyboardMarkup guideList() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("PDF Intrument");
        button1.setUrl("https://t.me/Cubic_Business/3");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Video Darslik");
        button2.setUrl("https://t.me/Cubic_Business/5");
        InlineKeyboardButton button3 = new InlineKeyboardButton("Video Keys");
        button3.setUrl("https://t.me/Cubic_Business/4");
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        row1.add(button2);
        row1.add(button3);
        keyboardMarkup.setKeyboard(Collections.singletonList(row1));
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup guideList1() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("PDF Instrument");
        button1.setUrl("https://t.me/Cubic_Business/3");

        InlineKeyboardButton button2 = new InlineKeyboardButton("Video Darslik");
        button2.setUrl("https://t.me/Cubic_Business/5");

        InlineKeyboardButton button3 = new InlineKeyboardButton("Video Keys");
        button3.setUrl("https://t.me/Cubic_Business/4");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(button2);
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(button3);

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        keyboardMarkup.setKeyboard(rowsInline);
        return keyboardMarkup;
    }


}

