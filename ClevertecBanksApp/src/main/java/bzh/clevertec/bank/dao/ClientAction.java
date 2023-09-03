package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Client;

import java.util.Optional;

public interface ClientAction {

    public Optional<Client> findById(long id);

    public Client save(Client client);

    public int update(Client client);

    public void delete(long id);

}
