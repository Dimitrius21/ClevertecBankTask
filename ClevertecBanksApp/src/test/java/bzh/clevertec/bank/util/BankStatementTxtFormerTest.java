package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.BankStatement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class BankStatementTxtFormerTest {
    private static BankStatement statement;
    private StatementFormer former;

    @BeforeAll
    public static void init() {
        statement = new BankStatement();
        statement.setBankName("Bank");
        statement.setDate(LocalDateTime.now());
        statement.setAccountId(1);
        statement.setAccountNumber("12345");
        statement.setCurrency("BYN");
        statement.setCreateAt(LocalDateTime.of(2023, 9, 01, 12, 0));
        statement.setOwner("Jon");
        statement.setBeginPeriod(LocalDateTime.of(2023, 1, 01, 0, 0));
        statement.setEndPeriod(LocalDateTime.of(2023, 12, 31, 23, 59));
        statement.setBalance(1000);
    }

    @BeforeEach
    public void initTestData() {
        former = new BankStatementTxtFormer();
    }


    @Test
    void formStatementTest() throws IOException {
        ExtendTransactionData transactionData = new ExtendTransactionData(1, 5000, "BYN", 1, 10000, "Ivanov", 2, 15000, "Petrov",
                LocalDateTime.of(2023, 10, 22, 15, 15), OperationType.TRANSFER);
        statement.setItems(List.of(transactionData));
        CharArrayWriter wr = (CharArrayWriter) former.formStatement(statement);
        String res = wr.toString();
        List<String> contains = Arrays.asList("Statement", statement.getBankName(), "Client",
                "Account", "Currency", "Open Date", "Period", "Дата и время формирования", "Balance", "Date", "Примечание", "Amount");
        Assertions.assertThat(res).contains(contains);

    }

    @Test
    void formTurnoverStatementTest() throws IOException {
        statement.setTurnover(List.of(2000L, 5000L));
        CharArrayWriter wr = (CharArrayWriter) former.formTurnoverStatement(statement);
        String res = wr.toString();

        List<String> contains = Arrays.asList("Statement", statement.getBankName(), "Client",
                "Account", "Currency", "Open Date", "Period", "Дата и время формирования", "Balance", "Getting", "Withdrawal");
        Assertions.assertThat(res).contains(contains);
    }

}