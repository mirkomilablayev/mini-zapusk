package com.example.payme.payme.dto.cardscreate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    @JsonProperty("number")
    private String number;
    @JsonProperty("expire")
    private String expire;
    @JsonProperty("token")
    private String token;
    @JsonProperty("recurrent")
    private Boolean recurrent;
    @JsonProperty("verify")
    private Boolean verify;
    @JsonProperty("type")
    private String type;

}
