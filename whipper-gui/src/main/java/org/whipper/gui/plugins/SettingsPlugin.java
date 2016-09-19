package org.whipper.gui.plugins;

import javax.swing.JPanel;

import org.whipper.gui.Settings;
import org.whipper.gui.WhipperGuiContext;
import org.whipper.gui.WhipperGuiException;
import org.whipper.gui.WhipperGuiPlugin;

public class SettingsPlugin implements WhipperGuiPlugin{

    private Settings settings;

    @Override
    public void init(WhipperGuiContext context) throws WhipperGuiException{
        settings = context.getSettings();
        JPanel panel = new JPanel();
        context.addTab(panel, "Settings");
        // TODO
    }

    @Override
    public void destroy(WhipperGuiContext context){}
}
