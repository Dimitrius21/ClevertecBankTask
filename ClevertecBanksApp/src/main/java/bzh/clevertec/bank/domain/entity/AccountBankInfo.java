package bzh.clevertec.bank.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountBankInfo {
    private long id;
    private String accountNumber;
    private String currencyCode;
    private String bankCode;
    private String bankName;
    private LocalDateTime createAt;
    private String clientFirstName;
    private String clientSecondName;
    private String clientSurname;

    public String getClientFullName(){
        return String.format("%s %s %s", clientSurname, clientFirstName, clientSecondName);
    }
}
