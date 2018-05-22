package com.revolut.interview;

import java.math.BigDecimal;

public class AccountTransaction {
    private Integer id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balance;    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public static class Builder {
        private AccountTransaction instance = new AccountTransaction();
        
        public Builder type(TransactionType type) {
            instance.type = type;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            instance.amount = amount;
            return this;
        }
        
        public Builder amount(Integer amount) {
            return amount(new BigDecimal(amount));
        }
        
        public Builder balance(BigDecimal balance) {
            instance.balance = balance;
            return this;
        }        
        
        public Builder balance(Integer amount) {
            return balance(new BigDecimal(amount));
        }
        
        public AccountTransaction build() {
            return instance;
        }
    }
}
