package com.revolut.interview;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AccountServiceControllerConcurrencyIT extends AccountServiceControllerITBase {
    
    @Parameterized.Parameters
    public static Object[][] timesToRunTests() {
        return new Object[10][0];
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
        assertThat(getAccount(joeSmith.getNumber()).getCurrentBalance().add(getAccount(fredBlogs.getNumber()).getCurrentBalance())).isEqualTo(BigDecimal.valueOf(2000));

    }
    
}
