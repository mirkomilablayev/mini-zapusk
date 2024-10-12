package com.example.payme.dto.billing;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardCreateRequest {

    private String number;
    private String expire;
    private Long amount;
    private String userChatId;

}
