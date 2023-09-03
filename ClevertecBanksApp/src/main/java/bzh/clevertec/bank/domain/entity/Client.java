package bzh.clevertec.bank.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    private long id;
    private String firstName;
    private String secondName;
    private String surname;
    private String passportNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm:ss")
    private LocalDateTime createDate;

    @JsonIgnore
    public String getClientFullName(){
        return surname + " " + firstName + " " + secondName;
    }
}
