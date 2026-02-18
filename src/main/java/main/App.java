package main;

import com.formdev.flatlaf.FlatDarkLaf;
import gui.MTSEditorGUI;
import handlers.ElementHandler;
import saving.SaveState;
import xml.XMLData;
import xml.XMLProfileReader;

import javax.swing.*;
import java.awt.*;

public class App {

    public static void main(String[] args) {
        new App();
    }

    public App() {
        XMLProfileReader xmlProfileReader = new XMLProfileReader();
        XMLData xmlData = xmlProfileReader.readXML();

        SaveState saveState = new SaveState();

        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlData);

        FlatDarkLaf.setup();
        UIManager.put("Button.hoverBackground", new Color(84, 89, 92));
        UIManager.put("List.background", new Color(60, 63, 65));
        UIManager.put("Table.background", new Color(60, 63, 65));

        MTSEditorGUI frame = new MTSEditorGUI("MTSEditor");
        frame.setSaveState(saveState);
        frame.addModelElements(elementHandler);
        frame.setVisible(true);
    }
}
