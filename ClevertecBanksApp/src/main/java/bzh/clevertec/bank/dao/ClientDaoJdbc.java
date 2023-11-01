package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Client;
import bzh.clevertec.bank.exception.DBException;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@NoArgsConstructor
public class ClientDaoJdbc implements ClientAction {

    private static final Logger logger = LoggerFactory.getLogger(ClientDaoJdbc.class);
    private static ThreadLocal<Connection> con = new ThreadLocal<>();

    public ClientDaoJdbc(Connection connection) {
        con.set(connection);
    }

    @Override
    public void setConnection(Connection connection) {
        con.set(connection);
    }

    @Override
    public Optional<Client> findById(long id) {
        try (PreparedStatement st = con.get().prepareStatement("SELECT * FROM clients WHERE id = ?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return Optional.of(getFromResult(rs));
            } else {
                logger.info("Client with id {} hasn't been found", id);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            logger.error("Error of DB request Client with id {}", id);
            throw new DBException("DB Error", ex);
        }
    }

    @Override
    public Client save(Client client) {
        final String createSQL = "INSERT INTO clients (first_name, second_name, surname, passport_number, create_date) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.get().prepareStatement(createSQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getSecondName());
            ps.setString(3, client.getSurname());
            ps.setString(4, client.getPassportNumber());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong("id");
            client.setId(id);
            return client;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public int update(Client client) {
        final String updateSQL = "UPDATE clients SET first_name = ?, second_name = ?, surname = ?, passport_number = ? WHERE id = ?";
        try (PreparedStatement ps = con.get().prepareStatement(updateSQL)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getSecondName());
            ps.setString(3, client.getSurname());
            ps.setString(4, client.getPassportNumber());
            ps.setLong(5, client.getId());
            int count = ps.executeUpdate();
            return count;
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    @Override
    public void delete(long id) {
        final String deleteSQL = "DELETE FROM clients WHERE id = ?";
        try (PreparedStatement ps = con.get().prepareStatement(deleteSQL)) {
            ps.setLong(1, id);
            ps.execute();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }


    private Client getFromResult(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String firstName = rs.getString("first_name");
        String secondName = rs.getString("second_name");
        String surname = rs.getString("surname");
        String passport = rs.getString("passport_number");
        LocalDateTime date = rs.getTimestamp("create_date").toLocalDateTime();
        logger.debug(" get Client with id={}, surname={}", id, surname);
        return new Client(id, firstName, secondName, surname, passport, date);
    }

}
