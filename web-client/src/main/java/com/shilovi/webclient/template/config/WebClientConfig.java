package com.shilovi.webclient.template.config;

import com.shilovi.webclient.config.WebClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.shilovi.webclient.props.HttpConnectionProperties;

@Configuration
public class WebClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "application.resource")
    public HttpConnectionProperties properties() {
        return new HttpConnectionProperties();
    }

    @Bean
    public WebClient metaParserClient(HttpConnectionProperties properties) {
        return new WebClientBuilder(properties).webClient();
    }

}