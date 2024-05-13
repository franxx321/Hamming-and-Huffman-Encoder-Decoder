package Utils;

import GUI.Frame;
import GUI.HammingSecundario;
import GUI.HuffmanSecundario;
import GUI.Panel;

import javax.swing.*;
import java.util.HashMap;

public class SMenuGUIHandler extends GUIHandler{

    private static SMenuGUIHandler instance;

    public static String huffmanSecundario = "Huffman Secundario",hammingSecundario = "Hamming secundario";

    public static SMenuGUIHandler getInstance(){
        if(instance==null){
            instance= new SMenuGUIHandler();
        }
        return instance;
    }

    private SMenuGUIHandler(){
        panels=new HashMap<>();
        panels.put(SMenuGUIHandler.huffmanSecundario,new HuffmanSecundario());
        panels.put(SMenuGUIHandler.hammingSecundario, new HammingSecundario());
    }

    HashMap<String, Panel> panels;
    @Override
    public void changePanel(String panelKey) {
        JPanel menuSecundario = Frame.getInstance().getMenuSecundario();
        menuSecundario.removeAll();
        menuSecundario.add(panels.get(panelKey));
        Frame.getInstance().repaint();
        Frame.getInstance().revalidate();

    }
}
