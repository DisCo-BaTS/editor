package gui.modelelements;

import gui.MTSEditorGUI;
import gui.ModelElementType;
import handlers.Logger;
import model.InheretedClass;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class AssociationElementPanel extends JPanel implements Serializable {
    private ModelElementPanel startModelElementPanel;
    private ModelElementPanel endModelElementPanel;
    private Point startPoint;
    private Point endPoint;
    private Point centerPoint;
    private Rectangle bounds;
    private Color color = new Color(80, 135, 166);
    private BasicStroke stroke;
    private boolean useInstanceOf = false;
    private MTSEditorGUI mtsEditorGUI;

    public ModelElementPanel getStartModelElementPanel() {
        return startModelElementPanel;
    }

    public ModelElementPanel getEndModelElementPanel() {
        return endModelElementPanel;
    }

    public AssociationElementPanel(ModelElementPanel startModelElementPanel, ModelElementPanel endModelElementPanel,
                                   MTSEditorGUI mtsEditorGUI) {
        this.startModelElementPanel = startModelElementPanel;
        this.endModelElementPanel = endModelElementPanel;
        this.mtsEditorGUI = mtsEditorGUI;
        startModelElementPanel.setAssociationElementGraphic(this);
        endModelElementPanel.setAssociationElementGraphic(this);
        startPoint = startModelElementPanel.getCenter();
        endPoint = endModelElementPanel.getCenter();
        centerPoint = getLineCenter();

        if ((startModelElementPanel.getModelElementType() == ModelElementType.INSTANCE && endModelElementPanel.getModelElementType() == ModelElementType.EXTENSION)
                || startModelElementPanel.getModelElementType() == ModelElementType.EXTENSION && endModelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
            //INSTANCE OF
            stroke = new BasicStroke(2);
            useInstanceOf = true;
        } else if (startModelElementPanel.getModelElementType() == ModelElementType.INSTANCE && endModelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
            //Regular association
            stroke = new BasicStroke(2);
        } else if (startModelElementPanel.getModelElementType() == ModelElementType.EXTENSION && endModelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
            //Either association or extension
            if (((InheretedClass) startModelElementPanel.getModelElement()).getSuperClass().equals(endModelElementPanel.getModelElement().name)
                    || ((InheretedClass) endModelElementPanel.getModelElement()).getSuperClass().equals(startModelElementPanel.getModelElement().name)) {
                //Extension
                float[] dashingPattern2 = {10f, 4f};
                stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 1.0f, dashingPattern2, 0.0f);
            } else {
                //Regular association
                stroke = new BasicStroke(2);
            }
        } else if(endModelElementPanel.getModelElementType() == ModelElementType.TAB) {
            stroke = new BasicStroke(2);
        }

        Logger.getInstance().writeInfo("Association between " + startModelElementPanel.getNameText() + " and " + endModelElementPanel.getNameText() +
                " was created");
        SwingUtilities.invokeLater(this::revalidate);
        SwingUtilities.invokeLater(this::repaint);
    }

    private void calculateSize(Point startPoint, Point endPoint) {
        int width = Math.max(endPoint.x, startPoint.x) - Math.min(endPoint.x, startPoint.x);
        int height = Math.max(endPoint.y, startPoint.y) - Math.min(endPoint.y, startPoint.y);
        bounds = new Rectangle(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y), width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int baseFontSize = 16;
        int scaledFontSize = (int) (baseFontSize * mtsEditorGUI.getCurrentZoomFactor());

        g2d.setFont(new Font("Consolas", Font.PLAIN, scaledFontSize));
        g2d.setColor(color);
        g2d.setStroke(stroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<JPanel> panels = new ArrayList<>(mtsEditorGUI.getElementPanelHandler().getModelElementPanels());
        panels.remove(startModelElementPanel);
        panels.remove(endModelElementPanel);
        if(!startModelElementPanel.isVisible() || !endModelElementPanel.isVisible() || mtsEditorGUI.isHideAllAssociationsSelected()) {
            return;
        }
        List<Point> path = findPath(startModelElementPanel.getCenter(), endModelElementPanel.getCenter(), panels);

        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        if (useInstanceOf) {
            String text = "<<instance of>>";
            int offset = g2d.getFontMetrics().stringWidth(text);
            Point center = getCenter(path);

            g2d.drawString(text, center.x - offset / 2, center.y);
        }
    }

    private static Point getCenter(List<Point> path) {
        Point center;
        if (path.size() % 2 == 1) {
            center = path.get(path.size() / 2);
        } else {
            Point p1 = path.get(path.size() / 2 - 1);
            Point p2 = path.get(path.size() / 2);
            center = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        }
        return center;
    }

    public void updateLocation() {
        startPoint = startModelElementPanel.getCenter();
        endPoint = endModelElementPanel.getCenter();
        centerPoint = getLineCenter();

        this.setBounds(mtsEditorGUI.getEditorPanel().getBounds());

        revalidate();
        repaint();
    }

    private Point getLineCenter() {
        int width = Math.max(endPoint.x, startPoint.x) - Math.min(endPoint.x, startPoint.x);
        int height = Math.max(endPoint.y, startPoint.y) - Math.min(endPoint.y, startPoint.y);
        Rectangle r = new Rectangle(Math.min(startPoint.x, endPoint.x), Math.min(startPoint.y, endPoint.y), width, height);
        return new Point((int) r.getCenterX(), (int) r.getCenterY());
    }

    /**
     * finds a path between two panels without going through other panels
     *
     * @param start starting point
     * @param end end point
     * @param panels list of all panels
     * @return List with points for the path
     */
    public List<Point> findPath(Point start, Point end, List<JPanel> panels) {
        List<Point> path = new ArrayList<>();
        ArrayList<Point> oldPoints = new ArrayList<>();
        path.add(start);

        Point currentPoint = start;
        while (!currentPoint.equals(end)) {
            Line2D line = new Line2D.Double(currentPoint, end);
            if (!intersectsAnyPanel(line, panels)) {
                path.add(end);
                break;
            }
            for (JPanel panel : panels) {
                if(!panel.isVisible()) {
                    continue;
                }
                Rectangle bounds = getBounds(panel);
                if (line.intersects(bounds)) {
                    Point nextPoint = null;
                    int counter = 0;

                    while (nextPoint == null) {
                        nextPoint = findNextPoint(oldPoints, currentPoint, end, bounds, panels);
                        counter++;
                        if(counter >= 10) {
                            nextPoint = end;
                        }
                    }
                    oldPoints.add(currentPoint);
                    path.add(nextPoint);
                    currentPoint = nextPoint;
                    break;
                }
            }
            if(path.size() > 7) {
                path.subList(1, path.size() - 1).clear();
            }
        }

        return path;
    }

    private Point findNextPoint(ArrayList<Point> oldPoints, Point currentPoint, Point endPoint, Rectangle bounds, List<JPanel> panels) {
        int i = 10;
        int maxAttempts = 10;
        int attempt = 1;

        while (attempt < maxAttempts) {
            Point[] directions = {
                    new Point(bounds.x + bounds.width + i * attempt, currentPoint.y),
                    new Point(currentPoint.x, bounds.y + bounds.height + i * attempt),
                    new Point(bounds.x - i * attempt, currentPoint.y),
                    new Point(currentPoint.x, bounds.y - i * attempt)
            };
            Arrays.sort(directions, new Comparator<>() {
                @Override
                public int compare(Point p1, Point p2) {
                    double distance1 = distance(p1, endPoint);
                    double distance2 = distance(p2, endPoint);
                    return Double.compare(distance1, distance2);
                }

                private double distance(Point p1, Point p2) {
                    return p1.distance(p2);
                }
            });

            for (Point direction : directions) {
                boolean isOldPoint = false;
                boolean intersectsLastLine = false;
                for(Point oldPoint : oldPoints) {
                    if (direction.equals(oldPoint)) {
                        isOldPoint = true;
                        break;
                    }
                }
                if(!oldPoints.isEmpty() && intersectsLastLine(direction, oldPoints.get(oldPoints.size() - 1), currentPoint)) {
                    intersectsLastLine = true;
                }
                if(direction.equals(currentPoint) || isOldPoint || intersectsLastLine) {
                    continue;
                }
                if (!intersectsAnyPanel(new Line2D.Double(currentPoint, direction), panels)) {
                    return direction;
                }
            }
            attempt++;
        }

        return null;
    }

    private boolean intersectsLastLine(Point nextPoint, Point oldPoint, Point currentPoint) {
        return (currentPoint.x == oldPoint.x && nextPoint.x == oldPoint.x
                && ((nextPoint.y < oldPoint.y && nextPoint.y > currentPoint.y)
                || (nextPoint.y > oldPoint.y && nextPoint.y < currentPoint.y)))
                || currentPoint.y == oldPoint.y && nextPoint.y == oldPoint.y
                && ((nextPoint.x < oldPoint.x && nextPoint.x > currentPoint.x)
                || (nextPoint.x > oldPoint.x && nextPoint.x < currentPoint.x));
    }

    private boolean intersectsAnyPanel(Line2D line, List<JPanel> panels) {
        for (JPanel panel : panels) {
            if(!panel.isVisible()) {
                continue;
            }
            Rectangle bounds = getBounds(panel);
            if (line.intersects(bounds)) {
                return true;
            }
        }
        return false;
    }

    private Rectangle getBounds(JPanel panel) {
        return new Rectangle(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());
    }

    public void removeAssociation() {
        startModelElementPanel.removeAssociation(this);
        endModelElementPanel.removeAssociation(this);
    }

    private boolean original;

    public void setDefaultValue() {
        original = useInstanceOf;
        this.useInstanceOf = false;
    }

    public void unsetDefaultValue() {
        useInstanceOf = original;
    }

    public boolean isUseInstanceOf() {
        return useInstanceOf;
    }

    public void setUseInstanceOf(boolean useInstanceOf) {
        this.useInstanceOf = useInstanceOf;
    }

    /**
     * figure out if a panel has an association
     *
     * @param modelElementPanel panel
     * @return true if the panel is part of the AssociationElementPanel
     */
    public boolean containsModelElement(ModelElementPanel modelElementPanel) {
        return this.startModelElementPanel.equals(modelElementPanel) || this.endModelElementPanel.equals(modelElementPanel);
    }
}
