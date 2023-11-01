package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.dao.TransactionDaoJdbc;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Класс реализующий слой Service для сущности Transaction
 */
@Slf4j
@AllArgsConstructor
public class TransactionService {

    private ConnectionSupplier connectionSupplier;
    private TransactionAction transactionDao;

    /**
     * Получить данные транзакции по ее id в БД
     *
     * @param id - transaction  в БД
     * @return - transaction из БД
     */
    @AJLogging
    public Transaction getTransactionById(long id) {
        Connection con = connectionSupplier.getConnection();
        //TransactionAction transactionDao = new TransactionDaoJdbc(con);
        transactionDao.setConnection(con);
        Optional<Transaction> transaction;
        try {
            con.setAutoCommit(true);
            transaction = transactionDao.findById(id);
            return transaction.orElseThrow(() -> new IllegalArgumentException(String.format("Transaction with Id = %d is absent", id)));
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in getTransactionById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

}
