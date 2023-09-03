package bzh.clevertec.bank;

import bzh.clevertec.bank.servlet.MainServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class AppRunner {
    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
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
        tomcat.getServer().await();
    }
}
