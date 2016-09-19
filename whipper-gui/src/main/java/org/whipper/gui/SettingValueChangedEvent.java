package org.whipper.gui;

import org.whipper.gui.Settings.Setting;

/**
 * Setting value changed event.
 *
 * @author Juraj Duráni
 */
public interface SettingValueChangedEvent{

    /**
     * Returns old value of the setting.
     *
     * @return old value
     */
    String oldValue();

    /**
     * Returns new value of the setting.
     *
     * @return new value
     */
    String newValue();

    /**
     * Returns key of the setting.
     *
     * @return setting key
     */
    String key();

    /**
     * Tries to convert current key into {@link Setting}.
     * If {@link Setting} with such key is not available,
     * {@code null} is returned.
     *
     * @return {@link Setting} or {@code null} if setting
     *      is not available
     */
    Setting keyAsSetting();
}
