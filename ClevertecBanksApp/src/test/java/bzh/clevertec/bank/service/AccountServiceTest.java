package bzh.clevertec.bank.service;

import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.ClientAction;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.domain.entity.Client;
import bzh.clevertec.bank.servlet.SQLPoolConnection;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    AccountAction accountDao;

    @InjectMocks
    AccountService accountService;

    @BeforeAll
    public static void init() {
        Map<String, Object> appConfigParams;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        appConfigParams = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) appConfigParams.get("db");
        connectionSupplier = SQLPoolConnection.createPool(db);
    }

    @AfterAll
    public static void finish(){
        connectionSupplier.closePool();
    }

    @Test
    void getAccountByIdTest() {
        long id = 1L;
        Account account = getAccount(id);
        when(accountDao.findById(id)).thenReturn(Optional.of(account));
        Account result = accountService.getAccountById(id);
        Assertions.assertThat(result).isEqualTo(account);
    }

    @Test
    void deleteAccountByIdTest() {
        long id = 1;
        doNothing().when(accountDao).delete(id);
        accountService.deleteAccountById(id);
        verify(accountDao).delete(id);
    }

    @Test
    void createAccountTest() {
        Account account = getAccount(0);
        when(accountDao.getLastAccount(1, "BYN")).thenReturn(Optional.of(account));
        Account account1 = getAccount(1);
        account1.setAccountNumber("BY11CLBK181901022");
        when(accountDao.save(account)).thenReturn(account1);
        Account result = accountService.createAccount(account);
        Assertions.assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getAllAccountTest() {
        RequestParam params = new RequestParam();
        String queryString = "page=1&size=2";
        params.parseParam(queryString);
        String pageable = " LIMIT 2 OFFSET 0";
        List<Account> accounts = new ArrayList<>(2);
        accounts.add(getAccount(0));
        accounts.add(getAccount(1));

        when(accountDao.findAll(pageable)).thenReturn(accounts);
        List<Account> result = accountService.getAllAccount(params);

        Assertions.assertThat(result).hasSize(2).contains(accounts.get(0), accounts.get(1));
    }

    @Test
    void updateAccountTest() {
        long id = 1;
        Account accountInDb = getAccount(id);
        Account account = getAccount(id);
        account.setCurrencyCode("EUR");
        account.setValue(1000);
        when(accountDao.findById(id)).thenReturn(Optional.of(accountInDb));
        when(accountDao.update(account)).thenReturn(1);
        Account result = accountService.updateAccount(account);
        Assertions.assertThat(result).isEqualTo(account);
    }

    private Account getAccount(long id) {
        return new Account(id, "BY11CLBK181901012", 0, "BYN", LocalDateTime.now(), 1,1);
    }
}