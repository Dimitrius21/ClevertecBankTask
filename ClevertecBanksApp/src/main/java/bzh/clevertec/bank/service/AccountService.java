package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.AccountDaoJdbc;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import bzh.clevertec.bank.util.PageableEnding;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Класс реализующий слой Service для сущности Account
 */
@Slf4j
public class AccountService {

    private ConnectionSupplier connectionSupplier;

    public AccountService(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Получить account по его id  в БД
     * @param id account в БД
     * @return account из БД
     */
    @AJLogging
    public Account getAccountById(long id) {
        Connection con = connectionSupplier.getConnection();
        AccountAction accountDao = new AccountDaoJdbc(con);
        Optional<Account> account;
        try {
            con.setAutoCommit(true);
            account = accountDao.findById(id);
            return account.orElseThrow(() -> new IllegalArgumentException(String.format("Account with Id = %d is absent", id)));
        }catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in getAccountId");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Удалить account в БД
     * @param id account под которым он был внесен в БД
     */
    @AJLogging
    public void deleteAccountById(long id) {
        Connection con = connectionSupplier.getConnection();
        AccountAction accountDao = new AccountDaoJdbc(con);
        try {
            con.setAutoCommit(true);
            accountDao.delete(id);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in deleteAccountById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Создать новый account в БД
     * @param account - данные для нового account
     * @return account с внесенным id по которому он записан в БД
     */
    @AJLogging
    public Account createAccount(Account account) {
        Connection con = connectionSupplier.getConnection();
        AccountAction accountDao = new AccountDaoJdbc(con);
        String currencyCode = account.getCurrencyCode();
        long bankId = account.getBankId();
        Account lastAccount = accountDao.getLastAccount(bankId, currencyCode)
                .orElseThrow(() -> new InvalidRequestDataException("Can't create account for bank with " + bankId +
                        " and currency " + currencyCode));
        String firstPartOfAccount = lastAccount.getAccountNumber().substring(0, 8);
        String digitPartOfAccount = lastAccount.getAccountNumber().substring(8);
        String nextDigit = String.valueOf(Long.parseLong(digitPartOfAccount) + 10);
        account.setAccountNumber(firstPartOfAccount + nextDigit);
        try {
            account.setCreateDate(LocalDateTime.now());
            account.setValue(0);
            con.setAutoCommit(true);
            account = accountDao.save(account);
            return account;
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in createAccount");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Получить список всех account в БД
     * @param param - параметры из http-запроса
     * @return - список всех account в БД
     */
    @AJLogging
    public List<Account> getAllAccount(RequestParam param) {
        String pageable = PageableEnding.createSqlPaging(param, Account.class);
        Connection con = connectionSupplier.getConnection();
        AccountAction accountDao = new AccountDaoJdbc(con);
        try {
            con.setAutoCommit(true);
            List<Account> accounts = accountDao.findAll(pageable);
            return accounts;
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in getAllAccount");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Обновить данные account в БД
     * @param account - со всеми полями
     * @return - обновленный account
     */
    @AJLogging
    public Account updateAccount(Account account) {
        Connection con = connectionSupplier.getConnection();
        AccountAction accountDao = new AccountDaoJdbc(con);
        try {
            long id = account.getId();
            Account accountForUpdate = accountDao.findById(id).orElseThrow(() -> {
                throw new InvalidRequestDataException("Invalid request data");
            });
            accountForUpdate.setAccountNumber(account.getAccountNumber());
            accountForUpdate.setValue(account.getValue());
            accountForUpdate.setCurrencyCode(account.getCurrencyCode());
            accountForUpdate.setBankId(account.getBankId());
            accountForUpdate.setClientId(account.getClientId());
            con.setAutoCommit(true);
            accountDao.update(account);
            return accountForUpdate;
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in updateAccount");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }
}
