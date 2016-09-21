package org.whipper.gui;

/**
 * Basic implementation of {@link SettingValueChangedEvent}.
 *
 * @author Juraj Duráni
 */
public class SettingValueChangedEvent{

    private final String oldV;
    private final String newV;
    private final String key;

    /**
     * Create new event.
     *
     * @param oldV old value
     * @param newV new value
     * @param key setting key
     */
    SettingValueChangedEvent(String oldV, String newV, String key){
        if(key == null){
            throw new IllegalArgumentException("Setting key cannot be null");
        }
        this.oldV = oldV;
        this.newV = newV;
        this.key = key;
    }

    /**
     * Returns old value of the setting.
     *
     * @return old value
     */
    public String oldValue(){
        return oldV;
    }

    /**
     * Returns new value of the setting.
     *
     * @return new value
     */
    public String newValue(){
        return newV;
    }

    /**
     * Returns key of the setting.
     *
     * @return setting key
     */
    public String key(){
        return key;
    }
}
