package com.shilovi.rest.template;

import com.shilovi.rest.client.info.RestTemplateInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RestTemplateBaseImpl extends RestTemplate implements RestOperation {

    public RestTemplateBaseImpl(HttpComponentsClientHttpRequestFactory requestFactory) {
        this.setMessageConverters(Arrays.asList(new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(StandardCharsets.UTF_8),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new AllEncompassingFormHttpMessageConverter(),
                this.getMappingJackson2HttpMessageConverter()));
        this.setRequestFactory(requestFactory);
    }

    public <T, R> ResponseEntity<T> exchange(RestTemplateInfo<T, R> info) {
        return super.exchange(info.getUrl(),
                info.getHttpMethod(),
                info.getRequestEntity(),
                info.getResponseType()
        );
    }

    private MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_NULL).build();
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        messageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
        return messageConverter;
    }
}