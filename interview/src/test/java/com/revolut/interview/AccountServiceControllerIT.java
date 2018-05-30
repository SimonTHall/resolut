package com.revolut.interview;

import static com.revolut.interview.AccountTestUtils.assertTransaction;
import static com.revolut.interview.TransactionType.CREDIT;
import static com.revolut.interview.TransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;

public class AccountServiceControllerIT extends AccountServiceControllerITBase {
    
    @Test
    public void testAddTwoAccountsAndTransferMoneyBetweenThem() {
        addAccountAndCheck("Joe Smith", 1000, 100);
        addAccountAndCheck("Fred Blogs", 1000, 100);
        transferFunds(1, 2, 1100);
        Account fromAccount = getAccount(1);
        assertThat(fromAccount.getNumber()).isEqualTo(1);
        assertThat(fromAccount.getOverdraftLimit()).isEqualTo(new BigDecimal(1000));
        assertThat(fromAccount.getTransactions()).hasSize(2);
        assertTransaction(fromAccount.getTransactions().get(0), 1, CREDIT, 100, 100);
        assertTransaction(fromAccount.getTransactions().get(1), 3, DEBIT, 1100, -1000);
        Account toAccount = getAccount(2);
        assertThat(toAccount.getNumber()).isEqualTo(2);
        assertThat(toAccount.getOverdraftLimit()).isEqualTo(new BigDecimal(1000));
        assertThat(toAccount.getTransactions()).hasSize(2);
        assertTransaction(toAccount.getTransactions().get(0), 2, CREDIT, 100, 100);
        assertTransaction(toAccount.getTransactions().get(1), 4, CREDIT, 1100, 1200);
    }

    @Test
    public void testNegativeDepositErrorIsHandled() {
        Response response = addAccount("Joe Smith", 1000, -100);
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(String.class)).isEqualTo("Initial deposit must be positive!");
    }
    
}
