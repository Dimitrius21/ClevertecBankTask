package bzh.clevertec.bank.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bank {

    private long id;
    private String bankCode;
    private String bankName;
    private String address;
}
