package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.dao.AccountDaoJdbc;
import bzh.clevertec.bank.domain.RequestBody;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.entity.Account;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.service.AccountService;
import bzh.clevertec.bank.util.ConnectionSupplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class AccountController {

    private ConnectionSupplier connectionSupplier;
    private AccountService service;

    public AccountController(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        service = new AccountService(connectionSupplier, new AccountDaoJdbc());
    }

    public ResponseBody getAccountById(RequestParam params) {
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "json";
            Account account = service.getAccountById(id);
            return new ResponseBody(account, code, type);
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

    public ResponseBody getAllAccount(RequestParam param) {
        int code = 200;
        String type = "json";
        List<Account> accounts = service.getAllAccount(param);
        return new ResponseBody(accounts, code, type);
    }

    public ResponseBody deleteAccount(RequestParam params) {
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "string";
            service.deleteAccountById(id);
            return new ResponseBody("Data has been removed", code, type);
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

    public ResponseBody createAccount(RequestBody body) {
        try {
            ObjectMapper mapper = body.getMapper();
            Account account = mapper.readValue(body.getBody().toString(), Account.class);
            account = service.createAccount(account);
            int code = 201;
            String type = "json";
            return new ResponseBody(account, code, type);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        }
    }

    public ResponseBody updateAccount(RequestBody body) {
        try {
            ObjectMapper mapper = body.getMapper();
            Account account = mapper.readValue(body.getBody().toString(), Account.class);
            account = service.updateAccount(account);
            int code = 200;
            String type = "json";
            return new ResponseBody(account, code, type);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        }
    }

}
