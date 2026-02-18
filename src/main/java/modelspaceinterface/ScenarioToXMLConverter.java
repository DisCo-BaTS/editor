package modelspaceinterface;

import com.thoughtworks.xstream.XStream;
import handlers.ElementHandler;
import library.model.dto.scenario.ScenarioDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ScenarioToXMLConverter {

    public static boolean saveToXML(Path file, ElementHandler elementHandler) {
        ScenarioDTO scenario = ScenarioGenerator.generateScenario(elementHandler);

        XStream xStream = new XStream();
        String output = xStream.toXML(scenario);
        try {
            Files.writeString(file, output);
            Path jarPath = Path.of(RuntimeCompiler.tmpPath + File.separator + "generated-classes-tssmeta.jar");
            Path newPath = Path.of(file.toAbsolutePath().toString().replace(".xml", ".jar"));
            Files.copy(jarPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static ArrayList<String> getJarContent(Path jarFile) {
        ArrayList<String> output = new ArrayList<>();
        try {
            JarFile jar = new JarFile(jarFile.toFile());

            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                output.add(file.getName().replace(".class", "").replace("/", "."));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
