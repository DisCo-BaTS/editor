package gui.modelelements;

import gui.*;
import gui.popups.RemoveElement;
import handlers.ElementHandler;
import handlers.Logger;
import handlers.UndoHandler;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelElement;
import model.ModelEnum;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class ModelElementPanel extends JPanel {
    private final UUID uuid;
    private String name;
    private String clazz;
    private MTSEditorGUI mtsEditorGUI;
    private JPanel containerPanel;
    private Point location;
    private InstanciatedClass instance;
    private ModelElement modelElement;
    private ArrayList<JLabel> propertyLabels = new ArrayList<>();
    private final ModelElementType modelElementType;
    private JLabel nameLabel;
    private TableModel tableModel;
    private boolean isDefaultValue;
    private HashMap<Double, Dimension> panelSizes = new HashMap<>();

    public String getNameText() {
        return name;
    }

    public ModelElementType getModelElementType() {
        return modelElementType;
    }

    public InstanciatedClass getInstance() {
        return instance;
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    private boolean isHighlighted = false;
    private Border defaultBorder;
    private Point center;
    private ArrayList<AssociationElementPanel> associationElementPanels;
    private Color color;
    private static final Color DEFAULT_COLOR = new Color(50, 50, 51, 255);
    private Tab tab;
    private String inTab;

    public ArrayList<AssociationElementPanel> getAssociationElementPanels() {
        return associationElementPanels;
    }

    public ModelElementPanel(String name, String clazz, MTSEditorGUI mtsEditorGUI, JPanel containerPanel, Point location, ModelElementType modelElementType) {
        this.modelElementType = modelElementType;
        uuid = UUID.randomUUID();
        this.mtsEditorGUI = mtsEditorGUI;
        this.containerPanel = containerPanel;
        this.location = location;
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setLayout(gridBagLayout);
        this.setLocation(location);
        this.setBorder(BorderFactory.createLineBorder(new Color(119, 119, 120)));
        this.defaultBorder = this.getBorder();
        this.color = DEFAULT_COLOR;
        this.setBackground(color);
        this.name = name;
        this.clazz = clazz;
        associationElementPanels = new ArrayList<>();
        this.inTab = mtsEditorGUI.getCurrentTab().getName();
        this.addMouseListener();
        useElementTypeStyle();

        ModelElementPanel modelElementPanel = this;
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    deleteModelElementPanel();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        if (modelElementType == ModelElementType.INSTANCE) {
            Logger.getInstance().writeInfo("Instance of " + clazz + "(" + name + ")" + " was created");
        } else if (modelElementType == ModelElementType.EXTENSION) {
            Logger.getInstance().writeInfo("Extension of " + clazz + "(" + name + ")" + " was created");
        } else if (modelElementType == ModelElementType.ENUM) {
            Logger.getInstance().writeInfo("Enum " + name + " was created");
        }

        SwingUtilities.invokeLater(() -> this.updateContainer(this));
        SwingUtilities.invokeLater(this::resize);
        SwingUtilities.invokeLater(this::adjustNewCreatedPanelSize);
    }

    public ModelElementPanel(ModelElementPanel modelElementPanel) {
        this.uuid = modelElementPanel.uuid;
        this.name = modelElementPanel.name;
        this.clazz = modelElementPanel.clazz;
        this.mtsEditorGUI = modelElementPanel.mtsEditorGUI;
        this.containerPanel = modelElementPanel.containerPanel;
        this.location = modelElementPanel.location;
        this.instance = modelElementPanel.instance;
        this.modelElement = modelElementPanel.modelElement;
        this.propertyLabels = modelElementPanel.propertyLabels;
        this.modelElementType = modelElementPanel.modelElementType;
        this.nameLabel = modelElementPanel.nameLabel;
        this.tableModel = modelElementPanel.tableModel;
        this.isDefaultValue = modelElementPanel.isDefaultValue;
        this.isHighlighted = modelElementPanel.isHighlighted;
        this.defaultBorder = modelElementPanel.defaultBorder;
        this.center = modelElementPanel.center;
        this.associationElementPanels = modelElementPanel.associationElementPanels;
        this.color = modelElementPanel.color;
        this.panelSizes = modelElementPanel.panelSizes;
    }

    public ModelElementPanel(String name, Point location, JPanel containerPanel, Tab tab, MTSEditorGUI mtsEditorGUI) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.clazz = "";
        this.location = location;
        this.setLocation(location);
        this.containerPanel = containerPanel;
        this.tab = tab;
        this.inTab = mtsEditorGUI.getCurrentTab().getName();
        this.mtsEditorGUI = mtsEditorGUI;
        this.modelElementType = ModelElementType.TAB;
        this.nameLabel = new JLabel(name);
        this.associationElementPanels = new ArrayList<>();
        this.color = DEFAULT_COLOR;
        this.setBackground(color);
        useElementTypeStyle();
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    deleteModelElementPanel();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        addMouseListener();
        SwingUtilities.invokeLater(() -> this.updateContainer(this));
        SwingUtilities.invokeLater(this::resize);
        SwingUtilities.invokeLater(this::adjustNewCreatedPanelSize);
    }

    /**
     * calculates the size of a new created panel
     */
    private void adjustNewCreatedPanelSize() {
        double zoomFactor = 1.0;
        while(zoomFactor != mtsEditorGUI.getCurrentZoomFactor()) {
            double scale;
            if(mtsEditorGUI.getCurrentZoomFactor() > 1.0) {
                scale = 0.1;
            } else {
                scale = -0.1;
            }
            double newZoomFactor = Math.round((zoomFactor + scale) * 10.0) / 10.0;
            double zoom = Math.round((newZoomFactor / zoomFactor) * 1000.0) / 1000.0;
            zoomFactor = Math.round((zoomFactor + scale) * 10.0) / 10.0;
            adjustPanelSize(zoomFactor, zoom);
        }
    }

    /**
     * adjusts the panel size by calculating a new size or using an already calculated and saved panel size
     *
     * @param currentZoomFactor current zoom factor for which the sizes are being calculated
     * @param zoom current zoom scale
     */
    public void adjustPanelSize(double currentZoomFactor, double zoom) {
        Dimension originalSize = getSize();

        if(panelSizes.get(currentZoomFactor) != null) {
            setSize(panelSizes.get(currentZoomFactor));
        } else {
            int newWidth = (int) (originalSize.width * zoom);
            int newHeight = (int) (originalSize.height * zoom);

            if (newWidth <= 0 || newHeight <= 0) {
                return;
            }

            Dimension size = new Dimension(newWidth, newHeight);
            panelSizes.put(currentZoomFactor, size);
            setSize(size);
        }

        adjustFontSize(zoom);
    }

    /**
     * changes the font size of the panel
     *
     * @param zoom current zoom factor
     */
    public void adjustFontSize(double zoom) {
        for (Component component : getComponents()) {
            if (component instanceof JLabel label) {
                Font originalFont = label.getFont();
                float originalSize = originalFont.getSize2D();

                float newSize = (float) (originalSize * zoom);

                label.setFont(originalFont.deriveFont(newSize));
            }
        }
        revalidate();
        repaint();
    }

    public void setAssociationElementGraphic(AssociationElementPanel associationElementPanel) {
        this.associationElementPanels.add(associationElementPanel);
    }

    public void useElementTypeStyle() {
        String headline = "";
        if (modelElementType == ModelElementType.INSTANCE) {
            headline = "<html><body><center><u>" + name + " :";
            if(!mtsEditorGUI.isHideTypeLabelsSelected()) {
                headline += "<br>" + clazz + "</u>";
            }
            headline += "</center></body></html>";
        } else if (modelElementType == ModelElementType.EXTENSION) {
            String t = name;
            t = t.replace("<", "&lt;");
            t = t.replace(">", "&gt;");
            headline = "<html><body><center>" + t;
            if(!mtsEditorGUI.isHideTypeLabelsSelected()) {
                headline += "<br>&lt;&lt;extends " + clazz + "&gt;&gt;";
            }
            headline += "</center></body></html>";
        } else if (modelElementType == ModelElementType.ENUM) {
            headline = "<html><body><center>&lt;&lt;enumeration&gt;&gt;<br>" + name + "</center></body></html>";
        } else if(modelElementType == ModelElementType.TAB) {
            headline = "<html><body><center>" + name + "</center></body></html>";
        } else {
            headline = "<html><body><center>" + name + " // " + clazz + "</center></body></html>";
        }
        this.addLabel(headline, true);
    }

    public Point getCenter() {
        int x = this.getX() + this.getWidth() / 2;
        int y = this.getY() + this.getHeight() / 2;
        center = new Point(x, y);
        return center;
    }


    public UUID getUuid() {
        return uuid;
    }

    public Point getModelElementLocation() {
        return location;
    }

    public void addMouseListener() {
        ModelElementPanel modelElementPanel = this;
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point p0;
            private Point loc0;
            private final Deque<Point> initialLoc = new ArrayDeque<>();
            private boolean isDragging;

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocus();
                isDragging = false;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    p0 = e.getLocationOnScreen();
                    loc0 = ((JComponent) e.getSource()).getLocation();
                    initialLoc.push(loc0);
                }
            }

            private void moveComponent(MouseEvent e) {
                if (p0 == null || loc0 == null) {
                    return;
                }
                Point p1 = e.getLocationOnScreen();
                JComponent comp = (JComponent) e.getSource();
                int x = loc0.x + p1.x - p0.x;
                if (x < 0) {
                    x = 0;
                }
                int y = loc0.y + p1.y - p0.y;
                if (y < 0) {
                    y = 0;
                }
                Point loc1 = new Point(x, y);
                comp.setLocation(loc1);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                isDragging = true;
                moveComponent(e);
                updateContainer(modelElementPanel);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1) {
                    mtsEditorGUI.selectMultipleElements(modelElementPanel);
                } else if(e.getButton() == MouseEvent.BUTTON1) {
                    mtsEditorGUI.elementSelected(modelElementPanel);
                }

                if (isDragging) {
                    mtsEditorGUI.noElementSelected();
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    moveComponent(e);
                    Point finalLoc = modelElementPanel.getLocation();
                    if(!initialLoc.isEmpty() && !initialLoc.getLast().equals(finalLoc)) {
                        UndoableEdit edit = new AbstractUndoableEdit() {
                            @Override
                            public void undo() throws CannotUndoException {
                                super.undo();
                                modelElementPanel.setLocation(initialLoc.pop());
                                updateContainer(modelElementPanel);
                                modelElementPanel.revalidate();
                                modelElementPanel.repaint();
                            }

                            @Override
                            public void redo() throws CannotUndoException {
                                super.redo();
                                initialLoc.push(finalLoc);
                                modelElementPanel.setLocation(finalLoc);
                                updateContainer(modelElementPanel);
                                modelElementPanel.revalidate();
                                modelElementPanel.repaint();
                            }
                        };
                        UndoHandler.getInstance().getUndoableEditSupport().postEdit(edit);
                    }

                    for (Component comp : modelElementPanel.getParent().getComponents()) {
                        if (comp instanceof ModelElementPanel otherPanel && otherPanel != modelElementPanel
                                && !modelElementPanel.getName().equals("Main") && otherPanel.getInTab().equals(modelElementPanel.inTab)) {
                            Rectangle bounds = modelElementPanel.getBounds();
                            Rectangle otherBounds = otherPanel.getBounds();
                            Rectangle intersection = bounds.intersection(otherBounds);

                            if (!intersection.isEmpty()) {
                                int minWidth = Math.min(bounds.width, otherBounds.width);
                                int minHeight = Math.min(bounds.height, otherBounds.height);

                                if (intersection.width >= 0.5 * minWidth && intersection.height >= 0.5 * minHeight && (otherPanel.getModelElementType() == ModelElementType.TAB)) {
                                    for(Tab currenTab : ElementHandler.getInstance().getTabs()) {
                                        if(currenTab.getName().equals(inTab)) {
                                            currenTab.getModelElementPanels().remove(modelElementPanel);
                                        }
                                        if(currenTab.getName().equals(otherPanel.tab.getName())) {
                                            currenTab.getModelElementPanels().add(modelElementPanel);
                                        }
                                    }
                                    inTab = otherPanel.tab.getName();
                                }
                            }
                        }
                    }

                    p0 = null;
                    loc0 = null;
                    updateContainer(modelElementPanel);
                    SwingUtilities.invokeLater(modelElementPanel::revalidate);
                    SwingUtilities.invokeLater(modelElementPanel::repaint);
                    SwingUtilities.invokeLater(mtsEditorGUI::revalidate);
                    SwingUtilities.invokeLater(mtsEditorGUI::repaint);
                    SwingUtilities.invokeLater(mtsEditorGUI::updateHiddenPanels);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    ModelElementContextMenu contextMenu = new ModelElementContextMenu(mtsEditorGUI, modelElementPanel);
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && modelElementType == ModelElementType.TAB) {
                    mtsEditorGUI.changeTab(modelElementPanel.getName());
                }
                //mtsEditorGUI.elementSelected(instance, modelElementPanel);
            }

            public void mouseEntered(MouseEvent e) {
                JPanel parent = (JPanel) e.getSource();
                if (isDefaultValue) {
                    parent.setBorder(BorderFactory.createDashedBorder(new Color(88, 135, 166), 2.0F, 5, 2, true));
                } else {
                    parent.setBorder(BorderFactory.createLineBorder(new Color(88, 135, 166), 2));
                }
                parent.revalidate();
            }

            public void mouseExited(MouseEvent e) {
                if (isHighlighted) {
                    return;
                }
                JPanel parent = (JPanel) e.getSource();
                if (isDefaultValue) {
                    parent.setBorder(BorderFactory.createDashedBorder(null, 1.0F, 5, 5, true));
                } else {
                    parent.setBorder(defaultBorder);
                }
                parent.revalidate();
            }
        };

        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);

    }

    /**
     * creates a new panel
     */
    public void addNewModelElementPanel() {
        ListTransferHandler.importData(mtsEditorGUI.getExtendedClass(name), null, mtsEditorGUI,
                (JPanel) mtsEditorGUI.getEditorLayeredPane().getParent());
    }

    /**
     * deletes a panel
     */
    public void deleteModelElementPanel() {
        if ((new RemoveElement(getNameText(), mtsEditorGUI).isResult())) {
            if(mtsEditorGUI.getElementPanelHandler().removeModelElementPanel(this, false)) {
                ArrayList<AssociationElementPanel> list = new ArrayList<>(getAssociationPanels());
                list.forEach(t -> mtsEditorGUI.getEditorPanel().removeAssociation(t));
            }
            mtsEditorGUI.noElementSelected();
            mtsEditorGUI.addModelElements(ElementHandler.getInstance().getInheretedClasses());
            mtsEditorGUI.repaint();
            mtsEditorGUI.revalidate();
        }
    }

    /**
     * hides a panel
     */
    public void hidePanel() {
        mtsEditorGUI.hidePanel(this);
    }

    public void updateContainer(ModelElementPanel modelElementPanel) {
        Point modelElementPosition = modelElementPanel.getLocation();
        int increasePanelX = modelElementPanel.getWidth() + modelElementPosition.x;
        int increasePanelY = modelElementPanel.getHeight() + modelElementPosition.y;
        if (containerPanel.getHeight() < increasePanelY && containerPanel.getWidth() < increasePanelX) {
            containerPanel.setPreferredSize(new Dimension(increasePanelX, increasePanelY));
        } else if (containerPanel.getWidth() < increasePanelX) {
            containerPanel.setPreferredSize(new Dimension(increasePanelX, containerPanel.getHeight()));
        } else if (containerPanel.getHeight() < increasePanelY) {
            containerPanel.setPreferredSize(new Dimension(containerPanel.getWidth(), increasePanelY));
        }
        if (modelElementPosition.x < containerPanel.getVisibleRect().x) {
            containerPanel.scrollRectToVisible(modelElementPanel.getBounds());
        }
        if (modelElementPosition.y < containerPanel.getVisibleRect().y) {
            containerPanel.scrollRectToVisible(modelElementPanel.getBounds());
        }
        if (increasePanelX > containerPanel.getVisibleRect().x + containerPanel.getVisibleRect().getWidth()) {
            containerPanel.scrollRectToVisible(modelElementPanel.getBounds());
        }
        if (increasePanelY > containerPanel.getVisibleRect().y + containerPanel.getVisibleRect().getHeight()) {
            containerPanel.scrollRectToVisible(modelElementPanel.getBounds());
        }
        revalidate();
        repaint();
        associationElementPanels.forEach(t -> t.updateLocation());
        mtsEditorGUI.getEditorPanel().setPanelSize(mtsEditorGUI.getEditorLayeredPane().getSize());
    }

    public void addLabel(String text, Boolean className) {
        JLabel label = new JLabel();
        GridBagConstraints c = new GridBagConstraints();
        if (className) {
            this.nameLabel = label;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0.5;
            c.ipadx = 40;
            c.ipady = 5;
            this.add(label, c);
            label.setText(text);
            label.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            int i = this.getComponentCount();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = i;
            c.ipady = 2;
            this.add(label, c);
            label.setText(text);
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            propertyLabels.add(label);
        }
        adjustFontSize(mtsEditorGUI.getCurrentZoomFactor());
    }

    private void clear() {
        this.propertyLabels.forEach(this::remove);
        this.propertyLabels = new ArrayList<>();
    }

    private void resize() {
        Dimension size = getPreferredSize();
        this.setSize(size);
        panelSizes.put(1.0, getSize());
        revalidate();
        repaint();
    }

    public void setClass(ModelElement me) {
        this.modelElement = me;
        ElementHandler elementHandler = ElementHandler.getInstance();
        HashMap<String, ModelAttribute> attr = elementHandler.getAllAttributesOfModelElement(me, null);
        ModelAttribute[] modelAttributes = attr.values().toArray(new ModelAttribute[0]);
        setAttributeData();
    }

    public void setInstance(InstanciatedClass ic) {
        this.modelElement = ic.getInstanceOf();
        this.instance = ic;
        ElementHandler elementHandler = ElementHandler.getInstance();
        HashMap<String, ModelAttribute> attr = elementHandler.getAllAttributesOfModelElement(ic.getInstanceOf(), null);
        ModelAttribute[] modelAttributes = attr.values().toArray(new ModelAttribute[0]);
        setAttributeData();
    }

    private void setAttributeData() {
        if (tableModel == null) {
            return;
        }
        StringBuilder table = new StringBuilder("<html><body><table>");
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            StringBuilder stringBuilder = new StringBuilder("<tr>");
            stringBuilder.append("<td>").append(tableModel.getValueAt(i, 0)).append("</td");

            if (this.getModelElement().getClass() != ModelEnum.class) {
                String tmp = tableModel.getValueAt(i, 1).toString();
                tmp = tmp.replace("<", "&lt;");
                tmp = tmp.replace(">", "&gt;");
                stringBuilder.append("<td>").append(tmp).append("</td");

                stringBuilder.append("<td>").append(tableModel.getValueAt(i, 2)).append("</td>");
            }

            stringBuilder.append("</tr>");
            table.append(stringBuilder);

        }

        table.append("<table></body></html>");
        addLabel(table.toString(), false);
    }

    public void refreshView() {
        clear();
        HashMap<String, ModelAttribute> attr = ElementHandler.getInstance().getAllAttributesOfModelElement(modelElement, null);
        setAttributeData();
    }

    public void addHighlight() {
        if (isDefaultValue) {
            this.setBorder(BorderFactory.createDashedBorder(new Color(88, 135, 166), 2.0F, 5, 2, true));
        } else {
            this.setBorder(BorderFactory.createLineBorder(new Color(88, 135, 166), 2));
        }
        this.isHighlighted = true;
    }

    public void removeHighlighting() {
        if (isDefaultValue) {
            this.setBorder(BorderFactory.createDashedBorder(null, 1.0F, 5, 5, true));
        } else {
            this.setBorder(defaultBorder);
        }
        this.isHighlighted = false;
    }

    public void setSubTypes(ArrayList<String> subTypes) {
        if (subTypes.isEmpty()) {
            return;
        }
        boolean first = true;
        this.clazz = clazz.split("<")[0] + "&lt;";
        for (String s : subTypes) {
            if (!first) {
                this.clazz = clazz + ",";
            } else {
                first = false;
            }
            this.clazz = clazz + s;
        }
        this.clazz = clazz + "&gt;";
        this.remove(nameLabel);
        Logger.getInstance().writeInfo("Subtype for instance " + name + "set: " + subTypes.toString());
        useElementTypeStyle();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(modelElementType == ModelElementType.TAB) {
            Graphics2D g2d = (Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();

            int size = 10;
            int scaledSize = (int) (size * mtsEditorGUI.getCurrentZoomFactor());

            Polygon tabShape = new Polygon();
            tabShape.addPoint(0, 0);
            tabShape.addPoint(width - scaledSize, 0);
            tabShape.addPoint(width, scaledSize);
            tabShape.addPoint(width, height);
            tabShape.addPoint(0, height);

            g2d.setColor(getBackground());
            g2d.fill(tabShape);
            g2d.setColor(getForeground());
            g2d.draw(tabShape);

            g2d.dispose();
        } else {
            super.paintComponent(g);
        }
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
        refreshView();
    }

    public void removeAssociation(AssociationElementPanel associationElementPanel) {
        this.associationElementPanels.remove(associationElementPanel);
    }

    public ArrayList<AssociationElementPanel> getAssociationPanels() {
        return associationElementPanels;
    }

    /**
     * adds a association panel
     * @param panel the association panel
     */
    public void addAssociationPanel(AssociationElementPanel panel) {
        associationElementPanels.add(panel);
    }

    public void setDefaultValue() {
        this.isDefaultValue = true;
        this.setBorder(BorderFactory.createDashedBorder(null, 1.0F, 5, 5, true));
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
        this.nameLabel.setText("");
        useElementTypeStyle();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.nameLabel.setText("");
        useElementTypeStyle();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.setBackground(color);
        if(modelElementType != ModelElementType.TAB) {
            modelElement.color = color;
        }
    }

    public Color getDefaultColor() {
        return DEFAULT_COLOR;
    }

    public String getInTab() {
        return inTab;
    }

    public void setInTab(String inTab) {
        this.inTab = inTab;
    }

    public Tab getTab() {
        return tab;
    }

    /**
     * updates the label of the panels
     */
    public void updateNameLabel() {
        this.nameLabel.setText("");
        useElementTypeStyle();
    }

    public void unsetDefaultValue() {
        this.isDefaultValue = false;
        this.setBorder(defaultBorder);
    }

    public boolean isDefaultValue() {
        return isDefaultValue;
    }
}
