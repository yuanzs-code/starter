package com.yoso.datasource.config;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.yoso.datasource.DataSourceRouting;
import com.yoso.datasource.apo.DataSourceChangeAop;
import com.yoso.datasource.apo.MultiTransactionManagerAop;
import com.yoso.datasource.config.druid.DruidConfig;
import com.yoso.datasource.config.druid.DruidSlf4jConfig;
import com.yoso.datasource.config.druid.DruidWallConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
//@ConditionalOnClass(DruidDataSource.class)
//@ConditionalOnMissingBean(DruidDataSource.class)
//@AutoConfigureOrder(1)
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
//@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MultiDataSourceAutoConfig {

    @Autowired
    DynamicDataSourceProperties multiDataSourceProperties;

    public MultiDataSourceAutoConfig() {
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRouting multiDataSource() {
        //???????????????????????????
        DataSourceRouting dataSourceRouting = new DataSourceRouting();
        //?????????????????????????????????
        LinkedHashMap<String, DataSourceProperty> dataSourcePropertiesMap = multiDataSourceProperties.getDatasource();
        for (String dataSourceName : dataSourcePropertiesMap.keySet()) {
            //????????????????????????dataSource
            DataSource dataSource = dataSource(dataSourcePropertiesMap.get(dataSourceName));
            //??????
            dataSourceRouting.addDataSouce(dataSourceName, dataSource);
            //?????????????????????????????????
            //setExProperties(dataSource, dataSourceProperties);
            System.out.println("-");
        }
        //?????????????????????AbstractRoutingDataSource
        dataSourceRouting.buildDataSouce();
        return dataSourceRouting;
    }


    @Bean
    public DataSourceChangeAop changeAop() {
        DataSourceChangeAop dataSourceChangeAop = new DataSourceChangeAop();
        return dataSourceChangeAop;
    }

    @Bean
    public MultiTransactionManagerAop multiTransactionManagerAop() {
        MultiTransactionManagerAop multiTransactionManagerAop = new MultiTransactionManagerAop();
        return multiTransactionManagerAop;
    }

    private DataSource dataSource(DataSourceProperty dataSourceProperty) {

//        DataSourceBuilder factory = DataSourceBuilder
//                .create(properties.getClassLoader())
//                .driverClassName(properties.getDriverClassName())
//                .url(properties.getUrl()).username(properties.getUsername())
//                .password(properties.getPassword());
//        if (properties.getType() != null) {
//            factory.type(properties.getType());
//        }
//        return factory.build();
//        DruidDataSource druidDataSource = DruidDataSourceBuilder.create().build();
//        try {
//            BeanUtils.copyProperties(druidDataSource,properties);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        DruidConfig config = dataSourceProperty.getDruid();
        Properties properties = config.toProperties(multiDataSourceProperties.getDruid());

        List<Filter> proxyFilters = this.initFilters(multiDataSourceProperties.getDruid(), dataSourceProperty, properties);
        dataSource.setProxyFilters(proxyFilters);

        dataSource.configFromPropety(properties);
        //????????????????????????
        dataSource.setConnectProperties(config.getConnectionProperties());
        //??????druid??????properties?????????????????????
        this.setParam(dataSource, config, multiDataSourceProperties.getDruid());
        if (!dataSourceProperty.getLazy()) {
            try {
                dataSource.init();
            } catch (SQLException e) {
                throw new DruidRuntimeException("druid create error", e);
            }
        }
        return dataSource;
    }

    /**
     * ????????????????????????????????????????????? DataSource ?????? druid dbcp ????????????????????????????????????????????????
     */
//    private void setExProperties(DataSource dataSource, DataSourceExtentProperties dataSourceProperties) {
//
//        Map<String, Object> pool = dataSourceProperties.getPool();
//        if (pool == null) {
//            return;
//        }
//        for (String key : pool.keySet()) {
//            Object value = pool.get(key);
//            Class<? extends DataSource> dataSourceClass = dataSource.getClass();
//            try {
//                //???key?????????
//                String newKey = keyConver(key);
//                Field field = getField(dataSourceClass, newKey);
//                if (field == null) {
//                    System.err.println(String.format("pool.%s = %s ?????????????????? ???????????????????????????", key, value));
//                    continue;
//                }
//                field.setAccessible(true);
//                field.set(dataSource, value);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * ??????????????????,???????????????
     *
     * @return
     */
    private Field getField(Class<? extends DataSource> dataSourceClass, String key) {
        try {
            Field field = dataSourceClass.getDeclaredField(key);
            return field;
        } catch (NoSuchFieldException e) {

        }
        Class<?> superclass = dataSourceClass.getSuperclass();
        try {
            return superclass.getDeclaredField(key);
        } catch (NoSuchFieldException e) {

        }
        return null;
    }


    private String keyConver(String data) {
        final StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile("-(\\w)");
        Matcher m = p.matcher(data);
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }


    private List<Filter> initFilters(DruidConfig gConfig, DataSourceProperty dataSourceProperty, Properties properties) {
        List<Filter> proxyFilters = new ArrayList<>(2);
        String filters = properties.getProperty("druid.filters");
        if (!StringUtils.isEmpty(filters)) {
            if (filters.contains("stat")) {
                StatFilter statFilter = new StatFilter();
                statFilter.configFromProperties(properties);
                proxyFilters.add(statFilter);
            }
            if (filters.contains("wall")) {
                WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(dataSourceProperty.getDruid().getWall(), gConfig.getWall());
                WallFilter wallFilter = new WallFilter();
                wallFilter.setConfig(wallConfig);
                proxyFilters.add(wallFilter);
            }
            if (filters.contains("slf4j")) {
                Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
                // ??????properties??????????????????LogFilter????????????configFromProperties??????????????????????????????set??????
                DruidSlf4jConfig slf4jConfig = gConfig.getSlf4j();
                slf4jLogFilter.setStatementLogEnabled(slf4jConfig.getEnable());
                slf4jLogFilter.setStatementExecutableSqlLogEnable(slf4jConfig.getStatementExecutableSqlLogEnable());
                proxyFilters.add(slf4jLogFilter);
            }
        }
        return proxyFilters;
    }

    private void setParam(DruidDataSource dataSource, DruidConfig config, DruidConfig gConfig) {
        String defaultCatalog = config.getDefaultCatalog() == null ? gConfig.getDefaultCatalog() : config.getDefaultCatalog();
        if (defaultCatalog != null) {
            dataSource.setDefaultCatalog(defaultCatalog);
        }
        Boolean defaultAutoCommit = config.getDefaultAutoCommit() == null ? gConfig.getDefaultAutoCommit() : config.getDefaultAutoCommit();
        if (defaultAutoCommit != null && !defaultAutoCommit) {
            dataSource.setDefaultAutoCommit(false);
        }
        Boolean defaultReadOnly = config.getDefaultReadOnly() == null ? gConfig.getDefaultReadOnly() : config.getDefaultReadOnly();
        if (defaultReadOnly != null) {
            dataSource.setDefaultReadOnly(defaultReadOnly);
        }
        Integer defaultTransactionIsolation = config.getDefaultTransactionIsolation() == null ? gConfig.getDefaultTransactionIsolation() : config.getDefaultTransactionIsolation();
        if (defaultTransactionIsolation != null) {
            dataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
        }

        Boolean testOnReturn = config.getTestOnReturn() == null ? gConfig.getTestOnReturn() : config.getTestOnReturn();
        if (testOnReturn != null && testOnReturn) {
            dataSource.setTestOnReturn(true);
        }
        Integer validationQueryTimeout =
                config.getValidationQueryTimeout() == null ? gConfig.getValidationQueryTimeout() : config.getValidationQueryTimeout();
        if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
            dataSource.setValidationQueryTimeout(validationQueryTimeout);
        }

        Boolean sharePreparedStatements =
                config.getSharePreparedStatements() == null ? gConfig.getSharePreparedStatements() : config.getSharePreparedStatements();
        if (sharePreparedStatements != null && sharePreparedStatements) {
            dataSource.setSharePreparedStatements(true);
        }
        Integer connectionErrorRetryAttempts =
                config.getConnectionErrorRetryAttempts() == null ? gConfig.getConnectionErrorRetryAttempts()
                        : config.getConnectionErrorRetryAttempts();
        if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
            dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        }
        Boolean breakAfterAcquireFailure =
                config.getBreakAfterAcquireFailure() == null ? gConfig.getBreakAfterAcquireFailure() : config.getBreakAfterAcquireFailure();
        if (breakAfterAcquireFailure != null && breakAfterAcquireFailure) {
            dataSource.setBreakAfterAcquireFailure(true);
        }

        Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ? gConfig.getRemoveAbandonedTimeoutMillis()
                : config.getRemoveAbandonedTimeoutMillis();
        if (timeout != null) {
            dataSource.setRemoveAbandonedTimeoutMillis(timeout);
        }

        Boolean abandoned = config.getRemoveAbandoned() == null ? gConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
        if (abandoned != null) {
            dataSource.setRemoveAbandoned(abandoned);
        }

        Boolean logAbandoned = config.getLogAbandoned() == null ? gConfig.getLogAbandoned() : config.getLogAbandoned();
        if (logAbandoned != null) {
            dataSource.setLogAbandoned(logAbandoned);
        }

        Integer queryTimeOut = config.getQueryTimeout() == null ? gConfig.getQueryTimeout() : config.getQueryTimeout();
        if (queryTimeOut != null) {
            dataSource.setQueryTimeout(queryTimeOut);
        }

        Integer transactionQueryTimeout =
                config.getTransactionQueryTimeout() == null ? gConfig.getTransactionQueryTimeout() : config.getTransactionQueryTimeout();
        if (transactionQueryTimeout != null) {
            dataSource.setTransactionQueryTimeout(transactionQueryTimeout);
        }
    }

}
