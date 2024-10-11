package com.example.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class InlineButtonService {




    public InlineKeyboardMarkup backToFAQT() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("⬅️");
        back.setCallbackData("faqSetting-back");
        navigationRow.add(back);

        InlineKeyboardButton rejectButton = new InlineKeyboardButton();
        rejectButton.setText("❌");
        rejectButton.setCallbackData("faqSetting-delete");
        navigationRow.add(rejectButton);

        rowsInline.add(navigationRow);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public ReplyKeyboard hotelStar() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton threeStart = new InlineKeyboardButton();
        threeStart.setText("3 ⭐️");
        threeStart.setCallbackData("hotel_star-3");
        navigationRow.add(threeStart);

        InlineKeyboardButton fourStar = new InlineKeyboardButton();
        fourStar.setText("4 ⭐️");
        fourStar.setCallbackData("hotel_star-4");
        navigationRow.add(fourStar);

        InlineKeyboardButton fiveStar = new InlineKeyboardButton();
        fiveStar.setText("5 ⭐️");
        fiveStar.setCallbackData("hotel_star-5");
        navigationRow.add(fiveStar);

        rowsInline.add(navigationRow);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public ReplyKeyboard responseToApplication(Long tourApplicationId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("JAVOB YOZISH");
        back.setCallbackData("answerRequest-" + tourApplicationId);
        navigationRow.add(back);

        rowsInline.add(navigationRow);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public ReplyKeyboard tourPaketApply(Long id) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Ariza Qoldirish ✍️");
        back.setCallbackData("tourPackageApply-" + id);
        navigationRow.add(back);

        rowsInline.add(navigationRow);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }


}
