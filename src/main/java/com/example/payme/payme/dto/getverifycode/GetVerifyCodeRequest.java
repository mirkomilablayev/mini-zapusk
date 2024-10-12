package com.example.payme.payme.dto.getverifycode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetVerifyCodeRequest {

    private String token;

}
