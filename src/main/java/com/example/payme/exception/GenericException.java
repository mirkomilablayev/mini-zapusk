package com.example.payme.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenericException extends RuntimeException {
    private Errors errors = Errors.UNKNOWN_ERROR;
    private String message;

    public GenericException(Errors errors){
        this.errors = errors;
    }

    public GenericException(String errorMessage){
        this.message = errorMessage;
    }
}
