package com.yoso.autovalue;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuanzs
 * @date 2021年04月30日 14:59
 */
@ConfigurationProperties(prefix = "yoso")
public class AutoProperties {
    private String logPath;
    private String expression;
    private boolean open;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
