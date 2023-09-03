package bzh.clevertec.bank.servlet;

import bzh.clevertec.bank.context.AppContext;
import bzh.clevertec.bank.context.ControllerMethod;
import bzh.clevertec.bank.domain.RequestBody;
import bzh.clevertec.bank.domain.RequestParam;
import bzh.clevertec.bank.domain.ResponseBody;
import bzh.clevertec.bank.domain.SimpleResponseBody;
import bzh.clevertec.bank.exception.FileOperationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;

import static bzh.clevertec.bank.util.Constants.APP_CONTEXT;

/**
 * Сервлет (реализующий паттерн dispatcher Servlet) на который поступают все http-запросы
 */
@WebServlet("/api/*")
public class MainServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest("post", req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest("put", req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest("delete", req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest("get", req, resp);
    }

    /**
     * Производится вызов необходимого метода контроллера исходя из http-запроса и отправка полученного результата
     *
     * @param methodType -  тип http-запроса
     * @param req        - HttpServletRequest
     * @param resp       - HttpServletResponse
     * @throws IOException
     */
    private void handleRequest(String methodType, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        AppContext context = (AppContext) req.getServletContext().getAttribute(APP_CONTEXT);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String queryString = req.getQueryString();

        //Для вызываемого метода контроллера создаем массив передаваемых аргументов
        ControllerMethod controllerMethod = context.getControllerMethod(methodType, servletPath + pathInfo);
        Object[] methodParams = controllerMethod.getArgs();
        Object[] methodArgs = new Object[methodParams.length];
        for (int i = 0; i < methodParams.length; i++) {
            switch (((Class) methodParams[i]).getSimpleName()) {
                case "RequestParam": {
                    RequestParam params = new RequestParam();
                    params.parseParam(queryString);
                    methodArgs[i] = params;
                    break;
                }
                case "RequestBody": {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
                    String body = reader.lines().collect(Collectors.joining());
                    RequestBody requestBody = new RequestBody(body, mapper);
                    methodArgs[i] = requestBody;
                    break;
                }
                case "HttpServletRequest": {
                    methodArgs[i] = req;
                    break;
                }
                case "HttpServletResponse": {
                    methodArgs[i] = resp;
                    break;
                }
            }
        }
        //вызываем метод контролера для соответствующего URI и отправляем полученный результат
        Method method = controllerMethod.getMethod();
        Object objectOfController = controllerMethod.getObject();
        String responseType = "text/plain";
        int responseCode;
        Object responseBody = null;
        resp.setCharacterEncoding("UTF-8");
        try {
            Object response = method.invoke(objectOfController, methodArgs);
            if (response instanceof ResponseBody) {
                PrintWriter pw = resp.getWriter();
                switch (((ResponseBody) response).getResponseType()) {
                    case "json": {
                        responseType = "application/json";
                        responseBody = ((ResponseBody) response).getBody();
                        responseBody = mapper.writeValueAsString(responseBody);
                        break;
                    }
                    case "string": {
                        responseBody = ((ResponseBody) response).getBody();
                        break;
                    }
                }
                responseCode = ((ResponseBody) response).getResponseCode();
                pw.println(responseBody);
                resp.setStatus(responseCode);
                resp.setContentType(responseType);
                pw.close();
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(400, "Invalid request data");
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof FileOperationException) {
                throw new ServletException(e);
            }
            resp.sendError(500, e.getTargetException().getMessage());
        } catch (IllegalAccessException e) {
            throw new ServletException("Cant invoke Controller method", e);
        }
    }
}

