package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Bank;
import bzh.clevertec.bank.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class BankDaoJdbc implements BankAction {

    private static final Logger logger = LoggerFactory.getLogger(BankDaoJdbc.class);

    private Connection con;

    public BankDaoJdbc(Connection con) {
        this.con = con;
    }

    @Override
    public Optional<Bank> findById(long id) {
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM banks WHERE id = ?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return Optional.of(getFromResult(rs));
            } else {
                logger.info("Bank with id {} hasn't been found", id);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Bank with id {}", id);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public Optional<Bank> findByCode(String bankCode) {
        try (PreparedStatement st = con.prepareStatement("SELECT * FROM banks WHERE code = bankCode")) {
            st.setString(1, bankCode);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return Optional.of(getFromResult(rs));
            } else {
                logger.info("Bank with bankCode {} hasn't been found", bankCode);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Bank with code {}", bankCode);
            throw new DBException("DB Error", ex);
        }
    }

    /**
     * Метод сохранения объекта Bank в БД
     *
     * @param bank - сохраняемый объект Bank
     * @return - сохраненный объект с присвоенным id
     */
    @Override
    public Bank save(Bank bank) {
        final String createSQL = "INSERT INTO banks (bank_code, bank_name, address) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(createSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bank.getBankCode());
            ps.setString(2, bank.getBankName());
            ps.setString(3, bank.getAddress());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong("id");
            bank.setId(id);
            return bank;
        } catch (SQLException e) {
            throw new DBException("Error of save Bank in DB", e);
        }
    }

    @Override
    public int update(Bank bank) {
        final String updateSQL = "UPDATE banks SET bank_code = ?, bank_name = ?, address = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateSQL)) {
            ps.setString(1, bank.getBankCode());
            ps.setString(2, bank.getBankName());
            ps.setString(3, bank.getAddress());
            ps.setLong(4, bank.getId());
            int count = ps.executeUpdate();
            return count;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public void delete(long id) {
        final String deleteSQL = "DELETE FROM banks WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteSQL)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    private Bank getFromResult(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String code = rs.getString("bank_code");
        String name = rs.getString("bank_name");
        String address = rs.getString("address");
        logger.debug(" get Bank id={}, name={}", id, name);
        return new Bank(id, code, name, address);
    }
}
