package org.jboss.bqt.exceptions;

/**
 * General BQT exception
 *
 * @author Juraj Duráni
 */
public class BqtException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception.
     */
    public BqtException() {
        super();
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public BqtException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception,
     *
     * @param message message
     */
    public BqtException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param cause cause
     */
    public BqtException(Throwable cause) {
        super(cause);
    }
}
