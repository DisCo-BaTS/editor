package gui.popups;

import gui.Tab;
import gui.modelelements.ModelElementPanel;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * manages the dialog to change the color of the panels
 */
public class ColorFilterDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton colorButton;
    private Color selectedColor;
    private JComboBox<String> filterList;
    private JComboBox<String> typeList;
    private ArrayList<ModelElementPanel> panels;
    private ArrayList<ModelElement> elements;
    private final ArrayList<ModelElementPanel> updatedPanels = new ArrayList<>();
    private final ArrayList<ModelElement> updatedElements = new ArrayList<>();
    private boolean confirmed;
    private Color allExtensionsColors;
    private Color allInstancesColors;
    private Color allTabsColors;

    public ColorFilterDialog(ArrayList<ModelElementPanel> panels, ArrayList<String> allModelElements, ArrayList<InheretedClass> inheretedClasses,
                             ArrayList<InstanciatedClass> instanciatedClasses, ArrayList<Tab> tabs, ArrayList<ModelElement> elements, Color allExtensionsColors, Color allInstancesColors, Color allTabsColors) {
        initDialog();
        this.allExtensionsColors = allExtensionsColors;
        this.allInstancesColors = allInstancesColors;
        this.allTabsColors = allTabsColors;
        this.panels = new ArrayList<>();
        this.elements = new ArrayList<>();
        for(ModelElementPanel panel : panels) {
            this.panels.add(new ModelElementPanel(panel));
        }
        for(ModelElement element : elements) {
            this.elements.add(new ModelElement(element));
        }
        for(InheretedClass inheretedClass : inheretedClasses) {
            this.elements.add(new InheretedClass(inheretedClass));
        }

        if(!(inheretedClasses.isEmpty() && instanciatedClasses.isEmpty())) {
            DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
            listModel.addElement("");

            for (InheretedClass element : inheretedClasses) {
                listModel.addElement(element.name);
            }
            for (InstanciatedClass element : instanciatedClasses) {
                listModel.addElement(element.getName());
            }
            for(Tab tab : tabs) {
                listModel.addElement(tab.getName());
            }
            filterList.setModel(listModel);
            filterList.setRenderer(new CustomListCellRenderer());
        }
        if(!allModelElements.isEmpty()) {
            Collections.sort(allModelElements);
            DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
            listModel.addElement("");
            listModel.addElement("all Extensions");
            listModel.addElement("all Instances");
            listModel.addElement("all Tabs");
            for (String element : allModelElements) {
                listModel.addElement(element);
            }
            typeList.setModel(listModel);
            typeList.setRenderer(new CustomListCellRenderer());
        }

        colorButton.addActionListener(e -> {
            Color color = JColorChooser.showDialog(this, "Choose Color", selectedColor);
            if (color != null) {
                selectedColor = color;
                colorButton.setBackground(color);
                colorButton.setOpaque(true);
                colorButton.setBorderPainted(false);

                String selectedItem = (String) filterList.getSelectedItem();
                if (selectedItem == null || selectedItem.isEmpty()) {
                    selectedItem = (String) typeList.getSelectedItem();
                }

                if (selectedItem != null && !selectedItem.isEmpty()) {
                    ModelElementPanel panel = findPanelByName(selectedItem);
                    ModelElement modelElement = findElementByName(selectedItem);

                    if(filterList.getSelectedItem() != null && !filterList.getSelectedItem().toString().isEmpty() && panel != null) {
                        typeList.setSelectedItem(null);
                        panel.setColor(color);
                        for (ModelElementPanel panel1 : this.panels) {
                            if (panel.getName().equals(panel1.getName()) && !panel.getColor().equals(panel1.getColor())) {
                                updatedPanels.removeIf(panel2 -> panel2.getName().equals(panel.getName()));
                                updatedPanels.add(panel);
                                panel1.setColor(color);
                                break;
                            }
                        }
                        SwingUtilities.invokeLater(() -> filterList.setSelectedItem(null));
                    }

                    if (typeList.getSelectedItem() != null && !typeList.getSelectedItem().toString().isEmpty() && modelElement != null) {
                        filterList.setSelectedItem(null);
                        if(modelElement.name.equals("all Extensions")) {
                            this.allExtensionsColors = color;
                        } else if(modelElement.name.equals("all Instances")) {
                            this.allInstancesColors = color;
                        } else if(modelElement.name.equals("all Tabs")) {
                            this.allTabsColors = color;
                        } else {
                            modelElement.color = color;
                            for(ModelElement modelElement1 : this.elements) {
                                if(modelElement.name.equals(modelElement1.name) && !modelElement.color.equals(modelElement1.color)) {
                                    updatedElements.removeIf(modelElement2 -> modelElement2.name.equals(modelElement.name));
                                    updatedElements.add(modelElement);
                                    modelElement1.color = color;
                                    break;
                                }
                            }
                        }
                        SwingUtilities.invokeLater(() -> typeList.setSelectedItem(null));
                    }

                    filterList.repaint();
                    typeList.repaint();
                }
            }
            confirmed = false;
        });
    }

    private class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null) {
                ModelElementPanel panel = findPanelByName(value.toString());
                ModelElement modelElement = findElementByName(value.toString());
                if(panel != null) {
                    Color color = panel.getColor();
                    c.setBackground(color);
                }
                if (modelElement != null) {
                    Color color = modelElement.color;
                    c.setBackground(color);
                }
            }
            return c;
        }
    }

    private ModelElementPanel findPanelByName(String name) {
        for (ModelElementPanel panel : panels) {
            if (panel.getName().equals(name)) {
                return new ModelElementPanel(panel);
            }
        }
        return null;
    }

    private ModelElement findElementByName(String name) {
        switch (name) {
            case "all Extensions" -> {
                ModelElement element = new ModelElement();
                element.name = name;
                element.color = allExtensionsColors;
                return element;
            }
            case "all Instances" -> {
                ModelElement element = new ModelElement();
                element.name = name;
                element.color = allInstancesColors;
                return element;
            }
            case "all Tabs" -> {
                ModelElement element = new ModelElement();
                element.name = name;
                element.color = allTabsColors;
                return element;
            }
        }
        for (ModelElement element : elements) {
            if (element.name.equals(name)) {
                return new ModelElement(element);
            }
        }
        return null;
    }

    private void initDialog() {
        setContentPane(contentPane);
        setTitle("Color Filter");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ArrayList<ModelElementPanel> getUpdatedPanels() {
        return updatedPanels;
    }

    public Collection<ModelElement> getUpdatedElements() {
        return updatedElements;
    }

    public Color getAllExtensionsColors() {
        return allExtensionsColors;
    }

    public Color getAllInstancesColors() {
        return allInstancesColors;
    }

    public Color getAllTabsColors() {
        return allTabsColors;
    }
}
