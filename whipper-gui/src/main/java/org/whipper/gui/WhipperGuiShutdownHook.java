package org.whipper.gui;

/**
 * Shutdown hook for Whipper GUI application.
 *
 * @author Juraj Duráni
 */
public interface WhipperGuiShutdownHook{

    /**
     * Processes current state of plugin and determines,
     * whether this plugin can be shutdown or not.
     * If method returns {@code false}, it will prevent
     * GUI application from shutdown.
     *
     * @return {@code true} if this plugin can be shutdown
     *      {@code false} otherwise
     */
    boolean couldShutDown();

    /**
     * Returns description of this shutdown hook. May return
     * reason why plugin cannot be shutdown (i.e. after last
     * call of method {@link #couldShutDown()}) or "static"
     * message.
     *
     * @return description.
     */
    String description();
}
