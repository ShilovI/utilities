package com.shilovi.webclient.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
public class HttpConnectionProperties {
    private String baseUrl;

    private boolean debug;

    private int connectionTimeout = 2;

    private int readTimeout = 2;

    private int writeTimeout = 2;

    private boolean enableMetrics = false;

    private SslConfig ssl = new SslConfig();

    @Getter
    @Setter
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

    private RetryConfig retry = new RetryConfig();

    @Getter
    @Setter
    @Validated
    public static class RetryConfig {
        long maxAttempts = 3;
        long minBackoff = 300;
    }
}
