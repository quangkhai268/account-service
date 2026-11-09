package com.example.account.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Cấu hình gọi API getAccountList (prefix account.api). */
@ConfigurationProperties(prefix = "account.api")
public class AccountApiProperties {

    /** URL đầy đủ của API. */
    private String url = "http://localhost:8081/api/v1/accounts/getAccountList";
    /** appCode cấp cho client. */
    private String appCode = "APP01";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration connectTimeout) { this.connectTimeout = connectTimeout; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }
}
