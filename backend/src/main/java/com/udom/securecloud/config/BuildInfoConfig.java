package com.udom.securecloud.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class BuildInfoConfig {

    @Bean
    public BuildProperties buildProperties() {
        Properties props = new Properties();
        props.setProperty("version", "1.0.0");
        props.setProperty("name", "Secure Cloud Storage");
        props.setProperty("artifact", "secure-cloud-storage");
        props.setProperty("group", "tz.ac.udom");
        props.setProperty("time", String.valueOf(System.currentTimeMillis()));
        return new BuildProperties(props);
    }
}
