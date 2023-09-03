package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.domain.RequestBody;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.entity.Bank;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.service.BankService;
import bzh.clevertec.bank.util.ConnectionSupplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class BankController {

    private ConnectionSupplier connectionSupplier;
    BankService service;

    public BankController(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        service = new BankService(connectionSupplier);
    }

    public ResponseBody getBankById(RequestParam params) {
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "json";
            Bank bank = service.getBankById(id);
            return new ResponseBody(bank, code, type);
        }
        catch (NumberFormatException | NullPointerException e){
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

    public ResponseBody createBank(RequestBody body) {
        try {
            ObjectMapper mapper = body.getMapper();
            Bank bank = mapper.readValue(body.getBody().toString(), Bank.class);
            bank = service.createBank(bank);
            int code = 201;
            String type = "json";
            return new ResponseBody(bank, code, type);
        }catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);

        }
    }


    public ResponseBody updateBank(RequestBody body){
        try {
            ObjectMapper mapper = body.getMapper();
            Bank bank = mapper.readValue(body.getBody().toString(), Bank.class);
            bank = service.updateBank(bank);
            int code = 200;
            String type = "json";
            return new ResponseBody(bank, code, type);
        }catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);

        }
    }

    public ResponseBody deleteBank(RequestParam params){
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "string";
            service.deleteBankById(id);
            return new ResponseBody("Data has been removed", code, type);
        }
        catch (NumberFormatException | NullPointerException e){
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

}
