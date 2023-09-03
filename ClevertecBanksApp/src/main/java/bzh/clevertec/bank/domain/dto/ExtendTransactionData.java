package bzh.clevertec.bank.domain.dto;

import bzh.clevertec.bank.util.OperationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtendTransactionData {
    private long id;
    private long sum;
    private String currencyCode;
    private long accountIdFrom;
    private long balanceFrom;
    private String nameFrom;
    private long accountIdTo;
    private long balanceTo;
    private String nameTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm:ss")
    private LocalDateTime carryOutAt;
    private OperationType transactionType;
}
