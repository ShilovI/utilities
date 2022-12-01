package template.config;

import config.CommonWebClientConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import props.HttpConnectionProperties;

@Configuration
public class WebClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "application.resource")
    public HttpConnectionProperties properties() {
        return new HttpConnectionProperties();
    }

    @Bean
    public WebClient metaParserClient(HttpConnectionProperties properties) {
        return new CommonWebClientConfiguration(properties).webClient();
    }

}