package cn.cqs.aop.navigation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Retrofit 的思想统一页面跳转的类型入口
 */
public class Navigator{

    private static final Map<Method, ProtocolMethod> methodCache = new LinkedHashMap<>();

    private Navigator() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> service) {
        Utils.validateProtocolInterface(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                final ProtocolMethod protocolMethod = loadProtocolMethod(method);
                return protocolMethod.invoke(args);
            }
        });
    }
    private static ProtocolMethod loadProtocolMethod(Method method) {
        ProtocolMethod result = methodCache.get(method);
        if (result != null) {
            return result;
        }
        synchronized (methodCache) {
            result = methodCache.get(method);
            if (result == null) {
                result = new ProtocolMethod.Builder(method).build();
                methodCache.put(method, result);
            }
        }
        return result;
    }
}