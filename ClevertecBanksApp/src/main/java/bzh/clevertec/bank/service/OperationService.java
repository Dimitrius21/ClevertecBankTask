package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.AccountDaoJdbc;
import bzh.clevertec.bank.dao.TransactionAction;
import bzh.clevertec.bank.dao.TransactionDaoJdbc;
import bzh.clevertec.bank.domain.dto.OneSideOperationDto;
import bzh.clevertec.bank.domain.dto.ResponseDto;
import bzh.clevertec.bank.domain.dto.TwoSideOperationDto;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.util.Check;
import bzh.clevertec.bank.util.ConnectionSupplier;
import bzh.clevertec.bank.util.OperationType;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Класс уровня сервис для основных операций над счетом
 */
public class OperationService {

    private ConnectionSupplier connectionSupplier;

    public OperationService(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Метод реализующий одностороннюю операцию пополнения счета или снятия средств со счета
     * с созданием обекта класса Transaction и сохранением в файле Чека с краткой информацией об операции
     *
     * @param operationDto - данные для проведения операции (счет, сумма, тип...)
     * @param req          - HttpServletRequest
     * @return - ответ с информацией о выполненной операции (выпонена или недостаточно средств)
     * @throws DBException  в случае ошибки при операции с базой данных
     * @throws SQLException в случае невозможности отката транзакции при выполнении ее отката
     */
    @AJLogging
    public ResponseDto changeAccount(OneSideOperationDto operationDto, HttpServletRequest req) throws DBException, SQLException {
        Connection con = connectionSupplier.getConnection();
        try {
            con.setAutoCommit(false);
            AccountAction accountDao = new AccountDaoJdbc(con);
            AccountBankInfo account = accountDao.findByNumber(operationDto.getAccountNumber(), operationDto.getBankCode())
                    .orElseThrow(() -> new InvalidRequestDataException("Such account does not exist"));
            con.commit();
            AccountBankInfo sender = null;
            AccountBankInfo recipient = null;
            String responseMessage = "Account change correspond";
            int responseCode = 200;
            Transaction transaction = null;
            boolean enable = Objects.nonNull(account) && account.getCurrencyCode().equals(operationDto.getCurrencyCode())
                    && (OperationType.WITHDRAW.ordinal() == operationDto.getType() || OperationType.ADDING.ordinal() == operationDto.getType());
            if (enable) {
                Transaction.TransactionBuilder builder = Transaction.builder()
                        .sum(operationDto.getSum()).currencyCode(operationDto.getCurrencyCode())
                        .transactionType(OperationType.getTypeByCode(operationDto.getType()));
                long amount = operationDto.getSum();
                long balanceTo = 0;
                long balanceFrom = 0;
                TransactionAction transactionDao = new TransactionDaoJdbc(con);
                if (OperationType.ADDING.ordinal() == operationDto.getType()) {
                    builder.accountIdTo(account.getId()).accountIdFrom(0);
                    balanceTo = accountDao.addSum(operationDto.getSum(), account.getId());
                    recipient = account;
                } else if (OperationType.WITHDRAW.ordinal() == operationDto.getType()) {
                    long balanceAccountFrom = accountDao.getBalance(account.getId());
                    if (balanceAccountFrom < operationDto.getSum()) {
                        responseMessage = "Haven't enough money for operation";
                        enable = false;
                    } else {
                        builder.accountIdTo(0).accountIdFrom(account.getId());
                        balanceFrom = accountDao.addSum(amount * (-1), account.getId());
                        sender = account;
                    }
                }
                if (enable) {
                    builder.carryOutAt(LocalDateTime.now())
                            .balanceFrom(balanceFrom)
                            .balanceTo(balanceTo);
                    transaction = builder.build();
                    transaction = transactionDao.save(transaction);
                }
                con.commit();
                if (enable) {
                    Check check = new Check(req.getServletContext().getRealPath("/"));
                    check.saveCheck(transaction, sender, recipient);
                }
            } else {
                responseCode = 400;
                responseMessage = "Invalid input data";
            }
            return new ResponseDto(responseCode, responseMessage);
        } catch (SQLException ex) {
            con.rollback();
            throw new DBException("Error of DB", ex);
        } finally {
            if (con != null)
                connectionSupplier.backConnection(con);
        }
    }

    /**
     * Метод реализующий перевод денег с одного счета на другой
     * с созданием обекта класса Transaction и сохранением в файле Чека с краткой информацией об операции
     *
     * @param operationDto
     * @param req          - HttpServletRequest
     * @return - ответ с информацией о выполненной операции (выпонена или недостаточно средств)
     * @throws DBException  в случае ошибки при операции с базой данных
     * @throws SQLException в случае невозможности отката транзакции при выполнении ее отката
     */
    @AJLogging
    public ResponseDto transferMoney(TwoSideOperationDto operationDto, HttpServletRequest req) throws DBException, SQLException {
        Connection con = connectionSupplier.getConnection();
        try {
            con.setAutoCommit(false);
            AccountAction accountDao = new AccountDaoJdbc(con);
            AccountBankInfo accountFrom = accountDao.findByNumber(operationDto.getAccountNumberFrom(), operationDto.getBankCodeFrom())
                    .orElseThrow(() -> new InvalidRequestDataException("Such account does not exist"));
            AccountBankInfo accountTo = accountDao.findByNumber(operationDto.getAccountNumberTo(), operationDto.getBankCodeTo())
                    .orElseThrow(() -> new InvalidRequestDataException("Such account does not exist"));
            con.commit();
            String responseMessage = "Transfer has been carried out";
            int responseCode = 200;
            Transaction transaction = null;
            if (Objects.nonNull(accountFrom) && Objects.nonNull(accountTo)
                    && accountFrom.getCurrencyCode().equals(operationDto.getCurrencyCode())
                    && accountTo.getCurrencyCode().equals(operationDto.getCurrencyCode())
                    && OperationType.TRANSFER.ordinal() == operationDto.getType()) {
                Transaction.TransactionBuilder builder = Transaction.builder()
                        .sum(operationDto.getSum()).currencyCode(operationDto.getCurrencyCode())
                        .transactionType(OperationType.getTypeByCode(operationDto.getType()))
                        .accountIdTo(accountTo.getId())
                        .accountIdFrom(accountFrom.getId());
                long balanceAccountFrom = accountDao.getBalance(accountFrom.getId());
                if (balanceAccountFrom < operationDto.getSum()) {
                    responseMessage = "Haven't enough money for transfer";
                } else {
                    long balanceFrom = accountDao.addSum(operationDto.getSum() * (-1), accountFrom.getId());
                    long balanceTo = accountDao.addSum(operationDto.getSum(), accountTo.getId());
                    builder.carryOutAt(LocalDateTime.now())
                            .balanceFrom(balanceFrom)
                            .balanceTo(balanceTo);
                    transaction = builder.build();
                    TransactionAction transactionDao = new TransactionDaoJdbc(con);
                    transaction = transactionDao.save(transaction);
                }
                con.commit();
                Check check = new Check(req.getServletContext().getRealPath("/"));
                check.saveCheck(transaction, accountFrom, accountTo);
            } else {
                responseCode = 400;
                responseMessage = "Invalid input data";
            }
            return new ResponseDto(responseCode, responseMessage);
        } catch (SQLException ex) {
            con.rollback();
            throw new DBException("Error of DB", ex);
        } finally {
            if (con != null)
                connectionSupplier.backConnection(con);
        }
    }

}
