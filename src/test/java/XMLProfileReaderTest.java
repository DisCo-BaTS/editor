import handlers.ElementHandler;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelElement;
import org.junit.jupiter.api.Test;
import xml.XMLProfileReader;

import java.util.ArrayList;
import java.util.HashMap;

class XMLProfileReaderTest {

    XMLProfileReader xmlProfileReader;

    public ElementHandler elementHandler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        xmlProfileReader = new XMLProfileReader();
    }

    @Test
    void readXML() {
        //System.out.println(xmlProfileReader.getFILENAME());
        xmlProfileReader.readXML();
    }

    @Test
    void extendClass() {
        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlProfileReader.readXML());

        InheretedClass inheretedClass = elementHandler.extendClass("SimulationObject", "Ship");
        System.out.println(inheretedClass.name);
    }

    @Test
    void createTestScenario() {
        elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlProfileReader.readXML());

        InheretedClass trafficParticipant = elementHandler.extendClass("DynamicSimulationObject", "TrafficParticipant");

        InheretedClass ship = elementHandler.extendClass("TrafficParticipant", "Ship");
        ModelAttribute speed = new ModelAttribute();
        speed.id = ElementHandler.getID();
        speed.name = "speed";
        speed.type = "SimulationProperty<Double>";
        speed.minMultiplicity = 0;
        speed.maxMultiplicity = 1;
        ship.attributes.put(speed.id, speed);

        InheretedClass cargoShip = elementHandler.extendClass("Ship", "CargoShip");
        ModelAttribute name = new ModelAttribute();
        name.id = ElementHandler.getID();
        name.name = "name";
        name.type = "SimulationProperty<String>";
        cargoShip.attributes.put(name.id, name);
        ModelAttribute mmsi = new ModelAttribute();
        mmsi.id = ElementHandler.getID();
        mmsi.name = "mmsi";
        mmsi.type = "SimulationProperty<String>";
        cargoShip.attributes.put(mmsi.id, mmsi);
        ModelAttribute width = new ModelAttribute();
        width.id = ElementHandler.getID();
        width.name = "width";
        width.type = "SimulationProperty<Integer>";
        width.setDefaultValue("48");
        cargoShip.attributes.put(width.id, width);
        ModelAttribute length = new ModelAttribute();
        length.id = ElementHandler.getID();
        length.name = "length";
        length.type = "SimulationProperty<Integer>";
        length.setDefaultValue("367");
        cargoShip.attributes.put(length.id, length);
        ModelAttribute heading = new ModelAttribute();
        heading.id = ElementHandler.getID();
        heading.name = "heading";
        heading.type = "SimulationProperty<Integer>";
        heading.setDefaultValue("180");
        cargoShip.attributes.put(heading.id, heading);

        InheretedClass obstacle = elementHandler.extendClass("SimulationObject", "Obstacle");

        InheretedClass lighthouse = elementHandler.extendClass("Obstacle", "Lighthouse");
        ModelAttribute height = new ModelAttribute();
        height.id = ElementHandler.getID();
        height.name = "height";
        height.type = "SimulationProperty<Double>";
        lighthouse.attributes.put(height.id, heading);

        InstanciatedClass hamburgExpress = new InstanciatedClass(cargoShip);
        elementHandler.addInstance(hamburgExpress);
        HashMap<String, Object> nameProp = new HashMap<>();
        nameProp.put("value", "HamburgExpress");
        nameProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        nameProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("name", nameProp, null);
        HashMap<String, Object> mmsiProp = new HashMap<>();
        mmsiProp.put("value", "218774000");
        mmsiProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        mmsiProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("mmsi", mmsiProp, null);
        HashMap<String, Object> positionProp = new HashMap<>();
        positionProp.put("value", "new Position(54.526461,5.475241)");
        positionProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        positionProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("position", positionProp, null);
        HashMap<String, Object> speedProp = new HashMap<>();
        speedProp.put("value", 10.0);
        speedProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        speedProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        hamburgExpress.setNamedAttribute("speed", speedProp, null);

        InstanciatedClass wittenbergen = new InstanciatedClass(lighthouse);
        elementHandler.addInstance(wittenbergen);
        HashMap<String, Object> lNameProperty = new HashMap<>();
        lNameProperty.put("value", "Lgihthouse Wittenbergen");
        lNameProperty.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lNameProperty.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("name", lNameProperty, null);
        HashMap<String, Object> lheight = new HashMap<>();
        lheight.put("value", 30.0);
        lheight.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lheight.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("height", lheight, null);
        HashMap<String, Object> lPositionProp = new HashMap<>();
        lPositionProp.put("value", "new Position(54.536461,5.475241)");
        lPositionProp.put("simulationVisibility", "SimulationVisibilityKind.READ");
        lPositionProp.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("position", lPositionProp, null);
        HashMap<String, Object> shipPositions = new HashMap<>();
        shipPositions.put("simulationVisibility", "SimulationVisibilityKind.WRITE");
        shipPositions.put("valueKind", "SimulationPropertyValueKind.SINGLE");
        wittenbergen.setNamedAttribute("shipPositions", shipPositions, null);

        ModelElement ioc = elementHandler.getNamedElement("InterObjectCommunication");
        InstanciatedClass positionExchange = new InstanciatedClass(ioc);
        elementHandler.addInstance(positionExchange);
        positionExchange.setNamedAttribute("communicatedProperties", new ArrayList<>().add(hamburgExpress.getNamedAttribute("position")), null);
        positionExchange.setNamedAttribute("end", wittenbergen, null);
        positionExchange.setNamedAttribute("start", hamburgExpress, null);

        ModelElement simComponent = elementHandler.getNamedElement("SimulationComponent");
        ModelElement identifier = elementHandler.getNamedElement("Identifier");
        ModelElement methodCall = elementHandler.getNamedElement("MethodCall");
        InstanciatedClass positioningComponent = new InstanciatedClass(simComponent);
        elementHandler.addInstance(positioningComponent);
        InstanciatedClass posCompIdent = new InstanciatedClass(identifier);
        elementHandler.addInstance(posCompIdent);
        posCompIdent.setNamedAttribute("path", "simplePos::step", null);
        InstanciatedClass posCompMethodCall = new InstanciatedClass(methodCall);
        elementHandler.addInstance(posCompMethodCall);
        posCompMethodCall.setNamedAttribute("path", posCompIdent, null);
        ArrayList<ModelAttribute> mcParameters = new ArrayList<>();
        mcParameters.add((ModelAttribute) hamburgExpress.getNamedAttribute("position"));
        mcParameters.add((ModelAttribute) hamburgExpress.getNamedAttribute("speed"));
        mcParameters.add((ModelAttribute) hamburgExpress.getNamedAttribute("heading"));
        posCompMethodCall.setNamedAttribute("methodParameters", mcParameters, null);
        positioningComponent.setNamedAttribute("linkedMethodCall", posCompMethodCall, null);


        System.out.println();
    }
}