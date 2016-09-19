package org.whipper.gui;

import org.whipper.gui.Settings.Setting;

/**
 * Basic implementation of {@link SettingValueChangedEvent}.
 *
 * @author Juraj Duráni
 */
class SettingValueChangedEventImpl implements SettingValueChangedEvent{

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
    SettingValueChangedEventImpl(String oldV, String newV, String key){
        if(key == null){
            throw new IllegalArgumentException("Setting key cannot be null");
        }
        this.oldV = oldV;
        this.newV = newV;
        this.key = key;
    }

    @Override
    public String oldValue(){
        return oldV;
    }

    @Override
    public String newValue(){
        return newV;
    }

    @Override
    public String key(){
        return key;
    }

    @Override
    public Setting keyAsSetting(){
        for(Setting s : Setting.values()){
            if(s.equalsKey(key)){
                return s;
            }
        }
        return null;
    }
}
