package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.Client;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bzh.clevertec.bank.util.Constants.*;

class AccountDaoJdbcTest {
    private static Connection con;
    private static final String YamlInitFile = "application.yaml";
    private AccountAction accountDao;

    @BeforeAll
    public static void configure() throws SQLException {
        Map<String, Object> appConfigParams;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        appConfigParams = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) appConfigParams.get("db");
        String url = db.get(URL).toString();
        String user = db.get(USER).toString();
        String password = db.get(PASSWORD).toString();
        con = DriverManager.getConnection(url, user, password);
    }

    @AfterAll
    public static void destroy(){
        if (con!=null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Error of connection closing");
            }
        }
    }

    @BeforeEach
    public void init(){
        accountDao = new AccountDaoJdbc(con);
    }


    @Test
    void addSumTest() {
        Account account = new Account(0, "BYN111", 0, "BYN", LocalDateTime.now(), 1,1);
        account = accountDao.save(account);
        long id = account.getId();
        long sum = 3000;
        long value = accountDao.addSum(sum, id);
        Assertions.assertThat(value).isEqualTo(sum);
        accountDao.delete(id);
    }

    @Test
    void getBalanceTest() {
        long id = 40;
        long value = accountDao.getBalance(id);
        Assertions.assertThat(value).isEqualTo(0);
    }

    @Test
    void getLastAccountTest() {
        long bankId = 6;
        String currency = "BYN";
        Optional<Account> account = accountDao.getLastAccount(bankId, currency);
        Assertions.assertThat(account).isNotEmpty();
        String number = account.get().getAccountNumber();
        Assertions.assertThat(number).isEqualTo("BY61PJCB382106003");
    }

    @Test
    void getLastAccountNotFoundTest() {
        long bankId = 7;
        String currency = "BYN";
        Optional<Account> account = accountDao.getLastAccount(bankId, currency);
        Assertions.assertThat(account).isEmpty();
    }

    @Test
    void findByIdTest() {
        long id = 1;
        Optional<Account> account = accountDao.findById(id);
        Assertions.assertThat(account).isNotEmpty();
        Account accountObj = account.get();
        Assertions.assertThat(accountObj.getId()).isEqualTo(id);
    }

    @Test
    void findByIdNotFoundTest() {
        long id = -1;
        Optional<Account> account = accountDao.findById(id);
        Assertions.assertThat(account).isEmpty();
    }

    @Test
    void findByNumberTest() {
        String number = "BY11CLBK181901001";
        String code = "CLBKBY22";
        Optional<AccountBankInfo> account = accountDao.findByNumber(number, code);
        Assertions.assertThat(account).isNotEmpty();
        AccountBankInfo accountObj = account.get();
        Assertions.assertThat(accountObj.getBankCode()).isEqualTo(code);
        Assertions.assertThat(accountObj.getAccountNumber()).isEqualTo(number);
        Assertions.assertThat(accountObj.getCurrencyCode()).isEqualTo("BYN");
    }

    @Test
    void findByNumberNotFoundTest() {
        String number = "BY11CLBK181909999";
        String code = "CLBKBY22";
        Optional<AccountBankInfo> account = accountDao.findByNumber(number, code);
        Assertions.assertThat(account).isEmpty();
    }

    @Test
    void findByClientId() {
        long clientId = 1;
        List<Account> accounts = accountDao.findByClientId(clientId);
        Assertions.assertThat(accounts).hasSizeGreaterThan(1);
    }

    @Test
    void findByClientNotFoundId() {
        long clientId = -1;
        List<Account> accounts = accountDao.findByClientId(clientId);
        Assertions.assertThat(accounts).hasSize(0);
    }

    @Test
    void saveTest() {
        Account account = new Account(0, "BYN111", 0, "BYN", LocalDateTime.now(), 1,1);
        account = accountDao.save(account);
        Assertions.assertThat(account.getId()).isNotZero();
        account.setCreateDate(null);
        long id = account.getId();
        Account accountGot = accountDao.findById(id).get();
        accountGot.setCreateDate(null);
        Assertions.assertThat(accountGot).isEqualTo(account);
        accountDao.delete(id);
    }

    @Test
    void updateTest() {
        Account account = new Account(0, "BYN111", 0, "BYN", LocalDateTime.now(), 1,1);
        account = accountDao.save(account);
        long id = account.getId();
        account.setAccountNumber("USD222");
        account.setCurrencyCode("USD");
        accountDao.update(account);
        Account accountGot = accountDao.findById(id).get();
        account.setCreateDate(null);
        accountGot.setCreateDate(null);
        Assertions.assertThat(accountGot).isEqualTo(account);
        accountDao.delete(id);
    }

    @Test
    void deleteTest() {
        Account account = new Account(0, "BYN111", 0, "BYN", LocalDateTime.now(), 1,1);
        account = accountDao.save(account);
        long id = account.getId();
        accountDao.delete(id);
        Optional<Account> accountGot = accountDao.findById(id);
        Assertions.assertThat(accountGot).isEmpty();
    }

    @Test
    void findAllTest() {
        String pageable = " ORDER BY currency_code LIMIT 2";
        List<Account> accounts = accountDao.findAll(pageable);
        Assertions.assertThat(accounts).hasSize(2);
        boolean res = accounts.stream().map(ac->ac.getCurrencyCode()).allMatch(it->it.equals("BYN"));
        Assertions.assertThat(res).isTrue();
    }
}