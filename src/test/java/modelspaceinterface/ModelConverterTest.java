package modelspaceinterface;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;

class ModelConverterTest {

    @Test
    void convert() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ModelConverter modelConverter = new ModelConverter();

        for (Map.Entry<String, Path> entry : modelConverter.convert().entrySet()) {

            URL classUrl = entry.getValue().getParent().toFile().toURI().toURL();
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
            Class<?> clazz = Class.forName(entry.getKey().split("<")[0], true, classLoader);
            if (!clazz.isEnum()) {
                Object o = clazz.newInstance();
                System.out.println(o.toString());
            }
        }
    }
}