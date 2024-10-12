package com.example.payme.exception;


import lombok.Getter;

@Getter
public enum Errors {
    SUCCESS("muvaffaqiyatli"),
    UNKNOWN_ERROR("Nomalum xatolik"),
    NOT_FOUND("Hechnarsa topilmadi"),
    SUCCESSFULLY_CREATED("Muvaffaqiyatli yaratildi");
    private final String message;

    Errors(String message) {
        this.message = message;
    }

}
