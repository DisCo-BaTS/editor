package gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.modelelements.AssociationElementPanel;
import gui.modelelements.ModelElementPanel;
import gui.popups.*;
import gui.tooltips.TooltipProvider;
import handlers.*;
import model.*;
import modelspaceinterface.DefaultDataTypes;
import modelspaceinterface.ScenarioToXMLConverter;
import org.jetbrains.annotations.NotNull;
import saving.SaveState;
import xml.XMLData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MTSEditorGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel modelElementPane;
    private JPanel workingSpacePane;
    private JPanel subWorkingSpacePane;
    private JPanel consolePane;
    private JPanel editorContainerPane;
    private JPanel propertiesPane;
    private JButton hideModelElementsButton;
    private JButton hidePropertiesButton;
    private JButton hideConsoleButton;
    private JSplitPane modelElementWorkingSpaceSplitPane;
    private JSplitPane subWorkingSpaceConsoleSplitPane;
    private JSplitPane editorPropertiesSplitPane;
    private CustomTable propertiesTable;
    private JScrollPane editorScrollPane;
    private JLayeredPane editorLayeredPane;
    private JPanel editorScrollPaneContainer;
    private JList modelElementList;
    private JList extendedElementList;
    private JSplitPane modelElementSplitPane;
    private JButton addAttributeButton;
    private JPanel editorTitlePanel;
    private JPanel modelElementTitlePanel;
    private JPanel propertiesTitlePanel;
    private JPanel consoleTitlePanel;
    private JTextArea consoleTextArea;
    private JLabel profileElementLabel;
    private JLabel createdElementLabel;
    private JPanel propertiesTablePane;
    private JScrollPane propertiesTableScrollPane;
    private JButton removeAttributeButton;
    private JTextArea elementNameText;
    private JPanel elementNamePane;
    private JButton filterButton;
    private JComboBox<String> allModelElementsList;
    private JButton templatesButton;
    private JButton sortElementsButton;
    private JMenuBar menuBar;
    private JMenu modeMenu;
    private JMenuItem userModeSwitch;
    private boolean isUserMode = false;
    private JMenu helpMenu;
    private JMenu editMenu;
    private JMenuItem editScenarioOptionsMenuItem;
    private JMenu viewMenu;
    private JMenu showMenu;
    private JMenuItem zoomInMenuItem;
    private JMenuItem zoomOutMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem undoMenuItem;
    private JMenuItem showModelElementsMenuItem;
    private JMenuItem showConsoleMenuItem;
    private JMenuItem showPropertiesMenuItem;
    private JMenu hideMenu;
    private JMenuItem hideModelElementsMenuItem;
    private JMenuItem hideConsoleMenuItem;
    private JMenuItem hidePropertiesMenuItem;
    private JMenu fileMenu;
    private JMenuItem newMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem loadMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem debugGUIMenuItem;
    private JMenuItem exportMenuItem;
    private HashMap<JSplitPane, Integer> subWindowSizes;
    private HashMap<JSplitPane, Integer> dividerSize;
    private HashMap<ModelElementPanel, ArrayList<String>> modelElementPanelToAddedAttributes;
    private boolean modelElementPaneHidden = false;
    private EditorPanel editorPanel;
    private JTabbedPane tabbedPane;
    private JButton loadElementListButton;
    private ArrayList<Tab> tabs;
    private Tab currentTab;
    private boolean modelElementSplitPaneDividerLocationManuallySet = false;
    private ElementPanelHandler elementPanelHandler;
    private SaveState saveState;
    private JMenuItem showAllItem;
    private JMenuItem hideAllItem;
    private JFileChooser jFileChooser;
    private HashMap<String, Boolean> visibleElements;
    private HashMap<String, Boolean> visibleTypes;
    private boolean hideAllExtensions;
    private boolean hideAllInstances;
    private boolean hideAllTabs;
    private boolean hideAllAssociations;
    private boolean hideTypeLabels;
    private Color allExtensionsColors = new Color(50, 50, 51, 255);
    private Color allInstancesColors = new Color(50, 50, 51, 255);
    private Color allTabsColors = new Color(50, 50, 51, 255);
    private double currentZoomFactor = 1.0;
    private static final double MAX_ZOOM_FACTOR = 4.0;
    private static final double MIN_ZOOM_FACTOR = 0.1;

    public void setSaveState(SaveState saveState) {
        this.saveState = saveState;
    }

    private DefaultListModel<ListItem> extentedClasses;

    public ElementPanelHandler getElementPanelHandler() {
        return elementPanelHandler;
    }

    public MTSEditorGUI(String title) {
        super(title);
        $$$setupUI$$$();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        initComponents();
        Logger logger = new Logger(consoleTextArea);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
        this.pack();
        this.setLocationRelativeTo(null);
        startTutorial();
        this.delayedInitialization();
        this.modelElementPanelToAddedAttributes = new HashMap<>();
    }

    /**
     * shows a tutorial when the program starts for the first time
     */
    private void startTutorial() {
        SwingUtilities.invokeLater(() -> {
            showTutorialStep("Change between User Mode and Developer Mode. After change, the space of the editor gets cleared.", modeMenu, () -> {
                showTutorialStep("List with all your created elements. Click on an element to find it in the editor.", allModelElementsList, () -> {
                    showTutorialStep("Create elements with drag and drop or double click.", modelElementList, () -> {
                        showTutorialStep("List with your new created extensions.", extendedElementList, () -> {
                            showTutorialStep("Sort your element panels in the editor.", sortElementsButton, () -> {
                                showTutorialStep("Select multiple panels with Ctrl and create a new tab with right mouse click. Change the tab with the tab list or double click the tab panel." +
                                        " Move panels between tabs by moving the panels to the tab panel.", tabbedPane, () -> {
                                    showTutorialStep("Select multiple panels with Ctrl and create a new template with right mouse click. Select your templates here.", templatesButton, () -> {
                                        showTutorialStep("Hide panels or give them different colors with the filter.", filterButton, () -> {
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * shows a tutorial step by explaining and highlighting a component
     */
    private void showTutorialStep(String message, JComponent componentToHighlight, Runnable nextStep) {
        Border oldBorder = componentToHighlight.getBorder();
        componentToHighlight.setBorder(BorderFactory.createLineBorder(Color.RED, 2));

        JPanel onboardingPanel = new JPanel();
        onboardingPanel.setLayout(new BorderLayout());

        JLabel label = new JLabel("<html><body style='width: 300px'>" + message + "</body></html>");
        onboardingPanel.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton nextButton = new JButton("Next");
        JButton skipButton = new JButton("Skip");

        JDialog dialog = new JDialog(this, "Tutorial", true);
        dialog.getContentPane().add(onboardingPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        skipButton.addActionListener(e -> {
            componentToHighlight.setBorder(oldBorder);

            dialog.dispose();
        });

        nextButton.addActionListener(e -> {
            componentToHighlight.setBorder(oldBorder);

            dialog.dispose();

            if (nextStep != null) {
                nextStep.run();
            }
        });
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                skipButton.doClick();
            }
        });

        buttonPanel.add(nextButton);
        buttonPanel.add(skipButton);

        onboardingPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setPreferredSize(new Dimension(400, 150));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public JLayeredPane getEditorLayeredPane() {
        return editorLayeredPane;
    }

    private void initComponents() {
        Thread thread = new Thread(() -> jFileChooser = new JFileChooser());
        thread.start();
        initializeMenu();

        editorTitlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 51)));
        modelElementTitlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 51)));
        propertiesTitlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 51)));
        consoleTitlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 51)));
        createdElementLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(50, 50, 51)));
        profileElementLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 51)));

        int defaultGridSize = 100;
        editorPanel = new EditorPanel(editorLayeredPane, this);
        editorPanel.setGridSize(defaultGridSize);
        editorPanel.create();

        elementPanelHandler = new ElementPanelHandler(editorPanel);

        mainPanel.addComponentListener(new ComponentAdapter() {
            boolean firstRun = true;

            public void componentResized(ComponentEvent componentEvent) {
                if (firstRun) {
                    firstRun = false;
                    return;
                }
                SwingUtilities.invokeLater(() -> resize());
            }
        });

        if (modelElementWorkingSpaceSplitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI) modelElementWorkingSpaceSplitPane.getUI()).getDivider().addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    subWindowSizes.put(modelElementWorkingSpaceSplitPane, modelElementWorkingSpaceSplitPane.getDividerLocation());
                    editorPanel.setPanelSize(editorLayeredPane.getSize());
                }
            });
        }

        if (subWorkingSpaceConsoleSplitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI) subWorkingSpaceConsoleSplitPane.getUI()).getDivider().addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    subWindowSizes.put(subWorkingSpaceConsoleSplitPane, subWorkingSpaceConsoleSplitPane.getHeight()
                            - subWorkingSpaceConsoleSplitPane.getDividerLocation());
                    //resize();
                }
            });
            ((BasicSplitPaneUI) subWorkingSpaceConsoleSplitPane.getUI()).getDivider().addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    editorPanel.setPanelSize(editorLayeredPane.getSize());
                }
            });
        }

        if (editorPropertiesSplitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI) editorPropertiesSplitPane.getUI()).getDivider().addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    subWindowSizes.put(editorPropertiesSplitPane, editorPropertiesSplitPane.getWidth()
                            - editorPropertiesSplitPane.getDividerLocation());
                    //resize();
                }
            });
            ((BasicSplitPaneUI) editorPropertiesSplitPane.getUI()).getDivider().addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    editorPanel.setPanelSize(editorLayeredPane.getSize());
                }
            });
            ((BasicSplitPaneUI) modelElementWorkingSpaceSplitPane.getUI()).getDivider().addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    subWindowSizes.put(editorPropertiesSplitPane, editorPropertiesSplitPane.getWidth()
                            - editorPropertiesSplitPane.getDividerLocation());
                    editorPanel.setPanelSize(editorLayeredPane.getSize());
                }
            });
        }

        if (modelElementSplitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI) modelElementSplitPane.getUI()).getDivider().addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    subWindowSizes.put(modelElementSplitPane, modelElementSplitPane.getHeight() - modelElementSplitPane.getDividerLocation());
                    modelElementSplitPaneDividerLocationManuallySet = true;
                    editorPanel.setPanelSize(editorLayeredPane.getSize());
                }
            });
        }

        MouseAdapter ma = new MouseAdapter() {

            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    origin = new Point(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                origin = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, editorScrollPaneContainer);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        editorScrollPaneContainer.scrollRectToVisible(view);
                    }
                }
            }
        };

        editorScrollPaneContainer.addMouseListener(ma);
        editorScrollPaneContainer.addMouseMotionListener(ma);


        var contentPane = getContentPane();

        createMenuBar();

        hideModelElementsButton.addActionListener(e ->
                hideGuiElements(modelElementPane, modelElementWorkingSpaceSplitPane));

        hideConsoleButton.addActionListener(e ->
                hideGuiElements(consolePane, subWorkingSpaceConsoleSplitPane));

        hidePropertiesButton.addActionListener(e ->
                hideGuiElements(propertiesPane, editorPropertiesSplitPane));

        newMenuItem.addActionListener(e -> clearWorkingSpace());

        filterButton.addActionListener(e -> {
            FilterDialog filterDialog = getFilterDialog();
            if(filterDialog.isConfirmed()) {
                if(filterDialog.isChangeColorSelected()) {
                    ColorFilterDialog changeColorDialog = getChangeColorDialog();
                    if(changeColorDialog.isConfirmed()) {
                        if(!allExtensionsColors.equals(changeColorDialog.getAllExtensionsColors())) {
                            allExtensionsColors = changeColorDialog.getAllExtensionsColors();
                            for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
                                if(panel.getModelElementType() == ModelElementType.EXTENSION) {
                                    panel.setColor(allExtensionsColors);
                                }
                            }
                        }
                        if(!allInstancesColors.equals(changeColorDialog.getAllInstancesColors())) {
                            allInstancesColors = changeColorDialog.getAllInstancesColors();
                            for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
                                if(panel.getModelElementType() == ModelElementType.INSTANCE) {
                                    panel.setColor(allInstancesColors);
                                }
                            }
                        }
                        if(!allTabsColors.equals(changeColorDialog.getAllTabsColors())) {
                            allTabsColors = changeColorDialog.getAllTabsColors();
                            for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
                                if(panel.getModelElementType() == ModelElementType.TAB) {
                                    panel.setColor(allTabsColors);
                                }
                            }
                        }
                        for(ModelElement modelElement : changeColorDialog.getUpdatedElements()) {
                            for(ModelElement modelElement1 : ElementHandler.getInstance().getXmlData().elements.values()) {
                                if(modelElement1.name.equals(modelElement.name)) {
                                    modelElement1.color = modelElement.color;
                                }
                            }
                            for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
                                if(panel.getName().equals(modelElement.name)) {
                                    panel.setColor(modelElement.color);
                                    panel.getModelElement().color = modelElement.color;
                                }
                            }
                        }
                        for(ModelElementPanel panel : changeColorDialog.getUpdatedPanels()) {
                            for(ModelElementPanel panel1 : elementPanelHandler.getModelElementPanels()) {
                                if(panel1.getName().equals(panel.getName())) {
                                    panel1.setColor(panel.getColor());
                                    if(panel1.getModelElementType() != ModelElementType.TAB) {
                                        panel1.getModelElement().color = panel.getColor();
                                    }
                                }
                            }
                        }
                        for(ModelElement modelElement : changeColorDialog.getUpdatedElements()) {
                            for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
                                if(panel.getClazz().replaceAll("&lt;.*&gt;", "<T>").equals(modelElement.name)) {
                                    panel.setColor(modelElement.color);
                                    panel.getModelElement().color = modelElement.color;
                                }
                            }
                        }
                    }
                } else {
                    this.visibleElements = filterDialog.getVisibleElements();
                    this.visibleTypes = filterDialog.getVisibleTypes();
                    this.hideAllExtensions = filterDialog.isHideAllExtensionsCheckBoxSelected();
                    this.hideAllInstances = filterDialog.isHideAllInstancesCheckBoxSelected();
                    this.hideAllTabs = filterDialog.isHideAllTabsCheckBoxSelected();
                    this.hideAllAssociations = filterDialog.isHideAllAssociationsCheckBoxSelected();
                    this.hideTypeLabels = filterDialog.isHideTypeLabelsCheckBoxSelected();
                    updateHiddenPanels();
                }
                repaint();
                revalidate();
            }
        });

        templatesButton.addActionListener(e -> {
            TemplatesListDialog templatesListDialog = new TemplatesListDialog();
            templatesListDialog.pack();
            templatesListDialog.setLocationRelativeTo(this);
            templatesListDialog.setVisible(true);
            if(templatesListDialog.isConfirmed()) {
                TemplateHandler.loadTemplate(this, templatesListDialog.getResult());

                repaint();
                revalidate();
                resize();
            }
        });

        allModelElementsList.addActionListener(e -> {
            String selectedItem = (String) allModelElementsList.getSelectedItem();
            if (selectedItem != null && !selectedItem.isEmpty()) {
                elementSelected(elementPanelHandler.getModelElementPanel(selectedItem));
            }
        });

        sortElementsButton.addActionListener(e -> editorPanel.sortAllElements());

        saveMenuItemActionListener();
        loadMenuItemActionListener();

        exportMenuItem.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            XMLFileFilter xmlFileFilter = new XMLFileFilter();
            jFileChooser.addChoosableFileFilter(xmlFileFilter);
            jFileChooser.setFileFilter(xmlFileFilter);
            jFileChooser.setSelectedFile(new File("scenario.xml"));
            int retrieval = jFileChooser.showSaveDialog(null);
            if (!(retrieval == JFileChooser.APPROVE_OPTION)) {
                return;
            }
            if (ScenarioToXMLConverter.saveToXML(jFileChooser.getSelectedFile().toPath(), ElementHandler.getInstance())) {
                Logger.getInstance().writeInfo("Scenario was successfully exported");
            } else {
                Logger.getInstance().writeWarning("Scenario export failed");
            }
        });

        editorScrollPaneContainer.addMouseWheelListener(e -> {
            if (e.getPreciseWheelRotation() < 0) {
                zoom(0.1);
            } else {
                zoom(-0.1);
            }
        });

        modelElementList.setDragEnabled(true);
        modelElementList.setTransferHandler(new ListTransferHandler(this, (JPanel) editorLayeredPane.getParent(), elementPanelHandler));
        extendedElementList.setDragEnabled(true);
        extendedElementList.setTransferHandler(new ListTransferHandler(this, (JPanel) editorLayeredPane.getParent(), elementPanelHandler));
        ((JPanel) editorLayeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER)[0]).setTransferHandler(new ListTransferHandler(this, (JPanel) editorLayeredPane.getParent(), elementPanelHandler));

        editorScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        editorScrollPane.getHorizontalScrollBar().setUnitIncrement(12);


        this.setFocusable(true);
        this.requestFocusInWindow();
        MTSEditorGUI mtsEditorGUI1 = this;
        this.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE && elementPanelHandler.getHighlightedPanel() != null) {
                    ModelElementPanel modelElementPanel = elementPanelHandler.getHighlightedPanel();
                    if ((new RemoveElement(modelElementPanel.getNameText(), mtsEditorGUI1).isResult())) {
                        if (elementPanelHandler.removeModelElementPanel(modelElementPanel, false)) {
                            List<AssociationElementPanel> associationsToRemove = new ArrayList<>(modelElementPanel.getAssociationPanels());
                            for (AssociationElementPanel panel : associationsToRemove) {
                                editorPanel.removeAssociation(panel);
                            }
                        }
                        noElementSelected();
                        addModelElements(ElementHandler.getInstance().getInheretedClasses());
                        repaint();
                        revalidate();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        MTSEditorGUI mtsEditorGUI = this;
        addAttributeMouseListener(mtsEditorGUI);
        removeAttributeActionListener();

        elementNamePane.setEnabled(false);
        elementNamePane.setVisible(false);
        addAttributeButton.setEnabled(false);
        addAttributeButton.setVisible(false);
        removeAttributeButton.setEnabled(false);
        removeAttributeButton.setVisible(false);

        elementNameText.getDocument().addDocumentListener(new DocumentListener() {
            Border defaultBorder = elementNameText.getBorder();
            @Override
            public void insertUpdate(DocumentEvent e) {
                propertyChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                propertyChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                propertyChange();
            }

            public void propertyChange() {
                if (elementNameText.getText() != null && !Objects.equals(elementNameText.getText(), "")
                        && !elementNameText.getText().replace(" ", "").isEmpty()
                        && !(elementNameText.getText().charAt(0) >= 48 && elementNameText.getText().charAt(0) <= 57)) {

                    for (int i = 0; i < elementNameText.getText().length(); i++) {
                        char c = elementNameText.getText().charAt(i);
                        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                            showTooltip("Name contains invalid characters. Only letters and digits are allowed.");
                            return;
                        }
                    }
                    if (editorPanel.getSelectedPanel().getName().equals(elementNameText.getText())
                            || !ElementHandler.getInstance().nameExists(elementNameText.getText())) {
                        elementNameText.setBorder(defaultBorder);
                        elementNameText.setToolTipText(null);
                    } else {
                        showTooltip("Name already exists. Choose another name.");
                        Logger.getInstance().writeWarning("Element name already exists");
                    }
                } else {
                    showTooltip("Name cannot be empty, start with a number, or contain only whitespace.");
                }
            }

            private void showTooltip(String message) {
                elementNameText.setBorder(BorderFactory.createLineBorder(Color.RED));
                elementNameText.setToolTipText(message);
            }
        });

        elementNameText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    if(elementNameText.getToolTipText() == null && !elementNameText.getText().equals(editorPanel.getSelectedPanel().getName())) {
                        changePanelName();
                        elementNameText.transferFocus();
                    }
                }
            }

            private void changePanelName() {
                if(editorPanel.getSelectedPanel().getInstance() != null) {
                    editorPanel.getSelectedPanel().getInstance().setName(elementNameText.getText());
                }
                if(editorPanel.getSelectedPanel().getModelElementType() == ModelElementType.EXTENSION) {
                    editorPanel.getSelectedPanel().getModelElement().name = elementNameText.getText();
                }
                elementPanelHandler.changePanelName(editorPanel.getSelectedPanel(), elementNameText.getText());
                mtsEditorGUI.addModelElements(ElementHandler.getInstance().getInheretedClasses());
                Logger.getInstance().writeInfo("Name of Panel changed to " + elementNameText.getText());

                editorPanel.revalidate();
                editorPanel.repaint();
            }
        });

        propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (propertiesTable.getSelectedRows().length == 0 || elementPanelHandler.getHighlightedPanel().getModelElementType() == ModelElementType.TAB) {
                    removeAttributeButton.setEnabled(false);
                    return;
                }
                for (Integer i : propertiesTable.getSelectedRows()) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    if (elementPanelHandler.getHighlightedPanel().getModelElementType() != ModelElementType.ENUM) {
                        elementPanelHandler.getHighlightedPanel().getModelElement().attributes.values().stream().toList().forEach(t -> arrayList.add(t.name));
                        if (!arrayList.contains(propertiesTable.getValueAt(i, 0).toString())) {
                            removeAttributeButton.setEnabled(false);
                            return;
                        }
                    } else {
                        ((ModelEnum) elementPanelHandler.getHighlightedPanel().getModelElement()).enumLiterals.stream().toList().forEach(t -> arrayList.add(t.getLiteral()));
                        if (!arrayList.contains(propertiesTable.getValueAt(i, 0).toString()) || !((ModelEnum) elementPanelHandler.getHighlightedPanel().getModelElement()).isAddedEnum()) {
                            removeAttributeButton.setEnabled(false);
                            return;
                        }
                    }
                }
                removeAttributeButton.setEnabled(true);
                elementNamePane.setEnabled(true);
                elementNamePane.setVisible(true);
            }
        });

        editorScrollPaneContainer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    mtsEditorGUI.noElementSelected();
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    ModelElementContextMenu contextMenu = new ModelElementContextMenu(mtsEditorGUI, e.getPoint());
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    if(((JList) e.getComponent()).getSelectedValue() != null) {
                        ListTransferHandler.importData((ListItem) ((JList) e.getComponent()).getSelectedValue(), null,
                                mtsEditorGUI, (JPanel) editorLayeredPane.getParent());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
        modelElementList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JList l = (JList) e.getSource();
                ListModel m = l.getModel();
                int index = l.locationToIndex(e.getPoint());
                if (index > -1) {
                    String tooltip = TooltipProvider.getToolTipForElement(m.getElementAt(index).toString().split("<")[0]);
                    l.setToolTipText(splitToHtml(tooltip));
                }
            }
        });
        extendedElementList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JList l = (JList) e.getSource();
                ListModel m = l.getModel();
                int index = l.locationToIndex(e.getPoint());
                if (index > -1 && !isUserMode) {
                    String tooltip = TooltipProvider.getToolTipForElement(m.getElementAt(index).toString().split("<")[0]);
                    l.setToolTipText(splitToHtml(tooltip));
                } else {
                    l.setToolTipText("");
                }
            }
        });

        ToolTipManager.sharedInstance().setDismissDelay(60000);
        modelElementList.addMouseListener(mouseListener);
        extendedElementList.addMouseListener(mouseListener);

        resetTabs();
    }

    private ColorFilterDialog getChangeColorDialog() {
        ColorFilterDialog changeColorDialog = new ColorFilterDialog(
                new ArrayList<>(elementPanelHandler.getModelElementPanels()),
                new ArrayList<>(ElementHandler.getInstance().getAllModelElementNames()),
                new ArrayList<>(ElementHandler.getInstance().getInheretedClasses()),
                new ArrayList<>(ElementHandler.getInstance().getInstanciatedClasses()),
                new ArrayList<>(ElementHandler.getInstance().getTabs()),
                new ArrayList<>(ElementHandler.getInstance().getXmlData().elements.values()),
                allExtensionsColors, allInstancesColors, allTabsColors);
        changeColorDialog.pack();
        changeColorDialog.setLocationRelativeTo(this);
        changeColorDialog.setVisible(true);
        return changeColorDialog;
    }

    public void updateHiddenPanels() {
        ArrayList<String> elementNames = new ArrayList<>();
        for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
            String clazz = "";
            if(panel.getModelElementType() != ModelElementType.ENUM && panel.getModelElementType() != ModelElementType.TAB) {
                clazz = panel.getClazz().replaceAll("&lt;.*&gt;", "<T>");
            }

            String name = panel.getName();

            boolean isExtensionHidden = panel.getModelElementType() == ModelElementType.EXTENSION && hideAllExtensions;
            boolean isInstanceHidden = panel.getModelElementType() == ModelElementType.INSTANCE && hideAllInstances;
            boolean isElementHidden = visibleElements != null && visibleElements.get(name) != null && !visibleElements.get(name);
            boolean isTypeHiddenByName = visibleTypes != null && visibleTypes.get(name) != null && !visibleTypes.get(name);
            boolean isTypeHiddenByClass = visibleTypes != null && visibleTypes.get(clazz) != null && !visibleTypes.get(clazz);
            boolean isTabHidden = panel.getModelElementType() == ModelElementType.TAB && hideAllTabs;
            boolean isFromAnotherTab = !panel.getInTab().equals(currentTab.getName());

            boolean isVisible = !(isInstanceHidden
                    || isExtensionHidden
                    || isElementHidden
                    || isTypeHiddenByName
                    || isTypeHiddenByClass
                    || isTabHidden
                    || isFromAnotherTab
            );
            panel.setVisible(isVisible);

            if(isVisible) {
                elementNames.add(name);
            }

            if(panel.getModelElementType() != ModelElementType.TAB) {
                panel.updateNameLabel();
            }
        }
        Collections.sort(elementNames);
        allModelElementsList.removeAllItems();
        allModelElementsList.addItem("");

        for (String name1 : elementNames) {
            allModelElementsList.addItem(name1);
        }

        revalidate();
        repaint();
    }

    @NotNull
    private FilterDialog getFilterDialog() {
        FilterDialog filterDialog = new FilterDialog(ElementHandler.getInstance().getAllModelElementNames(),
                ElementHandler.getInstance().getInheretedClasses(), ElementHandler.getInstance().getInstanciatedClasses(),
                ElementHandler.getInstance().getTabs(), visibleElements, visibleTypes, hideAllExtensions,
                hideAllInstances, hideAllTabs, hideAllAssociations, hideTypeLabels);
        filterDialog.pack();
        filterDialog.setLocationRelativeTo(this);
        filterDialog.setVisible(true);
        return filterDialog;
    }

    private void zoom(double scale) {
        double newZoomFactor = Math.round((currentZoomFactor + scale) * 10.0) / 10.0;
        if (newZoomFactor > MAX_ZOOM_FACTOR) {
            newZoomFactor = MAX_ZOOM_FACTOR;
        } else if (newZoomFactor < MIN_ZOOM_FACTOR) {
            newZoomFactor = MIN_ZOOM_FACTOR;
        }
        if(currentZoomFactor == newZoomFactor) {
            return;
        }

        scale = Math.round((newZoomFactor / currentZoomFactor) * 1000.0) / 1000.0;
        int gridSizeChange;
        if(newZoomFactor > currentZoomFactor) {
            gridSizeChange = 10;
        } else {
            gridSizeChange = -10;
        }
        currentZoomFactor = newZoomFactor;

        editorLayeredPane.setSize((int) (editorLayeredPane.getWidth() * scale), (int) (editorLayeredPane.getHeight() * scale));
        editorPanel.adjustEditorPanel(scale, gridSizeChange);

        editorLayeredPane.revalidate();
        editorLayeredPane.repaint();
    }

    public void hidePanel(ModelElementPanel panel) {
        if(this.visibleElements == null) {
            this.visibleElements = new HashMap<>();
        }
        this.visibleElements.put(panel.getName(), false);
        panel.setVisible(false);
        repaint();
        revalidate();
    }

    private void addAttributeMouseListener(MTSEditorGUI mtsEditorGUI) {
        addAttributeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!addAttributeButton.isEnabled()) {
                    return;
                }
                super.mouseClicked(e);
                AddDialogs dialog1;
                if (elementPanelHandler.getHighlightedPanel().getModelElementType() == ModelElementType.ENUM) {
                    dialog1 = new AddEnumLiteral();
                } else {
                    dialog1 = new AddAttributeDialog();
                }
                dialog1.pack();
                dialog1.setLocationRelativeTo(mtsEditorGUI);
                dialog1.setVisible(true);
                ModelElementPanel modelElementPanel = elementPanelHandler.getHighlightedPanel();
                if (dialog1.isResult() && dialog1.getClass() == AddAttributeDialog.class) {
                    AddAttributeDialog dialog = (AddAttributeDialog) dialog1;
                    if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                        ArrayList<String> cg = null;
                        if (!dialog.getComboBoxes().isEmpty()) {
                            cg = new ArrayList<>();
                            Map<Integer, JComboBox> swapped = dialog.getComboBoxes().entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
                            ArrayList<Integer> keys = new ArrayList<>(swapped.keySet());
                            Collections.sort(keys);
                            for (Integer i : keys) {
                                cg.add(((JTextField) swapped.get(i).getEditor().getEditorComponent()).getText());
                            }
                        }
                        String r = GuiActionHandler.getInstance().addAttributeToClass(modelElementPanel.getModelElement(), dialog.getNameProp(), dialog.getTypeProp(), dialog.getDefVal(), cg);
                        if (dialog.getMulti()) {
                            modelElementPanel.getModelElement().getNamedAttribute(dialog.getNameProp()).maxMultiplicity = -1;
                        }
                        if (!modelElementPanelToAddedAttributes.containsKey(modelElementPanel)) {
                            modelElementPanelToAddedAttributes.put(modelElementPanel, new ArrayList<>());
                        }
                        modelElementPanelToAddedAttributes.get(modelElementPanel).add(r);
                        elementSelected(modelElementPanel);
                        modelElementPanel.refreshView();
                        if (elementPanelHandler.getModelElementPanel(dialog.getTypeProp()) != null) {
                            AssociationElementPanel associationElementPanel =
                                    new AssociationElementPanel(modelElementPanel,
                                            elementPanelHandler.getModelElementPanel(dialog.getTypeProp()), mtsEditorGUI);
                            mtsEditorGUI.getEditorPanel().addAssociation(associationElementPanel);
                        }
                    }
                } else if (dialog1.isResult() && dialog1.getClass() == AddEnumLiteral.class) {
                    AddEnumLiteral dialog = (AddEnumLiteral) dialog1;
                    GuiActionHandler.getInstance().addLiteralToEnum(modelElementPanel.getModelElement(), dialog.getLiteral());
                    elementSelected(modelElementPanel);
                    modelElementPanel.refreshView();
                }
            }
        });
    }

    private void removeAttributeActionListener() {
        removeAttributeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelElementPanel modelElementPanel = elementPanelHandler.getHighlightedPanel();
                Integer[] selectedRows = new Integer[propertiesTable.getSelectedRows().length];
                for (int ii = 0; ii < selectedRows.length; ii++) {
                    selectedRows[ii] = propertiesTable.getSelectedRows()[ii];
                }
                Arrays.sort(selectedRows, Collections.reverseOrder());
                if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                    for (Integer i : selectedRows) {
                        String attributeName = propertiesTable.getValueAt(i, 0).toString();
                        for (Map.Entry<String, ModelAttribute> entry : modelElementPanel.getModelElement().attributes.entrySet()) {
                            if (entry.getValue().name.equals(attributeName)) {
                                modelElementPanel.getModelElement().attributes.remove(entry.getKey());
                                modelElementPanelToAddedAttributes.get(modelElementPanel).remove(entry.getKey());
                                Logger.getInstance().writeInfo("Attribute " + attributeName + " was deleted");
                                break;
                            }
                        }
                    }
                } else if (modelElementPanel.getModelElementType() == ModelElementType.ENUM && ((ModelEnum) modelElementPanel.getModelElement()).isAddedEnum()) {
                    for (Integer i : selectedRows) {
                        String literalName = propertiesTable.getValueAt(i, 0).toString();
                        for (EnumLiteral literal : ((ModelEnum) modelElementPanel.getModelElement()).enumLiterals) {
                            if (literal.getLiteral().equals(literalName)) {
                                ((ModelEnum) modelElementPanel.getModelElement()).enumLiterals.remove(literal);
                                Logger.getInstance().writeInfo("Attribute " + literalName + " was deleted");
                                break;
                            }
                        }
                    }
                }
                if (modelElementPanel.getModelElementType() != ModelElementType.INSTANCE) {
                    elementSelected(modelElementPanel);
                }

            }
        });
    }

    private void loadMenuItemActionListener() {
        loadMenuItem.addActionListener(e -> loadElements(true));
    }

    private void loadElements(boolean load) {
        TSSFileFilter tssFileFilter = new TSSFileFilter();
        if (jFileChooser == null) {
            jFileChooser = new JFileChooser();
        }
        jFileChooser.addChoosableFileFilter(tssFileFilter);
        jFileChooser.setFileFilter(tssFileFilter);
        jFileChooser.setSelectedFile(new File("scenario.tss"));
        int retrieval = jFileChooser.showOpenDialog(null);
        if (!(retrieval == JFileChooser.APPROVE_OPTION)) {
            return;
        }

        SaveState saveState1 = SaveState.load(jFileChooser.getSelectedFile().getAbsolutePath());
        if (saveState1 == null) {
            return;
        } else {
            saveState = saveState1;
        }
        ElementHandler elementHandler = ElementHandler.getInstance();

        clearWorkingSpace();

        saveState.inheretedClassesPositions.keySet().forEach(elementHandler::addExtension);
        if(!isUserMode || load) {
            saveState.instanciatedClassesPositions.keySet().forEach(elementHandler::addInstance);
        }
        saveState.addedEnumPositions.keySet().forEach(elementHandler::addEnum);

        for (Map.Entry<ModelEnum, Point> modelEnum : saveState.addedEnumPositions.entrySet()) {
            ModelElementPanel modelElementPanel = new ModelElementPanel(modelEnum.getKey().name, null, this, (JPanel) editorLayeredPane.getParent(), modelEnum.getValue(), ModelElementType.ENUM);
            modelElementPanel.setClass(modelEnum.getKey());
            modelElementPanel.setColor(modelEnum.getKey().color);
            //TODO check this case after change
            this.elementSelected(modelElementPanel);
            elementPanelHandler.addModelElementPanel(modelElementPanel);
        }

        for (Map.Entry<InheretedClass, Point> nc : saveState.inheretedClassesPositions.entrySet()) {
            ModelElementPanel modelElementPanel = new ModelElementPanel(nc.getKey().name, nc.getKey().getSuperClass(), this, (JPanel) editorLayeredPane.getParent(), nc.getValue(), ModelElementType.EXTENSION);
            boolean skip = false;
            if(isUserMode) {
                for(ModelElement element : ElementHandler.getInstance().getXmlData().elements.values()) {
                    if(nc.getKey().getSuperClass().equals(element.name)) {
                        skip = true;
                        break;
                    }
                }
            }
            if(!skip) {
                this.addNewExtendedClass(nc.getKey());
            }
            modelElementPanel.setClass(nc.getKey());
            modelElementPanel.setColor(nc.getKey().color);
            this.elementSelected(modelElementPanel);
            elementPanelHandler.addModelElementPanel(modelElementPanel);
            updateHiddenPanels();
        }

        if(!isUserMode || load) {
            for (Map.Entry<InstanciatedClass, Point> ic : saveState.instanciatedClassesPositions.entrySet()) {
                ModelElementPanel modelElementPanel = new ModelElementPanel(ic.getKey().getName(), ic.getKey().getInstanceOf().name, this, (JPanel) editorLayeredPane.getParent(), ic.getValue(), ModelElementType.INSTANCE);
                if (!ic.getKey().getSubTypes().isEmpty()) {
                    modelElementPanel.setSubTypes(ic.getKey().getSubTypes());
                }
                modelElementPanel.setInstance(ic.getKey());
                modelElementPanel.setColor(ic.getKey().getInstanceOf().color);
                this.elementSelected(modelElementPanel);
                elementPanelHandler.addModelElementPanel(modelElementPanel);
            }

            for(Map.Entry<String, Point> tab : saveState.tabsPositions.entrySet()) {
                ArrayList<ModelElementPanel> modelElementPanels = new ArrayList<>();
                for(Map.Entry<String, ArrayList<String>> entry : saveState.tabsPanels.entrySet()) {
                    if(tab.getKey().equals(entry.getKey())) {
                        for(String modelElementPanel : entry.getValue()) {
                            ModelElementPanel panel = elementPanelHandler.getModelElementPanel(modelElementPanel);
                            if(panel != null) {
                                modelElementPanels.add(panel);
                            }
                        }
                    }
                }
                Tab tab1 = new Tab(tab.getKey(), modelElementPanels);
                if(!tab1.getName().equals("Main")) {
                    tabs.add(tab1);
                    createTab(tab.getKey());
                }
                createTabPanel(tab.getKey(), tab.getValue(), tab1);
            }
        }

        for (int i = 0; i < saveState.associationElementPanels.length; i++) {
            ModelElementPanel a = elementPanelHandler.getModelElementPanel(saveState.associationElementPanels[i][0]);
            ModelElementPanel b = elementPanelHandler.getModelElementPanel(saveState.associationElementPanels[i][1]);
            if(a == null || b == null) {
                continue;
            }
            AssociationElementPanel associationElementPanel = new AssociationElementPanel(a, b, this);
            if ((a.getModelElementType() == ModelElementType.EXTENSION && b.getModelElementType() == ModelElementType.INSTANCE)
                    || (a.getModelElementType() == ModelElementType.INSTANCE && b.getModelElementType() == ModelElementType.EXTENSION)) {
                associationElementPanel.setDefaultValue();
                if (a.getModelElementType() == ModelElementType.INSTANCE && !(a.getModelElement() == b.getModelElement())) {
                    a.setDefaultValue();
                }
                if (b.getModelElementType() == ModelElementType.INSTANCE && !(a.getModelElement() == b.getModelElement())) {
                    b.setDefaultValue();
                }
            }
            a.setAssociationElementGraphic(associationElementPanel);
            b.setAssociationElementGraphic(associationElementPanel);
            editorPanel.addAssociation(associationElementPanel);
        }

        Logger.getInstance().writeInfo("Scenario \"" + jFileChooser.getSelectedFile().getName() + "\"" +
                " " +
                "successfully " +
                "loaded");

        loadElementListButton.setVisible(false);

        noElementSelected();
        this.repaint();
        this.revalidate();
        resize();
    }

    private void saveMenuItemActionListener() {
        saveMenuItem.addActionListener(e -> {
            TSSFileFilter tssFileFilter = new TSSFileFilter();
            jFileChooser.addChoosableFileFilter(tssFileFilter);
            jFileChooser.setFileFilter(tssFileFilter);
            jFileChooser.setSelectedFile(new File("scenario.tss"));
            int retrieval = jFileChooser.showSaveDialog(null);
            if (!(retrieval == JFileChooser.APPROVE_OPTION)) {
                return;
            }

            String[][] associationPanels = new String[editorPanel.getAssociationElementPanels().size()][2];
            for (int i = 0; i < associationPanels.length; i++) {
                associationPanels[i][0] = editorPanel.getAssociationElementPanels().get(i).getStartModelElementPanel().getNameText();
                associationPanels[i][1] = editorPanel.getAssociationElementPanels().get(i).getEndModelElementPanel().getNameText();
            }
            saveState.associationElementPanels = associationPanels;

            HashMap<InheretedClass, Point> inheretedClassPointHashMap = new HashMap<>();
            HashMap<InstanciatedClass, Point> instanciatedClassPointHashMap = new HashMap<>();
            HashMap<ModelEnum, Point> addedEnumsPointHashMap = new HashMap<>();
            HashMap<String, Point> addedTabsPointHashMap = new HashMap<>();
            HashMap<String, ArrayList<String>> addedTabsPanelsHashMap = new HashMap<>();

            for (ModelElementPanel elementPanel : elementPanelHandler.getModelElementPanels()) {
                if (elementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                    inheretedClassPointHashMap.put((InheretedClass) elementPanel.getModelElement(), elementPanel.getLocation());
                } else if (elementPanel.getModelElementType() == ModelElementType.INSTANCE) {
                    instanciatedClassPointHashMap.put(elementPanel.getInstance(), elementPanel.getLocation());
                } else if (elementPanel.getModelElementType() == ModelElementType.ENUM) {
                    addedEnumsPointHashMap.put((ModelEnum) elementPanel.getModelElement(), elementPanel.getLocation());
                } else if(elementPanel.getModelElementType() == ModelElementType.TAB) {
                    addedTabsPointHashMap.put(elementPanel.getTab().getName(), elementPanel.getLocation());
                    ArrayList<String> modelElementPanelNames = new ArrayList<>();
                    for(ModelElementPanel modelElementPanel : elementPanel.getTab().getModelElementPanels()) {
                        modelElementPanelNames.add(modelElementPanel.getName());
                    }
                    addedTabsPanelsHashMap.put(elementPanel.getName(), modelElementPanelNames);
                }
            }

            saveState.inheretedClassesPositions = inheretedClassPointHashMap;
            saveState.instanciatedClassesPositions = instanciatedClassPointHashMap;
            saveState.addedEnumPositions = addedEnumsPointHashMap;
            saveState.tabsPositions = addedTabsPointHashMap;
            saveState.tabsPanels = addedTabsPanelsHashMap;

            saveState.width = editorPanel.getWidth();
            saveState.height = editorPanel.getHeight();

            String fileName = jFileChooser.getSelectedFile().getAbsolutePath();
            if (!(fileName.endsWith(".tss") || fileName.endsWith(".TSS"))) {
                fileName = fileName + ".tss";
            }

            SaveState.save(saveState, fileName);
            Logger.getInstance().writeInfo("Scenario \"" + jFileChooser.getSelectedFile().getName() + "\" " +
                    "successfully " +
                    "saved");
        });
    }

    private void createMenuBar() {
        //======== menuBar ========
        {

            //======== fileMenu ========
            {
                fileMenu.setText("File");

                //---- newMenuItem ----
                newMenuItem.setText("New");
                fileMenu.add(newMenuItem);
                fileMenu.addSeparator();

                //---- saveMenuItem ----
                saveMenuItem.setText("Save...");
                fileMenu.add(saveMenuItem);

                //---- loadMenuItem ----
                loadMenuItem.setText("Load...");
                fileMenu.add(loadMenuItem);
                fileMenu.addSeparator();

                //---- loadMenuItem ----
                exportMenuItem.setText("Export...");
                fileMenu.add(exportMenuItem);
                fileMenu.addSeparator();

                //---- exitMenuItem ----
                exitMenuItem.setText("Exit");
                fileMenu.add(exitMenuItem);
                exitMenuItem.addActionListener(e -> exitWindow());
            }
            menuBar.add(fileMenu);

            //======== editMenu ========
            {
                editMenu.setText("Edit");
                editScenarioOptionsMenuItem.setText("Configure scenario options");
                editMenu.add(editScenarioOptionsMenuItem);
                editScenarioOptionsMenuItem.addActionListener(e -> {
                    ScenarioConfigurationDialog scenarioConfigurationDialog = new ScenarioConfigurationDialog();
                    scenarioConfigurationDialog.pack();
                    scenarioConfigurationDialog.setLocationRelativeTo(this);
                    scenarioConfigurationDialog.setVisible(true);
                    GuiActionHandler.getInstance().takeScenarioConfig(scenarioConfigurationDialog.getTableModel());
                });
                undoMenuItem.setText("Undo");
                redoMenuItem.setText("Redo");
                undoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
                redoMenuItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
                UndoManager undoManager = UndoHandler.getInstance().getUndoManager();
                redoMenuItem.addActionListener(e -> {
                    if(undoManager.canRedo()) {
                        undoManager.redo();
                        updateHiddenPanels();
                        noElementSelected();
                    }
                });

                undoMenuItem.addActionListener(e -> {
                    if(undoManager.canUndo()) {
                        undoManager.undo();
                        updateHiddenPanels();
                        noElementSelected();
                    }
                });
                editMenu.add(undoMenuItem);
                editMenu.add(redoMenuItem);
            }
            menuBar.add(editMenu);

            //======== viewMenu ========
            {
                viewMenu.setText("View");

                //---- showMenu ----
                showMenu.setText("Show");
                viewMenu.add(showMenu);
                showAllItem.setText("All");
                showMenu.add(showAllItem);
                showAllItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showGuiElements(modelElementPane, modelElementWorkingSpaceSplitPane);
                        showGuiElements(consolePane, subWorkingSpaceConsoleSplitPane);
                        showGuiElements(propertiesPane, editorPropertiesSplitPane);
                    }
                });
                zoomInMenuItem.setText("Zoom in");
                zoomOutMenuItem.setText("Zoom out");
                viewMenu.add(zoomInMenuItem);
                viewMenu.add(zoomOutMenuItem);
                zoomInMenuItem.addActionListener(e -> zoom(0.1));
                zoomOutMenuItem.addActionListener(e -> zoom(-0.1));
                showMenu.addSeparator();
                showModelElementsMenuItem.setText("Model Elements Window");
                showMenu.add(showModelElementsMenuItem);
                showModelElementsMenuItem.addActionListener(e -> showGuiElements(modelElementPane, modelElementWorkingSpaceSplitPane));
                showConsoleMenuItem.setText("Console Window");
                showMenu.add(showConsoleMenuItem);
                showConsoleMenuItem.addActionListener(e -> showGuiElements(consolePane, subWorkingSpaceConsoleSplitPane));
                showPropertiesMenuItem.setText("Properties Window");
                showMenu.add(showPropertiesMenuItem);
                showPropertiesMenuItem.addActionListener(e -> showGuiElements(propertiesPane, editorPropertiesSplitPane));


                //---- hideMenu ----
                hideMenu.setText("Hide");
                viewMenu.add(hideMenu);
                hideAllItem.setText("All");
                hideMenu.add(hideAllItem);
                hideAllItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideGuiElements(modelElementPane, modelElementWorkingSpaceSplitPane);
                        hideGuiElements(consolePane, subWorkingSpaceConsoleSplitPane);
                        hideGuiElements(propertiesPane, editorPropertiesSplitPane);
                    }
                });
                hideMenu.addSeparator();
                hideModelElementsMenuItem.setText("Model Elements Window");
                hideMenu.add(hideModelElementsMenuItem);
                hideModelElementsMenuItem.addActionListener(e -> hideGuiElements(modelElementPane, modelElementWorkingSpaceSplitPane));
                hideConsoleMenuItem.setText("Console Window");
                hideMenu.add(hideConsoleMenuItem);
                hideConsoleMenuItem.addActionListener(e -> hideGuiElements(consolePane, subWorkingSpaceConsoleSplitPane));
                hidePropertiesMenuItem.setText("Properties Window");
                hideMenu.add(hidePropertiesMenuItem);
                hidePropertiesMenuItem.addActionListener(e -> hideGuiElements(propertiesPane, editorPropertiesSplitPane));
                viewMenu.addSeparator();
                debugGUIMenuItem.setText("Debug GUI");
                viewMenu.add(debugGUIMenuItem);
                debugGUIMenuItem.addActionListener(e -> resize());

            }
            menuBar.add(viewMenu);

            //======== helpMenu ========
            {
                helpMenu.setText("Help");
            }
            menuBar.add(helpMenu);

            //======== modeMenu ========
            {
                modeMenu.setText("Developer Mode");
                userModeSwitch.setText("Change to User Mode");
                userModeSwitch.addActionListener(e -> {
                    isUserMode = userModeSwitch.getText().equals("Change to User Mode");
                    switchMode();
                });
                loadElementListButton.addActionListener(e -> loadElements(false));
                modeMenu.add(userModeSwitch);
            }
            menuBar.add(modeMenu);
        }

        setJMenuBar(menuBar);
    }

    private void switchMode() {
        if(isUserMode) {
            modeMenu.setText("User Mode");
            userModeSwitch.setText("Change to Developer Mode");
            clearWorkingSpace();
            loadElementListButton.setVisible(true);
            hideAllExtensions = true;
            updateHiddenPanels();
            Logger.getInstance().writeInfo("User mode activated");
        } else {
            modeMenu.setText("Developer Mode");
            userModeSwitch.setText("Change to User Mode");
            clearWorkingSpace();
            addModelElements(ElementHandler.getInstance().getXmlData());
            loadElementListButton.setVisible(false);
            hideAllExtensions = false;
            updateHiddenPanels();
            Logger.getInstance().writeInfo("Developer mode activated");
        }
    }

    private void initializeMenu() {
        menuBar = new JMenuBar();
        modeMenu = new JMenu();
        userModeSwitch = new JMenuItem();
        helpMenu = new JMenu();
        editMenu = new JMenu();
        editScenarioOptionsMenuItem = new JMenuItem();
        viewMenu = new JMenu();
        showMenu = new JMenu();
        zoomInMenuItem = new JMenuItem();
        zoomOutMenuItem = new JMenuItem();
        redoMenuItem = new JMenuItem();
        undoMenuItem = new JMenuItem();
        showModelElementsMenuItem = new JMenuItem();
        showConsoleMenuItem = new JMenuItem();
        showPropertiesMenuItem = new JMenuItem();
        showAllItem = new JMenuItem();
        hideMenu = new JMenu();
        hideModelElementsMenuItem = new JMenuItem();
        hideConsoleMenuItem = new JMenuItem();
        hidePropertiesMenuItem = new JMenuItem();
        hideAllItem = new JMenuItem();
        fileMenu = new JMenu();
        newMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        loadMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();
        debugGUIMenuItem = new JMenuItem();
        exportMenuItem = new JMenuItem();
    }

    private String splitToHtml(String input) {
        StringBuilder stringBuilder = new StringBuilder("<html>");
        String temp = "";
        if (this.getGraphics().getFontMetrics().stringWidth(input) > 200) {
            for (String splitted : input.split(" ")) {
                if (getGraphics().getFontMetrics().stringWidth(temp) < 200) {
                    temp = temp + " " + splitted;
                } else {
                    stringBuilder.append(temp).append("<br>");
                    temp = splitted;
                }
            }
        } else {
            stringBuilder.append(input);
        }
        stringBuilder.append(temp);
        stringBuilder.append("</html>");
        return stringBuilder.toString();
    }

    private void clearWorkingSpace() {
        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.clear();

        ArrayList<ModelElementPanel> modelElementPanels = new ArrayList<>(elementPanelHandler.getModelElementPanels());
        ArrayList<AssociationElementPanel> associationElementPanels = new ArrayList<>(editorPanel.getAssociationElementPanels());

        associationElementPanels.forEach(t -> editorPanel.removeAssociation(t));
        modelElementPanels.forEach(t -> elementPanelHandler.removeModelElementPanel(t, true));

        resetTabs();

        this.extentedClasses = new DefaultListModel<>();
        extendedElementList.setModel(this.extentedClasses);
        consoleTextArea.setText("");

        noElementSelected();
        resize();
    }

    private void resetTabs() {
        currentTab = new Tab("Main", null);
        tabs = new ArrayList<>();
        tabs.add(currentTab);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.removeAll();
        createTab(currentTab.getName());
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                currentTab = tabs.get(selectedIndex);
            }
            updateHiddenPanels();
        });
    }

    private void createTab(String title) {
        JPanel panel = new JPanel();
        tabbedPane.addTab(title, panel);

        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.setOpaque(false);

        JLabel tabTitle = new JLabel(title);
        tabPanel.add(tabTitle);

        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabPanel);
    }

    public void removeTab(ModelElementPanel panel) {
        tabs.remove(panel.getTab());
        ElementHandler.getInstance().removeTab(panel.getTab());
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if(panel.getTab().getName().equals(tabbedPane.getTitleAt(i))) {
                tabbedPane.removeTabAt(i);
                break;
            }
        }
    }

    private void hideGuiElements(JPanel panel, JSplitPane splitPane) {
        panel.setVisible(false);
        splitPane.setDividerSize(0);
        if (panel == modelElementPane) {
            modelElementPaneHidden = true;
        }
        SwingUtilities.invokeLater(this::resize);
    }

    private void showGuiElements(JPanel panel, JSplitPane splitPane) {
        panel.setVisible(true);
        //splitPane.setDividerLocation(subWindowSizes.get(splitPane));
        splitPane.setDividerSize(dividerSize.get(splitPane));
        SwingUtilities.invokeLater(this::resize);
    }

    private void exitWindow() {
        dispose();
    }

    public void resize() {
        if (modelElementWorkingSpaceSplitPane.getDividerLocation() != subWindowSizes.get(modelElementWorkingSpaceSplitPane)) {
            modelElementWorkingSpaceSplitPane.setDividerLocation(subWindowSizes.get(modelElementWorkingSpaceSplitPane));
        }
        if (subWorkingSpaceConsoleSplitPane.getHeight()
                - subWorkingSpaceConsoleSplitPane.getDividerLocation()
                != subWindowSizes.get(subWorkingSpaceConsoleSplitPane)) {
            subWorkingSpaceConsoleSplitPane.setDividerLocation(subWorkingSpaceConsoleSplitPane.getHeight()
                    - subWindowSizes.get(subWorkingSpaceConsoleSplitPane));
        }
        if (modelElementPaneHidden) {
            modelElementPaneHidden = false;
            editorPropertiesSplitPane.setDividerLocation(editorPropertiesSplitPane.getDividerLocation()
                    + modelElementPane.getWidth()
                    + dividerSize.get(modelElementWorkingSpaceSplitPane));
        } else if (editorPropertiesSplitPane.getWidth()
                - editorPropertiesSplitPane.getDividerLocation()
                != subWindowSizes.get(editorPropertiesSplitPane)) {
            editorPropertiesSplitPane.setDividerLocation(editorPropertiesSplitPane.getWidth()
                    - subWindowSizes.get(editorPropertiesSplitPane));
        }

        if (!modelElementSplitPaneDividerLocationManuallySet) {
            modelElementSplitPane.setDividerLocation((2.0 / 3.0));
        } else {
            modelElementSplitPane.setDividerLocation(modelElementSplitPane.getHeight() - subWindowSizes.get(modelElementSplitPane));
        }

        propertiesTable.getTableHeader().setSize(-1, 25);
        Dimension dim = new Dimension(propertiesTable.getWidth(),
                propertiesTable.getRowCount() * propertiesTable.getRowHeight() + propertiesTable.getTableHeader().getHeight());
        propertiesTable.setPreferredSize(dim);
        propertiesTablePane.setPreferredSize(propertiesTable.getPreferredSize());
        if (propertiesTable.getSize().getHeight() > propertiesTablePane.getSize().getHeight()) {
            propertiesTableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
        if (propertiesTableScrollPane.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED) {
            if (propertiesTable.getSize().getHeight() <= propertiesTablePane.getSize().getHeight()) {
                propertiesTableScrollPane.getViewport().setViewPosition(new Point(0, 0));
                propertiesTableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            }
        }

        editorPanel.setPanelSize(editorLayeredPane.getSize());
        SwingUtilities.invokeLater(this::repaint);
        SwingUtilities.invokeLater(this::revalidate);
    }


    private void delayedInitialization() {
        subWindowSizes = new HashMap<>();
        dividerSize = new HashMap<>();

        dividerSize.put(modelElementWorkingSpaceSplitPane, modelElementWorkingSpaceSplitPane.getDividerSize());
        dividerSize.put(subWorkingSpaceConsoleSplitPane, subWorkingSpaceConsoleSplitPane.getDividerSize());
        dividerSize.put(editorPropertiesSplitPane, editorPropertiesSplitPane.getDividerSize());

        subWindowSizes.put(modelElementWorkingSpaceSplitPane, modelElementWorkingSpaceSplitPane.getDividerLocation());
        subWindowSizes.put(subWorkingSpaceConsoleSplitPane, (int) (mainPanel.getPreferredSize().getHeight()
                - subWorkingSpaceConsoleSplitPane.getDividerLocation()));
        subWindowSizes.put(editorPropertiesSplitPane, (int) (mainPanel.getPreferredSize().getWidth()
                - editorPropertiesSplitPane.getDividerLocation()
                - modelElementWorkingSpaceSplitPane.getDividerLocation()));
        hideGuiElements(propertiesPane, editorPropertiesSplitPane);
        SwingUtilities.invokeLater(this::resize);
    }

    public void addModelElements(ElementHandler elementHandler) {
        addModelElements(elementHandler.getXmlData());
        addModelElements(elementHandler.getInheretedClasses());
    }

    public void addModelElements(XMLData xmlData) {
        ModelElement[] modelElements = xmlData.elements.values().toArray(new ModelElement[]{});
        ArrayList<ListItem> model = new ArrayList<>();
        for (int i = 0; i < xmlData.elements.size(); i++) {
            if (!modelElements[i].isMetaClass) {
                model.add(new ListItem(modelElements[i].name, modelElements[i].id));
            }
        }
        model.add(new ListItem("Enum", "ENUMDUMMY"));
        Collections.sort(model, new Comparator<ListItem>() {
            @Override
            public int compare(ListItem o1, ListItem o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        DefaultListModel<ListItem> model2 = new DefaultListModel<>();
        model2.addAll(model);

        modelElementList.setCellRenderer(new IconListRenderer());
        modelElementList.setModel(model2);
    }

    public void addModelElements(ArrayList<InheretedClass> inheretedClasses) {
        this.extentedClasses = new DefaultListModel<>();
        for (InheretedClass inheretedClass : inheretedClasses) {
            if (!inheretedClass.isMetaClass) {
                this.extentedClasses.addElement(new ListItem(inheretedClass.name, inheretedClass.id));
            }
        }
        extendedElementList.setCellRenderer(new IconListRenderer());
        extendedElementList.setModel(this.extentedClasses);
    }

    /**
     * adds or removes highlight to selected panel and the panels in association panel
     *
     * @param modelElementPanel panel which got selected
     */
    public void selectMultipleElements(ModelElementPanel modelElementPanel) {
        Set<ModelElementPanel> visitedPanels = new HashSet<>();
        if (modelElementPanel.isHighlighted()) {
            removeHighlight(modelElementPanel, visitedPanels);
        } else {
            highlightPanel(modelElementPanel, visitedPanels);
        }
    }

    /**
     * removes highlight to selected panel and every panel in association panel
     *
     * @param modelElementPanel selected panel
     * @param visitedPanels set with every panel which already got visited
     */
    private void removeHighlight(ModelElementPanel modelElementPanel, Set<ModelElementPanel> visitedPanels) {
        if (!visitedPanels.add(modelElementPanel)) {
            return;
        }
        modelElementPanel.removeHighlighting();
        if(modelElementPanel.getModelElementType() == ModelElementType.TAB) {
            for(ModelElementPanel panel : modelElementPanel.getTab().getModelElementPanels()) {
                removeHighlight(panel, visitedPanels);
            }
        } else {
            for (AssociationElementPanel panel : modelElementPanel.getAssociationPanels()) {
                if (panel.getEndModelElementPanel().equals(modelElementPanel)
                        && panel.getEndModelElementPanel().getModelElementType() != ModelElementType.INSTANCE) {
                    removeHighlight(panel.getStartModelElementPanel(), visitedPanels);
                }
            }
        }
    }

    /**
     * adds highlight to selected panel every panel in association panel
     *
     * @param modelElementPanel selected panel
     * @param visitedPanels set with every panel which already got visited
     */
    private void highlightPanel(ModelElementPanel modelElementPanel, Set<ModelElementPanel> visitedPanels) {
        if (!visitedPanels.add(modelElementPanel)) {
            return;
        }
        modelElementPanel.addHighlight();
        if(modelElementPanel.getModelElementType() == ModelElementType.TAB) {
            for(ModelElementPanel panel : modelElementPanel.getTab().getModelElementPanels()) {
                highlightPanel(panel, visitedPanels);
            }
        } else {
            for (AssociationElementPanel panel : modelElementPanel.getAssociationPanels()) {
                if (panel.getStartModelElementPanel().equals(modelElementPanel)
                        && (panel.getEndModelElementPanel().getModelElementType() == ModelElementType.EXTENSION
                        || (panel.getStartModelElementPanel().getModelElementType() == ModelElementType.INSTANCE
                        && panel.isUseInstanceOf()))) {
                    highlightPanel(panel.getEndModelElementPanel(), visitedPanels);
                }
            }
        }
    }

    public void noElementSelected() {
        for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
            panel.removeHighlighting();
        }
        String[] columnHeadings = new String[]{"Attribute", "Type", "Value"};
        TableModel tableModel = new AbstractTableModel() {
            final Object[][] rowData = new Object[0][3];
            final Object[] columnNames = columnHeadings;

            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public Object getValueAt(int row, int col) {
                return rowData[row][col];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                super.setValueAt(aValue, rowIndex, columnIndex);
            }
        };
        allModelElementsList.setSelectedItem(null);
        addAttributeButton.setEnabled(false);
        addAttributeButton.setVisible(false);
        removeAttributeButton.setEnabled(false);
        removeAttributeButton.setVisible(false);
        elementNamePane.setEnabled(false);
        elementNamePane.setVisible(false);
        propertiesTable.setModel(tableModel);
        hideGuiElements(propertiesPane, editorPropertiesSplitPane);
    }

    private void selectEnum(ModelElementPanel modelElementPanel) {
        if (modelElementPanel.getModelElementType() == ModelElementType.ENUM && ((ModelEnum) modelElementPanel.getModelElement()).isAddedEnum()) {
            addAttributeButton.setEnabled(true);
            addAttributeButton.setVisible(true);
            //removeAttributeButton.setEnabled(true);
            removeAttributeButton.setVisible(true);
        }

        ArrayList<EnumLiteral> attr = ((ModelEnum) modelElementPanel.getModelElement()).enumLiterals;

        String[] objects = new String[attr.size()];
        for (int i = 0; i < attr.size(); i++) {
            objects[i] = attr.get(i).toString();
        }

        ModelElementPanel modelElementPanel1 = modelElementPanel;
        String[] columnHeadings = new String[]{"Literal"};

        TableModel tableModel = new AbstractTableModel() {
            final ModelElementPanel modelElementPanel = modelElementPanel1;
            final String[] rowData = objects;
            final Object[] columnNames = columnHeadings;

            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getValueAt(int row, int col) {
                return rowData[row];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                super.setValueAt(aValue, rowIndex, columnIndex);
                String current = getValueAt(rowIndex, columnIndex);
                if (GuiActionHandler.getInstance().changeEnumLiteral(modelElementPanel1, current, aValue)) {
                    rowData[rowIndex] = aValue.toString();
                }
                modelElementPanel.refreshView();
            }
        };

        propertiesTable.setModel(tableModel);
        propertiesTable.setModelElementPanel(modelElementPanel1);

        modelElementPanel1.setTableModel(tableModel);
        modelElementPanel1.addHighlight();
        if (editorPanel.getSelectedPanel() != null && editorPanel.getSelectedPanel() != modelElementPanel1) {
            editorPanel.getSelectedPanel().removeHighlighting();
        }
        editorPanel.setSelectedPanel(modelElementPanel1);
        elementNameText.setText(modelElementPanel.getName());
        elementNamePane.setEnabled(true);
        elementNamePane.setVisible(true);

        SwingUtilities.invokeLater(this::resize);
    }

    private void selectTab(ModelElementPanel modelElementPanel) {
        String[] columnHeadings = new String[]{modelElementPanel.getName() + " contains following panels:"};
        String[] objects = new String[modelElementPanel.getTab().getModelElementPanels().size()];
        int j = 0;
        for (int i = 0; i < modelElementPanel.getTab().getModelElementPanels().size(); i++) {
            if(modelElementPanel.getTab().getModelElementPanels().get(i).getModelElementType() != ModelElementType.TAB) {
                objects[j] = modelElementPanel.getTab().getModelElementPanels().get(i).getName();
                j++;
            }
        }

        TableModel tableModel = new AbstractTableModel() {
            final String[] rowData = objects;
            final Object[] columnNames = columnHeadings;

            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getValueAt(int row, int col) {
                return rowData[row];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                super.setValueAt(aValue, rowIndex, columnIndex);
            }
        };
        propertiesTable.setModel(tableModel);
        propertiesTable.setModelElementPanel(modelElementPanel);

        modelElementPanel.addHighlight();
        if (editorPanel.getSelectedPanel() != null && editorPanel.getSelectedPanel() != modelElementPanel) {
            editorPanel.getSelectedPanel().removeHighlighting();
        }
        if(allModelElementsList.getSelectedItem() == null || !allModelElementsList.getSelectedItem().equals(modelElementPanel.getName())) {
            allModelElementsList.setSelectedItem(modelElementPanel.getName());
        }

        SwingUtilities.invokeLater(this::resize);
        elementNamePane.setEnabled(false);
        elementNamePane.setVisible(false);
        addAttributeButton.setEnabled(false);
        addAttributeButton.setVisible(false);
        removeAttributeButton.setEnabled(false);
        removeAttributeButton.setVisible(false);
        showGuiElements(propertiesPane, editorPropertiesSplitPane);
    }

    public void elementSelected(ModelElementPanel modelElementPanel) {
        ModelElement modelElement;
        InstanciatedClass instance;
        if(modelElementPanel.getModelElementType() == ModelElementType.TAB) {
            selectTab(modelElementPanel);
            return;
        }
        if (modelElementPanel.getModelElement() == null) {
            return;
        } else {
            modelElement = modelElementPanel.getModelElement();
        }
        if (modelElementPanel.getInstance() == null) {
            instance = null;
        } else {
            instance = modelElementPanel.getInstance();
        }
        MTSEditorGUI mtsEditorGUI = this;
        for(ModelElementPanel panel : elementPanelHandler.getModelElementPanels()) {
            if(panel.isHighlighted()) {
                panel.removeHighlighting();
            }
        }
        if (modelElementPanel.getModelElementType() == ModelElementType.ENUM) {
            selectEnum(modelElementPanel);
            return;
        }
        ElementHandler eh = ElementHandler.getInstance();
        addAttributeButton.setEnabled(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION);
        addAttributeButton.setVisible(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION);
        removeAttributeButton.setVisible(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION);
        HashMap<String, ModelAttribute> attr = eh.getAllAttributesOfModelElement(modelElement, null);
        ModelAttribute[] modelAttributes = attr.values().toArray(new ModelAttribute[0]);

        Object[][] objects = new Object[attr.size()][3];
        int count = 0;
        for (int i = 0; i < attr.size(); i++) {
            if (modelElementPanelToAddedAttributes.containsKey(modelElementPanel)
                    && modelElementPanelToAddedAttributes.get(modelElementPanel).contains(modelAttributes[i].id)) {
                count++;
                continue;
            }

            objects[i - count][0] = modelAttributes[i].name;
            if (eh.getElement(modelAttributes[i].type) != null) {
                ArrayList<String> a = eh.getPartOfModel(modelAttributes[i].type);
                objects[i - count][1] = eh.getElement(modelAttributes[i].type).name;
                if (a.size() > 1) {
                    objects[i - count][1] = objects[i - count][1].toString().split("<")[0];
                    objects[i - count][1] = objects[i - count][1].toString() + "<";
                    for (int k = 1; k < a.size(); k++) {
                        objects[i - count][1] = objects[i - count][1].toString() + a.get(k) + ",";
                    }
                    objects[i - count][1] = objects[i - count][1].toString().substring(0, objects[i - count][1].toString().length() - 1);
                    objects[i - count][1] = objects[i - count][1].toString() + ">";
                }
                if (modelAttributes[i].chosenSubTypes != null && !modelAttributes[i].chosenSubTypes.isEmpty()) {
                    objects[i - count][1] = objects[i - count][1].toString().replace(">", "");
                    objects[i - count][1] = objects[i - count][1].toString().replace("<", "");
                    objects[i - count][1] = objects[i - count][1].toString().substring(0, objects[i - count][1].toString().length() - 1);
                    for (String s : modelAttributes[i].chosenSubTypes) {
                        objects[i - count][1] = objects[i - count][1].toString() + "<" + s.split("<")[0];
                    }
                    for (int k = 0; k < modelAttributes[i].chosenSubTypes.size(); k++) {
                        objects[i - count][1] = objects[i - count][1] + ">";
                    }
                }
            } else if (DefaultDataTypes.getPrimitiveDatatype(modelAttributes[i].type) != null) {
                objects[i - count][1] = DefaultDataTypes.getPrimitiveDatatype(modelAttributes[i].type);
            } else if (DefaultDataTypes.getSpecialDatatype(modelAttributes[i].type) != null) {
                objects[i - count][1] = DefaultDataTypes.getSpecialDatatype(modelAttributes[i].type);
            } else {
                boolean hasSameGeneric = false;
                for (String s : modelElement.subTypes) {
                    if (modelAttributes[i].type.contains("_" + s + "_")) {
                        hasSameGeneric = true;
                        break;
                    }
                }
                if (hasSameGeneric && instance != null && !instance.getSubTypes().isEmpty()) {
                    objects[i - count][1] = instance.getSubTypes().get(0);
                } else {
                    objects[i - count][1] = "Unknown Type";
                }
            }

            if (instance != null && instance.getAttribute(modelAttributes[i].id) != null) {
                objects[i - count][2] = instance.getAttribute(modelAttributes[i].id);
            } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION
                    && modelElement.getDefaultValue(modelAttributes[i].name) != null) {
                objects[i - count][2] = modelElement.getDefaultValue(modelAttributes[i].name);
            } else {
                objects[i - count][2] = "Default Value";
            }
        }
        if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
            propertiesTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer(attr.size() - count));
        } else {
            propertiesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
        }
        if (modelElementPanelToAddedAttributes.containsKey(modelElementPanel) && !modelElementPanelToAddedAttributes.get(modelElementPanel).isEmpty()) {
            for (String s : modelElementPanelToAddedAttributes.get(modelElementPanel)) {
                objects[attr.size() - count][0] = attr.get(s).name;
                if (eh.getElement(attr.get(s).type) != null) {
                    objects[attr.size() - count][1] = eh.getElement(attr.get(s).type).name;
                    if (attr.get(s).chosenSubTypes != null && !attr.get(s).chosenSubTypes.isEmpty()) {
                        objects[attr.size() - count][1] = objects[attr.size() - count][1].toString().replace(">", "");
                        objects[attr.size() - count][1] = objects[attr.size() - count][1].toString().replace("<", "");
                        objects[attr.size() - count][1] = objects[attr.size() - count][1].toString().substring(0, objects[attr.size() - count][1].toString().length() - 1);
                        for (String s2 : attr.get(s).chosenSubTypes) {
                            objects[attr.size() - count][1] = objects[attr.size() - count][1].toString() + "<" + s2.split("<")[0];
                        }
                        for (int k = 0; k < attr.get(s).chosenSubTypes.size(); k++) {
                            objects[attr.size() - count][1] = objects[attr.size() - count][1] + ">";
                        }
                    }
                } else if (DefaultDataTypes.getPrimitiveDatatype(attr.get(s).type) != null) {
                    objects[attr.size() - count][1] = DefaultDataTypes.getPrimitiveDatatype(attr.get(s).type);
                } else if (DefaultDataTypes.getSpecialDatatype(attr.get(s).type) != null) {
                    objects[attr.size() - count][1] = DefaultDataTypes.getSpecialDatatype(attr.get(s).type);
                } else {
                    boolean hasSameGeneric = false;
                    for (String s2 : modelElement.subTypes) {
                        if (attr.get(s).type.contains("_" + s2 + "_")) {
                            hasSameGeneric = true;
                            break;
                        }
                    }
                    if (hasSameGeneric && instance != null && !instance.getSubTypes().isEmpty()) {
                        objects[attr.size() - count][1] = instance.getSubTypes().get(0);
                    } else {
                        objects[attr.size() - count][1] = "Unknown Type";
                    }
                }

                if (instance != null && instance.getAttribute(attr.get(s).id) != null) {
                    objects[attr.size() - count][2] = instance.getAttribute(attr.get(s).id);
                } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION
                        && modelElement.getDefaultValue(attr.get(s).name) != null) {
                    objects[attr.size() - count][2] = modelElement.getDefaultValue(attr.get(s).name);
                } else {
                    objects[attr.size() - count][2] = "Default Value";
                }
                count--;
            }
        }
        ModelElement modelElement1 = modelElement;
        String[] columnHeadings = new String[]{"Attribute", "Type", "Value"};
        if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
            columnHeadings = new String[]{"Attribute", "Type", "Default Value"};
        }


        if (instance != null) {
            ArrayList<String> done = new ArrayList<>();
            do {
                int counter = 0;
                for (int e = 0; e < objects.length; e++) {
                    if (instance.getInstanceOf().getNamedAttributeIncludingSuper((String) objects[e][0]).maxMultiplicity == -1
                            && instance.getNamedAttribute((String) objects[e][0]) != null
                            && !done.contains((String) objects[e][0])) {
                        ArrayList attrValue = (ArrayList) instance.getNamedAttribute((String) objects[e][0]);
                        Object[][] objects1 = new Object[objects.length + attrValue.size()][objects[0].length];
                        for (int i = 0; i < e; i++) {
                            objects1[i] = objects[i];
                        }
                        for (int i = 0; i < attrValue.size(); i++) {
                            objects1[e + i] = new Object[]{objects[e][0], objects[e][1], attrValue.get(i)};
                        }
                        objects1[e + attrValue.size()] = new Object[]{objects[e][0], objects[e][1], "Default Value"};
                        for (int i = 1; i < objects.length - e; i++) {
                            objects1[i + e + attrValue.size()] = objects[e + i];
                        }
                        objects = objects1;
                        done.add((String) objects[e][0]);
                        break;
                    } else {
                        counter++;
                    }
                }
                if (counter == objects.length) {
                    break;
                }
            } while (done.size() != instance.getAttributeValueSize());
        }


        Object[][] finalObjects = objects;
        String[] finalColumnHeadings = columnHeadings;
        ModelElementPanel modelElementPanel1 = modelElementPanel;
        TableModel tableModel = new AbstractTableModel() {
            final InstanciatedClass instanciatedClass = instance;
            final ModelElement modelElement = modelElement1;
            final ModelElementPanel modelElementPanel = modelElementPanel1;
            Object[][] rowData = finalObjects;
            final Object[] columnNames = finalColumnHeadings;

            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public Object getValueAt(int row, int col) {
                return rowData[row][col];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (modelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
                    if (columnIndex == 2) {
                        return true;
                    } else return false;
                } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {

                    ArrayList<String> attributes = new ArrayList<>();
                    modelElementPanel.getModelElement().attributes.values().stream().forEach(t -> attributes.add(t.name));
                    if (attributes.contains(getValueAt(rowIndex, 0).toString())) {
                        return true;
                    }
                    return columnIndex == 2;
                }
                return false;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                Object oldValue = getValueAt(rowIndex, columnIndex);
                if (oldValue != null && ElementHandler.getInstance().getInstanceByName(oldValue.toString()) != null) {
                    ModelElementPanel mep1 = modelElementPanel;
                    ModelElementPanel mep2 = elementPanelHandler.getModelElementPanel(oldValue.toString());
                    ArrayList<AssociationElementPanel> associationElementPanels = editorPanel.getAssociationPanels(mep1, mep2);
                    associationElementPanels.forEach(editorPanel::removeAssociation);
                    revalidate();
                    repaint();
                }
                super.setValueAt(aValue, rowIndex, columnIndex);
                if (modelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
                    if (aValue.toString().equals("") || aValue.toString().equals("Default Value")) {
                        instanciatedClass.removeAttributeValue((String) rowData[rowIndex][0], oldValue);
                    } else if (instanciatedClass != null) {
                        if (ElementHandler.getInstance().getInstanceByName(aValue.toString()) != null
                                && !DefaultDataTypes.endsWithAnyPrimitiveDatatype(getValueAt(rowIndex, 1).toString())) {
                            aValue = ElementHandler.getInstance().getInstanceByName(aValue.toString());
                            AssociationElementPanel associationElementPanelObject =
                                    new AssociationElementPanel(modelElementPanel,
                                            elementPanelHandler.getModelElementPanel(aValue.toString()), mtsEditorGUI);
                            editorPanel.addAssociation(associationElementPanelObject);
                        }
                        instanciatedClass.setNamedAttribute((String) rowData[rowIndex][0], aValue, oldValue);
                        rowData[rowIndex][columnIndex] = aValue;
                        if (instanciatedClass.getInstanceOf().getNamedAttributeIncludingSuper((String) rowData[rowIndex][0]).maxMultiplicity == -1) {
                            if (!rowData[rowIndex + 1][0].equals(rowData[rowIndex][0])) {
                                Object[][] objects1 = new Object[rowData.length + 1][rowData[0].length];
                                for (int i = 0; i <= rowIndex; i++) {
                                    objects1[i] = rowData[i];
                                }

                                objects1[rowIndex + 1] = new Object[]{objects1[rowIndex][0], objects1[rowIndex][1], "Default Value"};

                                for (int i = rowIndex + 1; i < rowData.length; i++) {
                                    objects1[i + 1] = rowData[i];
                                }
                                rowData = objects1;
                            }
                        }
                    }
                    elementSelected(modelElementPanel);
                    SwingUtilities.invokeLater(mtsEditorGUI::resize);
                    return;
                } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                    switch (columnIndex) {
                        case 0:
                            String currentName = getValueAt(rowIndex, columnIndex).toString();
                            if (GuiActionHandler.getInstance().changeAttributeName(currentName, aValue, modelElement)) {
                                rowData[rowIndex][columnIndex] = aValue;
                            }
                            break;
                        case 1:
                            if (GuiActionHandler.getInstance().changeAttributeType(getValueAt(rowIndex, 0).toString(), aValue, modelElement)) {
                                rowData[rowIndex][columnIndex] = aValue;
                                if (elementPanelHandler.getModelElementPanel(aValue.toString()) != null) {
                                    AssociationElementPanel associationElementPanel =
                                            new AssociationElementPanel(modelElementPanel,
                                                    elementPanelHandler.getModelElementPanel(aValue.toString()), mtsEditorGUI);
                                    getEditorPanel().addAssociation(associationElementPanel);
                                }
                            }
                            break;
                        case 2:
                            if (GuiActionHandler.getInstance().changeAttributeDefValue(getValueAt(rowIndex, 0).toString(), aValue, modelElement)) {
                                rowData[rowIndex][columnIndex] = aValue;
                                if (elementPanelHandler.getModelElementPanel(aValue.toString()) != null
                                        && !DefaultDataTypes.endsWithAnyPrimitiveDatatype(getValueAt(rowIndex, 1).toString())) {
                                    AssociationElementPanel associationElementPanel =
                                            new AssociationElementPanel(modelElementPanel,
                                                    elementPanelHandler.getModelElementPanel(aValue.toString()), mtsEditorGUI);
                                    associationElementPanel.setDefaultValue();
                                    elementPanelHandler.getModelElementPanel(aValue.toString()).setDefaultValue();
                                    getEditorPanel().addAssociation(associationElementPanel);
                                }
                            }
                            break;
                    }
                }
                modelElementPanel.refreshView();
            }
        };

        propertiesTable.setModel(tableModel);
        propertiesTable.setModelElementPanel(modelElementPanel);

        modelElementPanel.addHighlight();
        if (editorPanel.getSelectedPanel() != null && editorPanel.getSelectedPanel() != modelElementPanel) {
            editorPanel.getSelectedPanel().removeHighlighting();
        }
        editorPanel.setSelectedPanel(modelElementPanel);
        if(allModelElementsList.getSelectedItem() == null || !allModelElementsList.getSelectedItem().equals(modelElementPanel.getName())) {
            allModelElementsList.setSelectedItem(modelElementPanel.getName());
        }
        elementNameText.setText(modelElementPanel.getName());
        elementNamePane.setEnabled(true);
        elementNamePane.setVisible(true);
        SwingUtilities.invokeLater(this::resize);
        showGuiElements(propertiesPane, editorPropertiesSplitPane);
    }

    public JScrollPane getEditorScrollPane() {
        return editorScrollPane;
    }

    public void addNewExtendedClass(InheretedClass nc) {
        for (int i = 0; i < extentedClasses.size(); i++) {
            ListItem item = extentedClasses.getElementAt(i);
            if (item.getName().equals(nc.name)) {
                return;
            }
        }
        this.extentedClasses.addElement(new ListItem(nc.name, nc.id));
    }

    public void removeExtendedClass(String name) {
        for (int i = 0; i < extentedClasses.size(); i++) {
            ListItem item = extentedClasses.getElementAt(i);
            if (item.getName().equals(name)) {
                extentedClasses.remove(i);
                break;
            }
        }
    }

    public ListItem getExtendedClass(String name) {
        for (int i = 0; i < extentedClasses.size(); i++) {
            ListItem item = extentedClasses.getElementAt(i);
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    private void createUIComponents() {
        this.propertiesTable = new CustomTable();
    }

    public double getCurrentZoomFactor() {
        return currentZoomFactor;
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public boolean isHideAllAssociationsSelected() {
        return hideAllAssociations;
    }

    public boolean isHideTypeLabelsSelected() {
        return hideTypeLabels;
    }

    public Color getAllExtensionsColors() {
        return allExtensionsColors;
    }

    public Color getAllInstancesColors() {
        return allInstancesColors;
    }

    public void addNewTab(String name, Point point) {
        ArrayList<ModelElementPanel> modelElementPanels = new ArrayList<>();
        for(ModelElementPanel modelElementPanel : elementPanelHandler.getModelElementPanels()) {
            if(modelElementPanel.isHighlighted() && modelElementPanel.getModelElementType() != ModelElementType.TAB) {
                modelElementPanels.add(modelElementPanel);
            }
        }
        Tab tab = new Tab(name, modelElementPanels);
        addMainTab(tab);

        createTabPanel(name, point, tab);
        tabs.add(tab);
        createTab(tab.getName());
        currentTab = tab;
        tabbedPane.setSelectedIndex(tabs.size() - 1);
        updateHiddenPanels();
    }

    private void addMainTab(Tab tab) {
        ModelElementPanel mainTab = new ModelElementPanel(currentTab.getName(), new Point(0, 0), (JPanel) editorLayeredPane.getParent(), currentTab, this);
        mainTab.setInTab(tab.getName());
        elementPanelHandler.addModelElementPanel(mainTab);
        tab.getModelElementPanels().add(mainTab);
    }

    private void createTabPanel(String name, Point point, Tab tab) {
        ModelElementPanel modelElementPanel = new ModelElementPanel(name, point, (JPanel) editorLayeredPane.getParent(), tab, this);
        elementSelected(modelElementPanel);
        elementPanelHandler.addModelElementPanel(modelElementPanel);
        ElementHandler.getInstance().addTab(tab);
        for(Tab tab1 : ElementHandler.getInstance().getTabs()) {
            if(tab1.getName().equals(modelElementPanel.getInTab())) {
                tab1.getModelElementPanels().add(modelElementPanel);
            }
        }
        ArrayList<AssociationElementPanel> associationElementPanels = new ArrayList<>(editorPanel.getAssociationElementPanels());
        for(AssociationElementPanel associationElementPanel : associationElementPanels) {
            ModelElementPanel startPanel = associationElementPanel.getStartModelElementPanel();
            ModelElementPanel endPanel = associationElementPanel.getEndModelElementPanel();
            if(startPanel.getInTab().equals(currentTab.getName()) && endPanel.getInTab().equals(tab.getName())) {
                editorPanel.addAssociation(new AssociationElementPanel(startPanel, modelElementPanel, this));
            } else if(endPanel.getInTab().equals(currentTab.getName()) && startPanel.getInTab().equals(tab.getName())) {
                editorPanel.addAssociation(new AssociationElementPanel(endPanel, modelElementPanel, this));
            }
        }
    }

    public void changeTab(String name) {
        for(int i = 0; i < tabs.size(); i++) {
            if(tabs.get(i).getName().equals(name)) {
                currentTab = tabs.get(i);
                tabbedPane.setSelectedIndex(i);
                updateHiddenPanels();
                break;
            }
        }
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    public boolean isUserMode() {
        return userModeSwitch.isSelected();
    }

    public class TSSFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String s = f.getName().toLowerCase();

            return s.endsWith(".tss");
        }

        @Override
        public String getDescription() {
            return "*.tss,*.TSS";
        }
    }

    class XMLFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String s = f.getName().toLowerCase();

            return s.endsWith(".xml");
        }

        @Override
        public String getDescription() {
            return "*.xml,*.XML";
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setMinimumSize(new Dimension(-1, -1));
        mainPanel.setName("MTSEditor");
        mainPanel.setPreferredSize(new Dimension(1280, 720));
        mainPanel.setRequestFocusEnabled(true);
        modelElementWorkingSpaceSplitPane = new JSplitPane();
        modelElementWorkingSpaceSplitPane.setDividerLocation(300);
        modelElementWorkingSpaceSplitPane.setEnabled(true);
        mainPanel.add(modelElementWorkingSpaceSplitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modelElementWorkingSpaceSplitPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        modelElementPane = new JPanel();
        modelElementPane.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        modelElementPane.setMinimumSize(new Dimension(-1, -1));
        modelElementPane.setName("");
        modelElementPane.setPreferredSize(new Dimension(300, -1));
        modelElementPane.setRequestFocusEnabled(true);
        modelElementWorkingSpaceSplitPane.setLeftComponent(modelElementPane);
        modelElementPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-13487565)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        modelElementSplitPane = new JSplitPane();
        modelElementSplitPane.setDividerLocation(132);
        modelElementSplitPane.setOrientation(0);
        modelElementPane.add(modelElementSplitPane, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        modelElementSplitPane.setLeftComponent(panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        modelElementList = new JList();
        modelElementList.setRequestFocusEnabled(true);
        modelElementList.setSelectionMode(0);
        scrollPane1.setViewportView(modelElementList);
        profileElementLabel = new JLabel();
        profileElementLabel.setAlignmentX(0.5f);
        profileElementLabel.setText("  Profile Elements");
        panel1.add(profileElementLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setName("Extended Elements");
        modelElementSplitPane.setRightComponent(panel2);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel2.add(scrollPane2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        extendedElementList = new JList();
        extendedElementList.setSelectionMode(0);
        scrollPane2.setViewportView(extendedElementList);
        createdElementLabel = new JLabel();
        createdElementLabel.setAlignmentX(0.5f);
        createdElementLabel.setText("  Created Elements");
        panel2.add(createdElementLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        modelElementTitlePanel = new JPanel();
        modelElementTitlePanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        modelElementPane.add(modelElementTitlePanel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setAlignmentX(0.5f);
        label1.setText("  Model Elements");
        modelElementTitlePanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hideModelElementsButton = new JButton();
        hideModelElementsButton.setAutoscrolls(false);
        hideModelElementsButton.setBackground(new Color(-12828863));
        hideModelElementsButton.setBorderPainted(false);
        hideModelElementsButton.setContentAreaFilled(true);
        hideModelElementsButton.setDefaultCapable(true);
        hideModelElementsButton.setDoubleBuffered(false);
        hideModelElementsButton.setEnabled(true);
        hideModelElementsButton.setFocusCycleRoot(false);
        hideModelElementsButton.setFocusPainted(true);
        hideModelElementsButton.setFocusTraversalPolicyProvider(false);
        hideModelElementsButton.setFocusable(true);
        Font hideModelElementsButtonFont = this.$$$getFont2$$$(null, Font.BOLD, 16, hideModelElementsButton.getFont());
        if (hideModelElementsButtonFont != null) hideModelElementsButton.setFont(hideModelElementsButtonFont);
        hideModelElementsButton.setForeground(new Color(-4473925));
        hideModelElementsButton.setText("-");
        hideModelElementsButton.setToolTipText("Minimize Model Elements");
        modelElementTitlePanel.add(hideModelElementsButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(20, 20), 0, false));
        workingSpacePane = new JPanel();
        workingSpacePane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        modelElementWorkingSpaceSplitPane.setRightComponent(workingSpacePane);
        subWorkingSpaceConsoleSplitPane = new JSplitPane();
        subWorkingSpaceConsoleSplitPane.setDividerLocation(550);
        subWorkingSpaceConsoleSplitPane.setOrientation(0);
        workingSpacePane.add(subWorkingSpaceConsoleSplitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        subWorkingSpacePane = new JPanel();
        subWorkingSpacePane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        subWorkingSpaceConsoleSplitPane.setLeftComponent(subWorkingSpacePane);
        editorPropertiesSplitPane = new JSplitPane();
        editorPropertiesSplitPane.setDividerLocation(700);
        subWorkingSpacePane.add(editorPropertiesSplitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorContainerPane = new JPanel();
        editorContainerPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        editorContainerPane.setAlignmentX(0.0f);
        editorContainerPane.setAlignmentY(0.0f);
        editorContainerPane.setBackground(new Color(-12828863));
        editorContainerPane.setFocusCycleRoot(false);
        editorContainerPane.setMinimumSize(new Dimension(-1, -1));
        editorContainerPane.setPreferredSize(new Dimension(-1, -1));
        editorContainerPane.setRequestFocusEnabled(true);
        editorPropertiesSplitPane.setLeftComponent(editorContainerPane);
        editorContainerPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-13487565)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        editorScrollPane = new JScrollPane();
        editorScrollPane.setAlignmentX(0.0f);
        editorScrollPane.setAlignmentY(0.0f);
        editorScrollPane.setAutoscrolls(false);
        editorScrollPane.setBackground(new Color(-12828863));
        editorScrollPane.setHorizontalScrollBarPolicy(32);
        editorScrollPane.setOpaque(false);
        editorScrollPane.setVerticalScrollBarPolicy(22);
        editorContainerPane.add(editorScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editorScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        editorScrollPaneContainer = new JPanel();
        editorScrollPaneContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        editorScrollPaneContainer.setAlignmentX(0.0f);
        editorScrollPaneContainer.setAlignmentY(0.0f);
        editorScrollPaneContainer.setBackground(new Color(-12828863));
        editorScrollPane.setViewportView(editorScrollPaneContainer);
        editorLayeredPane = new JLayeredPane();
        editorLayeredPane.setLayout(new GridBagLayout());
        editorLayeredPane.setAutoscrolls(false);
        editorLayeredPane.setBackground(new Color(-12828863));
        editorLayeredPane.setOpaque(true);
        editorScrollPaneContainer.add(editorLayeredPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorTitlePanel = new JPanel();
        editorTitlePanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        editorContainerPane.add(editorTitlePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(2);
        label2.setHorizontalTextPosition(2);
        label2.setText("  Editor");
        editorTitlePanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 28), null, null, 0, false));
        propertiesPane = new JPanel();
        propertiesPane.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        propertiesPane.setMinimumSize(new Dimension(-1, -1));
        propertiesPane.setPreferredSize(new Dimension(-1, -1));
        editorPropertiesSplitPane.setRightComponent(propertiesPane);
        propertiesPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-13487565)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        propertiesTitlePanel = new JPanel();
        propertiesTitlePanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        propertiesPane.add(propertiesTitlePanel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("  Properties");
        propertiesTitlePanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(59, 26), null, 0, false));
        hidePropertiesButton = new JButton();
        hidePropertiesButton.setAutoscrolls(false);
        hidePropertiesButton.setBackground(new Color(-12828863));
        hidePropertiesButton.setBorderPainted(false);
        hidePropertiesButton.setContentAreaFilled(true);
        hidePropertiesButton.setDefaultCapable(true);
        hidePropertiesButton.setDoubleBuffered(false);
        hidePropertiesButton.setEnabled(true);
        hidePropertiesButton.setFocusCycleRoot(false);
        hidePropertiesButton.setFocusPainted(true);
        hidePropertiesButton.setFocusTraversalPolicyProvider(false);
        hidePropertiesButton.setFocusable(true);
        Font hidePropertiesButtonFont = this.$$$getFont2$$$(null, Font.BOLD, 16, hidePropertiesButton.getFont());
        if (hidePropertiesButtonFont != null) hidePropertiesButton.setFont(hidePropertiesButtonFont);
        hidePropertiesButton.setForeground(new Color(-4473925));
        hidePropertiesButton.setText("-");
        hidePropertiesButton.setToolTipText("Minimize Properties");
        propertiesTitlePanel.add(hidePropertiesButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(78, 26), new Dimension(20, 20), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        propertiesPane.add(panel3, new GridConstraints(1, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        propertiesTablePane = new JPanel();
        propertiesTablePane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(propertiesTablePane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        propertiesTableScrollPane = new JScrollPane();
        propertiesTablePane.add(propertiesTableScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        propertiesTableScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        propertiesTableScrollPane.setViewportView(propertiesTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addAttributeButton = new JButton();
        addAttributeButton.setAlignmentY(0.0f);
        addAttributeButton.setBackground(new Color(-12828863));
        addAttributeButton.setBorderPainted(false);
        addAttributeButton.setEnabled(true);
        addAttributeButton.setFocusable(true);
        Font addAttributeButtonFont = this.$$$getFont2$$$(null, Font.BOLD, 16, addAttributeButton.getFont());
        if (addAttributeButtonFont != null) addAttributeButton.setFont(addAttributeButtonFont);
        addAttributeButton.setForeground(new Color(-12679520));
        addAttributeButton.setText("+");
        addAttributeButton.setToolTipText("Add Attribute");
        addAttributeButton.setVerticalAlignment(0);
        panel4.add(addAttributeButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(20, 20), 0, false));
        removeAttributeButton = new JButton();
        removeAttributeButton.setAlignmentY(0.0f);
        removeAttributeButton.setBackground(new Color(-12828863));
        removeAttributeButton.setBorderPainted(false);
        removeAttributeButton.setEnabled(true);
        removeAttributeButton.setFocusable(true);
        Font removeAttributeButtonFont = this.$$$getFont2$$$(null, Font.BOLD, 16, removeAttributeButton.getFont());
        if (removeAttributeButtonFont != null) removeAttributeButton.setFont(removeAttributeButtonFont);
        removeAttributeButton.setForeground(new Color(-4571590));
        removeAttributeButton.setHorizontalTextPosition(11);
        removeAttributeButton.setText("-");
        removeAttributeButton.setToolTipText("Remove Attribute");
        removeAttributeButton.setVerticalAlignment(0);
        panel4.add(removeAttributeButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(20, 20), 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        consolePane = new JPanel();
        consolePane.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        consolePane.setMinimumSize(new Dimension(-1, -1));
        consolePane.setPreferredSize(new Dimension(-1, 150));
        subWorkingSpaceConsoleSplitPane.setRightComponent(consolePane);
        consolePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-13487565)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer2 = new Spacer();
        consolePane.add(spacer2, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        consoleTitlePanel = new JPanel();
        consoleTitlePanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        consolePane.add(consoleTitlePanel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setAlignmentX(0.5f);
        label4.setText("Console");
        consoleTitlePanel.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        hideConsoleButton = new JButton();
        hideConsoleButton.setAutoscrolls(false);
        hideConsoleButton.setBackground(new Color(-12828863));
        hideConsoleButton.setBorderPainted(false);
        hideConsoleButton.setContentAreaFilled(true);
        hideConsoleButton.setDefaultCapable(true);
        hideConsoleButton.setDoubleBuffered(false);
        hideConsoleButton.setEnabled(true);
        hideConsoleButton.setFocusCycleRoot(false);
        hideConsoleButton.setFocusPainted(true);
        hideConsoleButton.setFocusTraversalPolicyProvider(false);
        hideConsoleButton.setFocusable(true);
        Font hideConsoleButtonFont = this.$$$getFont2$$$(null, Font.BOLD, 16, hideConsoleButton.getFont());
        if (hideConsoleButtonFont != null) hideConsoleButton.setFont(hideConsoleButtonFont);
        hideConsoleButton.setForeground(new Color(-4473925));
        hideConsoleButton.setText("-");
        hideConsoleButton.setToolTipText("Minimize Console");
        consoleTitlePanel.add(hideConsoleButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(20, 20), 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setHorizontalScrollBarPolicy(31);
        consolePane.add(scrollPane3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        consoleTextArea = new JTextArea();
        consoleTextArea.setBackground(new Color(-13948117));
        Font consoleTextAreaFont = this.$$$getFont2$$$("Consolas", -1, -1, consoleTextArea.getFont());
        if (consoleTextAreaFont != null) consoleTextArea.setFont(consoleTextAreaFont);
        scrollPane3.setViewportView(consoleTextArea);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont2$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

