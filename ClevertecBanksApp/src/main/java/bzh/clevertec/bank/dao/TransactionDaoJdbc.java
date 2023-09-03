package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.util.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDaoJdbc implements TransactionAction {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDaoJdbc.class);

    private static final String FIND_BY_ID_SQL = "SELECT * FROM transactions WHERE id = ?";
    private static final String DELETE_BY_ID_SQL = "DELETE FROM transactions WHERE id = ?";
    private static final String CREATE_SQL = "INSERT INTO transactions (account_id_from, balance_from, account_id_to, balance_to," +
            "sum, currency_code, carry_out_at, transaction_type)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_TRANSACTION_PERIOD_SQL = """
                SELECT t.*, c2.surname AS name_send, c1.surname AS name_reс FROM transactions t
                LEFT JOIN accounts a1
                ON t.account_id_to = a1.id
                LEFT JOIN clients c1
                ON a1.client_id = c1.id
                LEFT JOIN accounts a2
                ON t.account_id_from = a2.id
                LEFT JOIN clients c2
                ON a2.client_id = c2.id
                WHERE account_id_from = ? OR account_id_to = ?
                AND carry_out_at BETWEEN ? AND ? ORDER BY carry_out_at ASC
            """;
    private static final String TURNOVER_SQL = """
                    (SELECT SUM(sum) AS turnover FROM transactions                    
                    WHERE carry_out_at BETWEEN ? AND ?
                    GROUP BY account_id_from
                    HAVING account_id_from = ?)
                    UNION
                    (SELECT SUM(sum) AS turnover FROM transactions t                    
                    WHERE carry_out_at BETWEEN ? AND ?
                    GROUP BY account_id_to
                    HAVING account_id_to = ?)
            """;

    private Connection con;

    public TransactionDaoJdbc(Connection con) {
        this.con = con;
    }

    @Override
    public Optional<Transaction> findById(long id) {
        try (PreparedStatement st = con.prepareStatement(FIND_BY_ID_SQL)) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                logger.debug(" get Transaction id={}", id);
                return Optional.of(getTransactionFromResult(rs));
            } else {
                logger.info("Transaction with id {} hasn't been found", id);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Transaction with id {}", id);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public List<ExtendTransactionData> getAccountTransactionDuring(long account_id, LocalDateTime from, LocalDateTime to) {
        try (PreparedStatement st = con.prepareStatement(FIND_TRANSACTION_PERIOD_SQL)) {
            st.setLong(1, account_id);
            st.setLong(2, account_id);
            st.setTimestamp(3, Timestamp.valueOf(from));
            st.setTimestamp(4, Timestamp.valueOf(to));
            ResultSet rs = st.executeQuery();
            return getExtTransactionsList(rs);
        } catch (SQLException ex) {
            logger.error("Error of DB request - getAccountTransactionDuring");
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public List<Long> getTurnover(long account_id, LocalDateTime from, LocalDateTime to) {
        try (PreparedStatement st = con.prepareStatement(TURNOVER_SQL)) {
            st.setTimestamp(1, Timestamp.valueOf(from));
            st.setTimestamp(2, Timestamp.valueOf(to));
            st.setLong(3, account_id);
            st.setTimestamp(4, Timestamp.valueOf(from));
            st.setTimestamp(5, Timestamp.valueOf(to));
            st.setLong(6, account_id);
            ResultSet rs = st.executeQuery();
            List<Long> turnovers = new ArrayList<>(2);
            if (rs.next()) {
                turnovers.add(rs.getLong("turnover"));
                if (rs.next()) {
                    turnovers.add(rs.getLong("turnover"));
                } else {
                    turnovers.add(turnovers.get(0));
                    turnovers.set(0, 0L);
                }
            } else {
                turnovers.add(0L);
                turnovers.add(0L);
            }
            return turnovers;
        } catch (SQLException ex) {
            logger.error("Error of DB request - getTurnover");
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public Transaction save(Transaction transaction) {
        try (PreparedStatement ps = con.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, transaction.getAccountIdFrom());
            ps.setLong(2, transaction.getBalanceFrom());
            ps.setLong(3, transaction.getAccountIdTo());
            ps.setLong(4, transaction.getBalanceTo());
            ps.setLong(5, transaction.getSum());
            ps.setString(6, transaction.getCurrencyCode());
            ps.setTimestamp(7, Timestamp.valueOf(transaction.getCarryOutAt()));
            ps.setString(8, transaction.getTransactionType().toString());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong("id");
            transaction.setId(id);
            return transaction;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public void delete(long id) {
        try (PreparedStatement ps = con.prepareStatement(DELETE_BY_ID_SQL)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private List<Transaction> getTransactionsList(ResultSet rs) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(getTransactionFromResult(rs));
        }
        return transactions;
    }

    public List<ExtendTransactionData> getExtTransactionsList(ResultSet rs) throws SQLException {
        List<ExtendTransactionData> extTransactions = new ArrayList<>();
        while (rs.next()) {
            extTransactions.add(getExtTransactionFromResult(rs));
        }
        return extTransactions;
    }

    private Transaction getTransactionFromResult(ResultSet rs) throws SQLException {
        Transaction.TransactionBuilder builder = Transaction.builder().
                id(rs.getLong("id")).
                accountIdFrom(rs.getLong("account_id_from")).
                balanceFrom(rs.getLong("balance_from")).
                accountIdTo(rs.getLong("account_to")).
                balanceTo(rs.getLong("balance_to")).
                sum(rs.getLong("sum")).
                currencyCode(rs.getString("currency_code")).
                carryOutAt(rs.getTimestamp("carry_out_at").toLocalDateTime()).
                transactionType(OperationType.valueOf(rs.getString("transaction_type")));
        Transaction transaction = builder.build();
        return transaction;
    }

    public ExtendTransactionData getExtTransactionFromResult(ResultSet rs) throws SQLException {
        ExtendTransactionData.ExtendTransactionDataBuilder builder = ExtendTransactionData.builder().
                id(rs.getLong("id"))
                .nameFrom(rs.getString("name_send"))
                .nameTo(rs.getString("name_reс"))
                .accountIdFrom(rs.getLong("account_id_from"))
                .accountIdTo(rs.getLong("account_id_to"))
                .balanceFrom(rs.getLong("balance_from"))
                .balanceTo(rs.getLong("balance_to"))
                .sum(rs.getLong("sum"))
                .currencyCode(rs.getString("currency_code"))
                .carryOutAt(rs.getTimestamp("carry_out_at").toLocalDateTime())
                .transactionType(OperationType.valueOf(rs.getString("transaction_type")));
        ExtendTransactionData extTransaction = builder.build();
        return extTransaction;
    }
}
