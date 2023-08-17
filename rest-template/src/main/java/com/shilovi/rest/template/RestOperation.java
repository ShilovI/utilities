package com.shilovi.rest.template;

import com.shilovi.rest.client.info.RestTemplateInfo;
import org.springframework.http.ResponseEntity;

public interface RestOperation {
    <T, R> ResponseEntity<T> exchange(RestTemplateInfo<T, R> var1);
}
