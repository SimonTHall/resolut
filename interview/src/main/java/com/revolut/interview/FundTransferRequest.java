package com.revolut.interview;

import java.math.BigDecimal;

public class FundTransferRequest {
    private Integer fromAccountNo;
    private Integer toAccountNo;
    private BigDecimal amount;
    
    public Integer getFromAccountNo() {
        return fromAccountNo;
    }
    public void setFromAccountNo(Integer fromAccountNo) {
        this.fromAccountNo = fromAccountNo;
    }
    public Integer getToAccountNo() {
        return toAccountNo;
    }
    public void setToAccountNo(Integer toAccountNo) {
        this.toAccountNo = toAccountNo;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
}
