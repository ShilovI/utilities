package com.shilovi.webclient.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.shilovi.webclient.exception.ClientException;
import com.shilovi.webclient.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import com.shilovi.webclient.props.HttpConnectionProperties;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@Slf4j
public record TemplateClientImpl(WebClient storageSystemWebClient,
                                 HttpConnectionProperties.RetryConfig retryProperties) {

    public Mono<JsonNode> send(Objects request) {
        logger.info("Transmitter send started {}", request);
        return storageSystemWebClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServerException(
                                "Resource server error, code : %s".formatted(response.statusCode()))))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> Mono.error(
                                new ClientException(
                                        "Resource request error, response : %s".formatted(errorBody)))))
                .bodyToMono(JsonNode.class)
                .doOnError(e -> logger.error("Unsuccessful resource request due to {}", e.getMessage(), e))
                .doOnSuccess(response -> logger.info("Successfully resource call, response %s".formatted(response)))
                .retryWhen(Retry.backoff(
                                retryProperties.getMaxAttempts(),
                                Duration.ofMillis(retryProperties.getMinBackoff())
                        )
                        .filter(throwable -> throwable instanceof ServerException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new ServerException(
                                    "Transmitter service failed to process after max retries");
                        }))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error(e.getMessage(), e);
                    return Mono.empty();
                });
    }

}
