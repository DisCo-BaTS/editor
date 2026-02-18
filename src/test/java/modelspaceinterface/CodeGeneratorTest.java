package modelspaceinterface;

import handlers.ElementHandler;
import org.junit.jupiter.api.Test;
import xml.XMLProfileReader;

class CodeGeneratorTest {

    @Test
    void generateClass() {
        XMLProfileReader xmlProfileReader = new XMLProfileReader();
        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlProfileReader.readXML());

        CodeGenerator codeGenerator = new CodeGenerator(elementHandler);

        String code = codeGenerator.generateClass(elementHandler.getNamedElement("MethodCall"));
        System.out.println();
    }
}