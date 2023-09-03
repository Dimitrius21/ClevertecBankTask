package bzh.clevertec.bank.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoSideOperationDto {

    private long sum;
    private String currencyCode;
    private int type;
    private String accountNumberFrom;
    private String bankCodeFrom;
    private String accountNumberTo;
    private String bankCodeTo;
}
