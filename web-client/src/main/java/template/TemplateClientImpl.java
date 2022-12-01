package template;

import com.fasterxml.jackson.databind.JsonNode;
import exception.IncorrectResourceRequestException;
import exception.ResourceNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import props.HttpConnectionProperties;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
public class TemplateClientImpl {

    private final WebClient storageSystemWebClient;
    private final HttpConnectionProperties properties;

    @Autowired
    public TemplateClientImpl(WebClient storageSystemClient, HttpConnectionProperties properties) {
        this.storageSystemWebClient = storageSystemClient;
        this.properties = properties;
    }

    public boolean sendToTransmitter(Objects request) {
        logger.info("Transmitter send started {}", request);
        return Boolean.TRUE.equals(storageSystemWebClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError,
                        response -> Mono.error(new ResourceNotAvailableException(
                                String.format("Resource server error, code : %s", response.rawStatusCode()))))
                .onStatus(HttpStatus::is4xxClientError,
                        response -> response.bodyToMono(String.class).flatMap(errorBody -> Mono.error(
                                new IncorrectResourceRequestException(
                                        String.format("Resource request error, response : %s", errorBody)))))
                .bodyToMono(JsonNode.class)
                .doOnError(e -> logger.error("Unsuccessful resource request due to {}", e.getMessage(), e))
                .doOnSuccess(response -> logger.info("Successfully resource call"))
                .retryWhen(Retry.backoff(properties.getRetry().getMaxAttempts(), Duration.ofMillis(properties.getRetry().getMinBackoff()))
                        .filter(throwable -> throwable instanceof ResourceNotAvailableException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new ResourceNotAvailableException(
                                    "Transmitter service failed to process after max retries");
                        }))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error(e.getMessage(), e);
                    return Mono.empty();
                })
                .map(Objects::nonNull)
                .block());
    }

}
