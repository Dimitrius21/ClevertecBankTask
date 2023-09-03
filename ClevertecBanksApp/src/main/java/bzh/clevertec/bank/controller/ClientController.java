package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.domain.RequestBody;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.entity.Client;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.service.ClientService;
import bzh.clevertec.bank.util.ConnectionSupplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ClientController {

    private ConnectionSupplier connectionSupplier;
    ClientService service;

    public ClientController(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        service = new ClientService(connectionSupplier);
    }

    public ResponseBody getClientById(RequestParam params){
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "json";
            Client client = service.getClientById(id);
            return new ResponseBody(client, code, type);
        }
        catch (NumberFormatException | NullPointerException e){
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }

    public ResponseBody createClient(RequestBody body) {
        try {
            ObjectMapper mapper = body.getMapper();
            Client client = mapper.readValue(body.getBody().toString(), Client.class);
            client = service.createClient(client);
            int code = 201;
            String type = "json";
            return new ResponseBody(client, code, type);
        }catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        }
    }

    public ResponseBody updateClient(RequestBody body){
        try {
            ObjectMapper mapper = body.getMapper();
            Client client = mapper.readValue(body.getBody().toString(), Client.class);
            client = service.updateClient(client);
            int code = 200;
            String type = "json";
            return new ResponseBody(client, code, type);
        }catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        }
    }

    public ResponseBody deleteClient(RequestParam params){
        List<String> idParam = params.getParam("id");
        try {
            long id = Long.parseLong(idParam.get(0));
            int code = 200;
            String type = "string";
            service.deleteClientById(id);
            return new ResponseBody("Data has been removed", code, type);
        }
        catch (NumberFormatException | NullPointerException e){
            throw new InvalidRequestDataException("Invalid parameter id", e);
        }
    }
}
