package com.revolut.interview;

import static com.revolut.interview.AccountTestUtils.assertTransaction;
import static com.revolut.interview.AccountTestUtils.createFundTransferRequest;
import static com.revolut.interview.TransactionType.CREDIT;
import static com.revolut.interview.TransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

public class AccountServiceControllerIT extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(AccountServiceController.class);
    }
    
    @Before
    public void clearCache() {
        // Need to do this so all test classes can run in one execution
        AccountCache.clearAndReset();
    }

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

    @Test
    public void testConcurrentTransfers() throws InterruptedException {

        // given    
        for (int i=0; i<200; i++) {
            addAccountAndCheck("Joe Smith", 0, 1000);
            addAccountAndCheck("Fred Blogs", 0, 1000);            
        }

        Account joeSmith = addAccountAndCheck("Joe Smith", 0, 1000);
        Account fredBlogs = addAccountAndCheck("Fred Blogs", 0, 1000);

        // when    
        ExecutorService service = Executors.newFixedThreadPool(8);    
        for (int i = 0; i < 1000; i++) {
            service.submit(() -> {    
                if (ThreadLocalRandom.current().nextBoolean()) {
                    transferFunds(joeSmith.getNumber(), fredBlogs.getNumber(), 1000);
                } else {    
                    transferFunds(fredBlogs.getNumber(), joeSmith.getNumber(), 1000);    
                }    
            });    
        }
    
        service.shutdown();    
        service.awaitTermination(10, TimeUnit.MINUTES);
    
        // then    
        assertThat(getAccount(joeSmith.getNumber()).getCurrentBalance()).isNotNegative();    
        assertThat(getAccount(joeSmith.getNumber()).getCurrentBalance()).isLessThanOrEqualTo(BigDecimal.valueOf(2000));    
        assertThat(getAccount(fredBlogs.getNumber()).getCurrentBalance()).isNotNegative();    
        assertThat(getAccount(fredBlogs.getNumber()).getCurrentBalance()).isLessThanOrEqualTo(BigDecimal.valueOf(2000));

    }
    private Account getAccount(Integer accountNo) {
        return target("accounts/" + accountNo).request().get(Account.class);                
    }
    
    private void transferFunds(Integer fromAccountNo, Integer toAccountNo, Integer amount) {
        FundTransferRequest request = createFundTransferRequest(fromAccountNo, toAccountNo, amount);
        Entity<FundTransferRequest> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
        target("accounts/transfer").request().post(requestEntity);        
    }

    private Account addAccountAndCheck(String name, Integer overDraftLimit, Integer initialDeposit) {
        Response response = addAccount(name, overDraftLimit, initialDeposit);
        Account newAccount = response.readEntity(Account.class);
        assertThat(newAccount.getCustomerName()).isEqualTo(name);
        return newAccount;
    }

    private Response addAccount(String name, Integer overDraftLimit, Integer initialDeposit) {
        NewAccountRequest request = new NewAccountRequest();
        request.setCustomerName(name);
        request.setOverDraftLimit(new BigDecimal(overDraftLimit));
        request.setInitialDeposit(new BigDecimal(initialDeposit));
        Entity<NewAccountRequest> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
        Response response = target("accounts").request().post(requestEntity);
        return response;
    }
    
}
