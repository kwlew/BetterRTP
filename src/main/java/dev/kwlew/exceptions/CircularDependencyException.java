package dev.kwlew.exceptions;

/**
 * Thrown by the {@link dev.kwlew.kernel.Registry} when resolving a type would require
 * constructing that same type again further down its own dependency chain.
 */
public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(String cycle) {
        super("Circular dependency detected: " + cycle);
    }
}
