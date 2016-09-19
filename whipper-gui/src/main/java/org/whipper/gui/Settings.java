package org.whipper.gui;

/**
 * Global properties settings. This interface allows read and write
 * properties across all plugins. It provides capability for adding
 * listener to observe change in properties value.
 *
 * @author Juraj Duráni
 */
public interface Settings{

    /**
     * Default settings.
     */
    public static enum Setting{
        PROPERTIES_FILE_PATH("whipper.properties"),
        OUTPUT_DIRECTORY("whipper.output.directory"),
        SCENARIO("whipper.scenario"),
        INCLUDE_SCENARIOS("whipper.scenario.include"),
        EXCLUDE_SCENARIOS("whipper.scenario.exclude"),
        JDBC_USER_NAME("jdbc.user.name"),
        JDBC_PASSWORD("jdbc.password"),
        JDBC_HOST("jdbc.host"),
        JDBC_PORT("jdbc.port");

        private final String key;

        private Setting(String key){
            this.key = key;
        }

        /**
         * Returns true, if key of this setting equals to
         * provided parameter.
         *
         * @param other other key
         * @return {@code true} if parameter equals to key of
         *      this setting, {@code false} otherwise
         */
        boolean equalsKey(String other){
            return this.key.equals(other);
        }

        /**
         * Returns key of this setting.
         *
         * @return key
         */
        String getKey(){
            return key;
        }
    };

    /**
     * Returns value of setting {@code key}.
     *
     * @param key setting
     * @return current value or {@code null} if not defined
     */
    String getSettingValue(Setting key);

    /**
     * Returns value of setting of specified {@code key}.
     * If key is {@code null} or empty, method returns
     * {@code null}.
     *
     * @param key key of the setting
     * @return current value or {@code null} if not defined
     */
    String getSettingValue(String key);

    /**
     * Sets setting {@code key} to value {@code value}.
     * If {@code value} is {@code null}, setting is
     * removed from this settings. After set/unset, all
     * registered listeners are triggered. Listeners are
     * triggered only if value changed.
     *
     * @param key setting
     * @param value value to be set
     * @see #addSettingValueChangedListener(SettingValueChangedListener)
     */
    void setSettingValue(Setting key, String value);

    /**
     * Sets setting of specified {@code key} to value
     * {@code value}. If {@code value} is {@code null},
     * setting is removed from this settings. After
     * set/unset, all registered listeners are triggered.
     * Listeners are triggered only if value changed. If
     * key is {@code null} or empty, no operation is
     * performed.
     *
     * @param key key of the setting
     * @param value value to be set
     * @see #addSettingValueChangedListener(SettingValueChangedListener)
     */
    void setSettingValue(String key, String value);

    /**
     * Register new {@link SettingValueChangedListener}. If
     * {@code listener} is {@code null}, no operation is performed.
     *
     * @param listener
     * @see Settings#removeSettingValueChangedListener(SettingValueChangedListener)
     * @see #setSettingValue(Setting, String)
     * @see #setSettingValue(String, String)
     */
    void addSettingValueChangedListener(SettingValueChangedListener listener);

    /**
     * Unregister previously registered {@link SettingValueChangedListener}.
     * If {@code listener} is {@code null}, no operation is performed.
     *
     * @param listener
     * @see #addSettingValueChangedListener(SettingValueChangedListener)
     */
    void removeSettingValueChangedListener(SettingValueChangedListener listener);
}
