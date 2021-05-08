package com.yoso.autovalue;

import com.yoso.autovalue.annotation.NeedSetValue;
import com.yoso.autovalue.annotation.NeedSetValueField;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author yuanzs
 * @date 2021年04月30日 15:22
 */
public class TracingInterceptor implements MethodInterceptor {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        //切前
        Object proceed = invocation.proceed();
        //切后
        if (method.isAnnotationPresent(NeedSetValueField.class)) {
            if (proceed instanceof Collection) {
                setFieldValueForColEnhance((Collection) proceed);
            } else {
                Collection arrayList = new ArrayList();
                arrayList.add(proceed);
                setFieldValueForColEnhance(arrayList);
            }
        }
//      System.out.println("method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args" +
//      " " + invocation.getArguments());
//      Object proceed = invocation.proceed();
//      System.out.println("method " + invocation.getMethod() + " returns " + proceed);
        return proceed;
    }

    public void setFieldValueForColEnhance(Collection col) throws Exception {
        if (col == null || col.isEmpty()) return;
        Class<?> clazz = col.iterator().next().getClass();
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Object> cache = new HashMap<>();
        for (Field needField : fields) {
            NeedSetValue sv = needField.getAnnotation(NeedSetValue.class);
            if (sv == null) continue;
            needField.setAccessible(true);
            Object bean = applicationContext.getBean(sv.beanClass());

            ArrayList<Class> params = new ArrayList();
            ArrayList<Field> paramFields = new ArrayList();

            for (int i = 0; i < sv.params().length; i++) {
                Field paramField = clazz.getDeclaredField(sv.params()[i]);
                paramField.setAccessible(true);
                params.add(paramField.getType());
                paramFields.add(paramField);
            }
            Method method = bean.getClass().getMethod(sv.method(), params.toArray(new Class[params.size()]));


            Field targetField = null;
            boolean needInnerField = !StringUtils.isEmpty(sv.targetFiled());
            String keyPrefix = sv.beanClass() + "-" + sv.method() + "-" + sv.targetFiled() + "-";
            for (Object obj : col) {

                List<Object> paramValues = new ArrayList<>();
                String allParamValue = "";
                for(Field paramField : paramFields){
                    Object paramValue = paramField.get(obj);
                    paramValues.add(paramValue);
                    allParamValue += paramValue + "-";
                }

                if (paramValues.isEmpty()) continue;
                Object value = null;
                String key = keyPrefix + allParamValue;
                if (cache.containsKey(key)) {
                    value = cache.get(key);
                } else {
                    value = method.invoke(bean, paramValues.toArray());
                    if (needInnerField) {
                        if (value != null) {
                            if (targetField == null) {
                                targetField = value.getClass().getDeclaredField(sv.targetFiled());
                                targetField.setAccessible(true);
                            }
                            value = targetField.get(value);
                        }
                    }
                    cache.put(key, value);
                }
                needField.set(obj, value);
            }
        }
    }
}