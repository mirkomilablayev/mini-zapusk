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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatCodePointException;
import java.util.List;

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

}

