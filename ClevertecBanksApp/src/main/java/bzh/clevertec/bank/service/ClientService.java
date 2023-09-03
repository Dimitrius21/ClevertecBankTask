package bzh.clevertec.bank.service;

import bzh.clevertec.bank.annotation.AJLogging;
import bzh.clevertec.bank.dao.ClientAction;
import bzh.clevertec.bank.dao.ClientDaoJdbc;
import bzh.clevertec.bank.domain.entity.Client;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Класс реализующий слой Service для сущности Client
 */
@Slf4j
public class ClientService {

    private ConnectionSupplier connectionSupplier;

    public ClientService(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Получить client по его id  в БД
     * @param id client в БД
     * @return client в БД
     */
    @AJLogging
    public Client getClientById(long id) {
        Connection con = connectionSupplier.getConnection();
        ClientAction clientDao = new ClientDaoJdbc(con);
        Optional<Client> bank;
        try {
            con.setAutoCommit(true);
            bank = clientDao.findById(id);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in getClientById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
        return bank.orElseThrow(() -> new IllegalArgumentException(String.format("Client with Id = %d is absent", id)));
    }

    /**
     * Создать нового client в БД
     * @param client - данные по новому client
     * @return - client с внесенным id по которому он записан в БД
     */
    @AJLogging
    public Client createClient(Client client) {
        Connection con = connectionSupplier.getConnection();
        ClientAction clientDao = new ClientDaoJdbc(con);
        try {
            client.setCreateDate(LocalDateTime.now());
            con.setAutoCommit(true);
            client = clientDao.save(client);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in createClient");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
        return client;
    }

    /**
     * Удалить client из БД
     * @param id - client под которым он был внесен в БД
     */
    @AJLogging
    public void deleteClientById(long id) {
        Connection con = connectionSupplier.getConnection();
        ClientAction clientDao = new ClientDaoJdbc(con);
        try {
            con.setAutoCommit(true);
            clientDao.delete(id);
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in deleteClientById");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }

    /**
     * Обновить данные cleient в БД
     * @param client - со всеми полями
     * @return обновленный client
     */
    @AJLogging
    public Client updateClient(Client client) {
        Connection con = connectionSupplier.getConnection();
        ClientAction clientDao = new ClientDaoJdbc(con);
        try {
            long id = client.getId();
            Client clientForUpdate = clientDao.findById(id).orElseThrow(() -> {
                throw new InvalidRequestDataException("Invalid request data");
            });
            clientForUpdate.setFirstName(client.getFirstName());
            clientForUpdate.setSecondName(client.getSecondName());
            clientForUpdate.setSurname(client.getSurname());
            clientForUpdate.setPassportNumber(client.getPassportNumber());
            con.setAutoCommit(true);
            clientDao.update(client);
            return clientForUpdate;
        } catch (SQLException e) {
            log.info("Error of setAutoCommit(true) in updateClient");
            throw new DBException("DB error");
        } finally {
            connectionSupplier.backConnection(con);
        }
    }
}
