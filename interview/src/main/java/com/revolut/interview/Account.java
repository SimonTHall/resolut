package com.revolut.interview;

import static com.revolut.interview.TransactionType.CREDIT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private Integer number;
    private String customerName;
    private BigDecimal overdraftLimit = BigDecimal.ZERO;
    private List<AccountTransaction> transactions = new ArrayList<>();

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer id) {
        this.number = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public List<AccountTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<AccountTransaction> transactions) {
        this.transactions = transactions;
    }
    
    public BigDecimal getCurrentBalance() {
        if (this.transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return getLastTransaction().getBalance();
    }
    
    public Integer getLastTransactionId() {
        return getLastTransaction().getId();
    }

    private AccountTransaction getLastTransaction() {
        return this.transactions.get(this.transactions.size() - 1);
    }
    
    public void addTransaction(AccountTransaction transaction) {
        this.transactions.add(transaction);
    }
    
    public void addTransaction(TransactionType type, BigDecimal amount) {
        BigDecimal newBalance = type.equals(CREDIT) ? getCurrentBalance().add(amount) : getCurrentBalance().subtract(amount);
        this.transactions.add(
                new AccountTransaction.Builder()
                    .type(type)
                    .amount(amount)
                    .balance(newBalance)
                    .build()
            );
    }


    public static class Builder {
        private Account instance = new Account();
        
        public Builder copy(Account account) {
            number(account.number);
            customerName(account.customerName);
            overdraftLimit(account.overdraftLimit);
            account.getTransactions().forEach(trans -> transaction(trans));
            return this;
        }
        
        public Builder number(Integer number) {
            instance.number = number;
            return this;
        }
        
        public Builder customerName(String customerName) {
            instance.customerName = customerName;
            return this;
        }
        
        public Builder overdraftLimit(BigDecimal overdraftLimit) {
            instance.overdraftLimit = overdraftLimit;
            return this;
        }
        
        public Builder overdraftLimit(Integer overdraftLimit) {
            return overdraftLimit(new BigDecimal(overdraftLimit));
        }
        
        public Builder transaction(AccountTransaction transaction) {
            instance.transactions.add(transaction);
            return this;
        }        
        public Account build() {
            return instance;
        }
    }
    
}
