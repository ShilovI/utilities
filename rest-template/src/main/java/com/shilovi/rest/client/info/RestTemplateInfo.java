package com.shilovi.rest.client.info;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class RestTemplateInfo<T, R> {
    protected final R properties;
    protected final Function<R, UriComponentsBuilder> uriBuilderSupplier;

    protected RestTemplateInfo(R properties, Function<R, UriComponentsBuilder> uriBuilderSupplier) {
        this.properties = properties;
        this.uriBuilderSupplier = uriBuilderSupplier;
    }

    public abstract String getUrl();

    public abstract ParameterizedTypeReference<T> getResponseType();

    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    public T extractResult(HttpEntity<T> httpEntity) {
        return httpEntity.getBody();
    }

    public HttpEntity<?> getRequestEntity() {
        return new HttpEntity<>(this.createHeaders());
    }

    protected UriComponentsBuilder createBaseUriBuilder() {
        if (Objects.isNull(this.uriBuilderSupplier)) {
            throw new IllegalStateException("Supply uriBuilder supplier or override this method.");
        } else {
            return this.uriBuilderSupplier.apply(this.properties);
        }
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}