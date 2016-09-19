package org.whipper.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

/**
 * Basic implementation of {@link WhipperGuiContext}.
 *
 * @author Juraj Duráni
 */
class WhipperGuiContextImpl implements WhipperGuiContext{

    private final List<WhipperGuiShutdownHook> shutDownHooks = new LinkedList<>();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final List<WhipperGuiShutdownHook> shutDownHooksSinceCheckpoint = new LinkedList<>();
    private final List<JComponent> componentsSinceChechpoint = new LinkedList<>();
    private Settings settings;

    @Override
    public void addShutdownHook(WhipperGuiShutdownHook hook){
        if(hook != null){
            shutDownHooks.add(hook);
            shutDownHooksSinceCheckpoint.add(hook);
        }
    }

    @Override
    public int addTab(JComponent component, String title){
        if(component == null){
            return -1;
        }
        tabbedPane.add(title, component);
        int idx = tabbedPane.indexOfComponent(component);
        if(idx == -1){
            throw new IllegalStateException("Component has not been inserted.");
        }
        componentsSinceChechpoint.add(component);
        return idx;
    }

    @Override
    public Settings getSettings(){
        if(settings == null){
            settings = new SettingsImpl();
        }
        return settings;
    }

    /**
     * Returns all registered shutdown hooks.
     *
     * @return registered shutdown hooks
     */
    List<WhipperGuiShutdownHook> getShutDownHooks(){
        return new ArrayList<>(shutDownHooks);
    }

    /**
     * Returns tabbed pane in this context.
     *
     * @return tabbed pane
     */
    JTabbedPane getTabbedPane(){
        return tabbedPane;
    }

    /**
     * Creates checkpoint for tabbed pane and
     * registered shutdown hook in this context.
     *
     * @see #reset()
     */
    void checkpoint(){
        shutDownHooksSinceCheckpoint.clear();
        componentsSinceChechpoint.clear();
    }

    /**
     * Resets registered shutdown hooks and tabs in
     * tabbed pane to the latest checkpoint.
     *
     * @see #checkpoint()
     */
    void reset(){
        shutDownHooks.removeAll(shutDownHooksSinceCheckpoint);
        for(JComponent c : componentsSinceChechpoint){
            tabbedPane.remove(c);
        }
        checkpoint();
    }
}
