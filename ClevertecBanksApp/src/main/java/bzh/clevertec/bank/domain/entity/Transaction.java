package bzh.clevertec.bank.domain.entity;

import bzh.clevertec.bank.util.OperationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode(exclude = {"carryOutAt"})
public class Transaction {
    private long id;
    private final long sum;
    private final String currencyCode;
    private final long accountIdFrom;
    private long balanceFrom;
    private final long accountIdTo;
    private long balanceTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm:ss")
    private final LocalDateTime carryOutAt;
    private final OperationType transactionType;
}
