package gui;

import gui.modelelements.AssociationElementPanel;
import gui.modelelements.ModelElementPanel;
import handlers.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class EditorPanel extends JPanel {
    private GridPanel gridPanel;
    private JPanel grid;
    private JPanel modeling;
    private JLayeredPane layeredPane;
    private Dimension dimension;
    private int gridSize;

    private ModelElementPanel selectedPanel = null;

    private MTSEditorGUI mtsEditorGUI;
    private JPanel associations;

    private ArrayList<AssociationElementPanel> associationElementPanels;

    public ArrayList<AssociationElementPanel> getAssociationElementPanels() {
        return associationElementPanels;
    }

    public ModelElementPanel getSelectedPanel() {
        return selectedPanel;
    }

    public void setSelectedPanel(ModelElementPanel selectedPanel) {
        this.selectedPanel = selectedPanel;
    }

    public EditorPanel(JLayeredPane layeredPane, MTSEditorGUI mtsEditorGUI) {
        this.layeredPane = layeredPane;
        this.mtsEditorGUI = mtsEditorGUI;
        this.associationElementPanels = new ArrayList<>();
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setPanelSize(Dimension dimension) {
        this.dimension = dimension;
        grid.setSize(dimension);
        modeling.setSize(dimension);
        associations.setSize(dimension);
        this.setSize(dimension);
    }

    public void create() {
        layeredPane.setLayout(null);

        grid = new JPanel();
        grid.setLayout(new GridBagLayout());
        layeredPane.add(grid, JLayeredPane.DEFAULT_LAYER);

        modeling = new JPanel();
        modeling.setLayout(null);
        modeling.setOpaque(false);
        layeredPane.add(modeling, JLayeredPane.DRAG_LAYER);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;

        associations = new JPanel();
        associations.setLayout(null);
        associations.setOpaque(false);
        layeredPane.add(associations, JLayeredPane.POPUP_LAYER);


        gridPanel = new GridPanel(gridSize);
        gridPanel.setBackground(new Color(43, 43, 44));
        grid.add(gridPanel, c);
    }

    /**
     * scales the panels if editor is zoomed in
     *
     * @param zoom current zoom factor
     * @param sizeChange value to change the grid size
     */
    public void adjustEditorPanel(double zoom, int sizeChange) {
        gridSize += sizeChange;
        if(gridSize <= 0) {
            gridSize -= sizeChange;
            return;
        }
        gridPanel.setGridSize(gridSize);
        scalePositions(zoom);

        for (Component component : modeling.getComponents()) {
            if (component instanceof ModelElementPanel panel) {
                panel.adjustPanelSize(mtsEditorGUI.getCurrentZoomFactor(), zoom);
            }
        }
        modeling.repaint();
    }

    /**
     * scales the positions of the all panels
     *
     * @param zoom current zoom factor
     */
    private void scalePositions(double zoom) {
        for (Component component : modeling.getComponents()) {
            if (component instanceof ModelElementPanel panel) {
                Point location = panel.getLocation();
                int newX = (int) Math.round(location.x * zoom);
                int newY = (int) Math.round(location.y * zoom);
                panel.setLocation(newX, newY);
            }
        }
        modeling.repaint();
    }

    public void addElement(ModelElementPanel modelElementPanel) {
        modeling.add(modelElementPanel);
        modeling.repaint();
    }

    /**
     * Sorts all element panels in the editor.
     * All panels that are part of an association panel are placed near each other
     */
    public void sortAllElements() {
        int startX = 10;
        int startY = 10;
        int paddingX = 100;
        int paddingY = 20;

        int maxHeight = modeling.getHeight();

        ArrayList<ModelElementPanel> panels = new ArrayList<>();
        Set<ModelElementPanel> visited = new HashSet<>();

        for (Component component : modeling.getComponents()) {
            if (component instanceof ModelElementPanel modelElementPanel
                    && !visited.contains(modelElementPanel)
                    && isEndPanel(modelElementPanel)) {
                sortPanelsList(modelElementPanel, panels, visited);
            }
        }

        for (Component component : modeling.getComponents()) {
            if (component instanceof ModelElementPanel modelElementPanel && !visited.contains(modelElementPanel)) {
                sortPanelsList(modelElementPanel, panels, visited);
            }
        }

        int currentX = startX;
        int currentY = startY;

        for (ModelElementPanel panel : panels) {
            if (currentY + panel.getHeight() + paddingY > maxHeight) {
                currentY = startY;
                currentX += panel.getWidth() + paddingX;
            }

            panel.setLocation(currentX, currentY);
            currentY += panel.getHeight() + paddingY;

            panel.revalidate();
            panel.repaint();
        }
        modeling.revalidate();
        modeling.repaint();
    }

    private void sortPanelsList(ModelElementPanel panel, ArrayList<ModelElementPanel> panels, Set<ModelElementPanel> visited) {
        if (visited.contains(panel) || !panel.getInTab().equals(mtsEditorGUI.getCurrentTab().getName())) {
            return;
        }

        visited.add(panel);
        panels.add(panel);

        for (AssociationElementPanel association : panel.getAssociationElementPanels()) {
            ModelElementPanel startPanel = association.getStartModelElementPanel();
            ModelElementPanel endPanel = association.getEndModelElementPanel();

            if (!visited.contains(endPanel)) {
                sortPanelsList(endPanel, panels, visited);
            }

            if (!visited.contains(startPanel)) {
                sortPanelsList(startPanel, panels, visited);
            }
        }
    }

    private boolean isEndPanel(ModelElementPanel panel) {
        for (Component component : modeling.getComponents()) {
            if (component instanceof ModelElementPanel otherPanel) {
                for (AssociationElementPanel association : otherPanel.getAssociationElementPanels()) {
                    if (association.getStartModelElementPanel().equals(panel)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void removeElement(ModelElementPanel modelElementPanel) {
        modeling.remove(modelElementPanel);
        modeling.repaint();
    }

    public void addAssociation(AssociationElementPanel associationElementPanel) {
        associations.add(associationElementPanel);
        associationElementPanel.setBounds(associations.getBounds());
        associationElementPanels.add(associationElementPanel);
    }

    public void removeAssociation(AssociationElementPanel associationElementPanel) {
        associations.remove(associationElementPanel);
        associationElementPanel.removeAssociation();
        associationElementPanels.remove(associationElementPanel);
        Logger.getInstance().writeInfo("Association between " +
                associationElementPanel.getStartModelElementPanel().getName() + " and " +
                associationElementPanel.getEndModelElementPanel().getName() + " was " +
                "deleted");
    }

    public ArrayList<AssociationElementPanel> getAssociationPanels(ModelElementPanel one, ModelElementPanel two) {
        ArrayList<AssociationElementPanel> output = new ArrayList<>();
        for (AssociationElementPanel associationElementPanel : associationElementPanels) {
            if ((associationElementPanel.getStartModelElementPanel() == one && associationElementPanel.getEndModelElementPanel() == two)
                    || (associationElementPanel.getStartModelElementPanel() == two && associationElementPanel.getEndModelElementPanel() == one)) {
                output.add(associationElementPanel);
            }
        }
        return output;
    }

    public MTSEditorGUI getMtsEditorGUI() {
        return mtsEditorGUI;
    }
}
