package exception;

import lombok.Getter;

@Getter
public class IncorrectResourceRequestException extends RuntimeException {
    public IncorrectResourceRequestException(String message) {
        super(message);
    }
}
