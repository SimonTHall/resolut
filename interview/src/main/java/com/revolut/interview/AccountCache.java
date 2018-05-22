package com.revolut.interview;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class AccountCache implements AccountRepository {
    
    private static AtomicInteger accountCounter = new AtomicInteger();
    private static AtomicInteger transactionCounter = new AtomicInteger();
    
    private static Map<Integer, Account> cache = new ConcurrentHashMap<>();

    public static void clearAndReset() {
        accountCounter.set(0);
        transactionCounter.set(0);
        cache.clear();
    }
    
    @Override
    public Account create(Account account) {
        Account newAccount = copyOf(account);
        newAccount.setNumber(accountCounter.incrementAndGet());
        setTransactionIds(newAccount);
        cache.put(newAccount.getNumber(), newAccount);
        return copyOf(newAccount);
    }

    /**
     * Updates the account only if no other thread has already updated it.
     * This is checked by ensuring that the last transaction id in the old version is present in the new version.
     * If it is not then the data being used is out of date, and thus the change is rolled back and 
     * a {@link ConcurrentModificationException} is thrown.
     */
    @Override
    public Account update(Account account) {
        Account newAccount = copyOf(account);
        synchronized (newAccount.getNumber()) {
            setTransactionIds(newAccount);
            Account oldAccount = cache.put(newAccount.getNumber(), newAccount);
            List<AccountTransaction> newTrans = newAccount.getTransactions();
            Integer lastOldTransId = oldAccount.getLastTransactionId();
            for(int i=newTrans.size() - 1; i>=0; i--) {
                if (newTrans.get(i).getId().equals(lastOldTransId)) {
                    return copyOf(newAccount);
                }
            }
            // if here then we need to roll back the change
            cache.put(oldAccount.getNumber(), oldAccount);
        }
                
        throw new ConcurrentModificationException("Current excecution is aborted as account is out of date!");
    }

    private void setTransactionIds(Account account) {
        List<AccountTransaction> transactions = account.getTransactions();
        int idCount = 0;
        for (int i = transactions.size() - 1; i>=0 ; i--) {
            AccountTransaction transaction = transactions.get(i);
            if (transaction.getId()==null) {
                idCount++;
            } else {
                break;
            }
        }
        for (int i = transactions.size() - idCount; i<transactions.size(); i++) {
            transactions.get(i).setId(transactionCounter.incrementAndGet());
        }
    }

    @Override
    public Account get(Integer accountNo) {
        Account account = cache.get(accountNo);
        Validate.notNull(account, "Account number %d is not valid!", accountNo);
        return copyOf(account);
    }

    /**
     * To reduces the chance of java deadlock we ensure that the accounts are synchronized in a consistent order.
     * The synchronization will then prevent any concurrent updates across the same accounts during the operation.
     * In addition this operation is atomic - all or nothing will complete.
     */
    @Override
    public Pair<Account, Account> update(Pair<Account, Account> accounts) {
        Account firstAccount;
        Account secondAccount;
        boolean reverseSyncOrder = accounts.getLeft().getNumber() > accounts.getRight().getNumber();
        if (reverseSyncOrder) {
            firstAccount = accounts.getRight();
            secondAccount = accounts.getLeft();            
        } else {
            firstAccount = accounts.getLeft();
            secondAccount = accounts.getRight();
        }
        Account firstResult;
        Account secondResult;
        
        synchronized (firstAccount.getNumber()) {
            synchronized (secondAccount.getNumber()) {
                Account rollbackAccount = cache.get(firstAccount.getNumber());
                firstResult = this.update(firstAccount);
                // If the second update fails we need to roll back the first
                try {
                    secondResult = this.update(secondAccount);                    
                } catch (ConcurrentModificationException e) {
                    cache.put(rollbackAccount.getNumber(), rollbackAccount);
                    throw e;
                }
            }            
        }
        
        return new ImmutablePair<Account, Account>(reverseSyncOrder?secondResult:firstResult, 
                            reverseSyncOrder?firstResult:secondResult);
    }

    private Account copyOf(Account account) {
        return new Account.Builder().copy(account).build();
    }

}
