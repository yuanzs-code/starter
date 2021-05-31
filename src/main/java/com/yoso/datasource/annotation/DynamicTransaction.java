package com.yoso.datasource.annotation;

import java.lang.annotation.*;
import java.sql.Connection;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DynamicTransaction {
    String[] value() default {};
    int transactionType() default Connection.TRANSACTION_READ_UNCOMMITTED;
}
