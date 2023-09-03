package bzh.clevertec.bank.domain;

import bzh.clevertec.bank.exception.InvalidRequestDataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для сохранения параметров http-запроса
 */
public class RequestParam {
    Map<String, List<String>> requestParams = new HashMap<>();

    /**
     * получить параметр по его имени
     *
     * @param name имя параметра
     * @return значения параметра
     */
    public List<String> getParam(String name) {
        return requestParams.get(name);
    }

    /**
     * Распарсить строку с параметрами http-запроса и помещения в Map для дальнейшего использования
     *
     * @param requestParamString строка с параметрами запроса
     */
    public void parseParam(String requestParamString) {
        String[] params = requestParamString.split("&");
        for (String param : params) {
            String[] paramParts = param.split("=");
            if (paramParts.length != 2) {
                throw new InvalidRequestDataException("RequestParam " + paramParts[0] + " is not correct");
            }
            String paramKey = paramParts[0].trim();
            String paramValue = paramParts[1].trim();
            if (!requestParams.containsKey(paramKey)) {
                requestParams.put(paramKey, new ArrayList<>());
            }
            requestParams.get(paramKey).add(paramValue);
        }
    }
}
