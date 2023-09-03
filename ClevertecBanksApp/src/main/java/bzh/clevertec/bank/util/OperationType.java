package bzh.clevertec.bank.util;

/**
 * Типы банковских операций
 */
public enum OperationType {
    WITHDRAW(0, "Снятие средств"),
    ADDING(1, "Пополнение счета"),
    TRANSFER(2, "Банковский перевод");

    private int code;
    private String description;

    OperationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public static OperationType getTypeByCode(int code){
        switch (code){
            case 0 : return OperationType.WITHDRAW;
            case 1 : return OperationType.ADDING;
            case 2 : return OperationType.TRANSFER;
            default: throw new RuntimeException();
        }
    }
}
