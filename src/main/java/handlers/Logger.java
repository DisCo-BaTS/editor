package handlers;

import javax.swing.*;

public class Logger {

    private JTextArea textArea;
    private static Logger instance;

    public Logger(JTextArea textArea) {
        this.textArea = textArea;
        if (instance == null) {
            instance = this;
        }
    }

    public  void writeWarning(String string) {
        textArea.append("[WARNING]: ");
        writeConsole(string);
    }

    public void writeInfo(String string) {
        textArea.append("[INFO]: ");
        writeConsole(string);
    }

    private void writeConsole(String string) {
        textArea.append(string);
        textArea.append(". \n");
    }

    public static Logger getInstance() {
        return instance;
    }
}
