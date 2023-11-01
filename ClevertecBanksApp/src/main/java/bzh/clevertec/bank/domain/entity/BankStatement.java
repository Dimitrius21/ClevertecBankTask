package bzh.clevertec.bank.domain.entity;

import bzh.clevertec.bank.domain.dto.ExtendTransactionData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"date"})
public class BankStatement {
    private LocalDateTime date;
    private long accountId;
    private String accountNumber;
    private String bankName;
    private String currency;
    private LocalDateTime createAt;
    private String owner;
    private LocalDateTime beginPeriod;
    private LocalDateTime endPeriod;
    private long balance;
    private List<ExtendTransactionData> items = new ArrayList<>();
    private List<Long> turnover;
}
