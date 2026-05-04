package com.traffic_lights.exception;

public class UndeclaredNextStateException extends RuntimeException {
    public UndeclaredNextStateException(String message){
        super(message);
    }

    public static UndeclaredNextStateException undeclaredNextStateException(String state){
        String defaultMessage = "%s state has not prior state.".formatted(state);
        return new UndeclaredNextStateException(defaultMessage);
    }
}
