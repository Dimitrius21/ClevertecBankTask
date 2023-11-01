package bzh.clevertec.bank.controller;

import bzh.clevertec.bank.servlet.MainServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

class BankControllerIntegrationTest {
    private static Tomcat tomcat;

    @BeforeAll
    public static void init() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir("temp");

        String contextPath = "/bank";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);
        context.addApplicationListener("bzh.clevertec.bank.servlet.AppInitializer");
        String servletName = "bankApp";
        MainServlet servlet = new MainServlet();
        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded("/api/*", servletName);

        tomcat.start();
        new Thread(() -> tomcat.getServer().await()).start();
    }

    @AfterAll
    public static void finish() throws LifecycleException {
        tomcat.stop();
    }


    @Test
    void getBankByIdTest() throws IOException {
        String uri = "http://localhost:8080/bank/api/bank?id=1";
        HttpResponse response = Request.Get(uri).execute().returnResponse();
        int statusCode = response.getStatusLine().getStatusCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String body = in.readLine();
        Assertions.assertThat(statusCode).isEqualTo(200);
        Assertions.assertThat(body).contains("\"id\":1");
    }

    @Test
    void createBankTest() throws IOException {
        String uri = "http://localhost:8080/bank/api/bank";
        String requestBody = "{\"bankCode\" : \"BYCode\", \"bankName\": \"Bank\", \"address\": \"Minsk\"}";
        HttpResponse response = Request.Post(uri).bodyString(requestBody, ContentType.APPLICATION_JSON).execute().returnResponse();
        int statusCode = response.getStatusLine().getStatusCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String body = in.readLine();
        Assertions.assertThat(statusCode).isEqualTo(201);
        String id = body.split(",")[0].split(":")[1].trim();
        Assertions.assertThat(Long.parseLong(id)).isNotZero();

        Request.Delete(uri + "?id=" + id).execute();
    }

    @Test
    void updateBankTest() throws IOException {
        String uri = "http://localhost:8080/bank/api/bank";
        String requestBody = "{\"bankCode\" : \"BYCode\", \"bankName\": \"Bank\", \"address\": \"Minsk\"}";
        String resp = Request.Post(uri).bodyString(requestBody, ContentType.APPLICATION_JSON).execute().returnContent().toString();
        String id = resp.split(",")[0].split(":")[1].trim();
        requestBody = "{\"id\":" + id + ",\"bankCode\" : \"BYBank\", \"bankName\": \"BigBank\", \"address\": \"Grodno\" }";
        resp = Request.Put(uri).bodyString(requestBody, ContentType.APPLICATION_JSON).execute().returnContent().toString();
        uri = uri + "?id=" + id;
        String body = Request.Get(uri).execute().returnContent().toString();
        Assertions.assertThat(body).contains("BYBank", "BigBank", "Grodno");

        Request.Delete(uri).execute();
    }

    @Test
    void deleteBankTest() throws IOException {
        String uri = "http://localhost:8080/bank/api/bank";
        String requestBody = "{\"bankCode\" : \"BYCode\", \"bankName\": \"Bank\", \"address\": \"Minsk\"}";
        HttpResponse response = Request.Post(uri).bodyString(requestBody, ContentType.APPLICATION_JSON).execute().returnResponse();
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String body = in.readLine();
        String id = body.split(",")[0].split(":")[1].trim();
        uri = uri + "?id=" + id;
        body = Request.Delete(uri).execute().returnContent().toString();
        Assertions.assertThat(body).contains("Data has been removed");
    }
}