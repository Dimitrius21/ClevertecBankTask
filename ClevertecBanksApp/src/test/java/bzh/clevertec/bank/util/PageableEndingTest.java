package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.entity.Bank;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PageableEndingTest {

    @Test
    void createSqlPagingTest() {
        RequestParam params = new RequestParam();
        String requestParamsString = "page=1&size=10&sort=bankName-desc";
        params.parseParam(requestParamsString);
        String res = PageableEnding.createSqlPaging(params, Bank.class);
        String exp = " ORDER BY bank_name DESC LIMIT 10 OFFSET 0";
        Assertions.assertThat(res).isEqualTo(exp);
    }

    @Test
    void createSqlPagingIncorrectDataTest() {
        RequestParam params = new RequestParam();
        String requestParamsString = "page=1&size=10&sort=Name-desc";
        params.parseParam(requestParamsString);
        Assertions.assertThatThrownBy(() -> PageableEnding.createSqlPaging(params, Bank.class))
                .isInstanceOf(InvalidRequestDataException.class);
    }
}