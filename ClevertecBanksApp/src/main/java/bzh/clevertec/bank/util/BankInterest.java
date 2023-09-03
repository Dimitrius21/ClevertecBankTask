package bzh.clevertec.bank.util;

import bzh.clevertec.bank.dao.BankAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Month;

/**
 * Класс определяющий необходимость начисления процентов и запускающий соответствующий процесс
 */
public class BankInterest implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BankInterest.class);

    private ConnectionSupplier supplier;
    private float interest;
    private volatile boolean startPercentCalculate = false;
    private ChargeBankPercent charger;
    private Month month;

    public BankInterest(ConnectionSupplier supplier, float interest) {
        this.supplier = supplier;
        this.interest = interest;
        charger = new ChargeBynPercent(supplier, interest);
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusNanos(500000000);

        if (now.getMonthValue() != before.getMonthValue() && !startPercentCalculate) {
            startPercentCalculate = true;
            month = before.getMonth();
            new Thread(new ChargePercent()).start();
        }
    }

    /**
     * Внутренний класс для возможности запуска процесса начисления в отдеьлном потоке
     */
    class ChargePercent implements Runnable {
        @Override
        public void run() {
            charger.charge();
            logger.info("interest accrued for {}", month);
            startPercentCalculate = false;
        }
    }
}
