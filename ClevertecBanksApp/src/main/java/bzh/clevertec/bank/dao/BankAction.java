package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.entity.Bank;

import java.sql.Connection;
import java.util.Optional;

public interface BankAction extends SettingConnection{

    public Optional<Bank> findById(long id);

    Optional<Bank> findByCode(String bankCode);

    public Bank save(Bank bank);

    public int update(Bank bank);

    public void delete(long id);
}
