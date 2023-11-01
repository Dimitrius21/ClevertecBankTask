package bzh.clevertec.bank.service;

import bzh.clevertec.bank.FakeHttpServletRequest;
import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.domain.dto.OneSideOperationDto;
import bzh.clevertec.bank.domain.dto.ResponseDto;
import bzh.clevertec.bank.domain.dto.TwoSideOperationDto;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.servlet.SQLPoolConnection;
import bzh.clevertec.bank.util.OperationType;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    AccountAction accountDao;

    @Mock
    TransactionAction transactionDao;

    @InjectMocks
    OperationService operationService;

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
    public static void finish() {
        connectionSupplier.closePool();
    }

    @Test
    void changeAccountAddCashTest() throws SQLException, IOException {
        HttpServletRequest request = new FakeHttpServletRequest();
        String basePath = request.getServletContext().getRealPath("/");
        AccountBankInfo accountInDb = getAccount(1, "BY11CLBK181901012");
        OneSideOperationDto operationDto = new OneSideOperationDto(1000, "BYN", 1, accountInDb.getBankCode(), accountInDb.getAccountNumber());

        when(accountDao.findByNumber(accountInDb.getAccountNumber(), accountInDb.getBankCode())).thenReturn(Optional.of(accountInDb));

        long balanceAccountFrom = 1000;
        long sumAfterOperation = balanceAccountFrom + operationDto.getSum();
        when(accountDao.addSum(operationDto.getSum(), accountInDb.getId())).thenReturn(sumAfterOperation);

        Transaction.TransactionBuilder builder = Transaction.builder()
                .sum(operationDto.getSum()).currencyCode(operationDto.getCurrencyCode())
                .carryOutAt(LocalDateTime.of(2023, 10, 24, 14, 0))
                .accountIdFrom(0).balanceFrom(0)
                .accountIdTo(accountInDb.getId()).balanceTo(sumAfterOperation)
                .transactionType(OperationType.getTypeByCode(operationDto.getType()));
        Transaction transaction = builder.id(0).build();
        Transaction transactionResult = builder.id(1).build();
        when(transactionDao.save(transaction)).thenReturn(transactionResult);

        int responseCode = 200;
        String responseMessage = "Account change correspond";
        ResponseDto response = new ResponseDto(responseCode, responseMessage);
        ResponseDto result = operationService.changeAccount(operationDto, request);
        Assertions.assertThat(result).isEqualTo(response);

        Path path = Path.of(basePath, "WEB-INF", "check", "check" + transactionResult.getId() + ".txt");
        Assertions.assertThat(Files.exists(path)).isTrue();
        Files.delete(path);
    }

    @Test
    void changeAccountWithdrawCashTest() throws SQLException, IOException {
        HttpServletRequest request = new FakeHttpServletRequest();
        String basePath = request.getServletContext().getRealPath("/");
        AccountBankInfo accountInDb = getAccount(1, "BY11CLBK181901012");
        OneSideOperationDto operationDto = new OneSideOperationDto(500, "BYN", 0, accountInDb.getBankCode(), accountInDb.getAccountNumber());

        when(accountDao.findByNumber(accountInDb.getAccountNumber(), accountInDb.getBankCode())).thenReturn(Optional.of(accountInDb));

        long balanceAccountFrom = 1000;
        when(accountDao.getBalance(accountInDb.getId())).thenReturn(balanceAccountFrom);

        long sumAfterOperation = balanceAccountFrom - operationDto.getSum();
        when(accountDao.addSum(operationDto.getSum() * -1, accountInDb.getId())).thenReturn(sumAfterOperation);

        Transaction.TransactionBuilder builder = Transaction.builder()
                .sum(operationDto.getSum()).currencyCode(operationDto.getCurrencyCode())
                .carryOutAt(LocalDateTime.of(2023, 10, 24, 14, 0))
                .accountIdFrom(accountInDb.getId()).balanceFrom(sumAfterOperation)
                .accountIdTo(0).balanceTo(0)
                .transactionType(OperationType.getTypeByCode(operationDto.getType()));
        Transaction transaction = builder.id(0).build();
        Transaction transactionResult = builder.id(1).build();
        when(transactionDao.save(transaction)).thenReturn(transactionResult);

        int responseCode = 200;
        String responseMessage = "Account change correspond";
        ResponseDto response = new ResponseDto(responseCode, responseMessage);
        ResponseDto result = operationService.changeAccount(operationDto, request);
        Assertions.assertThat(result).isEqualTo(response);

        Path path = Path.of(basePath, "WEB-INF", "check", "check" + transactionResult.getId() + ".txt");
        Assertions.assertThat(Files.exists(path)).isTrue();
        Files.delete(path);
    }

    @Test
    void changeAccountNotEnoughMoneyTest() throws SQLException {
        HttpServletRequest request = new FakeHttpServletRequest();
        String basePath = request.getServletContext().getRealPath("/");
        AccountBankInfo accountInDb = getAccount(1, "BY11CLBK181901012");
        OneSideOperationDto operationDto = new OneSideOperationDto(1500, "BYN", 0, accountInDb.getBankCode(), accountInDb.getAccountNumber());

        when(accountDao.findByNumber(accountInDb.getAccountNumber(), accountInDb.getBankCode())).thenReturn(Optional.of(accountInDb));

        long balanceAccountFrom = 1000;
        when(accountDao.getBalance(accountInDb.getId())).thenReturn(balanceAccountFrom);

        Transaction.TransactionBuilder builder = Transaction.builder()
                .transactionType(OperationType.getTypeByCode(operationDto.getType()));
        Transaction transactionResult = builder.id(1).build();

        int responseCode = 200;
        String responseMessage = "Haven't enough money for operation";
        ResponseDto response = new ResponseDto(responseCode, responseMessage);
        ResponseDto result = operationService.changeAccount(operationDto, request);
        Assertions.assertThat(result).isEqualTo(response);

        Path path = Path.of(basePath, "WEB-INF", "check", "check" + transactionResult.getId() + ".txt");
        Assertions.assertThat(Files.exists(path)).isFalse();
    }

    @Test
    void transferMoney() throws SQLException, IOException {
        HttpServletRequest request = new FakeHttpServletRequest();
        String basePath = request.getServletContext().getRealPath("/");
        AccountBankInfo accountInDbFrom = getAccount(1, "BY11CLBK181901012");
        AccountBankInfo accountInBdTo = getAccount(2, "BY11CLBK181901002");

        TwoSideOperationDto operationDto = new TwoSideOperationDto(1000, "BYN", 2,
                accountInDbFrom.getAccountNumber(), accountInDbFrom.getBankCode(),
                accountInBdTo.getAccountNumber(), accountInBdTo.getBankCode());

        when(accountDao.findByNumber(accountInDbFrom.getAccountNumber(), accountInDbFrom.getBankCode())).thenReturn(Optional.of(accountInDbFrom));
        when(accountDao.findByNumber(accountInBdTo.getAccountNumber(), accountInBdTo.getBankCode())).thenReturn(Optional.of(accountInBdTo));

        long balanceAccountFrom = 1000;
        when(accountDao.getBalance(accountInDbFrom.getId())).thenReturn(balanceAccountFrom);

        long sumAfterOperationFrom = balanceAccountFrom - operationDto.getSum();
        long sumAfterOperationTo = balanceAccountFrom + operationDto.getSum();
        when(accountDao.addSum(operationDto.getSum() * -1, accountInDbFrom.getId())).thenReturn(sumAfterOperationFrom);
        when(accountDao.addSum(operationDto.getSum(), accountInBdTo.getId())).thenReturn(sumAfterOperationTo);

        Transaction.TransactionBuilder builder = Transaction.builder()
                .sum(operationDto.getSum()).currencyCode(operationDto.getCurrencyCode())
                .carryOutAt(LocalDateTime.of(2023, 10, 24, 14, 0))
                .accountIdFrom(accountInDbFrom.getId()).balanceFrom(sumAfterOperationFrom)
                .accountIdTo(accountInBdTo.getId()).balanceTo(sumAfterOperationTo)
                .transactionType(OperationType.getTypeByCode(operationDto.getType()));
        Transaction transaction = builder.id(0).build();
        Transaction transactionResult = builder.id(1).build();
        when(transactionDao.save(transaction)).thenReturn(transactionResult);

        int responseCode = 200;
        String responseMessage = "Transfer has been carried out";
        ResponseDto response = new ResponseDto(responseCode, responseMessage);
        ResponseDto result = operationService.transferMoney(operationDto, request);
        Assertions.assertThat(result).isEqualTo(response);

        Path path = Path.of(basePath, "WEB-INF", "check", "check" + transactionResult.getId() + ".txt");
        Assertions.assertThat(Files.exists(path)).isTrue();
        Files.delete(path);
    }

    private AccountBankInfo getAccount(long id, String accountNumber) {
        AccountBankInfo.AccountBankInfoBuilder builder = AccountBankInfo.builder();
        builder.id(id).accountNumber(accountNumber).currencyCode("BYN")
                .bankCode("CLBKBY22").bankName("Clever-Bank")
                .createAt(LocalDateTime.now())
                .clientFirstName("Сергей").clientSecondName("Сергеевич").clientSurname("Иванов");
        return builder.build();
    }
}