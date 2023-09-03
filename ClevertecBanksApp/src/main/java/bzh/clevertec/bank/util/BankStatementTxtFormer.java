package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.BankStatement;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Класс для создания текстового представления выписки
 */
public class BankStatementTxtFormer implements StatementFormer {

    /**
     * Метод формирует текстовое представление полученных данных выписки
     *
     * @param bankStatement - данные выписки ввиде BankStatement
     * @return - сформированное текстовое представление выписки
     */
    @Override
    public Writer formStatement(BankStatement bankStatement) {

        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        CharArrayWriter wr = new CharArrayWriter();
        wr.append("              Statement\n");
        createMainPartOfDocument(wr, bankStatement);
        wr.append(String.format("Date       |      Примечание                        | Amount\n"));
        wr.append(String.format("---------------------------------------------------------------\n"));
        for (ExtendTransactionData it : bankStatement.getItems()) {
            wr.append(it.getCarryOutAt().format(formatterData) + " |");
            if (it.getTransactionType() == OperationType.ADDING || it.getTransactionType() == OperationType.WITHDRAW) {
                wr.append(String.format("%-50s|", it.getTransactionType()));
            } else if (bankStatement.getAccountId() == it.getAccountIdTo()) {
                wr.append(String.format("%-50s|", "Getting money from " + it.getNameFrom()));
            } else {
                wr.append(String.format("%-50s|", "Transfer money to " + it.getNameTo()));
            }
            wr.append(String.format("%.2f", it.getSum() / 100.0) + "\n");
        }
        return wr;
    }

    /**
     * Метод формирует текстовое представление полученных данных оборотной выписки по счету
     *
     * @param statement - данные выписки ввиде BankStatement
     * @return - сформированное текстовое представление выписки
     */
    @Override
    public Writer formTurnoverStatement(BankStatement statement) {
        CharArrayWriter wr = new CharArrayWriter();
        wr.append("              Statement\n");
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        createMainPartOfDocument(wr, statement);
        wr.append(String.format("        Getting           |      Withdrawal       \n"));
        wr.append(String.format("--------------------------------------------------\n"));
        List<Long> turnover = statement.getTurnover();
        wr.append(String.format("     %-20.2f|     %-20.2f\n", turnover.get(1) / 100.0, turnover.get(0) / 100.0));
        return wr;
    }

    private void createMainPartOfDocument(CharArrayWriter wr, BankStatement bankStatement) {
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter formatterDataTime = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");
        wr.append(bankStatement.getBankName() + "\n");
        wr.append(String.format("Client                     | %s\n", bankStatement.getOwner()));
        wr.append(String.format("Account                    | %s\n", bankStatement.getAccountNumber()));
        wr.append(String.format("Currency                   | %s\n", bankStatement.getCurrency()));
        wr.append(String.format("Open Date                  | %s\n", bankStatement.getCreateAt().format(formatterData)));
        wr.append(String.format("Period                     | %s - %s\n",
                bankStatement.getBeginPeriod().format(formatterData), bankStatement.getEndPeriod().format(formatterData)));
        wr.append(String.format("Дата и время формирования  | %s\n", bankStatement.getDate().format(formatterDataTime)));
        wr.append(String.format("Balance                    | %.2f\n", bankStatement.getBalance() / 100.0));
    }
}


