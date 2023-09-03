package bzh.clevertec.bank.servlet;

import bzh.clevertec.bank.context.AppContext;
import bzh.clevertec.bank.context.ControllerMethod;
import bzh.clevertec.bank.exception.AppContextException;
import bzh.clevertec.bank.exception.DBException;
import bzh.clevertec.bank.exception.ServletInitialisationException;
import bzh.clevertec.bank.util.ConnectionSupplier;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bzh.clevertec.bank.util.BankInterest;

import static bzh.clevertec.bank.util.Constants.*;

/**
 * Класс производящий создание необходимой инфраструктуры для работы приложения.
 */
@WebListener
public class AppInitializer implements ServletContextListener {
    private final String YamlInitFile = "application.yaml";
    private final String YamlPaths = "paths.yaml";
    ScheduledExecutorService executorService;
    private Map<String, Object> appConfigParams;
    private float bankInterest = 1;

    /**
     * Инициализация приложение, создание необходимых объектов классов
     *
     * @param sce the ServletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        appConfigParams = yaml.load(is);
        ConnectionSupplier pool = initDB(sc);
        System.out.println("POOL create");
        controllersObjectCreating(sc, pool);
        sc.setAttribute(CONNECTION, pool);
        if (appConfigParams.containsKey(BANK_INTEREST)) {
            bankInterest = Float.parseFloat(appConfigParams.get(BANK_INTEREST).toString());
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new BankInterest(pool, bankInterest), 1000, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Создание контекста (AppContext) - содержащего список объектов с данными о контроллере, методе для http-запроса
     * Список соответствий находится в paths.yaml файле
     *
     * @param sc
     * @param pool
     */
    private void controllersObjectCreating(ServletContext sc, ConnectionSupplier pool) {
        Yaml yaml = new Yaml();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlPaths);
        Map<String, Object> objPaths = yaml.load(is);
        Set<String> requestTypes = objPaths.keySet();
        AppContext context = new AppContext();
        Map<String, Object> controllers = new HashMap<>();
        for (String requestType : requestTypes) {
            Map<String, String> paths = (Map<String, String>) objPaths.get(requestType);
            Set<String> pathsSet = paths.keySet();
            for (String path : pathsSet) {
                String[] classData = paths.get(path).split(",");
                String classFullName = classData[0].trim();
                try {
                    if (!controllers.containsKey(classFullName)) {
                        Class<?> clazz = Class.forName(classFullName);
                        Object controllerObject = clazz.getDeclaredConstructor(ConnectionSupplier.class).newInstance(pool);
                        controllers.put(classFullName, controllerObject);
                    }
                    Object controller = controllers.get(classFullName);
                    Method[] methods = controller.getClass().getMethods();
                    //Поиск Method соответствующего указанному методу в файле конфигурации контроллеров
                    Method method = Arrays.stream(methods).filter(m -> m.getName().equals(classData[1].trim())).findFirst().orElseThrow();
                    Class<?>[] argsType = method.getParameterTypes();

                    ControllerMethod controllerMethod = new ControllerMethod();
                    controllerMethod.setObject(controller);
                    controllerMethod.setMethod(method);
                    controllerMethod.setArgs(argsType);

                    context.addControllerMethod(requestType, path, controllerMethod);

                } catch (ClassNotFoundException e) {
                    throw new AppContextException("Class " + classFullName + " not found");
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    throw new AppContextException("Can't create an object of " + classFullName);
                }
            }
        }
        sc.setAttribute(APP_CONTEXT, context);
    }

    /**
     * Метод запускает создание пула конэкшенов к базе данных и в случае наличия инициализирующих файлов SQL выполняет
     * создание необходимых таблиц, если они еще не существуют и внесение данных
     *
     * @param sc - ServletContext
     * @return - созданный пула конэкшенов
     * @throws ServletInitialisationException
     */
    private SQLPoolConnection initDB(ServletContext sc) throws ServletInitialisationException {

        Map<String, Object> db = (Map<String, Object>) appConfigParams.get("db");
        if (Objects.isNull(db)) {
            throw new ServletInitialisationException("Data for DB initialize is absent");
        }
        SQLPoolConnection pool;
        Connection con;
        try {
            pool = SQLPoolConnection.createPool(db);
            con = pool.getConnection();
            try {
                String sql = "";
                DatabaseMetaData md = con.getMetaData();
                ResultSet rs = md.getTables(null, null, "transactions", null);
                boolean existTables = false;
                while (rs.next()) {
                    System.out.println(rs.getString(3));
                    existTables = true;
                }
                if (!existTables) {
                    Statement st = con.createStatement();
                    if (!Objects.isNull(db.get(SCHEMA))) {
                        sql = getSqlStingFromFile(db.get(SCHEMA).toString());
                        st.executeUpdate(sql);
                        if (!Objects.isNull(db.get(DATA))) {
                            sql = getSqlStingFromFile(db.get(DATA).toString());
                            st.execute(sql);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new ServletInitialisationException("Error of DB initializer", ex);
            } catch (URISyntaxException | IOException ex) {
                throw new ServletInitialisationException("Can't read .sql files", ex);
            } finally {
                pool.backConnection(con);
            }
            return pool;

        } catch (DBException ex) {
            throw new ServletInitialisationException("Error of pool connection creating", ex);
        }
    }

    /**
     * Формирует строку с SQL запросами из файла где они сохранены
     *
     * @param filename - имя файла где сохранены SQL запросы
     * @return строка со всеми запросами
     * @throws URISyntaxException
     * @throws IOException
     */
    private String getSqlStingFromFile(String filename) throws URISyntaxException, IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        byte[] bytes = is.readAllBytes();
        String str = new String(bytes);
        return str;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (executorService != null) {
            executorService.shutdown();
        }
        SQLPoolConnection pool = (SQLPoolConnection) sce.getServletContext().getAttribute(CONNECTION);
        if (pool != null) {
            pool.closePool();
        }
    }
}
