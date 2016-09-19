package org.whipper.gui;

/**
 * Listener interface.
 *
 * @author Juraj Duráni
 */
public interface SettingValueChangedListener{

    /**
     * This method will be invoked each time some
     * setting changed.
     *
     * @param event value changed event
     */
    void valueChanged(SettingValueChangedEvent event);
}
