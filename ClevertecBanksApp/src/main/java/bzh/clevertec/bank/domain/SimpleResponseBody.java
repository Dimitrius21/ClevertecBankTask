package bzh.clevertec.bank.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс содержащий краткую информацию для подготовеки http-ответа
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleResponseBody {
    private int responseCode;
    private String responseType;
}
