package bzh.clevertec.bank.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс для инкопсуляции данных из тела запроса и маппера с помощью которого можно преобразовать данные в объект необходимого класса
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestBody {
    private Object body;
    private ObjectMapper mapper;
}
