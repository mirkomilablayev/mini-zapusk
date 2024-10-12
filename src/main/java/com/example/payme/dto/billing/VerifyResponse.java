package com.example.payme.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResponse {
    private Long transactionId;
    private String payerCard;
    private String payerCardPhone;
    private Long amount;
    private Long transactionTime;
    private Long currency;
    private String status;

}
