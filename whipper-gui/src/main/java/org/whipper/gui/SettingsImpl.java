package org.whipper.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link Settings} interface.
 *
 * @author Juraj Duráni
 */
class SettingsImpl implements Settings{
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final List<SettingValueChangedListener> listeners = new LinkedList<>();
    private final Properties settings = new Properties();
    private final File settingsFile;

    /**
     * Creates new instance. Default properties are load from user's
     * home directory in file "whipper_gui_settings.properties".
     * A new system shutdown hook is registered, which stores all
     * settings mentioned above.
     */
    SettingsImpl(){
        settingsFile = new File(System.getProperty("user.home"), "whipper_gui_settings.properties");
        try{
            if(!settingsFile.exists()){
                settingsFile.createNewFile();
            } else {
                try(FileInputStream fis = new FileInputStream(settingsFile)){
                    settings.load(fis);
                }
            }
        } catch (IOException ex){
            LOG.warn("Cannot prepare settings file. Saved settings will not be available.", ex);
        }
        // store on JVM termination
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
            @Override
            public void run(){
                try(FileOutputStream fos = new FileOutputStream(settingsFile)){
                    settings.store(fos, "Automatically stored by whipper (ts - " + new Date(System.currentTimeMillis()) + ")");
                } catch (IOException ex){
                    LOG.warn("Cannot store settings file.", ex);
                }
            }
        }));
    }

    @Override
    public String getSettingValue(Setting key){
        return getSettingValue(key.getKey());
    }

    @Override
    public String getSettingValue(String key){
        if(key == null || key.isEmpty()){
            return null;
        }
        return settings.getProperty(key);
    }

    @Override
    public void setSettingValue(Setting key, String value){
        setSettingValue(key.getKey(), value);
    }

    @Override
    public void setSettingValue(String key, String value){
        if(key != null && !key.isEmpty()){
            String oldV = (String)settings.setProperty(key, value);
            if(value == null){
                settings.remove(key);
            }
            if((oldV == null && value != null)
                || (oldV != null && !oldV.equals(value))){
                runListeners(oldV, value, key);
            }
        }
    }

    @Override
    public void addSettingValueChangedListener(SettingValueChangedListener listener){
        if(listener != null){
            listeners.add(listener);
        }
    }

    @Override
    public void removeSettingValueChangedListener(SettingValueChangedListener listener){
        if(listener != null){
            listeners.remove(listener);
        }
    }

    /**
     * Runs all registered listeners.
     *
     * @param oldV old setting value
     * @param newV new setting value
     * @param key setting key
     */
    private void runListeners(String oldV, String newV, String key){
        SettingValueChangedEvent event = new SettingValueChangedEventImpl(oldV, newV, key);
        for(SettingValueChangedListener l : listeners){
            l.valueChanged(event);
        }
    }
}
