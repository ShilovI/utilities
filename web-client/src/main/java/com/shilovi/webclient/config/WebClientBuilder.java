package com.shilovi.webclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import com.shilovi.webclient.props.HttpConnectionProperties;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public record WebClientBuilder(HttpConnectionProperties properties) {

    public WebClient build() {
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(this.http());
        WebClient.Builder builder = this.commonConfig(connector);
        this.configureDebug(builder);
        return builder.build();
    }

    private HttpClient http() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.properties.getConnectionTimeout() * 1000)
                .doOnConnected((connection) -> connection
                        .addHandlerLast(new ReadTimeoutHandler(this.properties.getReadTimeout() * 1000L,
                                TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(this.properties.getWriteTimeout() * 1000L,
                                TimeUnit.MILLISECONDS)));
        if (this.properties.getSsl().isEnabled()) {
            return httpClient.secure(this::configSsl);
        } else {
            if (this.properties.getSsl().isSelfSigned()) {
                SslContext sslContext;

                try {
                    sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                } catch (SSLException var4) {
                    logger.error("http com.shilovi.webclient.config failed", var4);
                    throw new RuntimeException(var4);
                }

                httpClient = httpClient.secure(t -> t.sslContext(sslContext));
            }

            httpClient.metrics(properties.isEnableMetrics(), Function.identity());

            return httpClient;
        }
    }

    private void configSsl(SslProvider.SslContextSpec spec) {
        HttpConnectionProperties.SslConfig ssl = this.properties.getSsl();
        SslProvider.Builder builder = spec.sslContext(this.getSslContextBuilder(ssl))
                .handshakeTimeout(Duration.ofSeconds(this.properties.getSsl().getHandshakeTimeout()));
        if (!ssl.isSniCheck()) {
            builder.handlerConfigurator((h) -> {
                SSLEngine engine = h.engine();
                SSLParameters params = new SSLParameters();
                params.setSNIMatchers(Collections.singletonList(new SNIMatcher(0) {
                    public boolean matches(SNIServerName serverName) {
                        return true;
                    }
                }));
                engine.setSSLParameters(params);
            });
        }

    }

    private SslContext getSslContextBuilder(HttpConnectionProperties.SslConfig ssl) {
        try {
            char[] ksPass = ssl.getKeystorePassword().toCharArray();
            char[] tsPass = ssl.getTrustStorePassword().toCharArray();
            KeyStore keyStore = this.readKeystore(ssl.getKeystoreLocation(), ssl.getKeystoreType(), ksPass);
            KeyStore truststore = this.readKeystore(ssl.getTruststoreLocation(), ssl.getTrustStoreType(), tsPass);
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
            keyFactory.init(keyStore, ksPass);
            trustFactory.init(truststore);
            return SslContextBuilder.forClient()
                    .sslProvider(io.netty.handler.ssl.SslProvider.JDK)
                    .keyManager(keyFactory)
                    .trustManager(trustFactory)
                    .build();
        } catch (Exception var8) {
            logger.error("Couldn't Initialize Keystores");
            throw new IllegalStateException(var8);
        }
    }

    private KeyStore readKeystore(String path, String type, char[] password) {
        try {
            Path absPath = Paths.get(path).toAbsolutePath();
            KeyStore ks = KeyStore.getInstance(type);
            ks.load(Files.newInputStream(absPath, StandardOpenOption.READ), password);
            return ks;
        } catch (Exception var6) {
            logger.error("Could not read the Keystore {}: {}", path, var6.getMessage(), var6);
            return null;
        }
    }

    private WebClient.Builder commonConfig(ReactorClientHttpConnector connector) {
        return WebClient.builder()
                .baseUrl(this.properties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(connector);
    }

    private void configureDebug(WebClient.Builder webClientBuilder) {
        if (this.properties.isDebug()) {
            webClientBuilder.filters((ef) -> {
                ef.add(ExchangeFilterFunction.ofRequestProcessor((r) -> {
                    r.headers().forEach((n, v) -> logger.info("Request Header {}: {}", n, v));
                    return Mono.just(r);
                }));
                ef.add(ExchangeFilterFunction.ofResponseProcessor((resp) -> {
                    resp.headers().asHttpHeaders().forEach((n, v) -> logger.info("Response Header {}: {}", n, v));
                    return Mono.just(resp);
                }));
            });
            logger.info("DEBUG configured");
        }

    }
}
