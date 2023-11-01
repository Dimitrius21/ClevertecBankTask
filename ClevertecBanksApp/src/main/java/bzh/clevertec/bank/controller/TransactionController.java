package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.dao.TransactionDaoJdbc;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.entity.Transaction;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.service.TransactionService;
import bzh.clevertec.bank.util.ConnectionSupplier;

import java.util.List;

public class TransactionController {
    private ConnectionSupplier connectionSupplier;
    TransactionService service;

    public TransactionController(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        service = new TransactionService(connectionSupplier, new TransactionDaoJdbc());
    }

    public ResponseBody getTransactionById(RequestParam params) {
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "json";
            Transaction transaction = service.getTransactionById(id);
            return new ResponseBody(transaction, code, type);
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

}
