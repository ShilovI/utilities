package com.shilovi.rest.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import com.shilovi.rest.template.RestOperation;
import com.shilovi.rest.template.RestTemplateBaseImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public record CommonRestTemplateConfig(RestProperties properties) {

    public RestOperation restOperation() throws Exception {
        return new RestTemplateBaseImpl(httpRequestFactory());
    }

    private LayeredConnectionSocketFactory socketFactory() throws Exception {
        var sslContext = SSLContexts.custom();
        sslContext.loadKeyMaterial(keyStore(), properties.getSsl().getKeystorePassword().toCharArray());
        sslContext.loadKeyMaterial(trustStore(), properties.getSsl().getTrustStorePassword().toCharArray());
        return new SSLConnectionSocketFactory(sslContext.build(), NoopHostnameVerifier.INSTANCE);
    }

    private PoolingHttpClientConnectionManager connection() throws Exception {
        PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create();
        if (this.properties.getSsl().isEnabled()) {
            builder.setSSLSocketFactory(socketFactory());
        }
        builder.setDefaultSocketConfig(socketConfig());
        builder.setMaxConnTotal(this.properties.getMaxThread());
        builder.setMaxConnPerRoute(this.properties.getMaxRoute());
        return builder.build();
    }

    private CloseableHttpClient httpClient() throws Exception {
        return HttpClients.custom()
                .setConnectionManager(connection())
                .build();
    }

    private HttpComponentsClientHttpRequestFactory httpRequestFactory() throws Exception {
        var httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
        httpRequestFactory.setConnectTimeout(this.properties.getConnectionTimeout());
        return httpRequestFactory;
    }

    private SocketConfig socketConfig() {
        return SocketConfig.custom()
                .setSoTimeout(Timeout.of(this.properties.getReadTimeout(), TimeUnit.MILLISECONDS))
                .build();

    }

    private KeyStore trustStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        var trustStore = KeyStore.getInstance(properties.getSsl().getTrustStoreType());
        try (var truststore = new FileInputStream(properties.getSsl().getTruststoreLocation())) {
            trustStore.load(truststore, properties.getSsl().getTrustStorePassword().toCharArray());
        }
        return trustStore;
    }


    private KeyStore keyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        var keyStore = KeyStore.getInstance(properties.getSsl().getKeystoreType());
        try (var keystore = new FileInputStream(properties.getSsl().getKeystoreLocation())) {
            keyStore.load(keystore, properties.getSsl().getKeystorePassword().toCharArray());
        }
        return keyStore;
    }

}
