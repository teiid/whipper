package org.jboss.bqt.exceptions;

/**
 * Exception indicates that DB is not available.
 *
 * @author Juraj Duráni
 */
public class DbNotAvailableException extends BqtException {

    private static final long serialVersionUID = 226241066580400083L;

    /**
     * Creates a new exception.
     */
    public DbNotAvailableException() {
        super();
    }

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

    /**
     * Creates a new exception.
     *
     * @param cause cause
     */
    public DbNotAvailableException(Throwable cause) {
        super(cause);
    }
}
