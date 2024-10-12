package com.example.payme.payme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymeResponse {
    private Boolean success;
    private String message;
    private String data;
}
