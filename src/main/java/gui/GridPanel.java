package gui;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {

    private int gridSize;
    static final int originX = 0;
    static final int originY = 0;

    public GridPanel(int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0xFF3A3A3B, false));

        int rows = (this.getParent().getHeight() / gridSize) + 1;
        int columns = (this.getParent().getWidth() / gridSize) + 1;

        for (int i = 0; i <= rows; i++) {
            g.drawLine(originX, originY + i * gridSize, originX + columns * gridSize, originY + i * gridSize);
        }
        for (int i = 0; i <= columns; i++) {
            g.drawLine(originX + i * gridSize, originY, originX + i * gridSize, originY + rows * gridSize);
        }
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }
}