package com.example.payme.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardCreateResponse {

    private String phone;
    private String message;
    private Long transactionId;

}
