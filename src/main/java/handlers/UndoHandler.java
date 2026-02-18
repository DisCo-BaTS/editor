package handlers;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

/**
 * UndoHandler manages the undo and redo calls
 * including creating and deleting panels and changing the position of panels
 *
 */
public class UndoHandler {
    private static UndoHandler undoHandler;
    private static UndoManager undoManager;
    private static UndoableEditSupport undoableEditSupport;

    public static UndoHandler getInstance() {
        if(undoHandler == null) {
            undoHandler = new UndoHandler();
        }
        if(undoManager == null) {
            undoManager = new UndoManager();
        }
        if(undoableEditSupport == null) {
            undoableEditSupport = new UndoableEditSupport();
            undoableEditSupport.addUndoableEditListener(undoManager);
        }

        return undoHandler;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public UndoableEditSupport getUndoableEditSupport() {
        return undoableEditSupport;
    }
}
