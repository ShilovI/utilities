package template;

import client.info.RestTemplateInfo;
import org.springframework.http.ResponseEntity;

public interface RestOperation {
    <T, R> ResponseEntity<T> exchange(RestTemplateInfo<T, R> var1);
}
