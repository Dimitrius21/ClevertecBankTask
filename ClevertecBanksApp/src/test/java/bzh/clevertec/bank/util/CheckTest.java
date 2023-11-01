package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.Transaction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class CheckTest {

    @Test
    void saveCheckTest() throws IOException {
        AccountBankInfo sender = new AccountBankInfo(1, "BY11CLBK181901001", "BYN", "MTBKBY22", "JSC «MTBank»", LocalDateTime.now(), "Сергей", "Сергеевич", "Иванов");
        AccountBankInfo recipient = new AccountBankInfo(2, "BY11CLBK181901002", "BYN", "MTBKBY22", "JSC «MTBank»", LocalDateTime.now(), "Андрей", "Петрович", "Ковалев");
        Transaction.TransactionBuilder builder = Transaction.builder();
        builder.id(1).sum(5000).currencyCode("BYN")
                .accountIdFrom(1).balanceFrom(10000)
                .accountIdTo(2).balanceTo(150000)
                .carryOutAt(LocalDateTime.now())
                .transactionType(OperationType.TRANSFER);
        Transaction transaction = builder.build();
        File f = new File(".");
        String basePath = f.getCanonicalPath();
        Check check = new Check(basePath);
        String res = check.saveCheck(transaction, sender, recipient);

        Path path = Path.of(basePath, "WEB-INF", "check", "check" + transaction.getId() + ".txt");
        Assertions.assertThat(Files.exists(path)).isTrue();

        List<String> content = Files.readAllLines(path);
        Assertions.assertThat(content).hasSize(9);
        Files.delete(path);
    }

    @Test
    void checkGenerateTest() {
        AccountBankInfo sender = new AccountBankInfo(1, "BY11CLBK181901001", "BYN", "MTBKBY22", "JSC «MTBank»", LocalDateTime.now(), "Сергей", "Сергеевич", "Иванов");
        AccountBankInfo recipient = new AccountBankInfo(2, "BY11CLBK181901002", "BYN", "MTBKBY22", "JSC «MTBank»", LocalDateTime.now(), "Андрей", "Петрович", "Ковалев");
        Transaction.TransactionBuilder builder = Transaction.builder();
        builder.id(1).sum(5000).currencyCode("BYN")
                .accountIdFrom(1).balanceFrom(10000)
                .accountIdTo(2).balanceTo(150000)
                .carryOutAt(LocalDateTime.now())
                .transactionType(OperationType.TRANSFER);
        Transaction transaction = builder.build();
        Check check = new Check(".");
        String res = check.checkGenerate(transaction, sender, recipient);
        Assertions.assertThat(res).contains("Bank receipt:", "Number:", "Transaction type:", "Sender's bank:", "Recipient's bank:",
                "Sender's account:", "Recipient's account:", "Amount:");
    }

    @Test
    void saveToFileTest() throws IOException {
        File f = new File(".");
        String basePath = f.getCanonicalPath();
        Check check = new Check(basePath);
        check.saveToFile("123", 1);
        Path path = Path.of(basePath, "WEB-INF", "check", "check1.txt");
        Assertions.assertThat(Files.exists(path)).isTrue();
        Files.delete(path);
    }
}