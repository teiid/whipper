package org.jboss.bqt.exceptions;

/**
 * Exception indicates that database server is not available.
 *
 * @author Juraj Duráni
 */
public class ServerNotAvailableException extends BqtException {

    private static final long serialVersionUID = 1938750049163384722L;

    /**
     * Creates a new exception.
     */
    public ServerNotAvailableException() {
        super();
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     * @param cause cause
     */
    public ServerNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception.
     *
     * @param message message
     */
    public ServerNotAvailableException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param cause cause
     */
    public ServerNotAvailableException(Throwable cause) {
        super(cause);
    }
}
