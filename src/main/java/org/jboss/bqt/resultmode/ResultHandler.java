package org.jboss.bqt.resultmode;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling result of the query.
 *
 * @author Juraj Duráni
 */
public class ResultHandler {

    private Throwable exception;
    private List<String> errors;

    /**
     * Returns {@code true} if there was an exception during handling of result.
     *
     * @return {@code true} in case of exception during handling of result, {@code false} otherwise
     */
    public boolean isException() {
        return exception != null;
    }

    /**
     * Return {@code true} if there are any errors during handling of result (e.g. comparison errors).
     *
     * @return {@code true} in case of error during handling of result, {@code false} otherwise
     */
    public boolean isError() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Returns {@code true} if there is an exception or any error.
     *
     * @return {@code true} in case of exception or error, false otherwise
     * @see #isError()
     * @see #isException()
     */
    public boolean isFail() {
        return isException() || isError();
    }

    /**
     * Returns exception from handling of result, if any.
     *
     * @return exception
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Returns errors from handling of result, if any.
     *
     * @return list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Sets errors from handling of result.
     *
     * @param errors errors to be set
     */
    void setErrors(List<String> errors) {
        this.errors = errors == null ? null : new ArrayList<String>(errors);
    }

    /**
     * Sets exception from handling of result.
     *
     * @param exception exception to be set
     */
    void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "Is error: " + isError() + ", is exception: " + isException();
    }
}
