package com.example.payme.payme.dto.cardscreate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymeCardsCreateRequest {

    private String number;
    private String expire;

}
