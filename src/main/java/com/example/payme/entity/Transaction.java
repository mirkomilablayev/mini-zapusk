package com.example.payme.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userChatId;
    private String cardNumber;
    private String cardExpire;
    @Column(columnDefinition = "VARCHAR(3200)")
    private String cardToken;
    private String cardPhone;
    private Long wait;
    private Boolean otpSent;
    private String otpCode;
    private String receiptId;
    private Long createTime;
    private Long payTime;
    private Long amount;
    private Long currency;
    private Long state;
    private String status = TransactionStatus.CREATED;

}
