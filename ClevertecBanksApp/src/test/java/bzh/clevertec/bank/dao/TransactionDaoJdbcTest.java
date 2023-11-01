package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.util.OperationType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bzh.clevertec.bank.util.Constants.*;

class TransactionDaoJdbcTest {
    private static Connection con;
    private static final String YamlInitFile = "application.yaml";
    private TransactionAction transactionDao;

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
    public static void destroy() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Error of connection closing");
            }
        }
    }

    @BeforeEach
    public void init() {
        transactionDao = new TransactionDaoJdbc(con);
    }

    @Test
    void getAccountTransactionDuringTest() {
        long accountId = 10;
        LocalDateTime from = LocalDateTime.of(2023, 10, 20, 12, 00);
        LocalDateTime to = LocalDateTime.of(2023, 10, 20, 15, 00);
        Transaction.TransactionBuilder builder = Transaction.builder();
        builder.id(0).sum(5000).currencyCode("BYN").accountIdFrom(0).balanceFrom(0).accountIdTo(accountId).balanceTo(5000).transactionType(OperationType.ADDING);
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(0, builder.carryOutAt(LocalDateTime.of(2023, 10, 20, 10, 45)).build());
        transactionList.add(1, builder.carryOutAt(LocalDateTime.of(2023, 10, 20, 13, 45)).build());
        transactionList.add(2, builder.carryOutAt(LocalDateTime.of(2023, 10, 20, 14, 15)).build());
        transactionList.add(3, builder.carryOutAt(LocalDateTime.of(2023, 10, 20, 15, 45)).build());
        transactionList.stream().forEach(tr -> tr.setId(transactionDao.save(tr).getId()));
        List<ExtendTransactionData> transactionsBetween = transactionDao.getAccountTransactionDuring(accountId, from, to);
        Assertions.assertThat(transactionsBetween).hasSize(2);
        long[] ids = transactionsBetween.stream().mapToLong(ExtendTransactionData::getId).toArray();
        Assertions.assertThat(ids).containsExactly(transactionList.get(1).getId(), transactionList.get(2).getId());
        transactionList.stream().forEach(tr -> transactionDao.delete(tr.getId()));
    }

    @Test
    void getTurnover() {
        long accountId = 10;
        long accountIdAnother = accountId + 1;
        LocalDateTime from = LocalDateTime.of(2023, 10, 20, 11, 00);
        LocalDateTime to = LocalDateTime.of(2023, 10, 20, 15, 00);
        Transaction.TransactionBuilder builder = Transaction.builder().currencyCode("BYN").id(0);
        List<Transaction> transactionList = new ArrayList<>();

        transactionList.add(0, builder.sum(5000).accountIdFrom(0).balanceFrom(0).accountIdTo(accountId).balanceTo(5000)
                .transactionType(OperationType.ADDING).carryOutAt(LocalDateTime.of(2023, 10, 20, 10, 45)).build());

        transactionList.add(1, builder.sum(2500).accountIdFrom(accountId).balanceFrom(2500).accountIdTo(0).balanceTo(0)
                .transactionType(OperationType.WITHDRAW).carryOutAt(LocalDateTime.of(2023, 10, 20, 11, 45)).build());
        transactionList.add(2, builder.sum(1000).accountIdFrom(accountId).balanceFrom(1500).accountIdTo(accountIdAnother).balanceTo(1000)
                .transactionType(OperationType.TRANSFER).carryOutAt(LocalDateTime.of(2023, 10, 20, 12, 15)).build());
        transactionList.add(3, builder.sum(500).accountIdFrom(accountIdAnother).balanceFrom(500).accountIdTo(accountId).balanceTo(2000)
                .transactionType(OperationType.TRANSFER).carryOutAt(LocalDateTime.of(2023, 10, 20, 12, 45)).build());
        transactionList.add(4, builder.sum(2000).accountIdFrom(0).balanceFrom(0).accountIdTo(accountId).balanceTo(4000)
                .transactionType(OperationType.ADDING).carryOutAt(LocalDateTime.of(2023, 10, 20, 13, 45)).build());

        transactionList.add(5, builder.sum(1000).accountIdFrom(accountId).balanceFrom(3000).accountIdTo(0).balanceTo(0)
                .transactionType(OperationType.WITHDRAW).carryOutAt(LocalDateTime.of(2023, 10, 20, 15, 45)).build());

        transactionList.stream().forEach(tr -> tr.setId(transactionDao.save(tr).getId()));

        List<Long> turnoverList = transactionDao.getTurnover(accountId, from, to);
        Assertions.assertThat(turnoverList).hasSize(2);
        Assertions.assertThat(turnoverList).containsExactly(2500L, 3500L);
        transactionList.stream().forEach(tr -> transactionDao.delete(tr.getId()));
    }

    @Test
    void save() {
        Transaction transaction = Transaction.builder()
                .id(0).sum(5000).currencyCode("BYN").accountIdFrom(0).balanceFrom(0).accountIdTo(2).balanceTo(5000)
                .carryOutAt(LocalDateTime.of(2023, 10, 20, 13, 45)).transactionType(OperationType.ADDING).build();
        transaction = transactionDao.save(transaction);
        Assertions.assertThat(transaction.getId()).isNotZero();
        long id = transaction.getId();
        Transaction transactionGot = transactionDao.findById(id).get();
        Assertions.assertThat(transactionGot).isEqualTo(transaction);
        transactionDao.delete(id);
    }

    @Test
    void delete() {
        Transaction transaction = Transaction.builder()
                .id(0).sum(5000).currencyCode("BYN").accountIdFrom(0).balanceFrom(0).accountIdTo(2).balanceTo(5000)
                .carryOutAt(LocalDateTime.of(2023, 10, 20, 13, 45)).transactionType(OperationType.ADDING).build();
        transaction = transactionDao.save(transaction);
        long id = transaction.getId();
        transactionDao.delete(id);
        Optional<Transaction> transactionGot = transactionDao.findById(id);
        Assertions.assertThat(transactionGot).isEmpty();
    }

}