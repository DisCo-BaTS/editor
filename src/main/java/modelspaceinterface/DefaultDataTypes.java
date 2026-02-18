package modelspaceinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DefaultDataTypes {
    //Primitives : int Integer short Short float Float byte Byte long Long double Double boolean Boolean String
    //Other : ArrayList<> HashMap<>
    //Special Case : xx[]

    public static String[] datatypes =
            new String[]{"int", "Integer", "short", "Short", "float", "Float", "byte", "Byte", "long", "Long", "double",
                    "Double", "boolean", "Boolean", "String"};
    public static String[] special_datatypes = new String[]{"ArrayList_<>", "HashMap_<,>"};

    public static boolean endsWithAnyPrimitiveDatatype(String type) {
        for (String s : datatypes) {
            if (type.toLowerCase(Locale.ROOT).endsWith(s.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String getPrimitiveDatatype(String type) {
        for (String s : datatypes) {
            if (type.toLowerCase(Locale.ROOT).endsWith(s.toLowerCase(Locale.ROOT))) {
                return s;
            }
        }
        return null;
    }

    public static boolean containsAnySpecialDatatype(String type) {
        for (String s : special_datatypes) {
            if (type.toLowerCase(Locale.ROOT).contains(s.split("_")[0].toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static String[] getSpecialDatatype(String type) {
        for (String s : special_datatypes) {
            if (type.toLowerCase(Locale.ROOT).contains(s.split("_")[0].toLowerCase(Locale.ROOT))) {
                return s.split("_");
            }
        }
        return null;
    }

    public static ArrayList<String> getContainedDatatypes(String type, ArrayList<String> output) {
        if (output == null) {
            output = new ArrayList<>();
        }

        if (getFirstPrimitiveType(type) == null) {
            return output;
        }

        String dt = getFirstPrimitiveType(type);
        output.add(dt);
        return getContainedDatatypes(type.split(dt)[1], output);
    }

    private static String getFirstPrimitiveType(String type) {
        HashMap<Integer, String> candidates = new HashMap<>();
        for (String s : datatypes) {
            if (type.contains(s)) {
                candidates.put(type.indexOf(s), s);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        } else {
            int min = Integer.MAX_VALUE;
            for (Integer i : candidates.keySet()) {
                if (i < min) {
                    min = i;
                }
            }
            return candidates.get(min);
        }
    }

    public static String startsWithPrimitiveDataType(String name) {
        for (String s : datatypes) {
            if (name.startsWith(s)) {
                return s;
            }
        }
        return null;
    }

    public static boolean isEditableDatatype(String name) {
        for(String s : datatypes) {
            if(name.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
