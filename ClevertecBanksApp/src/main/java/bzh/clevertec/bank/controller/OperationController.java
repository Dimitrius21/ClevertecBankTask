package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.domain.RequestBody;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.SimpleResponseBody;
import bzh.clevertec.bank.domain.dto.StatementRequestDto;
import bzh.clevertec.bank.domain.dto.OneSideOperationDto;
import bzh.clevertec.bank.domain.dto.ResponseDto;
import bzh.clevertec.bank.domain.dto.TwoSideOperationDto;
import bzh.clevertec.bank.domain.entity.BankStatement;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.InvalidRequestDataException;
import bzh.clevertec.bank.service.OperationService;
import bzh.clevertec.bank.service.StatementService;
import bzh.clevertec.bank.util.BankStatementTxtFormer;
import bzh.clevertec.bank.util.ConnectionSupplier;
import bzh.clevertec.bank.util.StatementFormer;
import bzh.clevertec.bank.util.StatementToPdf;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.sql.SQLException;

public class OperationController {

    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

    private ConnectionSupplier connectionSupplier;
    private OperationService service;
    private StatementService statementService;

    public OperationController(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
        service = new OperationService(connectionSupplier);
        statementService = new StatementService(connectionSupplier);
    }

    public ResponseBody changeAccount(RequestBody body, HttpServletRequest req) {
        try {
            ObjectMapper mapper = body.getMapper();
            OneSideOperationDto operationDto = mapper.readValue(body.getBody().toString(), OneSideOperationDto.class);
            ResponseDto response = service.changeAccount(operationDto, req);
            int code = 200;
            String type = "json";
            return new ResponseBody(response, code, type);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        } catch (SQLException e) {
            throw new DBException("Error in DB", e);
        }
    }

    public ResponseBody transferMoney(RequestBody body, HttpServletRequest req) {
        try {
            ObjectMapper mapper = body.getMapper();
            TwoSideOperationDto operationDto = mapper.readValue(body.getBody().toString(), TwoSideOperationDto.class);
            ResponseDto response = service.transferMoney(operationDto, req);
            int code = 200;
            String type = "json";
            return new ResponseBody(response, code, type);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        } catch (SQLException e) {
            throw new DBException("Error in DB", e);
        }
    }

    public SimpleResponseBody getBankStatement(RequestBody body, HttpServletRequest req, HttpServletResponse resp) {
        try {
            ObjectMapper mapper = body.getMapper();
            StatementRequestDto requestDto = mapper.readValue(body.getBody().toString(), StatementRequestDto.class);

            BankStatement statement = statementService.getBankStatement(requestDto.getAccount(), requestDto.getBankCode(),
                    requestDto.getFrom().atStartOfDay(), requestDto.getTo().atStartOfDay());
            int code = 200;
            if (requestDto.getType().equalsIgnoreCase("pdf")) {
                OutputStream os = resp.getOutputStream();
                StatementToPdf asPdf = new StatementToPdf(os);
                asPdf.createStatementPdf(statement);
                os.close();
                return new SimpleResponseBody(code, "pdf");
            } else {
                StatementFormer former = new BankStatementTxtFormer();
                Writer wr = former.formStatement(statement);
                return new ResponseBody(wr.toString(), code, "string");
            }
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleResponseBody getBankTurnoverStatement(RequestBody body, HttpServletRequest req, HttpServletResponse resp) {
        try {
            ObjectMapper mapper = body.getMapper();
            StatementRequestDto requestDto = mapper.readValue(body.getBody().toString(), StatementRequestDto.class);

            BankStatement statement = statementService.getBankTurnOverStatement(requestDto.getAccount(), requestDto.getBankCode(),
                    requestDto.getFrom().atStartOfDay(), requestDto.getTo().atStartOfDay());
            int code = 200;
            if (requestDto.getType().equalsIgnoreCase("pdf")) {
                OutputStream os = resp.getOutputStream();
                StatementToPdf asPdf = new StatementToPdf(os);
                asPdf.createTurnoverStatementPdf(statement);
                resp.setStatus(200);
                resp.setContentType("application/pdf");
                os.close();
                return new SimpleResponseBody(code, "pdf");
            } else {
                StatementFormer former = new BankStatementTxtFormer();
                Writer wr = former.formTurnoverStatement(statement);
                return new ResponseBody(wr.toString(), code, "string");
            }
        } catch (JsonProcessingException e) {
            throw new InvalidRequestDataException("Invalid data in request body", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
