package dev.kwlew.exceptions;

/**
 * Thrown by the {@link dev.kwlew.kernel.Registry} when a constructor needs an interface or
 * abstract type that was never registered or bound to an implementation.
 */
public class UnresolvedDependencyException extends RuntimeException {

    public UnresolvedDependencyException(Class<?> type) {
        super("No registered instance or binding for " + type.getName()
                + "; register or bind it before anything depends on it.");
    }
}
