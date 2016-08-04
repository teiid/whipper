package org.whipper.exceptions;

/**
 * General Whipper exception
 *
 * @author Juraj Duráni
 */
public class WhipperException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception.
     */
    public WhipperException() {
        super();
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public WhipperException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception,
     *
     * @param message message
     */
    public WhipperException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param cause cause
     */
    public WhipperException(Throwable cause) {
        super(cause);
    }
}
