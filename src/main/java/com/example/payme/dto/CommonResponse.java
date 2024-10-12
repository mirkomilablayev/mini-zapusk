package com.example.payme.dto;

import com.example.payme.exception.Errors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommonResponse <T>{
    private Boolean success;
    private String message;
    private T item;

    public CommonResponse(T item) {
        this.item = item;
        this.message = Errors.SUCCESS.getMessage();
        this.success = true;
    }

    public CommonResponse(Boolean success, T item) {
        this.item = item;
        this.message = Errors.SUCCESS.getMessage();
        this.success = success;
    }

    public CommonResponse(Boolean success, String message) {
        this.message = message;
        this.success = success;
    }
}
