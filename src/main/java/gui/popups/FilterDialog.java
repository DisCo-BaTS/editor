package gui.popups;

import gui.Tab;
import model.InheretedClass;
import model.InstanciatedClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * manages the filter dialog to hide certain panels
 */
public class FilterDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> filterList;
    private JComboBox<String> typeList;
    private JCheckBox hideTypeLabelsCheckBox;
    private JCheckBox hideAllExtensionsCheckBox;
    private JCheckBox hideAllInstancesCheckBox;
    private JCheckBox hideAllTabsCheckBox;
    private JCheckBox hideAllAssociationsCheckBox;
    private JButton changeColorsButton;
    private HashMap<String, Boolean> visibleElements;
    private HashMap<String, Boolean> visibleTypes;
    private boolean confirmed;
    private boolean changeColor;

    public FilterDialog(ArrayList<String> allModelElements, ArrayList<InheretedClass> inheretedClasses, ArrayList<InstanciatedClass> instanciatedClasses,
                        ArrayList<Tab> tabs, HashMap<String, Boolean> visibleElements, HashMap<String, Boolean> visibleTypes,
                        boolean hideAllExtensions, boolean hideAllInstances, boolean hideAllTabs, boolean hideAllAssociations, boolean hideTypeLabels) {
        initFilterDialog();
        hideAllExtensionsCheckBox.setSelected(hideAllExtensions);
        hideAllInstancesCheckBox.setSelected(hideAllInstances);
        hideAllTabsCheckBox.setSelected(hideAllTabs);
        hideAllAssociationsCheckBox.setSelected(hideAllAssociations);
        hideTypeLabelsCheckBox.setSelected(hideTypeLabels);

        if(visibleElements != null && !visibleElements.isEmpty()) {
            this.visibleElements = visibleElements;
        } else {
            this.visibleElements = new HashMap<>();
        }
        if(visibleTypes != null && !visibleTypes.isEmpty()) {
            this.visibleTypes = visibleTypes;
        } else {
            this.visibleTypes = new HashMap<>();
        }
        for(InheretedClass element : inheretedClasses) {
            if(!this.visibleElements.containsKey(element.name)) {
                this.visibleElements.put(element.name, true);
            }
        }
        for(InstanciatedClass element : instanciatedClasses) {
            if(!this.visibleElements.containsKey(element.getName())) {
                this.visibleElements.put(element.getName(), true);
            }
        }
        for(Tab tab : tabs) {
            if(!this.visibleElements.containsKey(tab.getName())) {
                this.visibleElements.put(tab.getName(), true);
            }
        }
        for(String element : allModelElements) {
            if(!this.visibleTypes.containsKey(element)) {
                this.visibleTypes.put(element, true);
            }
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
            for (Tab tab : tabs) {
                listModel.addElement(tab.getName());
            }
            filterList.setModel(listModel);

            filterList.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedItem = (String) e.getItem();
                    if(!selectedItem.isEmpty()) {
                        this.visibleElements.put(selectedItem, !this.visibleElements.get(selectedItem));
                    }
                }
            });
            CustomListCellRenderer filterRenderer = new CustomListCellRenderer();
            filterRenderer.setElementsMap(this.visibleElements);
            filterList.setRenderer(filterRenderer);
            filterList.addActionListener(e -> {
                String selectedItem = (String) filterList.getSelectedItem();
                if (selectedItem != null) {
                    SwingUtilities.invokeLater(() -> {
                        filterList.setSelectedItem(null);
                        filterList.showPopup();
                    });
                }
            });
        }
        if(!allModelElements.isEmpty()) {
            Collections.sort(allModelElements);
            DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
            listModel.addElement("");
            for (String element : allModelElements) {
                listModel.addElement(element);
            }
            typeList.setModel(listModel);
            typeList.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedItem = (String) e.getItem();
                    if(!selectedItem.isEmpty()) {
                        this.visibleTypes.put(selectedItem, !this.visibleTypes.get(selectedItem));
                    }
                }
            });
            CustomListCellRenderer typeRenderer = new CustomListCellRenderer();
            typeRenderer.setElementsMap(this.visibleTypes);
            typeList.setRenderer(typeRenderer);
            typeList.addActionListener(e -> {
                String selectedItem = (String) typeList.getSelectedItem();
                if (selectedItem != null) {
                    SwingUtilities.invokeLater(() -> {
                        typeList.setSelectedItem(null);
                        typeList.showPopup();
                    });
                }
            });
        }
        changeColorsButton.addActionListener(e -> {
            changeColor = true;
            onOK();
        });
    }

    private class CustomListCellRenderer extends DefaultListCellRenderer {
        private HashMap<String, Boolean> elementsMap;

        public void setElementsMap(HashMap<String, Boolean> elementsMap) {
            this.elementsMap = elementsMap;
        }
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String elementName = (String) value;
            if (elementName != null && !elementName.isEmpty() && elementsMap != null && elementsMap.containsKey(elementName)) {
                if (elementsMap.get(elementName)) {
                    label.setText("✓ " + elementName);
                    label.setForeground(Color.GREEN);
                } else {
                    label.setText("✗ " + elementName);
                    label.setForeground(Color.RED);
                }
            }
            return label;
        }
    }

    private void initFilterDialog() {
        setContentPane(contentPane);
        setTitle("Filter");
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

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public HashMap<String, Boolean> getVisibleElements() {
        return visibleElements;
    }

    public HashMap<String, Boolean> getVisibleTypes() {
        return visibleTypes;
    }

    public boolean isHideAllExtensionsCheckBoxSelected() {
        return hideAllExtensionsCheckBox.isSelected();
    }

    public boolean isHideAllInstancesCheckBoxSelected() {
        return hideAllInstancesCheckBox.isSelected();
    }

    public boolean isHideAllTabsCheckBoxSelected() {
        return hideAllTabsCheckBox.isSelected();
    }

    public boolean isHideAllAssociationsCheckBoxSelected() {
        return hideAllAssociationsCheckBox.isSelected();
    }

    public boolean isHideTypeLabelsCheckBoxSelected() {
        return hideTypeLabelsCheckBox.isSelected();
    }

    public boolean isChangeColorSelected() {
        return changeColor;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
