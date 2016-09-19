package org.whipper.gui;

/**
 * General exception of Whipper GUI.
 *
 * @author Juraj Duráni
 */
public class WhipperGuiException extends Exception{

    private static final long serialVersionUID = -2099218119511825787L;

    /**
     * Create new exception.
     *
     * @param message message
     * @param cause cause
     */
    public WhipperGuiException(String message, Throwable cause){
        super(message, cause);
    }

    /**
     * Creates new exception.
     *
     * @param message message
     */
    public WhipperGuiException(String message){
        super(message);
    }
}
