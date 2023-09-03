package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.domain.entity.AccountBankInfo;
import bzh.clevertec.bank.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDaoJdbc implements AccountAction {

    private static final Logger logger = LoggerFactory.getLogger(AccountDaoJdbc.class);

    private static final String CREATE_SQL = "INSERT INTO accounts (account_number, value, currency_code, create_date, bank_id, client_id)" +
            " VALUES (?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM accounts WHERE id = ?";
    private static final String FIND_BY_NUMBER_SQL = """
            SELECT ac.id AS id, account_number, currency_code, bank_code, bank_name, ac.create_date AS create, first_name, second_name, surname 
            FROM accounts AS ac 
            INNER JOIN banks AS b 
            ON b.id = ac.bank_id 
            INNER JOIN clients AS cl
            ON ac.client_id = cl.id
            WHERE ac.account_number = ? AND b.bank_code = ?;
            """;
    private static final String DELETE_SQL = "DELETE FROM accounts WHERE id = ?";
    private static final String FIND_BY_CLIENT_ID_SQL = "SELECT * FROM accounts WHERE client_id = ?";
    private static final String FIND_ALL_SQL = "SELECT * FROM accounts";
    private static final String UPDATE_SQL = "UPDATE accounts SET account_number = ?, value = ?, currency_code = ?, bank_id = ?, client_id = ? WHERE id=?";

    private Connection con;

    public AccountDaoJdbc(Connection con) {
        this.con = con;
    }

    @Override
    public long addSum(Long sum, long accountId) {
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET value=value + ? WHERE id = ? RETURNING value")) {
            ps.setLong(1, sum);
            ps.setLong(2, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public long getBalance(long accountId) {
        try (PreparedStatement ps = con.prepareStatement("SELECT value FROM accounts WHERE id = ? FOR UPDATE")) {
            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("value");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public Optional<Account> getLastAccount(long bankId, String currency) {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE currency_code = ? AND bank_id = ? " +
                "ORDER BY create_date DESC LIMIT 1")) {
            ps.setString(1, currency);
            ps.setLong(2, bankId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(getAccountFromResult(rs));
            } else {
                logger.info("Account with bankId {} and currency {} hasn't been found", bankId, currency);
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }


    @Override
    public Optional<Account> findById(long id) {
        try (PreparedStatement st = con.prepareStatement(FIND_BY_ID_SQL)) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Account account = getAccountFromResult(rs);
                logger.info("Get account by id={}, number={}", account.getId(), account.getAccountNumber());
                return Optional.of(account);
            } else {
                logger.info("Account with id {} hasn't been found", id);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Account with id {}", id);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public Optional<AccountBankInfo> findByNumber(String accountNumber, String bankCode) {
        try (PreparedStatement st = con.prepareStatement(FIND_BY_NUMBER_SQL)) {
            st.setString(1, accountNumber);
            st.setString(2, bankCode);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                AccountBankInfo account = getAccountBankInfoFromResult(rs);
                logger.info(" get AccountBankInfo by number={}, id={}", account.getAccountNumber(), account.getId());
                return Optional.of(account);
            } else {
                logger.info("Account with number {} hasn't been found", accountNumber);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Account with number {}", accountNumber);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public List<Account> findByClientId(long clientId) {
        try (PreparedStatement st = con.prepareStatement(FIND_BY_CLIENT_ID_SQL)) {
            st.setLong(1, clientId);
            logger.info("Get the list of client's accounts with id {}", clientId);
            ResultSet rs = st.executeQuery();
            return getAccontsList(rs);
        } catch (SQLException ex) {
            logger.error("Error of DB request Accounts with id {}", clientId);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public Account save(Account account) {
        try (PreparedStatement ps = con.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getValue());
            ps.setString(3, account.getCurrencyCode());
            ps.setTimestamp(4, Timestamp.valueOf(account.getCreateDate()));
            ps.setLong(5, account.getBankId());
            ps.setLong(6, account.getClientId());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong("id");
            account.setId(id);
            logger.info("Save new account - {}", account);
            return account;
        } catch (SQLException e) {
            logger.error("Error of DB request: create Account");
            throw new DBException(e);
        }
    }

    @Override
    public int update(Account account) {
        try (PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, account.getAccountNumber());
            ps.setLong(2, account.getValue());
            ps.setString(3, account.getCurrencyCode());
            ps.setLong(4, account.getBankId());
            ps.setLong(5, account.getClientId());
            ps.setLong(6, account.getId());
            int count = ps.executeUpdate();
            logger.info("Update new account - {}", account);
            return count;
        } catch (SQLException e) {
            logger.error("Error of DB request: update Account");
            throw new DBException(e);
        }
    }

    @Override
    public void delete(long id) {
        try (PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            logger.info("Account with id = {} has been deleted", id);
            ps.execute();
        } catch (SQLException e) {
            logger.error("Error of DB request: delete Account");
            throw new DBException(e);
        }
    }

    @Override
    public List<Account> findAll(String pageable) {
        try (PreparedStatement st = con.prepareStatement(FIND_ALL_SQL + pageable)) {
            ResultSet rs = st.executeQuery();
            return getAccontsList(rs);
        } catch (SQLException ex) {
            logger.error("Error of DB request findAll accounts");
            throw new DBException("DB Error", ex);
        }
    }

    private Account getAccountFromResult(ResultSet rs) throws SQLException {
        Account.AccountBuilder builder = Account.builder().
                id(rs.getLong("id")).
                accountNumber(rs.getString("account_number")).
                value(rs.getLong("value")).
                currencyCode(rs.getString("currency_code")).
                createDate(rs.getTimestamp("create_date").toLocalDateTime()).
                bankId(rs.getLong("bank_id")).
                clientId(rs.getLong("client_id"));
        Account account = builder.build();

        return account;
    }

    private List<Account> getAccontsList(ResultSet rs) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        while (rs.next()) {
            accounts.add(getAccountFromResult(rs));
        }
        return accounts;
    }

    private AccountBankInfo getAccountBankInfoFromResult(ResultSet rs) throws SQLException {
        AccountBankInfo.AccountBankInfoBuilder builder = AccountBankInfo.builder().
                id(rs.getLong("id"))
                .accountNumber(rs.getString("account_number"))
                .currencyCode(rs.getString("currency_code"))
                .bankCode(rs.getString("bank_code"))
                .bankName(rs.getString("bank_name"))
                .createAt(rs.getTimestamp("create").toLocalDateTime())
                .clientFirstName(rs.getString("first_name"))
                .clientSecondName(rs.getString("second_name"))
                .clientSurname(rs.getString("surname"));

        AccountBankInfo account = builder.build();
        logger.debug(" get AccountBankInfo id={}, number={}", account.getId(), account.getAccountNumber());
        return account;
    }


}
