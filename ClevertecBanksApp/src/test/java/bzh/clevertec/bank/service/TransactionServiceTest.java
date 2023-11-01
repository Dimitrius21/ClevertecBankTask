package bzh.clevertec.bank.service;

import bzh.clevertec.bank.dao.ClientAction;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.domain.entity.Client;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.servlet.SQLPoolConnection;
import bzh.clevertec.bank.util.ConnectionSupplier;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    TransactionAction transactionDao;

    @InjectMocks
    TransactionService transactionService;

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
    void getTransactionById() {
        long id = 1L;
        Transaction.TransactionBuilder builder = Transaction.builder();
        builder.id(1).sum(1000).currencyCode("BYN")
                .accountIdFrom(1).balanceFrom(1000)
                .accountIdTo(2).balanceTo(1000)
                .carryOutAt(LocalDateTime.now())
                .transactionType(OperationType.TRANSFER);
        Transaction transaction = builder.build();
        when(transactionDao.findById(id)).thenReturn(Optional.of(transaction));
        Transaction result = transactionService.getTransactionById(id);
        Assertions.assertThat(result).isEqualTo(transaction);
    }
}