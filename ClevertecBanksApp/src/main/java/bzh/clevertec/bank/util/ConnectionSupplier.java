package bzh.clevertec.bank.util;

import java.sql.Connection;

public interface ConnectionSupplier {
    public Connection getConnection();

    public void backConnection(Connection con);
}
