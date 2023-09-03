package bzh.clevertec.bank.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AppContext {
    Map<String, Map<String, ControllerMethod>> context = new HashMap();

    public ControllerMethod getControllerMethod(String type, String path){
        ControllerMethod controllerMethod = null;
        Map<String, ControllerMethod> method = context.get(type);
        if (method!=null) {
            controllerMethod = method.get(path);
        }
        return controllerMethod;
    }

    public void addControllerMethod(String type, String path, ControllerMethod controllerMethod){
        Map<String, ControllerMethod> method = context.get(type);
        if (Objects.isNull(method)){
            context.put(type, new HashMap<>());
        }
        context.get(type).put(path, controllerMethod);
    }
}
