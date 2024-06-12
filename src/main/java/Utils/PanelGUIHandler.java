package Utils;

import GUI.*;
import javax.swing.*;
import java.util.HashMap;

public class PanelGUIHandler extends GUIHandler {

    HashMap<String, Panel>panels;

    private static PanelGUIHandler instance;

    public static String PanelCompactar ="Panel Compactar",panelProteger = "Panel proteger", panelDesproteger = "Panel desproteger", PanelEstadisticas = "Panel Estadisticas", PanelDescompactar = "Panel descompactar", panelComparar = "Panel Comparar"
            ,hammingEstadisticas= "Hamming Estadisticas";

    private PanelGUIHandler(){
        panels = new HashMap<>();
        panels.put(PanelGUIHandler.PanelCompactar,new CompactarPanel());
        panels.put(PanelGUIHandler.PanelEstadisticas,new EstadisticasPanel());
        panels.put(PanelGUIHandler.panelProteger, new ProtegerPanel());
        panels.put(PanelGUIHandler.panelDesproteger,new DesprotegerPanel());
        panels.put(PanelGUIHandler.PanelDescompactar, new DescompactarPanel());
        panels.put(PanelGUIHandler.panelComparar,new CompararHammingPanel());
        panels.put(PanelGUIHandler.hammingEstadisticas, new HammingEstadisticasPanel());
       
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
        Panel newPanel = panels.get(panelKey);
        newPanel.init();
        panelPrincipal.add(newPanel);
        Frame.getInstance().repaint();
        Frame.getInstance().revalidate();

    }
}
