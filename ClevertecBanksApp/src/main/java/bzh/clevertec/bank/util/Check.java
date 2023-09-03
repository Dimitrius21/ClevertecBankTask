package bzh.clevertec.bank.util;

import bzh.clevertec.bank.dao.BankAction;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.exception.FileOperationException;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Класс формирующий и сохраняющий чек после операций над счетом
 */
public class Check {

    private static final Logger logger = LoggerFactory.getLogger(Check.class);

    private String basePath;

    public Check(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Основной метод организующей весть процесс по формированию и сохранеию чека операции
     * @param transaction - информация о проведенной операции
     * @param sender - информация о банке отправители денежных средств
     * @param recipient - информация о банке получателе денежных средств
     * @return строковое представление сформированного чека
     */
    public String saveCheck(Transaction transaction, AccountBankInfo sender, AccountBankInfo recipient) {
        String check;
        try {
            check = checkGenerate(transaction, sender, recipient);
            saveToFile(check, transaction.getId());
            return check;
        } catch (IOException e) {
            logger.info("Error of check writing to file, transaction id = " + transaction.getId());
            throw new FileOperationException("Error of check writing to file");
        }
    }

    /**
     * Метод генерирующий чек ввиде текстовой формы
     * @param transaction - информация о проведенной операции
     * @param sender - информация о банке отправители денежных средств
     * @param recipient - информация о банке получателе денежных средств
     * @return - сформированный чек
     * @throws IOException
     */
    public String checkGenerate(Transaction transaction, AccountBankInfo sender, AccountBankInfo recipient)  {
        StringBuilder check = new StringBuilder();
        String str;
        check.append(String.format("|%-51s|\n", "        Bank receipt:"));
        check.append(String.format("|%-25s %25d|\n", "Number:", transaction.getId()));
        check.append(String.format("|%-25s %25s|\n", transaction.getCarryOutAt().toLocalDate(),
                transaction.getCarryOutAt().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        check.append(String.format("|%-25s %25s|\n", "Transaction type:", transaction.getTransactionType().getDescription()));
        check.append(String.format("|%-25s %25s|\n", "Sender's bank:", sender!=null ? sender.getBankName() : " - "));
        check.append(String.format("|%-25s %25s|\n", "Recipient's bank:", recipient!=null ? recipient.getBankName() : " -  "));
        check.append(String.format("|%-25s %25s|\n", "Sender's account:", sender!=null ? sender.getAccountNumber() : " - "));
        check.append(String.format("|%-25s %25s|\n", "Recipient's account:", recipient!=null ? recipient.getAccountNumber() : " -  "));
        str = String.format("%.2f %s", transaction.getSum() / 100.0, transaction.getCurrencyCode());
        check.append(String.format("|%-25s %25s|\n", "Amount:", str));
        return check.toString();
    }

    /**
     * Метод сохраняет полученный чек в файл на диске
     * @param check - сформированный чек
     * @param number - дополнительная часть имени файла, где сохраняется чек
     */
    public void saveToFile(String check, long number) throws IOException {
        String fileNameBase = "check";
        String fileName = fileNameBase + number + ".txt";
        Path path = Path.of(basePath, "WEB-INF", "check");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            path = Path.of(basePath, "WEB-INF", "check", fileName);
            Files.writeString(path, check);
    }
}
