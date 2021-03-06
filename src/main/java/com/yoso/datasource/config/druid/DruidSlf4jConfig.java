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
package com.yoso.datasource.config.druid;

/**
 * Druid日志配置
 *
 * @author Lhx
 */
public class DruidSlf4jConfig {

    private Boolean enable = true;

    private Boolean statementExecutableSqlLogEnable = false;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getStatementExecutableSqlLogEnable() {
        return statementExecutableSqlLogEnable;
    }

    public void setStatementExecutableSqlLogEnable(Boolean statementExecutableSqlLogEnable) {
        this.statementExecutableSqlLogEnable = statementExecutableSqlLogEnable;
    }
}