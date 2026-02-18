package gui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;


class MTSEditorGUITest {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        UIManager.put("Button.hoverBackground", new Color(84, 89, 92));

        MTSEditorGUI frame = new MTSEditorGUI("MTSEditor");
        frame.setVisible(true);
    }

    @Test
    void GUITest() {
        FlatDarkLaf.setup();
        MTSEditorGUI frame = new MTSEditorGUI("MTSEditorTest");
        frame.setVisible(false);
    }


}