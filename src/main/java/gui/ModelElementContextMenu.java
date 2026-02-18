package gui;

import gui.modelelements.ModelElementPanel;
import gui.popups.CreateNamedTemplateDialog;
import gui.popups.CreateNewTabDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Context menu of a selected panel
 */
public class ModelElementContextMenu {
    private final JPopupMenu contextMenu;
    public ModelElementContextMenu(MTSEditorGUI mtsEditorGUI, ModelElementPanel panel) {
        contextMenu = new JPopupMenu();

        JMenuItem menuItem1 = new JMenuItem("Create new Element");
        JMenuItem menuItem2 = new JMenuItem("Delete");
        JMenuItem menuItem3 = new JMenuItem("Hide");
        JMenuItem menuItem4 = new JMenuItem("Create Template");
        JMenuItem menuItem5 = new JMenuItem("Create new Tab");

        menuItem1.addActionListener(event -> panel.addNewModelElementPanel());
        menuItem2.addActionListener(event -> panel.deleteModelElementPanel());
        menuItem3.addActionListener(event -> panel.hidePanel());
        menuItem4.addActionListener(event -> {
            CreateNamedTemplateDialog dialog = new CreateNamedTemplateDialog(mtsEditorGUI);
            dialog.pack();
            dialog.setLocationRelativeTo(mtsEditorGUI);
            dialog.setVisible(true);
        });
        menuItem5.addActionListener(event -> {
            CreateNewTabDialog dialog = new CreateNewTabDialog(mtsEditorGUI, panel.getLocation());
            dialog.pack();
            dialog.setLocationRelativeTo(mtsEditorGUI);
            dialog.setVisible(true);
        });

        if(panel.getModelElementType() == ModelElementType.EXTENSION) {
            contextMenu.add(menuItem1);
        }
        if(!(panel.getModelElementType() == ModelElementType.TAB && panel.getName().equals("Main"))) {
            contextMenu.add(menuItem2);
        }
        contextMenu.add(menuItem3);
        contextMenu.add(menuItem4);
        contextMenu.add(menuItem5);
    }

    public ModelElementContextMenu(MTSEditorGUI mtsEditorGUI, Point point) {
        contextMenu = new JPopupMenu();
        JMenuItem menuItem1 = new JMenuItem("Create Template");
        JMenuItem menuItem2 = new JMenuItem("Create new Tab");

        menuItem1.addActionListener(event -> {
            CreateNamedTemplateDialog dialog = new CreateNamedTemplateDialog(mtsEditorGUI);
            dialog.pack();
            dialog.setLocationRelativeTo(mtsEditorGUI);
            dialog.setVisible(true);
        });
        menuItem2.addActionListener(event -> {
            CreateNewTabDialog dialog = new CreateNewTabDialog(mtsEditorGUI, point);
            dialog.pack();
            dialog.setLocationRelativeTo(mtsEditorGUI);
            dialog.setVisible(true);
        });

        contextMenu.add(menuItem1);
        contextMenu.add(menuItem2);
    }

    public void show(Component component, int x, int y) {
        this.contextMenu.show(component, x, y);
    }
}
