package Utils;

import GUI.CompactarPanel;
import GUI.Frame;
import GUI.Panel;

import javax.swing.*;
import java.util.HashMap;

public class PanelGUIHandler extends GUIHandler {

    HashMap<String, Panel>panels;

    private static PanelGUIHandler instance;

    public static String PanelCompactar ="Panel Compactar";

    private PanelGUIHandler(){
        panels = new HashMap<>();
        panels.put(PanelGUIHandler.PanelCompactar,new CompactarPanel());
    }

    public static PanelGUIHandler getInstance(){
        if(instance==null){
            instance = new PanelGUIHandler();
        }
        return instance;
    }


    @Override
    public void changePanel(String panelKey) {
        JPanel panelPrincipal= Frame.getInstance().getPanelPrincipal();
        panelPrincipal.removeAll();
        panelPrincipal.add(panels.get(panelKey));
        Frame.getInstance().repaint();
        Frame.getInstance().revalidate();

    }
}
