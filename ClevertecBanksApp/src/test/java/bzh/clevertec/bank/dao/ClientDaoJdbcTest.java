package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Client;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static bzh.clevertec.bank.util.Constants.*;


class ClientDaoJdbcTest {
    private static Connection con;
    private static final String YamlInitFile = "application.yaml";
    private ClientAction clientDao;
    
    @BeforeAll
    public static void configure() throws SQLException {
        Map<String, Object> appConfigParams;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        appConfigParams = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) appConfigParams.get("db");
        String url = db.get(URL).toString();
        String user = db.get(USER).toString();
        String password = db.get(PASSWORD).toString();
        con = DriverManager.getConnection(url, user, password);
    }

    @AfterAll
    public static void destroy(){
        if (con!=null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Error of connection closing");
            }
        }
    }

    @BeforeEach
    public void init(){
        clientDao = new ClientDaoJdbc(con);
    }

    @Test
    void findById() {
        long id = 1;
        Optional<Client> client = clientDao.findById(id);
        Assertions.assertThat(client).isNotEmpty();
        Client clientObj = client.get();
        Assertions.assertThat(clientObj.getId()).isEqualTo(id);
    }

    @Test
    void save() {
        Client client = new Client(0, "Михаил", "Батькович", "Ковалев", "AH951753", null);
        client = clientDao.save(client);
        Assertions.assertThat(client.getId()).isNotZero();
        long id = client.getId();
        Client clientGot = clientDao.findById(id).get();
        clientGot.setCreateDate(null);
        Assertions.assertThat(clientGot).isEqualTo(client);
        clientDao.delete(id);
    }

    @Test
    void update() {
        Client client = new Client(0, "Михаил", "Батькович", "Ковалев", "AH951753", null);
        client = clientDao.save(client);
        long id = client.getId();
        client.setSurname("Ковальчук");
        client.setSecondName("Павлович");
        clientDao.update(client);
        Client clientGot = clientDao.findById(id).get();
        clientGot.setCreateDate(null);
        Assertions.assertThat(clientGot).isEqualTo(client);
        clientDao.delete(id);
    }

    @Test
    void delete() {
        Client client = new Client(0, "Михаил", "Батькович", "Ковалев", "AH951753", null);
        client = clientDao.save(client);
        long id = client.getId();
        clientDao.delete(id);
        Optional<Client> clientGot = clientDao.findById(id);
        Assertions.assertThat(clientGot).isEmpty();
    }
}