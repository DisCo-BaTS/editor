package gui.tooltips;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import handlers.ElementHandler;
import model.InheretedClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class TooltipProvider {

    private static HashMap<String, String> tooltipMap;
    private static boolean readFailed = false;

    private static HashMap<String, String> getTooltips() {
        Gson g = new Gson();

        String txt = null;
        try {
            InputStream inputStream = TooltipProvider.class.getClassLoader().getResourceAsStream("gui/tooltips/tooltips.json");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            for (String line; (line = in.readLine()) != null; ) {
                stringBuilder.append(line).append("\n");
            }
            txt = stringBuilder.toString();
            ArrayList tooltips = g.fromJson(txt, ArrayList.class);

            HashMap<String, String> output = new HashMap<>();
            tooltips.forEach(t -> output.put(((LinkedTreeMap<String, String>) t).get("modelElement"), ((LinkedTreeMap<String, String>) t).get("description")));
            return output;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            readFailed = true;
            tooltipMap = new HashMap<>();
        }
        return new HashMap<>();
    }

    public static String getToolTipForElement(String name) {
        for (InheretedClass inheretedClass : ElementHandler.getInstance().getInheretedClasses()) {
            if (inheretedClass.name.equals(name)) {
                return "extends " + inheretedClass.getSuperClass();
            }
        }
        if (tooltipMap == null && !readFailed) {
            tooltipMap = getTooltips();
        }
        return tooltipMap.getOrDefault(name, name);
    }
}
