package com.yoso.datasource.apo;


import com.yoso.datasource.DataSourceRouting;
import com.yoso.datasource.annotation.DataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

@Aspect
public class DataSourceChangeAop {

    @Autowired
    private DataSourceRouting dataSourceRouting;

    @Pointcut("@annotation(com.yoso.datasource.annotation.DataSource)")
    public void annotationPointcut() {
    }


    @Around("annotationPointcut()")
    public Object invoke(ProceedingJoinPoint point) throws Throwable {
        Signature signature = point.getSignature();
        if (signature instanceof MethodSignature) {
            //获取当前方法的签名
            MethodSignature methodSignature = (MethodSignature) signature;
            //获取当前方法
            Method method = methodSignature.getMethod();
            //获取指定注解的引用
            DataSource annotation = method.getAnnotation(DataSource.class);
            //获取参数，使用的数据源
            String value = annotation.value();
            //切换了数据源
            dataSourceRouting.changeDataSource(value);
        } else {
            Class<?> target = point.getTarget().getClass();
            DataSource annotation = target.getAnnotation(DataSource.class);
            String value = annotation.value();
            dataSourceRouting.changeDataSource(value);
        }
        Object object = point.proceed();
        dataSourceRouting.clearThreadLocal();
        return object;
    }

}
