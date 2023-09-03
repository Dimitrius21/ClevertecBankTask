package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.BankAction;
import bzh.clevertec.bank.dao.BankDaoJdbc;
import bzh.clevertec.bank.domain.entity.Bank;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Класс реализующий слой Service для сущности Bank
 */
@Slf4j
public class BankService {

    private ConnectionSupplier connectionSupplier;

    public BankService(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Получить bank по его id  в БД
     * @param id bank в БД
     * @return bank из БД
     */
    @AJLogging
    public Bank getBankById(long id) {
        Connection con = connectionSupplier.getConnection();
        BankAction bankDao = new BankDaoJdbc(con);
        Optional<Bank> bank;
        try {
            con.setAutoCommit(true);
            bank = bankDao.findById(id);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in getBankById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
        return bank.orElseThrow(() -> new IllegalArgumentException(String.format("Bank with Id = %d is absent", id)));
    }

    /**
     * Создать новый bank в БД
     * @param bank - данные для нового bank
     * @return bank с внесенным id по которому он записан в БД
     */
    public Bank createBank(Bank bank) {
        Connection con = connectionSupplier.getConnection();
        BankAction bankDao = new BankDaoJdbc(con);
        try {
            con.setAutoCommit(true);
            bank = bankDao.save(bank);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in createBank");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
        return bank;
    }

    /**
     * Удалить bank в БД
     * @param id bank под которым он был внесен в БД
     */
    @AJLogging
    public void deleteBankById(long id) {
        Connection con = connectionSupplier.getConnection();
        BankAction bankDao = new BankDaoJdbc(con);
        try {
            con.setAutoCommit(true);
            bankDao.delete(id);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in deleteBankById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Обновить данные банка в БД
     * @param bank со всеми полями
     * @return обновленный bank
     */
    @AJLogging
    public Bank updateBank(Bank bank) {
        Connection con = connectionSupplier.getConnection();
        BankAction bankDao = new BankDaoJdbc(con);
        try {
            long id = bank.getId();
            Bank bankForUpdate = bankDao.findById(id).orElseThrow(() -> {
                throw new InvalidRequestDataException("Invalid request data");
            });
            bankForUpdate.setBankCode(bank.getBankCode());
            bankForUpdate.setBankName(bank.getBankName());
            bankForUpdate.setAddress(bank.getAddress());
            con.setAutoCommit(true);
            bankDao.update(bank);
            return bankForUpdate;
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in updateBank");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }
}
