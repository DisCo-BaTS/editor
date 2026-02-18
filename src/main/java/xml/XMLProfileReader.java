package xml;

import model.EnumLiteral;
import model.ModelAttribute;
import model.ModelElement;
import model.ModelEnum;
import org.apache.commons.compress.utils.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class XMLProfileReader {

    public static final InputStream FILENAME = XMLProfileReader.class.getClassLoader().getResourceAsStream("profile/test_profil.xmi");

    public XMLData readXML() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setIgnoringElementContentWhitespace(true);

            DocumentBuilder db = dbf.newDocumentBuilder();

            String tmp = System.getProperty("java.io.tmpdir");
            File file = new File(tmp + File.separator + "tmpinputstream.xmi");
            file.deleteOnExit();
            try (OutputStream outputStream = new FileOutputStream(file)) {
                IOUtils.copy(FILENAME, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("uml:Model");
            list = list.item(0).getChildNodes();

            XMLData xmlData = new XMLData();

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (((Element) node).getAttribute("name").equals("TSSMeta")) {
                        NodeList packedElementsList = ((Element) node).getElementsByTagName("packagedElement");
                        for (int i = 0; i < packedElementsList.getLength(); i++) {
                            Node innerNode = packedElementsList.item(i);
                            Element e = (Element) innerNode;
                            ModelElement modelElement;
                            if (e.getAttribute("xmi:type").equals("uml:Enumeration")) {
                                modelElement = new ModelEnum();
                            } else if (e.getAttribute("xmi:type").equals("uml:Class")) {
                                modelElement = new ModelElement();
                                modelElement.isMetaClass = true;
                            } else {
                                modelElement = new ModelElement();
                            }
                            if (e.getAttribute("name") == null || e.getAttribute("name").equals("")) {
                                continue;
                            }
                            modelElement.name = e.getAttribute("name");
                            modelElement.type = e.getAttribute("xmi:type");
                            modelElement.id = e.getAttribute("xmi:id");
                            if (e.getAttribute("xmi:type").equals("uml:Enumeration")) {
                                NodeList attributeList = e.getElementsByTagName("ownedLiteral");
                                for (int j = 0; j < attributeList.getLength(); j++) {
                                    Node attributeNode = attributeList.item(j);
                                    Element f = (Element) attributeNode;
                                    ((ModelEnum) modelElement).enumLiterals.add(new EnumLiteral(f.getAttribute("name")));
                                }
                            } else {
                                if (e.getElementsByTagName("generalization").getLength() != 0) {
                                    modelElement.dependency.add(0, ((Element) e.getElementsByTagName("generalization").item(0)).getAttribute("general"));
                                }
                                if (e.getElementsByTagName("ownedTemplateSignature").getLength() != 0) {
                                    NodeList nl = ((Element) e.getElementsByTagName("ownedTemplateSignature").item(0)).getElementsByTagName("ownedParameter");
                                    modelElement.name = modelElement.name + "<";
                                    for (int c = 0; c < nl.getLength(); c++) {
                                        Element attributeNode = (Element) nl.item(c);
                                        if (c != 0) {
                                            modelElement.name = modelElement.name + ",";
                                        }
                                        String n = ((Element) attributeNode.getElementsByTagName("ownedParameteredElement").item(0)).getAttribute("name");
                                        modelElement.name = modelElement.name + n;
                                    }
                                    modelElement.name = modelElement.name + ">";
                                    if (modelElement.name.split("<").length > 1) {
                                        String tmpString = modelElement.name.split("<", 2)[1];
                                        int endIndex = Integer.MAX_VALUE;
                                        for (int o = tmpString.length() - 1; o >= 0; o--) {
                                            if (tmpString.charAt(o) == '>') {
                                                endIndex = o;
                                            }
                                        }
                                        tmpString = tmpString.substring(0, endIndex);
                                        ArrayList<String> subTypes = new ArrayList<>(Arrays.asList(tmpString.split(",")));
                                        modelElement.hasGenericType = true;
                                        modelElement.subTypes = subTypes;
                                    }
                                }
                                NodeList attributeList = e.getElementsByTagName("ownedAttribute");
                                for (int j = 0; j < attributeList.getLength(); j++) {
                                    Node attributeNode = attributeList.item(j);
                                    Element f = (Element) attributeNode;
                                    ModelAttribute modelAttribute = new ModelAttribute();
                                    if (f.getElementsByTagName("xmi:id") != null || !f.getElementsByTagName("xmi:id").equals("")) {
                                        modelAttribute.id = f.getAttribute("xmi:id");
                                    }
                                    if (f.getElementsByTagName("name") != null || !f.getElementsByTagName("name").equals("")) {
                                        modelAttribute.name = f.getAttribute("name");
                                    }
                                    if (f.getElementsByTagName("visibility") != null || !f.getElementsByTagName("visibility").equals("")) {
                                        modelAttribute.visibility = f.getAttribute("visibility");
                                    }
                                    if (f.getElementsByTagName("lowerValue").getLength() > 0) {
                                        modelAttribute.minMultiplicity = Short.parseShort(((Element) f.getElementsByTagName("lowerValue").item(0)).getAttribute("value"));
                                    }
                                    if (f.getElementsByTagName("upperValue").getLength() > 0) {
                                        modelAttribute.maxMultiplicity = Short.parseShort(((Element) f.getElementsByTagName("upperValue").item(0)).getAttribute("value"));
                                    }
                                    if (f.getElementsByTagName("defaultValue").getLength() > 0) {
                                        modelAttribute.setDefaultValue(((Element) f.getElementsByTagName("defaultValue").item(0)).getAttribute("value"));
                                    }
                                    if (f.getElementsByTagName("type").getLength() > 0) {
                                        modelAttribute.type = ((Element) f.getElementsByTagName("type").item(0)).getAttribute("xmi:idref");
                                    }
                                    modelElement.attributes.put(modelAttribute.id, modelAttribute);
                                }
                            }
                            xmlData.elements.put(modelElement.id, modelElement);
                        }
                        break;
                    }
                }
            }
            //Dependencies
            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (((Element) node).getAttribute("name").equals("TSSMeta")) {
                        NodeList packedElementsList = ((Element) node).getElementsByTagName("packagedElement");
                        for (int i = 0; i < packedElementsList.getLength(); i++) {
                            Node innerNode = packedElementsList.item(i);
                            Element e = (Element) innerNode;
                            ModelElement modelElement = new ModelElement();
                            if (e.getAttribute("xmi:type") == null || !e.getAttribute("xmi:type").equals("uml:Dependency")) {
                                continue;
                            }
                            modelElement = xmlData.elements.get(e.getAttribute("client"));
                            modelElement.dependency.add(e.getAttribute("supplier"));
                        }
                        break;
                    }
                }
            }

            return xmlData;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}