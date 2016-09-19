package org.whipper.gui;

/**
 * Whipper GUI interface.
 *
 * @author Juraj Duráni
 */
public interface WhipperGuiPlugin{

    /**
     * Initializes this plugin. If this method
     * throws exception, plugin will not be added to
     * GUI application.
     *
     * @param context Whipper GUI context
     * @throws WhipperGuiException if plugin cannot be initialized
     */
    void init(WhipperGuiContext context) throws WhipperGuiException;

    /**
     * Destroys this plugin. Method will be called after
     * each registered {@link WhipperGuiShutdownHook} has
     * been successfully processed.
     *
     * @param context
     */
    void destroy(WhipperGuiContext context);
}
