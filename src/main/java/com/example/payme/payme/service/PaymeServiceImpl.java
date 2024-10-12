package com.example.payme.payme.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.payme.exception.GenericException;
import com.example.payme.payme.dto.PaymeRequest;
import com.example.payme.payme.dto.PaymeResponse;
import com.example.payme.payme.dto.cardscreate.Card;
import com.example.payme.payme.dto.cardscreate.PaymeCardsCreateRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeResponse;
import com.example.payme.payme.dto.getverifycode.VerifyCodeRequest;
import com.example.payme.payme.dto.receipts.PayRequest;
import com.example.payme.payme.dto.receipts.ReceiptsResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class PaymeServiceImpl implements PaymeService {

// test
    private static final String ID = "5e730e8e0b852a417aa49ceb";
    private static final String KEY = "ZPDODSiTYKuX0jyO7Kl2to4rQbNwG08jbghj";
// prod
//    private static final String ID = "656490f894dc4293bdd3cfd4";
//    private static final String KEY = "CV220Q3trURsGfH2YrBX1?0VzrSpmcBRgDUO";

    private final PaymeSenderService paymeSenderService;

    @Override
    public Card cardsCreate(PaymeCardsCreateRequest request) throws RuntimeException{
        Map<String, Object> params = new HashMap<>();
        params.put("card", request);
        params.put("save", false);

        PaymeRequest paymeRequest = new PaymeRequest();
        paymeRequest.setId(1);
        paymeRequest.setMethod("cards.create");
        paymeRequest.setParams(params);
        try {
            System.out.println(new ObjectMapper().writeValueAsString(paymeRequest));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        PaymeResponse paymeResponse = paymeSenderService.send(paymeRequest, ID, "");

        if (paymeResponse.getSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String data = paymeResponse.getData();
                JsonNode jsonNode = objectMapper.readTree(data).get("card");
                return new ObjectMapper().readValue(jsonNode.toString(), Card.class);
            } catch (JsonProcessingException e) {
                throw new GenericException(e.getMessage());
            }
        } else {
            throw new GenericException(paymeResponse.getMessage());
        }


    }

    @Override
    public GetVerifyCodeResponse cardsGetVerifyCode(GetVerifyCodeRequest request) throws RuntimeException{
        Map<String, Object> params = new HashMap<>();
        params.put("token", request.getToken());

        PaymeRequest paymeRequest = new PaymeRequest();
        paymeRequest.setId(1);
        paymeRequest.setMethod("cards.get_verify_code");
        paymeRequest.setParams(params);

        PaymeResponse paymeResponse = paymeSenderService.send(paymeRequest, ID, "");

        if (paymeResponse.getSuccess()) {
            try {
                String data = paymeResponse.getData();
                return new ObjectMapper().readValue(data, GetVerifyCodeResponse.class);
            } catch (JsonProcessingException e) {
                throw new GenericException(e.getMessage());
            }
        } else {
            throw new GenericException(paymeResponse.getMessage());
        }
    }

    @Override
    public Card cardsVerify(VerifyCodeRequest request) throws RuntimeException{
        Map<String, Object> params = new HashMap<>();
        params.put("token", request.getToken());
        params.put("code", request.getCode());

        PaymeRequest paymeRequest = new PaymeRequest();
        paymeRequest.setId(1);
        paymeRequest.setMethod("cards.verify");
        paymeRequest.setParams(params);
        PaymeResponse paymeResponse = paymeSenderService.send(paymeRequest, ID, "");

        if (paymeResponse.getSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String data = paymeResponse.getData();
                JsonNode jsonNode = objectMapper.readTree(data).get("card");
                return new ObjectMapper().readValue(jsonNode.toString(), Card.class);
            } catch (JsonProcessingException e) {
                throw new GenericException(e.getMessage());
            }
        } else {
            throw new GenericException(paymeResponse.getMessage());
        }
    }

    @Override
    public ReceiptsResponse receiptsCreate(Long amount) throws RuntimeException{
        Map<String, String> account = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        account.put("order_id", "prodigital");
        params.put("account", account);

        PaymeRequest paymeRequest = new PaymeRequest();
        paymeRequest.setId(1);
        paymeRequest.setMethod("receipts.create");
        paymeRequest.setParams(params);
        PaymeResponse paymeResponse = paymeSenderService.send(paymeRequest, ID, KEY);

        if (paymeResponse.getSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String data = paymeResponse.getData();
                JsonNode jsonNode = objectMapper.readTree(data).get("receipt");
                System.out.println(jsonNode);
                return new ObjectMapper().readValue(jsonNode.toString(), ReceiptsResponse.class);
            } catch (JsonProcessingException e) {
                throw new GenericException(e.getMessage());
            }
        } else {
            throw new GenericException(paymeResponse.getMessage());
        }
    }

    @Override
    public ReceiptsResponse receiptsPay(PayRequest payRequest) throws RuntimeException{
        Map<String, Object> params = new HashMap<>();
        params.put("id", payRequest.getId());
        params.put("token", payRequest.getToken());

        PaymeRequest paymeRequest = new PaymeRequest();
        paymeRequest.setId(1);
        paymeRequest.setMethod("receipts.pay");
        paymeRequest.setParams(params);
        PaymeResponse paymeResponse = paymeSenderService.send(paymeRequest, ID, KEY);

        if (paymeResponse.getSuccess()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String data = paymeResponse.getData();
                JsonNode jsonNode = objectMapper.readTree(data).get("receipt");
                System.out.println(jsonNode);
                return new ObjectMapper().readValue(jsonNode.toString(), ReceiptsResponse.class);
            } catch (JsonProcessingException e) {
                throw new GenericException(e.getMessage());
            }
        } else {
            throw new GenericException(paymeResponse.getMessage());
        }
    }

}
