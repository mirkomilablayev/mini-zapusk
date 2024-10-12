package com.example.payme.payme.service;


import com.example.payme.payme.dto.cardscreate.Card;
import com.example.payme.payme.dto.cardscreate.PaymeCardsCreateRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeResponse;
import com.example.payme.payme.dto.getverifycode.VerifyCodeRequest;
import com.example.payme.payme.dto.receipts.PayRequest;
import com.example.payme.payme.dto.receipts.ReceiptsResponse;

public interface PaymeService {

    Card cardsCreate(PaymeCardsCreateRequest request) throws Exception;
    GetVerifyCodeResponse cardsGetVerifyCode(GetVerifyCodeRequest request) throws Exception;
    Card cardsVerify(VerifyCodeRequest request) throws Exception;
    ReceiptsResponse receiptsCreate(Long amount) throws Exception;
    ReceiptsResponse receiptsPay(PayRequest payRequest) throws Exception;



}
