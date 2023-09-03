package bzh.clevertec.bank.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControllerMethod {
    private Object object;
    private Method method;
    private Object[] args;
    //private List<Object> args = new ArrayList<>();

    /*public void addArg(Object arg){
        args.add(arg);
    }*/

}
