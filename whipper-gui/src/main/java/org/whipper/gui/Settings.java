package org.whipper.gui;

/**
 * Global properties settings. This interface allows read and write
 * properties across all plugins. It provides capability for adding
 * listener to observe change in properties value.
 *
 * @author Juraj Duráni
 */
public interface Settings{

    public static final String PROPERTIES_FILE_PATH = "whipper.properties";
    public static final String OUTPUT_DIRECTORY = "whipper.output.directory";
    public static final String SCENARIO = "whipper.scenario";
    public static final String INCLUDE_SCENARIOS = "whipper.scenario.include";
    public static final String EXCLUDE_SCENARIOS = "whipper.scenario.exclude";
    public static final String JDBC_USER_NAME = "jdbc.user.name";
    public static final String JDBC_PASSWORD = "jdbc.password";
    public static final String JDBC_HOST = "jdbc.host";
    public static final String JDBC_PORT = "jdbc.port";

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
     * @see #addSettingValueChangedListener(SettingValueChangedListener, String...)
     */
    void setSettingValue(String key, String value);

    /**
     * Register new {@link SettingValueChangedListener}. If
     * {@code listener} is {@code null}, no operation is performed.
     * If {@code keys} is {@code null}, listener is added for all keys.
     *
     * @param listener listener to be added
     * @param keys setting keys
     * @see Settings#removeSettingValueChangedListener(SettingValueChangedListener, String...)
     * @see #setSettingValue(String, String)
     */
    void addSettingValueChangedListener(SettingValueChangedListener listener, String... keys);

    /**
     * Unregister previously registered {@link SettingValueChangedListener}.
     * If {@code listener} is {@code null}, no operation is performed.
     * If {code keys} is {@code null}, listener is removed from all keys.
     *
     * @param listener to be removed
     * @param keys setting keys
     * @see #addSettingValueChangedListener(SettingValueChangedListener, String...)
     */
    void removeSettingValueChangedListener(SettingValueChangedListener listener, String... keys);
}
