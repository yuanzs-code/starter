/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yoso.datasource.config;

import com.yoso.datasource.config.druid.DruidConfig;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.Ordered;

import java.util.LinkedHashMap;

/**
 * DynamicDataSourceProperties
 *
 * @author TaoYu Kanyuxia
 * @see DataSourceProperties
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = DynamicDataSourceProperties.PREFIX)
public class DynamicDataSourceProperties {

    public static final String PREFIX = "spring.datasource.dynamic";
    public static final String HEALTH = PREFIX + ".health";
    public static final String DEFAULT_VALID_QUERY = "SELECT 1";
    /**
     * 必须设置默认的库,默认masterlazy
     */
    private String primary = "master";
    /**
     * 是否启用严格模式,默认不启动. 严格模式下未匹配到数据源直接报错, 非严格模式下则使用默认数据源primary所设置的数据源
     */
    private Boolean strict = false;
    /**
     * 是否使用p6spy输出，默认不输出
     */
    private Boolean p6spy = false;
    /**
     * 是否使用开启seata，默认不开启
     */
    private Boolean seata = false;
    /**
     * 是否懒加载数据源
     */
    private Boolean lazy = false;

    /**
     * 是否使用 spring actuator 监控检查，默认不检查
     */
    private boolean health = false;
    /**
     * 监控检查SQL
     */
    private String healthValidQuery = DEFAULT_VALID_QUERY;
    /**
     * 每一个数据源
     */
    private LinkedHashMap<String, DataSourceProperty> datasource = new LinkedHashMap<>();
    /**
     * aop切面顺序，默认优先级最高
     */
    private Integer order = Ordered.HIGHEST_PRECEDENCE;
    /**
     * Druid全局参数配置
     */
    @NestedConfigurationProperty
    private DruidConfig druid = new DruidConfig();
    /**
     * HikariCp全局参数配置
     */
//    @NestedConfigurationProperty
//    private HikariCpConfig hikari = new HikariCpConfig();

    /**
     * aop 切面是否只允许切 public 方法
     */
    private boolean allowedPublicOnly = true;

    public static String getPREFIX() {
        return PREFIX;
    }

    public static String getHEALTH() {
        return HEALTH;
    }

    public static String getDefaultValidQuery() {
        return DEFAULT_VALID_QUERY;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public Boolean getStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Boolean getP6spy() {
        return p6spy;
    }

    public void setP6spy(Boolean p6spy) {
        this.p6spy = p6spy;
    }

    public Boolean getSeata() {
        return seata;
    }

    public void setSeata(Boolean seata) {
        this.seata = seata;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isHealth() {
        return health;
    }

    public void setHealth(boolean health) {
        this.health = health;
    }

    public String getHealthValidQuery() {
        return healthValidQuery;
    }

    public void setHealthValidQuery(String healthValidQuery) {
        this.healthValidQuery = healthValidQuery;
    }

    public LinkedHashMap<String, DataSourceProperty> getDatasource() {
        return datasource;
    }

    public void setDatasource(LinkedHashMap<String, DataSourceProperty> datasource) {
        this.datasource = datasource;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public DruidConfig getDruid() {
        return druid;
    }

    public void setDruid(DruidConfig druid) {
        this.druid = druid;
    }

    public boolean isAllowedPublicOnly() {
        return allowedPublicOnly;
    }

    public void setAllowedPublicOnly(boolean allowedPublicOnly) {
        this.allowedPublicOnly = allowedPublicOnly;
    }
}
