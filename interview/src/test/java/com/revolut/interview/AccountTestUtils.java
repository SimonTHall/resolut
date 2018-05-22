package com.revolut.interview;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

public class AccountTestUtils {

    public static FundTransferRequest createFundTransferRequest(Integer fromAccountNo, Integer toAccountNo, Integer amount) {
        FundTransferRequest request = new FundTransferRequest();
        request.setFromAccountNo(fromAccountNo);
        request.setToAccountNo(toAccountNo);
        request.setAmount(new BigDecimal(amount));
        return request;
    }
    
    public static void assertTransaction(AccountTransaction transaction, Integer expectedId, 
            TransactionType expectedTransType, Integer expectedAmount, Integer expectedBalance) {
        assertThat(transaction.getId()).isEqualTo(expectedId);
        assertThat(transaction.getType()).isEqualTo(expectedTransType);
        assertThat(transaction.getAmount()).isEqualTo(new BigDecimal(expectedAmount));
        assertThat(transaction.getBalance()).isEqualTo(new BigDecimal(expectedBalance));

    }
}
