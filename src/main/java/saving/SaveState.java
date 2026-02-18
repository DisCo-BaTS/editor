package saving;

import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelEnum;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SaveState implements Serializable {

    public String profile;

    public int width;
    public int height;

    public HashMap<InheretedClass, Point> inheretedClassesPositions;

    public HashMap<InstanciatedClass, Point> instanciatedClassesPositions;

    public HashMap<ModelEnum, Point> addedEnumPositions;

    public String[][] associationElementPanels;
    public HashMap<String, Point> tabsPositions;
    public HashMap<String, ArrayList<String>> tabsPanels;

    public static void save(SaveState saveState, String file) {
        try {
            FileOutputStream fileOutputStream = null;

            fileOutputStream = new FileOutputStream(file);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(saveState);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SaveState load(String absolutePath) {
        try {
            FileInputStream fileInputStream = null;

            fileInputStream = new FileInputStream(absolutePath);

            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            SaveState saveState = (SaveState) objectInputStream.readObject();
            objectInputStream.close();
            return saveState;

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
