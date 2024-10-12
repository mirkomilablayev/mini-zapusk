package com.example.payme.payme.dto.getverifycode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetVerifyCodeResponse {

    @JsonProperty("sent")
    private Boolean sent;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("wait")
    private Long wait;

}
