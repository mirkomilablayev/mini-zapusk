package com.example.payme.payme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymeRequest {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private Map<String, Object> params;

}
