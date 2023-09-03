package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.exception.InvalidRequestDataException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс содержит метод преобразования входных параметров http-запроса в строку являющуюся окончанием SQL запроса для
 * ограничения количества получаемых из БД записей и их сортировку
 */
public class PageableEnding {

    /**
     * метод преобразует входные параметры http-запроса в строку являющуюся окончанием SQL запроса для
     * * ограничения количества получаемых из БД записей и их сортировку
     *
     * @param param - входные параметры http-запроса
     * @param clazz - Class для сущности данные которой будут извлекаться из БД
     * @return
     */
    public static String createSqlPaging(RequestParam param, Class<?> clazz) {
        StringBuffer buffer = new StringBuffer();
        List<String> pageParam = param.getParam("page");
        List<String> sizeParam = param.getParam("size");
        List<String> sortParam = param.getParam("sort");
        int page = 0;
        int size = 20;
        try {
            if (sizeParam != null) {
                size = Integer.parseInt(sizeParam.get(0).toString());
            }
            if (pageParam != null) {
                page = Integer.parseInt(pageParam.get(0).toString());
            }
            if (size < 1 || page < 1) {
                throw new InvalidRequestDataException("Request params are not correct");
            }
            if (sortParam != null) {
                String[] sort = sortParam.get(0).toString().split("-");
                String sortField = sort[0].trim();
                String sortType = sort[1].trim();
                List<String> fields = Arrays.stream(clazz.getDeclaredFields()).map(f -> f.getName()).collect(Collectors.toList());
                if (!fields.contains(sortField) || !("asc".equalsIgnoreCase(sortType) || "desc".equalsIgnoreCase(sortType))) {
                    throw new InvalidRequestDataException("Request params - field " + sortField + " are not correct");
                }
                char[] charField = sortField.toCharArray();
                List<Character> charFieldInDb = new ArrayList<>();
                for (int i = 0; i < charField.length; i++) {
                    if (Character.isUpperCase(charField[i])) {
                        charFieldInDb.add('_');
                        charFieldInDb.add(Character.toLowerCase(charField[i]));
                    } else {
                        charFieldInDb.add(charField[i]);
                    }
                }
                StringBuffer fieldInDb = new StringBuffer(charFieldInDb.size());
                charFieldInDb.stream().forEach(fieldInDb::append);
                fieldInDb.append(" ");
                buffer.append(" ORDER BY ");
                buffer.append(fieldInDb);
                buffer.append(sortType.toUpperCase());
            }
            buffer.append(" LIMIT ");
            buffer.append(size);
            buffer.append(" OFFSET ");
            buffer.append((page - 1) * size);

            return buffer.toString();
        } catch (NumberFormatException ex) {
            throw new InvalidRequestDataException("Request params are not correct", ex);
        }
    }
}
