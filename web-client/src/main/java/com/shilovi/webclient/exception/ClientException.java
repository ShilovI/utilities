package com.shilovi.webclient.exception;

import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
