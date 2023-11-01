package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.AccountDaoJdbc;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.dao.TransactionDaoJdbc;
import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.BankStatement;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import bzh.clevertec.bank.util.OperationType;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Класс уровня сервис для получения выписок по счету
 */
@AllArgsConstructor
public class StatementService {

    private ConnectionSupplier connectionSupplier;
    private AccountAction accountDao;
    private TransactionAction transactionDao;

    /**
     * Метод формирует выписку с полной информацией о движении средств за указанный период
     *
     * @param account   - номер счета для которого формируется выписка
     * @param bank_code - код банка в котором находится данный счет
     * @param from      - дата начала периода операций
     * @param to        - да окончания периода операций
     * @return - объект класса BankStatement со списком операций
     * @throws DBException -  в случае ошибок доступа к БД
     */
    @AJLogging
    public BankStatement getBankStatement(String account, String bank_code, LocalDateTime from, LocalDateTime to) throws DBException {
        Connection con = connectionSupplier.getConnection();
        try {
            //AccountAction accountDao = new AccountDaoJdbc(con);
            accountDao.setConnection(con);
            BankStatement statement = new BankStatement();
            con.setAutoCommit(false);

            AccountBankInfo accountBankInfo = accountDao.findByNumber(account, bank_code)
                    .orElseThrow(() -> new InvalidRequestDataException("Such account does not exist"));
            con.commit();
            statement.setAccountNumber(account);
            statement.setAccountId(accountBankInfo.getId());
            statement.setCurrency(accountBankInfo.getCurrencyCode());
            statement.setBankName(accountBankInfo.getBankName());
            statement.setCreateAt(accountBankInfo.getCreateAt());
            statement.setBeginPeriod(from);
            statement.setEndPeriod(to);
            statement.setDate(LocalDateTime.now());
            statement.setOwner(accountBankInfo.getClientFullName());

            //TransactionAction transactionDao = new TransactionDaoJdbc(con);
            transactionDao.setConnection(con);
            List<ExtendTransactionData> extTransactions = transactionDao.getAccountTransactionDuring(accountBankInfo.getId(),
                    from, to);
            con.commit();
            ExtendTransactionData lastTransaction = extTransactions.get(extTransactions.size() - 1);
            OperationType type = lastTransaction.getTransactionType();
            if (type == OperationType.WITHDRAW || type == OperationType.ADDING) {
                statement.setBalance(lastTransaction.getBalanceFrom() + lastTransaction.getBalanceTo());
            } else {
                if (lastTransaction.getAccountIdFrom() == accountBankInfo.getId()) {
                    statement.setBalance(lastTransaction.getBalanceFrom());
                } else {
                    statement.setBalance(lastTransaction.getBalanceTo());
                }
            }
            statement.setItems(extTransactions);
            return statement;
        } catch (SQLException e) {
            throw new DBException("Внутренняя ошибка БД", e);
        } finally {
            if (con != null)
                connectionSupplier.backConnection(con);
        }
    }

    /**
     * Метод формирует краткую выписку с информацией об оборотах по счету за указанный период
     *
     * @param account   - номер счета для которого формируется выписка
     * @param bank_code - код банка в котором находится данный счет
     * @param from      - дата начала периода операций
     * @param to        - да окончания периода операций
     * @return - объект класса BankStatement содерщий информацию об оборотах
     * @throws DBException -  в случае ошибок доступа к БД
     */
    @AJLogging
    public BankStatement getBankTurnOverStatement(String account, String bank_code, LocalDateTime from, LocalDateTime to) throws DBException {
        Connection con = connectionSupplier.getConnection();
        try {
            //AccountAction accountDao = new AccountDaoJdbc(con);
            accountDao.setConnection(con);
            BankStatement statement = new BankStatement();
            con.setAutoCommit(false);

            AccountBankInfo accountBankInfo = accountDao.findByNumber(account, bank_code)
                    .orElseThrow(() -> new InvalidRequestDataException("Such account does not exist"));
            con.commit();
            statement.setAccountNumber(account);
            statement.setAccountId(accountBankInfo.getId());
            statement.setCurrency(accountBankInfo.getCurrencyCode());
            statement.setBankName(accountBankInfo.getBankName());
            statement.setCreateAt(accountBankInfo.getCreateAt());
            statement.setBeginPeriod(from);
            statement.setEndPeriod(to);
            statement.setDate(LocalDateTime.now());
            statement.setOwner(accountBankInfo.getClientFullName());

            long balance = accountDao.getBalance(accountBankInfo.getId());
            con.commit();
            //TransactionAction transactionDao = new TransactionDaoJdbc(con);
            transactionDao.setConnection(con);
            statement.setBalance(balance);
            List<Long> turnovers = transactionDao.getTurnover(accountBankInfo.getId(), from, to);
            con.commit();
            statement.setTurnover(turnovers);
            return statement;
        } catch (SQLException e) {
            throw new DBException("Внутренняя ошибка БД", e);
        } finally {
            if (con != null)
                connectionSupplier.backConnection(con);
        }
    }

}
