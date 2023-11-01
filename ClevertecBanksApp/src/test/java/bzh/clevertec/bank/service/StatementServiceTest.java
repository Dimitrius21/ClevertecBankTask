package bzh.clevertec.bank.service;

import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.dto.OneSideOperationDto;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.BankStatement;
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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    AccountAction accountDao;

    @Mock
    TransactionAction transactionDao;

    @InjectMocks
    StatementService statementService;

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
    void getBankStatement() {
        String accountNumber = "BY11CLBK181901012";
        long id = 1;
        AccountBankInfo accountInDb = getAccount(id, accountNumber);

        when(accountDao.findByNumber(accountInDb.getAccountNumber(), accountInDb.getBankCode())).thenReturn(Optional.of(accountInDb));
        LocalDateTime from = LocalDateTime.of(2023, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2023, 10, 31, 23, 59);

        BankStatement statement = new BankStatement();
        statement.setAccountNumber(accountNumber);
        statement.setAccountId(accountInDb.getId());
        statement.setCurrency(accountInDb.getCurrencyCode());
        statement.setBankName(accountInDb.getBankName());
        statement.setCreateAt(accountInDb.getCreateAt());
        statement.setBeginPeriod(from);
        statement.setEndPeriod(to);
        statement.setDate(LocalDateTime.now());
        statement.setOwner(accountInDb.getClientFullName());

        List<ExtendTransactionData> extTransactions = new ArrayList<>(2);

        ExtendTransactionData.ExtendTransactionDataBuilder builder = ExtendTransactionData.builder().id(1)
                .sum(1000).currencyCode("BYN")
                .carryOutAt(LocalDateTime.of(2023, 10, 24, 14, 0))
                .accountIdFrom(0).balanceFrom(0).nameFrom("")
                .accountIdTo(2).balanceTo(2000).nameTo("Иванов")
                .transactionType(OperationType.ADDING);
        extTransactions.add(builder.build());
        builder = ExtendTransactionData.builder().id(2)
                .sum(500).currencyCode("BYN")
                .carryOutAt(LocalDateTime.of(2023, 10, 25, 14, 0))
                .accountIdFrom(2).balanceFrom(1500).nameFrom("Иванов")
                .accountIdTo(0).balanceTo(0).nameTo("")
                .transactionType(OperationType.WITHDRAW);
        extTransactions.add(builder.build());

        statement.setItems(extTransactions);
        statement.setBalance(1500);
        when(transactionDao.getAccountTransactionDuring(accountInDb.getId(),from, to)).thenReturn(extTransactions);

        BankStatement result = statementService.getBankStatement(accountInDb.getAccountNumber(), accountInDb.getBankCode(), from, to);
        Assertions.assertThat(result).isEqualTo(statement);
    }

    @Test
    void getBankTurnOverStatement() {
        String accountNumber = "BY11CLBK181901012";
        long id = 1;
        AccountBankInfo accountInDb = getAccount(id, accountNumber);
        when(accountDao.findByNumber(accountInDb.getAccountNumber(), accountInDb.getBankCode())).thenReturn(Optional.of(accountInDb));
        LocalDateTime from = LocalDateTime.of(2023, 10, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2023, 10, 31, 23, 59);
        BankStatement statement = new BankStatement();
        statement.setAccountNumber(accountNumber);
        statement.setAccountId(accountInDb.getId());
        statement.setCurrency(accountInDb.getCurrencyCode());
        statement.setBankName(accountInDb.getBankName());
        statement.setCreateAt(accountInDb.getCreateAt());
        statement.setBeginPeriod(from);
        statement.setEndPeriod(to);
        statement.setDate(LocalDateTime.now());
        statement.setOwner(accountInDb.getClientFullName());

        long balance = 1500;
        when(accountDao.getBalance(accountInDb.getId())).thenReturn(balance);
        statement.setBalance(1500);

        List<Long> turnovers = List.of(1000L, 500L);
        when(transactionDao.getTurnover(accountInDb.getId(), from, to)).thenReturn(turnovers);
        statement.setTurnover(turnovers);

        BankStatement result = statementService.getBankTurnOverStatement(accountInDb.getAccountNumber(), accountInDb.getBankCode(), from, to);
        Assertions.assertThat(result).isEqualTo(statement);
    }

    private AccountBankInfo getAccount(long id, String accountNumber) {
        AccountBankInfo.AccountBankInfoBuilder builder = AccountBankInfo.builder();
        builder.id(id).accountNumber(accountNumber).currencyCode("BYN")
                .bankCode("CLBKBY22").bankName("Clever-Bank")
                .createAt(LocalDateTime.of(2023, 10, 1, 15, 20))
                .clientFirstName("Сергей").clientSecondName("Сергеевич").clientSurname("Иванов");
        return builder.build();
    }
}