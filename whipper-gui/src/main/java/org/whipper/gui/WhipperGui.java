package org.whipper.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for GUI interface of Whipper.
 *
 * @author Juraj Duráni
 */
public class WhipperGui{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperGui.class);

    private final List<WhipperGuiPlugin> plugins = new ArrayList<>();
    private WhipperGuiContextImpl context;

    /**
     * Main class. No arguments needed.
     *
     * @param args ignored
     */
    public static void main(String... args){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                new WhipperGui().start();
            }
        });
    }

    /**
     * Starts GUI interface.
     */
    public void start(){
        initPlugins();
        initFrame();
    }

    /**
     * Initializes {@link JFrame} of application.
     */
    private void initFrame(){
        final JFrame f = new JFrame("Whipper");
        f.add(context.getTabbedPane());
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final List<WhipperGuiShutdownHook> hooks = context.getShutDownHooks();
        f.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                for(WhipperGuiShutdownHook h : hooks){
                    if(!h.couldShutDown()){
                        LOG.info("Cannot close Whipper window - {}", h.description());
                        return;
                    }
                }
                for(WhipperGuiPlugin p : plugins){
                    p.destroy(context);
                }
                f.dispose();
            }
        });
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /**
     * Initialized GUI plugins. Method will load all
     * plugins which are installed as services.
     *
     * @see ServiceLoader
     */
    private void initPlugins(){
        context = new WhipperGuiContextImpl();
        for(Iterator<WhipperGuiPlugin> iter = ServiceLoader.load(WhipperGuiPlugin.class).iterator(); iter.hasNext();){
            WhipperGuiPlugin plugin = iter.next();
            try{
                context.checkpoint();
                plugin.init(context);
                plugins.add(plugin);
            } catch (WhipperGuiException ex){
                LOG.error("Cannot initialize plugin {}. It will not be available.", plugin, ex);
                context.reset();
            }
        }
    }
}
