package bzh.clevertec.bank.dao;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import bzh.clevertec.bank.domain.entity.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionAction extends SettingConnection{

    public Optional<Transaction> findById(long id);

    public List<ExtendTransactionData> getAccountTransactionDuring(long account_id, LocalDateTime from, LocalDateTime to);

    public List<Long> getTurnover(long account_id, LocalDateTime from, LocalDateTime to);

    public Transaction save(Transaction transaction);

    public void delete(long id);
}
