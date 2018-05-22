package com.revolut.interview;

import static com.revolut.interview.AccountTestUtils.assertTransaction;
import static com.revolut.interview.AccountTestUtils.createFundTransferRequest;
import static com.revolut.interview.TransactionType.CREDIT;
import static com.revolut.interview.TransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @InjectMocks
    private AccountService testee = new AccountServiceImpl();
    
    @Mock
    private AccountRepository mockRepo;
        
    @Mock
    private Account mockAccount;

    @Captor 
    private ArgumentCaptor<Account> accountCaptor;
    
    @Captor 
    private ArgumentCaptor<Account> accountFromCaptor;
    
    @Captor 
    private ArgumentCaptor<Account> accountToCaptor;
    
    @Captor 
    private ArgumentCaptor<Pair<Account, Account>> accountsToCaptor;
    
    @Test
    public void testCreateAccount() {
        NewAccountRequest request = new NewAccountRequest();
        request.setCustomerName("Joe Smith");
        request.setOverDraftLimit(new BigDecimal(1000));
        request.setInitialDeposit(new BigDecimal(100));
        
        Account newAccount = new Account.Builder().build();
        
        when(mockRepo.create(any())).thenReturn(newAccount);
        
        Account resultAccount = testee.createAccount(request);

        // Check account repository is called with the right data
        verify(mockRepo).create(accountCaptor.capture());
        Account accountToRepo = accountCaptor.getValue();
        assertThat(accountToRepo.getCustomerName()).isEqualTo("Joe Smith");
        assertThat(accountToRepo.getOverdraftLimit()).isEqualTo(new BigDecimal(1000));
        assertThat(accountToRepo.getTransactions()).hasSize(1);
        assertTransaction(accountToRepo.getTransactions().get(0), null, CREDIT, 100, 100);
 
        // And that the return object from the repository is passed back
        assertThat(resultAccount).isSameAs(newAccount);

    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testTransfer() {
        Account toRepoFromAccount = new Account.Builder()
                                .number(1)
                                .overdraftLimit(200)
                                .transaction(new AccountTransaction.Builder()
                                        .balance(100)
                                        .build())
                                .build();
        Account toRepoToAccount = new Account.Builder()
                                .number(2)
                                .transaction(new AccountTransaction.Builder()
                                        .balance(200)
                                        .build())
                                .build();
        
        Account updatedRepoFromAccount = new Account.Builder().build();
        Account updatedRepoToAccount = new Account.Builder().build();

        when(mockRepo.get(1)).thenReturn(toRepoFromAccount);
        when(mockRepo.get(2)).thenReturn(toRepoToAccount);
        when(mockRepo.update(new ImmutablePair(toRepoFromAccount, toRepoToAccount)))
                .thenReturn(new ImmutablePair(updatedRepoFromAccount, updatedRepoToAccount));
        
        this.testee.transfer(createFundTransferRequest(1, 2, 300));

        verify(mockRepo).update(accountsToCaptor.capture());

        Account fromAccountSavedToRepo = accountsToCaptor.getValue().getLeft();
        assertThat(fromAccountSavedToRepo.getNumber()).isEqualTo(1);
        assertThat(fromAccountSavedToRepo.getOverdraftLimit()).isEqualTo(new BigDecimal(200));
        assertThat(fromAccountSavedToRepo.getTransactions()).hasSize(2);
        assertThat(fromAccountSavedToRepo.getTransactions().get(0).getBalance()).isEqualTo(new BigDecimal(100));
        assertTransaction(fromAccountSavedToRepo.getTransactions().get(1), null, DEBIT, 300, -200);

        Account toAccountSavedToRepo = accountsToCaptor.getValue().getRight();
        assertThat(toAccountSavedToRepo.getNumber()).isEqualTo(2);
        assertThat(toAccountSavedToRepo.getOverdraftLimit()).isEqualTo(BigDecimal.ZERO);
        assertThat(toAccountSavedToRepo.getTransactions()).hasSize(2);
        assertThat(toAccountSavedToRepo.getTransactions().get(0).getBalance()).isEqualTo(new BigDecimal(200));
        assertTransaction(toAccountSavedToRepo.getTransactions().get(1), null, CREDIT, 300, 500);
    }
    
    @Test
    public void testGetAccount() {
        when(testee.getAccount(1)).thenReturn(mockAccount);        
        Account resultAccount = testee.getAccount(1);
        assertThat(resultAccount).isSameAs(mockAccount);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateAccountWithNegativeDeposit() {
        NewAccountRequest request = new NewAccountRequest();
        request.setCustomerName("Joe Smith");
        request.setOverDraftLimit(new BigDecimal(1000));
        request.setInitialDeposit(new BigDecimal(-100));
        testee.createAccount(request);
    }

    @Test(expected=IllegalStateException.class)
    public void testInsufficientFundsForTransfer() {
        Account toRepoFromAccount = new Account.Builder()
                .number(1)
                .overdraftLimit(200)
                .transaction(new AccountTransaction.Builder()
                        .balance(100)
                        .build())
                .build();
        Account toRepoToAccount = new Account.Builder()
                        .number(2)
                        .transaction(new AccountTransaction.Builder()
                                .balance(200)
                                .build())
                        .build();
        
        when(mockRepo.get(1)).thenReturn(toRepoFromAccount);
        when(mockRepo.get(2)).thenReturn(toRepoToAccount);
        
        this.testee.transfer(createFundTransferRequest(1, 2, 301));
    }
}
