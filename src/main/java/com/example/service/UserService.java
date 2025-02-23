package com.example.service;

import com.example.entity.User;
import com.example.entity.UserRepository;
import com.example.util.Steps;
import com.example.util.UserStatus;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;



    public User createOrGetUser(Update update) {
        String chatId = getChatId(update);
        Optional<User> userOptional = userRepository.findByChatId(chatId);
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setStep(Steps.NEW_USER);
            return userRepository.save(user);
        }
        return userOptional.get();
    }

    public String getText(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                return update.getMessage().getText();
            }
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getText();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getText();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getText();
        }
        return "error_text";
    }

    private String getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getChatId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getChatId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId().toString();
        } else {
            return update.getMessage().getChatId().toString();
        }
    }


    public File applicantsExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Data");

            // Create the header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("F.I.O");
            headerRow.createCell(1).setCellValue("Telefon raqami");
            headerRow.createCell(2).setCellValue("Faoliyati");

            int rowNum = 1;

            List<User> userList = userRepository.findAll();
            for (User applicant : userList) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(applicant.getFullName());
                dataRow.createCell(1).setCellValue(applicant.getPhoneNumber());
                dataRow.createCell(2).setCellValue(applicant.getEmploymentActivity());
            }
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            File excelFile = File.createTempFile("applicants-", ".xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            }

            return excelFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
