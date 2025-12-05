package com.deerbank.Security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api") // Make sure this prefix matches your application.properties key
public class ApiKeyProperties {

    private String key; // This will map to 'api.key' property

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
