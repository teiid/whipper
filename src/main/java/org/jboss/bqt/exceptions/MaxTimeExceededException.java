package org.jboss.bqt.exceptions;

/**
 * Exception indicates that maximum time for scenario has been reached.
 *
 * @author Juraj Duráni
 */
public class MaxTimeExceededException extends BqtException {

    private static final long serialVersionUID = 68460808307865098L;

    /**
     * Creates a new exception.
     */
    public MaxTimeExceededException() {
        super();
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public MaxTimeExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     */
    public MaxTimeExceededException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param cause cause
     */
    public MaxTimeExceededException(Throwable cause) {
        super(cause);
    }
}
