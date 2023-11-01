package bzh.clevertec.bank.service;

import bzh.clevertec.bank.dao.ClientAction;
import bzh.clevertec.bank.domain.entity.Client;
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
class ClientServiceTest {
    private static final String YamlInitFile = "application.yaml";
    @Spy
    private static SQLPoolConnection connectionSupplier;
    @Mock
    ClientAction clientDao;

    @InjectMocks
    ClientService clientService;

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
    void getClientById() {
        long id = 1L;
        Client client = getClient(id);
        when(clientDao.findById(id)).thenReturn(Optional.of(client));
        Client result = clientService.getClientById(id);
        Assertions.assertThat(result).isEqualTo(client);
    }

    @Test
    void createClient() {
        Client client = getClient(0);
        Client client1 = getClient(1);
        when(clientDao.save(client)).thenReturn(client1);
        Client result = clientService.createClient(client);
        Assertions.assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void deleteClientById() {
        long id = 1;
        doNothing().when(clientDao).delete(id);
        clientService.deleteClientById(id);
        verify(clientDao).delete(id);
    }

    @Test
    void updateClient() {
        long id = 1;
        Client clientInDb = getClient(id);
        Client client = getClient(id);
        client.setSecondName("Петрович");
        client.setPassportNumber("PP789654");
        when(clientDao.findById(id)).thenReturn(Optional.of(clientInDb));
        when(clientDao.update(client)).thenReturn(1);
        Client result = clientService.updateClient(client);
        Assertions.assertThat(result).isEqualTo(client);
    }

    private static Client getClient(long id) {
        return new Client(id, "Михаил", "Батькович", "Ковалев", "AH951753", null);
    }
}