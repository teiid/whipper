package org.whipper.gui;

import javax.swing.JComponent;

/**
 * Initialization context for GUI plugins.
 *
 * @author Juraj Duráni
 */
public interface WhipperGuiContext{

    /**
     * Register new shutdown hook for application.
     * Hooks are triggered before disposing main
     * {@link javax.swing.JFrame} of the application.
     * If any of hooks prevent application from disposing
     * window, no other hook is processed.
     *
     * @param hook
     */
    void addShutdownHook(WhipperGuiShutdownHook hook);

    /**
     * Register new tab for application.
     *
     * @param component component which should be
     *      added into new tab
     * @param title title of new tab
     * @return index of the tab in {@link javax.swing.JTabbedPane}
     */
    int addTab(JComponent component, String title);

    /**
     * Returns global settings for application.
     *
     * @return global settings
     */
    Settings getSettings();
}
