package com.yoso.autovalue;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @author yuanzs
 * @date 2021年04月30日 15:05
 */
@Configuration
//该注解用来开启上面一步 @ConfigurationProperties 注解配置bean的支持，
//就是告诉springboot能够支持@ConfigurationProperties，
// 或者你也可以在@ConfigurationProperties注解标识的类上添加@Configuration和@Component，效果一样
@EnableConfigurationProperties(AutoProperties.class)
//@ConditionalOnProperty用来控制@Configuration是否生效，简单来说也就是我们可以通过在yml或properties配置文件中控制 @Configuration 注解的配置类是否生效。
@ConditionalOnProperty(prefix = "yoso", name = "open", havingValue = "true")
public class AutoConfigure {

    @Autowired
    private AutoProperties properties;

    @Bean
    public YosoAutoTest getProperties() {
        return new YosoAutoTest();
    }

    @Bean
    public DefaultPointcutAdvisor autoPointcutAdvisor() {
        TracingInterceptor interceptor = new TracingInterceptor();
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        if(StringUtils.isEmpty(properties.getExpression())){
            pointcut.setExpression("@annotation(com.yoso.autovalue.annotation.NeedSetValueField)");
        }else{
            pointcut.setExpression(properties.getExpression());
        }
        // 配置增强类advisor
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        advisor.setAdvice(interceptor);
        return advisor;
    }

}
