package com.revolut.interview;

import static com.revolut.interview.TransactionType.CREDIT;
import static com.revolut.interview.TransactionType.DEBIT;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class AccountServiceImpl implements AccountService {
    
    private AccountRepository accountRepo = new AccountCache();
    
    @Override
    public Account createAccount(NewAccountRequest newAccountRequest) {
        Validate.isTrue(newAccountRequest.getInitialDeposit().doubleValue()>0, "Initial deposit must be positive!");
        Account.Builder accountBuilder = new Account.Builder()
                .customerName(newAccountRequest.getCustomerName())
                .overdraftLimit(newAccountRequest.getOverDraftLimit());
        if (!newAccountRequest.getInitialDeposit().equals(BigDecimal.ZERO)) {
            accountBuilder.transaction(
                        new AccountTransaction.Builder()
                            .type(CREDIT)
                            .amount(newAccountRequest.getInitialDeposit())
                            .balance(newAccountRequest.getInitialDeposit())
                            .build()
                    );
        }
        return accountRepo.create(accountBuilder.build());
    }

    @Override
    public Account getAccount(Integer accountNo) {
        return this.accountRepo.get(accountNo);
    }

    @Override
    public void transfer(FundTransferRequest transferRequest) {
        Account fromAccount = accountRepo.get(transferRequest.getFromAccountNo());

        BigDecimal fundsAvailable = fromAccount.getCurrentBalance().add(fromAccount.getOverdraftLimit());

        // Need to validate there is enough cash in the account
        Validate.validState(fundsAvailable.compareTo(transferRequest.getAmount()) >= 0, 
                "Insufficient funds in account %d of %f for transfer of %f!",
                transferRequest.getFromAccountNo(), fundsAvailable, transferRequest.getAmount());
        
        Account toAccount = accountRepo.get(transferRequest.getToAccountNo());
        
        fromAccount.addTransaction(DEBIT, transferRequest.getAmount());
        toAccount.addTransaction(CREDIT, transferRequest.getAmount());
        
        this.accountRepo.update(new ImmutablePair<Account, Account>(fromAccount, toAccount));
        
    }
    
}
