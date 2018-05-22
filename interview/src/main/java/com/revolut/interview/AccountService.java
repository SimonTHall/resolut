package com.revolut.interview;

/**
 * Provides all business methods to create and query {@link Account}s, plus also allows transferring of money between accounts
 */
public interface AccountService {
    
    /**
     * @param newAccountRequest
     * @return - a copy of the newly created Account. 
     * @throws - IllegalArgumentException when data in invalid
     */
    Account createAccount(NewAccountRequest newAccountRequest);

    /**
     * @return - returns a copy of the required Account to the client
     * @throws - IllegalArgumentException when the account does not exist in the cache
     */
    Account getAccount(Integer accountNo);
    
    /**
     * @throws - IllegalArgumentException when either of the accounts for the transfer do not exist
     * @throws - IllegalStateException when the account transferring the money does not have enough funds
     * @throws - ConcurrentModificationException when the account is modified by another thread and the 
     *              data in the current thread is now out of date
     */
    void transfer(FundTransferRequest transferRequest);
}
