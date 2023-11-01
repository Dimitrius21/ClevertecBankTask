package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;

import java.util.List;
import java.util.Optional;

public interface AccountAction extends SettingConnection{

    public Optional<Account> getLastAccount(long bankId, String currency);

    public Optional<Account> findById(long id);

    public List<Account> findAll(String pageable);

    public Optional<AccountBankInfo> findByNumber(String accountNumber, String bankCode);

    public List<Account> findByClientId(long clientId);

    public long addSum(Long sum, long accountId);

    public long getBalance(long accountId);

    public Account save(Account account);

    public int update(Account account);

    public void delete(long id);
}

