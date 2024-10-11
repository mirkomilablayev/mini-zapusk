package com.example.service;

import com.example.util.ButtonConst;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class ButtonService {

    public ReplyKeyboardMarkup shareContactButton() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText(ButtonConst.SHARE_CONTACT);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardMarkup backToMainMenu() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(ButtonConst.BACK_MAIN_MENU);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardRemove createEmptyKeyboard() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }




    public ReplyKeyboardMarkup createButtons(int countPerLine, List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        int i = 0;
        int counter = 0;
        int qoldiq = buttonNames.size() % countPerLine;
        int size = buttonNames.size();
        for (String name : buttonNames) {
            keyboardRow.add(name);
            i++;
            if (i == countPerLine || (size - counter == qoldiq && i == qoldiq)) {
                keyboardRowList.add(keyboardRow);
                keyboardRow = new KeyboardRow();
                counter += i;
                i = 0;
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }

}

