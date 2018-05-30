package com.revolut.interview;

import static com.revolut.interview.AccountTestUtils.createFundTransferRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

public class AccountServiceControllerITBase extends JerseyTest {
        
    @Override
    protected Application configure() {
        return new ResourceConfig(AccountServiceController.class);
    }
    
    @Before
    public void clearCache() {
        // Need to do this so all test classes can run in one execution
        AccountCache.clearAndReset();
    }

    protected Account getAccount(Integer accountNo) {
        return target("accounts/" + accountNo).request().get(Account.class);                
    }
    
    protected void transferFunds(Integer fromAccountNo, Integer toAccountNo, Integer amount) {
        FundTransferRequest request = createFundTransferRequest(fromAccountNo, toAccountNo, amount);
        Entity<FundTransferRequest> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
        target("accounts/transfer").request().post(requestEntity);
    }

    protected Account addAccountAndCheck(String name, Integer overDraftLimit, Integer initialDeposit) {
        Response response = addAccount(name, overDraftLimit, initialDeposit);
        Account newAccount = response.readEntity(Account.class);
        assertThat(newAccount.getCustomerName()).isEqualTo(name);
        return newAccount;
    }

    protected Response addAccount(String name, Integer overDraftLimit, Integer initialDeposit) {
        NewAccountRequest request = new NewAccountRequest();
        request.setCustomerName(name);
        request.setOverDraftLimit(new BigDecimal(overDraftLimit));
        request.setInitialDeposit(new BigDecimal(initialDeposit));
        Entity<NewAccountRequest> requestEntity = Entity.entity(request, MediaType.APPLICATION_JSON);
        Response response = target("accounts").request().post(requestEntity);
        return response;
    }
    
}
