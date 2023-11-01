package bzh.clevertec.bank.service;

import bzh.clevertec.bank.dao.BankAction;
import bzh.clevertec.bank.domain.entity.Bank;
import bzh.clevertec.bank.servlet.SQLPoolConnection;
import bzh.clevertec.bank.util.ConnectionSupplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    BankAction bankDao;

    @InjectMocks
    BankService bankService;

    @BeforeAll
    public static void init() {
        Map<String, Object> appConfigParams;
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        appConfigParams = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) appConfigParams.get("db");
        connectionSupplier = SQLPoolConnection.createPool(db);
    }

    @AfterAll
    public static void finish(){
        connectionSupplier.closePool();
    }

    @Test
    void getBankById() {
        long id = 1L;
        Bank bank = getBank(id);
        when(bankDao.findById(id)).thenReturn(Optional.of(bank));
        Bank result = bankService.getBankById(id);
        Assertions.assertThat(result).isEqualTo(bank);
    }

    @Test
    void createBank() {
        Bank bank = getBank(0);
        Bank Bank1 = getBank(1);
        when(bankDao.save(bank)).thenReturn(Bank1);
        Bank result = bankService.createBank(bank);
        Assertions.assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deleteBankById() {
        long id = 1;
        doNothing().when(bankDao).delete(id);
        bankService.deleteBankById(id);
        verify(bankDao).delete(id);
    }

    @Test
    void updateBank() {
        long id = 1;
        Bank BankInDb = getBank(id);
        Bank bank = getBank(id);
        bank.setAddress("Minsk");
        when(bankDao.findById(id)).thenReturn(Optional.of(BankInDb));
        when(bankDao.update(bank)).thenReturn(1);
        Bank result = bankService.updateBank(bank);
        Assertions.assertThat(result).isEqualTo(bank);
    }

    private Bank getBank(long id) {
        return new Bank(id, "BY2222", "BigBank", "World");
    }
}