package com.revolut.interview;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Manages the persistence and retrieval of all {@link Account} information.
 */
public interface AccountRepository {

    Account create(Account account);
    
    /**
     * @throws - ConcurrentModificationException when the account is modified by another thread and the 
     *              data in the current thread is now out of date
     */
    Account update(Account account);

    /**
     * @throws - ConcurrentModificationException when either of the accounts are modified by another thread and the 
     *              data in the current thread is now out of date
     */
    Pair<Account, Account> update(Pair<Account, Account> accounts);

    /**
     * @throws - IllegalArgumentException when either of the accounts for the transfer do not exist
     */
    Account get(Integer accountNo);
}
