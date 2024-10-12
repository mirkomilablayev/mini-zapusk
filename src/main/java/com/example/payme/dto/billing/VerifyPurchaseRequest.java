package com.example.payme.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyPurchaseRequest {
    private String code;
    private Long transactionId;
    private Long courseId;
}
