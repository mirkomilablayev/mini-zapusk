package com.example.payme.payme.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.payme.exception.Errors;
import com.example.payme.exception.GenericException;
import com.example.payme.payme.dto.PaymeRequest;
import com.example.payme.payme.dto.PaymeResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class PaymeSenderService {

    private static final String URL = "https://checkout.test.paycom.uz/api"; // test
//    private static final String URL = "https://checkout.paycom.uz/api"; // prod

    private final RestTemplate restTemplate;


    public PaymeResponse send(PaymeRequest request, String id, String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth", id + (key.isEmpty() ? "" : ":" + key));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cache-Control", "no-cache");
        headers.set("Accept-Language", "uz");
        try {
            System.out.println(new ObjectMapper().writeValueAsString(headers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpEntity<PaymeRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                URL,
                requestEntity,
                String.class
        );

        String body = responseEntity.getBody();
        return extractResponseFromJson(body != null ? body : "");
    }


    private PaymeResponse extractResponseFromJson(String s) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(s);
            if (jsonNode.has("result")) {
                String result = jsonNode.get("result").toString();
                return new PaymeResponse(true, Errors.SUCCESSFULLY_CREATED.getMessage(), result);
            } else if (jsonNode.has("error")) {
                JsonNode errorNode = jsonNode.get("error");
                return new PaymeResponse(false, errorNode.get("message").asText(""), null);
            } else {
                return new PaymeResponse(false, Errors.UNKNOWN_ERROR.getMessage(), null);
            }
        } catch (JsonProcessingException e) {
            throw new GenericException(e.getMessage());
        }
    }


}
