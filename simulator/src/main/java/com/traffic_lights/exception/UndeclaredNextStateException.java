package com.traffic_lights.exception;

/**
 * Exception thrown when a state machine or sequential process attempts to transition
 * to a next state that has not been defined or declared.
 */
public class UndeclaredNextStateException extends RuntimeException {

    /**
     * Constructs a new {@code UndeclaredNextStateException} with the specified detail message.
     *
     * @param message The detail message explaining the specific transition failure.
     */
    public UndeclaredNextStateException(String message){
        super(message);
    }

    /**
     * A static factory method to create an instance of this exception with a
     * standardized default message.
     *
     * @param state The name of the current state that failed to declare its subsequent transition.
     * @return A new {@code UndeclaredNextStateException} initialized with the formatted message.
     */
    public static UndeclaredNextStateException undeclaredNextStateException(String state){
        String defaultMessage = "%s state has not prior state.".formatted(state);
        return new UndeclaredNextStateException(defaultMessage);
    }
}
