package com.shilovi.rest.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class RestProperties {
    @Min(1L)
    @Max(200L)
    private Integer maxThread = 100;
    @Min(1L)
    @Max(20L)
    private Integer maxRoute = 10;
    @Min(1000L)
    @Max(90000L)
    private Integer readTimeout = 15000;
    @Min(1000L)
    @Max(90000L)
    private Integer connectionTimeout = 10000;
    private boolean enabled;

    private SslConfig ssl = new SslConfig();

    @Data
    @Validated
    public static class SslConfig {
        private boolean enabled = false;
        private boolean sniCheck = false;
        private boolean selfSigned = false;
        private String keystoreLocation = "./ssl/keystore.jks";
        private String truststoreLocation = "./ssl/truststore.jks";
        private String keystorePassword = "123456";
        private String trustStorePassword = "123456";
        private String keystoreType = "PKCS12";
        private String trustStoreType = "PKCS12";
        private int handshakeTimeout = 2;
    }

}
