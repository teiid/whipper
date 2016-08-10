package org.whipper.exceptions;

/**
 * Exception indicates that DB is not available.
 *
 * @author Juraj Duráni
 */
public class DbNotAvailableException extends WhipperException {

    private static final long serialVersionUID = 226241066580400083L;

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public DbNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     */
    public DbNotAvailableException(String message) {
        super(message);
    }
}
