package bzh.clevertec.bank.util;

import bzh.clevertec.bank.domain.entity.BankStatement;

import java.io.Writer;

/**
 * Интерфейс определяющий методы формирования текстового представления бвнковских выписок
 */
public interface StatementFormer {

    public Writer formStatement(BankStatement bankStatement);

    public Writer formTurnoverStatement(BankStatement statement);
}
