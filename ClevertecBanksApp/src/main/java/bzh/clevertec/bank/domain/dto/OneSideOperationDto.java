package bzh.clevertec.bank.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OneSideOperationDto {

    private long sum;
    private String currencyCode;
    private int type;
    private String bankCode;
    private String accountNumber;
}
