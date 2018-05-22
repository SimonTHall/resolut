package com.revolut.interview;

import static com.revolut.interview.AccountTestUtils.assertTransaction;
import static com.revolut.interview.TransactionType.CREDIT;
import static com.revolut.interview.TransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountCacheTest {

    private AccountCache testee = new AccountCache();
    
    @Before
    public void setUpTestData() {
        AccountCache.clearAndReset();
    }
    
    @Test
    public void testCreateAccount() {
        Account testAccount = createAccount();
        
        Account resultAccount = testee.create(testAccount);
        
        assertThat(resultAccount).isNotSameAs(testAccount);
        assertThat(resultAccount.getNumber()).isEqualTo(1);
        assertThat(resultAccount.getCustomerName()).isEqualTo("Joe Smith");
        assertThat(resultAccount.getTransactions()).hasSize(2);
        assertTransaction(resultAccount.getTransactions().get(0), 1, CREDIT, 50, 50);
        assertTransaction(resultAccount.getTransactions().get(1), 2, DEBIT, 10, 40);
    }
    
    @Test
    public void testUpdateAccount() {
        Account testAccount = createAccount();
        
        Account resultAccountToUpdate = testee.create(testAccount);
        resultAccountToUpdate.addTransaction(CREDIT, new BigDecimal(100));
        Account updatedAccount = testee.update(resultAccountToUpdate);
        
        assertThat(updatedAccount).isNotSameAs(resultAccountToUpdate);
        assertThat(updatedAccount.getNumber()).isEqualTo(1);
        assertThat(updatedAccount.getCustomerName()).isEqualTo("Joe Smith");
        assertThat(updatedAccount.getTransactions()).hasSize(3);
        assertTransaction(updatedAccount.getTransactions().get(0), 1, CREDIT, 50, 50);
        assertTransaction(updatedAccount.getTransactions().get(1), 2, DEBIT, 10, 40);
        assertTransaction(updatedAccount.getTransactions().get(2), 3, CREDIT, 100, 140);
    }

    @Test(expected=ConcurrentModificationException.class)
    public void testUpdateConcurrencyException() {
        Account testAccount = createAccount();
        testee.create(testAccount);
        
        Account testAccount1 = testee.get(1);
        testAccount1.addTransaction(CREDIT, new BigDecimal(100));
        
        Account testAccount2 = testee.get(1);
        testAccount2.addTransaction(DEBIT, new BigDecimal(10));

        try {
            testee.update(testAccount2);
        } catch (ConcurrentModificationException e) {
            Assert.fail("Not expecting exception from first update!");
        }
        testee.update(testAccount1);        
    }
    
    @Test
    public void testUpdateAccounts() {
        Account testAccount1 = createAccount();        
        Account resultAccountToUpdate1 = testee.create(testAccount1);
        resultAccountToUpdate1.addTransaction(CREDIT, new BigDecimal(100));
        
        Account testAccount2 = createAccount();        
        Account resultAccountToUpdate2 = testee.create(testAccount2);

        Pair<Account, Account> updatedAccounts = testee.update(new ImmutablePair<Account, Account>(resultAccountToUpdate1, resultAccountToUpdate2));
        
        assertThat(updatedAccounts.getLeft().getNumber()).isEqualTo(1);
        assertThat(updatedAccounts.getLeft().getTransactions()).hasSize(3);
        
        assertThat(updatedAccounts.getRight().getNumber()).isEqualTo(2);
        assertThat(updatedAccounts.getRight().getTransactions()).hasSize(2);    
    }
    
    @Test
    public void testUpdateAccountsWithPairInReverse() {
        Account testAccount1 = createAccount();        
        Account resultAccountToUpdate1 = testee.create(testAccount1);
        resultAccountToUpdate1.addTransaction(CREDIT, new BigDecimal(100));
        
        Account testAccount2 = createAccount();        
        Account resultAccountToUpdate2 = testee.create(testAccount2);

        Pair<Account, Account> updatedAccounts = testee.update(new ImmutablePair<Account, Account>(resultAccountToUpdate2, resultAccountToUpdate1));
        
        assertThat(updatedAccounts.getLeft().getNumber()).isEqualTo(2);
        assertThat(updatedAccounts.getLeft().getTransactions()).hasSize(2);
        
        assertThat(updatedAccounts.getRight().getNumber()).isEqualTo(1);
        assertThat(updatedAccounts.getRight().getTransactions()).hasSize(3);    
    }    
    
    @Test
    public void testGetAccount() {
        Account testAccount = createAccount();
        
        testee.create(testAccount);

        Account resultAccount = testee.get(1);
        
        assertThat(resultAccount).isNotSameAs(testAccount);
        assertThat(resultAccount.getNumber()).isEqualTo(1);
        assertThat(resultAccount.getCustomerName()).isEqualTo("Joe Smith");
        assertThat(resultAccount.getTransactions()).hasSize(2);
        assertTransaction(resultAccount.getTransactions().get(0), 1, CREDIT, 50, 50);
        assertTransaction(resultAccount.getTransactions().get(1), 2, DEBIT, 10, 40);
    }

    private Account createAccount() {
        Account testAccount = new Account.Builder()
                .customerName("Joe Smith")
                .overdraftLimit(new BigDecimal(100))
                .transaction(new AccountTransaction.Builder()
                            .type(CREDIT)
                            .amount(50)
                            .balance(50)
                            .build())
                .transaction(new AccountTransaction.Builder()
                        .type(DEBIT)
                        .amount(10)
                        .balance(40)
                        .build())
                .build();
        return testAccount;
    }

}
