package com.udom.securecloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiVersionConfig {

    @Value("${api.version:v1}")
    private String currentVersion;

    @Value("${api.deprecated-versions:}")
    private String deprecatedVersions;

    @Value("${api.enable-version-header:true}")
    private boolean enableVersionHeader;

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String[] getDeprecatedVersions() {
        if (deprecatedVersions == null || deprecatedVersions.isEmpty()) {
            return new String[0];
        }
        return deprecatedVersions.split(",");
    }

    public boolean isVersionDeprecated(String version) {
        for (String deprecated : getDeprecatedVersions()) {
            if (deprecated.trim().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVersionHeaderEnabled() {
        return enableVersionHeader;
    }
}
