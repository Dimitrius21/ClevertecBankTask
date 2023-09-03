package bzh.clevertec.bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Класс содержащий расширенную информацию для подготовеки http-ответа
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ResponseBody extends SimpleResponseBody {
    private Object body;

    public ResponseBody(Object body, int responseCode, String responseType) {
        super(responseCode, responseType);
        this.body = body;
    }
}
