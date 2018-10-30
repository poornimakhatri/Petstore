package org.mypetstore.service;

import org.mypetstore.domain.Account;
import org.mypetstore.persistence.AccountDAO;
import org.mypetstore.persistence.impl.AccountDAOImpl;

public class AccountService {

    private AccountDAO accountDAO;

    public AccountService(){
        accountDAO = new AccountDAOImpl();
    }

    public Account getAccount(String username) {
        return accountDAO.getAccountByUsername(username);
    }

    public Account getAccount(String username, String password) {
        if(accountDAO.signIn(username,password)){
            return accountDAO.getAccountByUsername(username);
        }else{
            return null;
        }
    }

    public void insertAccount(Account account) {
        accountDAO.insertAccount(account);
        accountDAO.insertProfile(account);
        accountDAO.insertSignon(account);
    }

    public void updateAccount(Account account) {
        accountDAO.updateAccount(account);
        accountDAO.updateProfile(account);

        if (account.getPassword() != null && account.getPassword().length() > 0) {
            accountDAO.updateSignon(account);
        }
    }

}