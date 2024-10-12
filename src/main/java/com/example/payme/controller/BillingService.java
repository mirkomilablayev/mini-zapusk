package com.example.payme.controller;


import com.example.payme.dto.CommonResponse;
import com.example.payme.dto.billing.CardCreateRequest;
import com.example.payme.dto.billing.CardCreateResponse;
import com.example.payme.dto.billing.VerifyRequest;
import com.example.payme.dto.billing.VerifyResponse;
import com.example.payme.entity.Transaction;
import com.example.payme.entity.TransactionRepository;
import com.example.payme.entity.TransactionStatus;
import com.example.payme.exception.Errors;
import com.example.payme.exception.GenericException;
import com.example.payme.payme.dto.cardscreate.Card;
import com.example.payme.payme.dto.cardscreate.PaymeCardsCreateRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeRequest;
import com.example.payme.payme.dto.getverifycode.GetVerifyCodeResponse;
import com.example.payme.payme.dto.getverifycode.VerifyCodeRequest;
import com.example.payme.payme.dto.receipts.PayRequest;
import com.example.payme.payme.dto.receipts.ReceiptsResponse;
import com.example.payme.payme.service.PaymeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class BillingService {

    private final PaymeService paymeService;
    private final TransactionRepository transactionRepository;

    public CommonResponse<CardCreateResponse> getVerifyCode(CardCreateRequest request) throws Exception {

        if (request.getNumber() != null && request.getNumber().length() != 16) {
            return new CommonResponse<>(false, "Karta Raqamida Xatolik topildi");
        }
        if (request.getExpire() != null && request.getExpire().length() != 4) {
            return new CommonResponse<>(false, "Kartaning amal qilish muddatida xatolik topildi");
        }
        if (request.getAmount() != null && request.getAmount() < 0L) {
            return new CommonResponse<>(false, "To'lov so'mmasida xatolik aniqlandi");
        }


        PaymeCardsCreateRequest cardsCreateRequest = new PaymeCardsCreateRequest();
        cardsCreateRequest.setNumber(request.getNumber());
        cardsCreateRequest.setExpire(request.getExpire());

        Card card = paymeService.cardsCreate(cardsCreateRequest);

        Transaction transaction = new Transaction();
        transaction.setUserChatId(request.getUserChatId());
        transaction.setCardNumber(card.getNumber());
        transaction.setCardExpire(card.getExpire());
        transaction.setCardToken(card.getToken());
        transaction.setAmount(request.getAmount());

        GetVerifyCodeResponse getVerifyCode = paymeService.cardsGetVerifyCode(new GetVerifyCodeRequest(card.getToken()));

        transaction.setCardPhone(getVerifyCode.getPhone());
        transaction.setWait(getVerifyCode.getWait());
        transaction.setOtpSent(getVerifyCode.getSent());
        transaction.setStatus(TransactionStatus.CREATED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new CommonResponse<>(true, Errors.SUCCESS.getMessage(), new CardCreateResponse(savedTransaction.getCardPhone(), "Tastiqlash codini kiriting. Tastiqlash Kodi " + savedTransaction.getCardPhone() + " ga yuborildi!", savedTransaction.getId()));

    }


    public CommonResponse<VerifyResponse> verify(VerifyRequest request) throws Exception {
        return verifyPayment(request);
    }


    private CommonResponse<VerifyResponse> verifyPayment(VerifyRequest request) throws Exception {
        if (request.getCode() == null || request.getCode().length() != 6) {
            return new CommonResponse<>(false, "Tastiqlash kodi xato kiritildi!");
        }
        Transaction transaction = transactionRepository.findById(request.getTransactionId()).orElseThrow(() -> new GenericException(Errors.NOT_FOUND));
        Card card = paymeService.cardsVerify(new VerifyCodeRequest(transaction.getCardToken(), request.getCode()));
        if (!card.getVerify()) throw new GenericException("Karta Tastiqlanmadi");

        ReceiptsResponse receiptsResponse = paymeService.receiptsCreate(transaction.getAmount());
        ReceiptsResponse receiptsPay = paymeService.receiptsPay(new PayRequest(receiptsResponse.getId(), transaction.getCardToken()));

        transaction.setReceiptId(receiptsPay.getId());
        transaction.setCreateTime(receiptsPay.getCreateTime());
        transaction.setPayTime(receiptsPay.getPayTime());
        transaction.setCurrency(receiptsPay.getCurrency());
        transaction.setState(receiptsPay.getState());
        transaction.setStatus(receiptsPay.getState() == 4 ? TransactionStatus.PAYED : TransactionStatus.CANCELED);
        Transaction savedTransaction = transactionRepository.save(transaction);
        if (TransactionStatus.CANCELED.equals(transaction.getStatus()))
            return new CommonResponse<>(false, "To'lov rad etildi!");
        return new CommonResponse<>(getVerifyResponse(savedTransaction));
    }

    private static VerifyResponse getVerifyResponse(Transaction savedTransaction) {
        VerifyResponse verifyResponse = new VerifyResponse();
        verifyResponse.setTransactionId(savedTransaction.getId());
        verifyResponse.setStatus(savedTransaction.getStatus());
        verifyResponse.setCurrency(savedTransaction.getCurrency());
        verifyResponse.setAmount(savedTransaction.getAmount());
        verifyResponse.setPayerCard(savedTransaction.getCardNumber());
        verifyResponse.setTransactionTime(savedTransaction.getPayTime());
        verifyResponse.setPayerCardPhone(savedTransaction.getCardPhone());
        return verifyResponse;
    }


}
