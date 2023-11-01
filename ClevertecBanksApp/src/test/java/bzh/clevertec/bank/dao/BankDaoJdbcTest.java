package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Bank;
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

class BankDaoJdbcTest {
    private static Connection con;
    private static final String YamlInitFile = "application.yaml";
    private BankAction bankDao;

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
        bankDao = new BankDaoJdbc(con);
    }

    @Test
    void findById() {
        long id = 1;
        Optional<Bank> bank = bankDao.findById(id);
        Assertions.assertThat(bank).isNotEmpty();
        Bank bankObj = bank.get();
        Assertions.assertThat(bankObj.getId()).isEqualTo(id);
    }

    @Test
    void findByCode() {
        String code = "CLBKBY22";
        Optional<Bank> bank = bankDao.findByCode(code);
        Assertions.assertThat(bank).isNotEmpty();
        Bank bankObj = bank.get();
        Assertions.assertThat(bankObj.getBankCode()).isEqualTo(code);
    }

    @Test
    void save() {
        Bank bank = new Bank(0,"MYMYBY2X'", "MyBank", "Minsk");
        bank = bankDao.save(bank);
        Assertions.assertThat(bank.getId()).isNotZero();
        long id = bank.getId();
        Bank bankGot = bankDao.findById(id).get();
        Assertions.assertThat(bankGot).isEqualTo(bank);
        bankDao.delete(id);
    }

    @Test
    void update() {
        Bank bank = new Bank(0,"MYMYBY2X'", "MyBank", "Minsk");
        bank = bankDao.save(bank);
        long id = bank.getId();
        bank.setBankName("BigBank");
        bank.setAddress("Bobruisk");
        bankDao.update(bank);
        Bank bankGot = bankDao.findById(id).get();
        Assertions.assertThat(bankGot).isEqualTo(bank);
        bankDao.delete(id);
    }

    @Test
    void delete() {
        Bank bank = new Bank(0,"MYMYBY2X'", "MyBank", "Minsk");
        bank = bankDao.save(bank);
        long id = bank.getId();
        bankDao.delete(id);
        Optional<Bank> bankGot = bankDao.findById(id);
        Assertions.assertThat(bankGot).isEmpty();
    }
}