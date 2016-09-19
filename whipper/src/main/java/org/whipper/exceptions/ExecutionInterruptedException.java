package org.whipper.exceptions;

/**
 * Exception indicates that execution thread has been interrupted.
 *
 * @author Juraj Duráni
 */
public class ExecutionInterruptedException extends WhipperException {

    private static final long serialVersionUID = 3188956648394456684L;

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public ExecutionInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     */
    public ExecutionInterruptedException(String message) {
        super(message);
    }
}
