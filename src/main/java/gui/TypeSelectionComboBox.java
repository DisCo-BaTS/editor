package gui;

import handlers.ElementHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

public class TypeSelectionComboBox {

    private final ArrayList<String> allListItems;
    private final KeyAdapter ka;
    private final Vector<String> v;
    private boolean hide_flag = false;

    private final JComboBox typePropertyTextField;

    public JComboBox getTypePropertyTextField() {
        return typePropertyTextField;
    }

    public static final ArrayList<String> defaultDataTypes = new ArrayList<>(Arrays.asList("Byte", "Short", "Integer",
            "Long", "Float", "Double", "Boolean", "Character", "String", "ArrayList<>", "HashMap<>"));
    public static final ArrayList<String> defaultDataTypes2 = new ArrayList<>(Arrays.asList("byte", "Byte", "short", "Short", "int", "Integer",
            "long", "Long", "float", "Float", "double", "Double",
            "boolean", "Boolean", "char", "Character", "String", "ArrayList<>", "HashMap<>"));

    public KeyAdapter getKa() {
        return ka;
    }

    public TypeSelectionComboBox(JComboBox typePropertyTextField, ArrayList<String> allListItems, Vector<String> v) {
        this.v = v;
        this.typePropertyTextField = typePropertyTextField;
        this.allListItems = allListItems;

        ka = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                EventQueue.invokeLater(() -> {
                    String text = ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText();
                    if (text.length() == 0) {
                        typePropertyTextField.hidePopup();
                        setModel(new DefaultComboBoxModel(v), "");
                    } else {
                        DefaultComboBoxModel m = getSuggestedModel(v, text);
                        if (m.getSize() == 0 || hide_flag) {
                            typePropertyTextField.hidePopup();
                            hide_flag = false;
                        } else {
                            setModel(m, text);
                            typePropertyTextField.showPopup();
                        }
                    }
                });
            }

            public void keyPressed(KeyEvent e) {
                String text = ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText();
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER) {
                    hide_flag = true;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    hide_flag = true;
                } else if (code == KeyEvent.VK_RIGHT) {
                    for (int i = 0; i < v.size(); i++) {
                        String str = v.elementAt(i);
                        if (str.startsWith(text)) {
                            typePropertyTextField.setSelectedIndex(-1);
                            ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).setText(str);
                            return;
                        }
                    }
                }
            }
        };
    }

    public void setModel(DefaultComboBoxModel mdl, String str) {
        typePropertyTextField.setModel(mdl);
        typePropertyTextField.setSelectedIndex(-1);
        ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).setText(str);
    }

    private static DefaultComboBoxModel getSuggestedModel(java.util.List<String> list, String text) {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (String s : list) {
            if (s.toLowerCase(Locale.ROOT).startsWith(text.toLowerCase(Locale.ROOT))) m.addElement(s);
        }
        return m;
    }

    private static ArrayList<String> getSuggestedList(java.util.List<String> list, String text) {
        ArrayList<String> m = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase(Locale.ROOT).startsWith(text.toLowerCase(Locale.ROOT))) m.add(s);
        }
        return m;
    }

    public void updateModel() {
        ArrayList<String> allListItems = new ArrayList<>();
        allListItems.addAll(ElementHandler.getInstance().getAllModelElementNames());
        allListItems.addAll(TypeSelectionComboBox.defaultDataTypes);

        Vector<String> v = new Vector<>();
        allListItems.forEach(v::addElement);

        setModel(new DefaultComboBoxModel(v), "");
    }
}
