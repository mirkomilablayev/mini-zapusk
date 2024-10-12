package com.example.payme.payme.dto.receipts;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiptsResponse {

    @JsonProperty("_id")
    private String id;
    @JsonProperty("create_time")
    private Long createTime;
    @JsonProperty("pay_time")
    private Long payTime;
    @JsonProperty("cancel_time")
    private Long cancelTime;
    @JsonProperty("state")
    private Long state;
    @JsonProperty("type")
    private Long type;
    @JsonProperty("external")
    private Boolean external;
    @JsonProperty("operation")
    private Long operation;
//    @JsonProperty("category")
//    private String category;
    @JsonProperty("error")
    private String error;
    @JsonProperty("description")
    private String description;
    @JsonProperty("amount")
    private Long amount;
    @JsonProperty("currency")
    private Long currency;
    @JsonProperty("commission")
    private Long commission;
    @JsonProperty("processing_id")
    private String processingId;


}
