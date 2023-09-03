package bzh.clevertec.bank.util;

import bzh.clevertec.bank.dao.AccountAction;
import bzh.clevertec.bank.dao.AccountDaoJdbc;
import bzh.clevertec.bank.dao.BankAction;
import bzh.clevertec.bank.domain.entity.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс производящий начисление процентов на все счета с типом валюты BYN
 */
public class ChargeBynPercent implements ChargeBankPercent {

    private static final Logger logger = LoggerFactory.getLogger(BankAction.class);
    private final ConnectionSupplier supplier;
    private final Float rate;
    private AccountAction accountDao;

    public ChargeBynPercent(ConnectionSupplier supplier, Float rate) {
        this.supplier = supplier;
        this.rate = rate;
    }

    public void charge() {
        Connection con = supplier.getConnection();
        accountDao = new AccountDaoJdbc(con);
        try {
            con.setAutoCommit(false);
            List<Account> accountList = accountDao.findAll("");
            con.commit();
            for (Account account : accountList) {
                if (account.getCurrencyCode().equals("BYN")) {
                    long accruedInterest = Math.round(account.getValue() * (rate / 100.0));
                    accountDao.addSum(accruedInterest, account.getId());
                }
            }
            con.commit();
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        } finally {
            supplier.backConnection(con);
        }
    }
}
