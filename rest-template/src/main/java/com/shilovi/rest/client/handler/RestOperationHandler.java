package com.shilovi.rest.client.handler;

public interface RestOperationHandler<T, E> {
    void onSuccess(T var1);

    void onError(E var1);
}