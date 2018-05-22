package com.revolut.interview;

import java.math.BigDecimal;

public class NewAccountRequest {
    private String customerName;
    private BigDecimal overDraftLimit = BigDecimal.ZERO;
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public BigDecimal getOverDraftLimit() {
        return overDraftLimit;
    }
    public void setOverDraftLimit(BigDecimal overDraftLimit) {
        this.overDraftLimit = overDraftLimit;
    }
    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }
    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
        
}
