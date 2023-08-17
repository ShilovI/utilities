package com.shilovi.rest.config.template;

import com.shilovi.rest.config.CommonRestTemplateConfig;
import com.shilovi.rest.config.RestProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.shilovi.rest.template.RestOperation;

@Configuration
public class RestTemplateConfig {

    @Bean
    @ConfigurationProperties(prefix = "application.resource")
    public RestProperties properties() {
        return new RestProperties();
    }

    @Bean
    public RestOperation metaParserClient(RestProperties properties) throws Exception {
        return new CommonRestTemplateConfig(properties).restOperation();
    }

}